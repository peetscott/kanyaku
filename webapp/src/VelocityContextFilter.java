import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import org.apache.velocity.VelocityContext;

public class VelocityContextFilter implements Filter  {

  public void init(FilterConfig config) throws ServletException  {

  }

  public void destroy()  {

  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws ServletException, IOException  {

    VelocityContext vc = new VelocityContext();
    //req.setCharacterEncoding("UTF-8");
    //req.getServletContext().log(req.getCharacterEncoding());
    vc.put("ime_text", req.getParameter("ime_text"));
    vc.put("input_type", req.getParameter("kana"));
    vc.put("dict_name", req.getParameter("dict"));
    vc.put("Format", Format.class);
    vc.put("URLEncoder", java.net.URLEncoder.class);
    req.setAttribute("velocity_context", vc);
    chain.doFilter(req, res);
  }
}
