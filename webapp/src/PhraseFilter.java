import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Map;

import java.io.IOException;

import org.apache.velocity.VelocityContext;

public class PhraseFilter implements Filter  {

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
    vc.put("edict_phrase_rows", null);
    vc.put("kanjidic_phrase_rows", null);

    if (dict != null &&
        ime_text != null)  {
      try  {
        if (dict.equals("edict"))  {
          doEdictQuery(req);
        }
        else if (dict.equals("kanjidic"))  {
          doKanjidicQuery(req);
        }
      }
      catch (Exception ex)  {
        req.getServletContext().log(ex.toString());
      }
    }
    chain.doFilter(req, res);
  }

  private void doEdictQuery(ServletRequest req)  {
    // ime_text should not be null here.
    String ime_text = req.getParameter("ime_text");
    String stripped = ime_text.replaceAll("[_%]", "");

    if (KanaUtil.getStringType(stripped) == KanaUtil.OTHER_TYPE)  {
      try  {
        VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
        Connection conn = (Connection) req.getServletContext().getAttribute("conn");
        StringBuilder sb = new StringBuilder();

        sb.append(
          "SELECT phrase, reading, gloss " +
          "FROM edict.entry JOIN " +
          "(SELECT line_no FROM edict.pidx WHERE " +
          "phrase LIKE '");
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
        vc.put("edict_phrase_rows", rows);
      }
      catch (Exception ex)  {
        req.getServletContext().log(ex.toString());
      }
    }
  }

  private void doKanjidicQuery(ServletRequest req)  {
    // ime_text should not be null here.
    String ime_text = req.getParameter("ime_text");
    String stripped = ime_text.replaceAll("[_%]", ""); // Need This ??

    // This is a very broad test. Lots of stuff slips through. Refine it ??
    if (KanaUtil.getStringType(ime_text) == KanaUtil.OTHER_TYPE)  {
      try  {
        VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
        Connection conn = (Connection) req.getServletContext().getAttribute("conn");
        StringBuilder sb = new StringBuilder();

        sb.append(
          "SELECT kanji, u, b, f, g, p, s " +
          "FROM kanjidic.entry WHERE kanji IN ");

        sb.append("(");
        for (int i = 0; i < ime_text.length(); ++i)  {
          if (i > 0)  {
            sb.append(", ");
          }
          sb.append("'" + ime_text.charAt(i) + "'");
        }
        sb.append(")");

        Statement stmnt = conn.createStatement();
        Statement stmntSub = conn.createStatement();
        PreparedStatement psKun =
          conn.prepareStatement("SELECT reading FROM kanjidic.kun WHERE kanji = ?");
        PreparedStatement psOhn =
          conn.prepareStatement("SELECT reading FROM kanjidic.ohn WHERE kanji = ?");
        PreparedStatement psMean =
          conn.prepareStatement("SELECT meaning FROM kanjidic.kanji_meaning WHERE kanji = ?");
        PreparedStatement psRad =
          conn.prepareStatement("SELECT kanji FROM kanjidic.radical WHERE b = ?");
        PreparedStatement psName =
          conn.prepareStatement("SELECT reading FROM kanjidic.radical_name WHERE kanji = ?");
        PreparedStatement psPinyin =
          conn.prepareStatement("SELECT reading FROM kanjidic.pinyin WHERE kanji = ?");
        PreparedStatement psKorean =
          conn.prepareStatement("SELECT reading FROM kanjidic.korean WHERE kanji = ?");

        ResultSet rs = stmnt.executeQuery(sb.toString());
        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        while (rs.next())  {
          Map<String, String> row = new java.util.HashMap<String, String>();
          String kanji = rs.getString(1);
          row.put("kanji", rs.getString(1));  // --> kanji
          row.put("unicode", rs.getString(2));
          row.put("bushu", Integer.toString(rs.getInt(3)));
          row.put("frequency",
            rs.getInt(4) == 0 ? "" : Integer.toString(rs.getInt(4)));
          row.put("grade",
            rs.getInt(5) == 0 ? "" : Integer.toString(rs.getInt(5)));
          row.put("skip", rs.getString(6));
          row.put("strokes", Integer.toString(rs.getInt(7)));
          // kun readings ..
          psKun.setString(1, rs.getString(1));  // --> kanji
          ResultSet rsSub = psKun.executeQuery();
          sb = new StringBuilder();
          while (rsSub.next())  {
            sb.append(rsSub.getString(1));
            sb.append("\u3000");  // Space character
          }
          row.put("kun_readings", sb.toString());
          // ohn readings ..
          psOhn.setString(1, kanji);
          rsSub = psOhn.executeQuery();
          sb = new StringBuilder();
          while (rsSub.next())  {
            sb.append(rsSub.getString(1));
            sb.append("\u3000");  // Space character
          }
          row.put("ohn_readings", sb.toString());
          // kanji meanings ..
          psMean.setString(1, kanji);
          rsSub = psMean.executeQuery();
          sb = new StringBuilder();
          while (rsSub.next())  {
            sb.append(rsSub.getString(1));
            sb.append("; ");
          }
          row.put("kanji_meanings",
            sb.substring(0,
              sb.length() > 2 ? sb.length() - 2 : 0));
          // radical ..
          psRad.setInt(1, rs.getInt(3));
          rsSub = psRad.executeQuery();
          rsSub.next();
          row.put("radical", rsSub.getString(1));
          // name readings ..
          psName.setString(1, kanji);
          rsSub = psName.executeQuery();
          sb = new StringBuilder();
          while (rsSub.next())  {
            sb.append(rsSub.getString(1));
            sb.append("\u3000");  // Space character
          }
          row.put("name_readings", sb.toString());
          // pinyin ..
          psPinyin.setString(1, kanji);
          rsSub = psPinyin.executeQuery();
          sb = new StringBuilder();
          while (rsSub.next())  {
            sb.append(rsSub.getString(1));
            sb.append(" ");
          }
          row.put("pinyin", sb.toString());
          // korean ..
          psKorean.setString(1, kanji);
          rsSub = psKorean.executeQuery();
          sb = new StringBuilder();
          while (rsSub.next())  {
            sb.append(rsSub.getString(1));
            sb.append(" ");
          }
          row.put("korean", sb.toString());

          // done ..
          rows.add(row);
        }  // end rows.
        stmnt.close();
        stmntSub.close();
        if (rows.size() > 0)  {
          vc.put("kanjidic_phrase_rows", rows);
        }
      }
      catch (Exception ex)  {
        req.getServletContext().log(ex.toString());
      }
    }
  }
}
