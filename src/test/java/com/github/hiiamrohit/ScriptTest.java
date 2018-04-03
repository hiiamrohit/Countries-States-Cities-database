package com.github.hiiamrohit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
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
            "jdbc:mysql://localhost:" + port + "/" + schema,
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
    //Now try to impor the data
    List<SqlScriptSource> scripts = new ArrayList<>();
    scripts.add(Sources.fromURL(new File("countries.sql").toURI().toURL()));
    scripts.add(Sources.fromURL(new File("states.sql").toURI().toURL()));
    scripts.add(Sources.fromURL(new File("cities.sql").toURI().toURL()));
    mysqld.executeScripts(schema, scripts);
  }
}
