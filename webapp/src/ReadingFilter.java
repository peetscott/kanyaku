import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Map;

import java.io.IOException;

import org.apache.velocity.VelocityContext;

public class ReadingFilter implements Filter  {

  public void init(FilterConfig config) throws ServletException  {

  }

  public void destroy()  {

  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws ServletException, IOException  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    String dict = req.getParameter("dict");
    String ime_text = req.getParameter("ime_text");


    // Define variables in velocity context ..
    vc.put("edict_reading_rows", null);
    vc.put("kanjidic_reading_rows", null);


    if (dict != null &&
        ime_text != null)  {
      //try  {
        if (dict.equals("edict"))  {
          doEdictQuery(req);

        }
        else if (dict.equals("kanjidic"))  {
          doKanjidicQuery(req);
        }
      //}
      //catch (Exception ex)  {
      //  req.getServletContext().log(ex.toString());
      //}
    }

    chain.doFilter(req, res);
  }
  private void doEdictQuery(ServletRequest req)  {

    String ime_text = req.getParameter("ime_text");
    String stripped = ime_text.replaceAll("[_%]", "");

    if (KanaUtil.getStringType(stripped) == KanaUtil.HIRAGANA_TYPE ||
        KanaUtil.getStringType(stripped) == KanaUtil.KATAKANA_TYPE)  {
      try  {
        VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
        Connection conn = (Connection) req.getServletContext().getAttribute("conn");
        StringBuilder sb = new StringBuilder();

        sb.append(
          "SELECT phrase, reading, gloss " +
          "FROM edict.entry JOIN " +
          "(SELECT line_no FROM edict.ridx WHERE " +
          "reading LIKE '");
        sb.append(ime_text);
        sb.append("' GROUP BY line_no) AS LINES ON edict.entry.line_no = LINES.line_no");

        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery(sb.toString());
        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        while (rs.next())  {
          Map<String, String> row = new java.util.HashMap<String, String>();
          row.put("phrase", rs.getString(1));
          row.put("reading", rs.getString(2) == null ? "" : rs.getString(2));
          row.put("gloss", rs.getString(3));
          rows.add(row);
        }
        stmnt.close();
        vc.put("edict_reading_rows", rows);
      }
      catch (Exception ex)  {
        req.getServletContext().log(ex.toString());
      }
    }
  }

  private void doKanjidicQuery(ServletRequest req)  {

    String ime_text = req.getParameter("ime_text");  // Should not be null.
    String stripped = ime_text.replaceAll("[_%\u3002\u30fc]", "");

    if (KanaUtil.getStringType(stripped) == KanaUtil.HIRAGANA_TYPE ||
        KanaUtil.getStringType(stripped) == KanaUtil.KATAKANA_TYPE)  {

      try  {
        VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
        Connection conn = (Connection) req.getServletContext().getAttribute("conn");
        StringBuilder sb = new StringBuilder();
        String tableName =
          KanaUtil.getStringType(stripped) == KanaUtil.HIRAGANA_TYPE ?
          "kun" :
          "ohn";
        ime_text = ime_text.replaceAll("\u3002", ".");  // Relevant to kun readings only.
        ime_text = ime_text.replaceAll("\u30fc", "-");  // Relevant to kun readings only.
        // NOTE: kanji table name has changed to entry.
        // This depends on .derby\jdict database ..
        //sb.append(
        //  "SELECT kanjidic.kanji.entry " +
        //  "FROM kanjidic.kanji WHERE kanjidic.kanji.entry IN " +
        //  "(SELECT kanjidic.");
        sb.append(
          "SELECT kanjidic.entry.kanji " +
          "FROM kanjidic.entry WHERE kanjidic.entry.kanji IN " +
          "(SELECT kanjidic.");

        sb.append(tableName);
        sb.append(".kanji FROM kanjidic.");
        sb.append(tableName);
        sb.append(" WHERE kanjidic.");
        sb.append(tableName);
        sb.append(".reading LIKE '");
        sb.append(ime_text);
        sb.append("')");

        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery(sb.toString());
        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        while (rs.next())  {
          Map<String, String> row = new java.util.HashMap<String, String>();
          row.put("kanji", rs.getString(1));
          rows.add(row);
        }
        stmnt.close();
        vc.put("kanjidic_reading_rows", rows);
      }
      catch (Exception ex)  {
        req.getServletContext().log(ex.toString());
      }
    }
  }
}
