import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

public class JDictServlet extends HttpServlet  {

  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, java.io.IOException  {

    Template vt = Velocity.getTemplate("templ/jdict.vm");

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");

    res.setCharacterEncoding("UTF-8");
    vt.merge(vc, res.getWriter());
  }
}
