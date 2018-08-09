package com.github.hiiamrohit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.Sources;
import com.wix.mysql.SqlScriptSource;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;

/**
 *
 * @author Javier Ortiz Bultron <javierortiz@pingidentity.com>
 */
public class ScriptTest
{
  private static EmbeddedMysql mysqld;
  private static Connection conn;
  private final String user = "testUser";
  private final String password = "testPassword";
  private final String schema = "countries-states-cities-db-test";
  private final int port = 3336;
  private static final Logger LOG
          = Logger.getLogger(ScriptTest.class.getName());

  @Before
  public void setupMySQL() throws SQLException
  {
    MysqldConfig config = aMysqldConfig(Version.v5_7_latest)
            .withPort(port)
            .withUser(user, password)
            .build();

    mysqld = anEmbeddedMysql(config)
            .addSchema(schema)
            .start();

    conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:" + port + "/" + schema + "?useSSL=false",
            user, password);
  }

  @After
  public void shutdown()
  {
    try
    {
      if (conn != null)
      {
        conn.close();
      }
      if (mysqld != null)
      {
        mysqld.stop();
      }
    }
    catch (SQLException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Check the
   */
  @Test
  public void testIntegrity()
  {
    Map<String, Map<String, List<Integer>>> results = new HashMap<>();
    ArrayList<File> scripts = new ArrayList<>();
    scripts.add(new File("countries.sql"));
    scripts.add(new File("states.sql"));
    scripts.add(new File("cities.sql"));
    scripts.forEach(file ->
    {
      checkIntegrity(file, file.getName().equals("countries.sql"), results);
    });

    //Now check that al the keys are valid
    List<Integer> countryKeys = results.get("countries.sql").get("keys");
    assertFalse(countryKeys.isEmpty());
    List<Integer> stateKeys = results.get("states.sql").get("keys");
    assertFalse(stateKeys.isEmpty());

    List<Integer> stateLinks = results.get("states.sql").get("links");
    assertFalse(stateLinks.isEmpty());
    List<Integer> cityLinks = results.get("cities.sql").get("links");
    assertFalse(cityLinks.isEmpty());

    //Make sure all state links exist
    stateLinks.forEach(link ->
    {
      assertTrue(link + " not found in coutry!", countryKeys.contains(link));
    });

    //Make sure all city links exist
    cityLinks.forEach(link ->
    {
      assertTrue(link + " not found in state!", stateKeys.contains(link));
    });
  }

  @Test
  public void testScripts() throws MalformedURLException
  {
    //Now try to import the data
    importData();
  }

  @Test
  public void testEmptyCountries() throws MalformedURLException, SQLException
  {
    importData();
    //Now query for empty Countries
    PreparedStatement ps = conn.prepareStatement("select id,name from countries "
            + "where id not in (select country_id from states)");
    ResultSet rs = ps.executeQuery();
    boolean isEmpty = !rs.isBeforeFirst() && rs.getRow() == 0;
    int count = 0;
    List<Integer> invalid = new ArrayList<>();
    while (rs.next())
    {
      System.err.println(rs.getInt(1) + ") " + rs.getString(2));
      invalid.add(rs.getInt(1));
      count++;
    }
    if (count > 0)
    {
      System.err.println("There are " + count + " entries without children!");
      writeCleanFile(new File("countries.sql"), new File("countries-clean.sql"),
              invalid);
    }
    assertTrue(isEmpty);
  }

  @Test
  public void testEmptyStates() throws MalformedURLException, SQLException
  {
    importData();
    //Now query for empty Countries
    PreparedStatement ps = conn.prepareStatement("select s.id, s.name,(select "
            + "name from countries where id=s.country_id) from states s where "
            + "s.id not in (select state_id from cities)");
    ResultSet rs = ps.executeQuery();
    boolean isEmpty = !rs.isBeforeFirst() && rs.getRow() == 0;
    int count = 0;
    List<Integer> invalid = new ArrayList<>();
    while (rs.next())
    {
      System.err.println(rs.getInt(1) + ") " + rs.getString(2) + " ("
              + rs.getString(3) + ")");
      invalid.add(rs.getInt(1));
      count++;
    }
    if (count > 0)
    {
      System.err.println("There are " + count + " states without children!");
      writeCleanFile(new File("cities.sql"), new File("cities-clean.sql"),
              invalid);
    }
    assertTrue(isEmpty);
  }

  /**
   * Import scripts. It'll throw a RuntimeException if something goes wrong.
   *
   * @throws MalformedURLException
   */
  private void importData() throws MalformedURLException
  {
    List<SqlScriptSource> scripts = new ArrayList<>();
    scripts.add(Sources.fromURL(new File("countries.sql").toURI().toURL()));
    scripts.add(Sources.fromURL(new File("states.sql").toURI().toURL()));
    scripts.add(Sources.fromURL(new File("cities.sql").toURI().toURL()));
    mysqld.executeScripts(schema, scripts);
  }

  private void writeCleanFile(File src, File dest, List<Integer> invalid)
  {
    try (BufferedWriter writer = Files.newBufferedWriter(dest.toPath()))
    {
      try (BufferedReader br = new BufferedReader(new FileReader(src)))
      {
        String line;
        while ((line = br.readLine()) != null)
        {
          if (line.startsWith("("))
          {
            int id = Integer.valueOf(line.substring(1, line.indexOf(",")));
            if (!invalid.contains(id))
            {
              writer.write(line);
              writer.append('\n');
            }
          }
          else
          {
            writer.write(line);
            writer.append('\n');
          }
        }
      }
      catch (IOException ex)
      {
        LOG.log(Level.SEVERE, null, ex);
      }
    }
    catch (IOException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Scan the file for duplicate keys or references
   *
   * @param file File to check.
   * @param onlyKeys Use true to ignore integer columns beyond the first one
   * (key).
   * @param results Map storing results in the following format:
   *
   * script file name ->keys, links
   */
  private void checkIntegrity(File file, boolean onlyKeys,
          Map<String, Map<String, List<Integer>>> results)
  {
    //Read file
    List<String> list = null;
    try (Stream<String> lines = Files.lines(file.toPath()))
    {
      list = lines.collect(Collectors.toList());
    }
    catch (IOException e)
    {
      fail(e.getLocalizedMessage());
    }
    assertNotNull(list);
    ArrayList<Integer> keys = new ArrayList<>();
    ArrayList<Integer> relationships = new ArrayList<>();
    int lineNumber = 0;
    for (String line : list)
    {
      lineNumber++;
      if (line.startsWith("("))
      {
        line = line.substring(1, line.lastIndexOf(")"));
        StringTokenizer st = new StringTokenizer(line, ",");
        int counter = 0;
        while (st.hasMoreTokens())
        {
          try
          {
            String token = st.nextToken().trim();
            if (counter == 0)
            {
              //This is the key

              int key = Integer.valueOf(token);
              if (keys.contains(key))
              {
                fail("Duplicated key: " + key + " on file " + file.getName()
                        + ", line number: " + lineNumber);
              }
              else
              {
                keys.add(key);
              }
              if (onlyKeys)
              {
                //Don't check the rest
                break;
              }
            }
            else
            {
              int value = Integer.valueOf(token);
              if (!st.hasMoreTokens())
              {
                relationships.add(value);
              }
            }
          }
          catch (NumberFormatException ne)
          {
            //Ignore.
          }
          counter++;
        }
      }
      results.put(file.getName(), new HashMap<>());
      results.get(file.getName()).put("keys", keys);
      results.get(file.getName()).put("links", relationships);
    }
  }
}
