package com.github.hiiamrohit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static org.junit.Assert.assertTrue;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
      System.err.println("There are " + count + " entries without children!");
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
}
