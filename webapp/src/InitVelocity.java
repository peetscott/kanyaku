import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

import org.apache.velocity.app.Velocity;


public class InitVelocity implements javax.servlet.ServletContextListener  {

  public void contextInitialized(ServletContextEvent sce)  {


    Velocity.setProperty("resource.loader", "classpath");
    Velocity.setProperty("classpath.resource.loader.description",
      "Velocity classpath resource loader");
    Velocity.setProperty("classpath.resource.loader.class",
      "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

    Velocity.setProperty("velocimacro.library",
      "templ/jdict_macros.vm"
    );

    Velocity.init();
  }

  public void contextDestroyed(ServletContextEvent sce)  {

  }
}
