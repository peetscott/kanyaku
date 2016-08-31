import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection implements ServletContextListener  {

  public void contextInitialized(ServletContextEvent sce)  {
    ServletContext sc = sce.getServletContext();
    java.sql.Connection conn = null;

    System.setProperty(
      "derby.system.home",
      sc.getInitParameter("derby_system_home")
    );

    /*
     TO DO: Have a separate servlet start the Derby engine. ??
     */
    try  {
      // This shouldn't be necessary in Java 6
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    }
    catch (ClassNotFoundException ex)  {
      sce.getServletContext().log("derby.jar may not be in the classpath. " + ex.toString());
    }

    try  {
      conn = DriverManager.getConnection("jdbc:derby:jdict_db");
      sc.setAttribute("conn", conn);
    }
    catch (SQLException ex)  {
      sc.log("Unable to connect to jdict_db. " + ex.toString());
    }
  }

  public void contextDestroyed(ServletContextEvent sce)  {
    ServletContext sc = sce.getServletContext();
    Connection conn = (Connection) sc.getAttribute("conn");

    try  {
      conn.close();  // Need this??
    }
    catch (SQLException ex)  {

    }

    try  {
      /* What If? Other servlet contexts are using Derby?
         Shutdown just the database here?
       */
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    }
    catch (SQLException ex)  {
      // Shutting down derby generates an SQLException ..
      sc.log("Shutting down derby.");
    }
  }
}
