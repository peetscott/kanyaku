import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Map;

import java.net.URLEncoder;

import java.io.IOException;

import org.apache.velocity.VelocityContext;

public class AsciiFilter implements Filter  {

  public void init(FilterConfig config) throws ServletException  {

  }

  public void destroy()  {

  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws ServletException, IOException  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();
    String dict = req.getParameter("dict");
    String ime_text = req.getParameter("ime_text");
    int i = 0;

    vc.put("kanjidic_radno_rows", null);
    vc.put("kanjidic_radical_row", null);
    vc.put("kanjidic_rad_row", null);
    vc.put("kanjidic_strokes_rows", null);
    vc.put("kanjidic_grade_rows", null);
    vc.put("kanjidic_skip_rows", null);
    vc.put("kanjidic_keyword_rows", null);
    vc.put("edict_keyword_rows", null);

    if (ime_text != null &&
        KanaUtil.getStringType(ime_text) == KanaUtil.ASCII_TYPE)  {
      try  {
        switch (Character.toLowerCase(ime_text.charAt(0)))  {
          case 'r' :
            try  {
              i = Integer.parseInt(ime_text.substring(1));
            }
            catch (Exception ex)  {
              i = 0;  // flag
            }
            if (i > 0 && i < 215)  {
              doRadicalQuery(req, i);
            }
            else  {
              i = 0;  // flag
            }
            break;
          case 's' :
            try  {
              i = Integer.parseInt(ime_text.substring(1));
            }
            catch (Exception ex)  {
              i = 0;  // flag
            }
            if (i > 0 && i < 31)  {
              doStrokesQuery(req, i);
            }
            else  {
              i = 0;  // flag
            }
            break;
          case 'g' :
            try  {
              i = Integer.parseInt(ime_text.substring(1));
            }
            catch (Exception ex)  {
              i = 0;  // flag
            }
            if ((i > 0 && i < 7) ||
                (i > 7 && i < 11))  {
              doGradeQuery(req, i);
            }
            else  {
              i = 0;  // flag
            }
            break;
          case 'u' :
            if (ime_text.length() == 5)  {
              try  {
                String num = ime_text.substring(1);  // uxxxx
                i = Integer.parseInt(num, 16);
                num = new String(new int[] { i }, 0, 1);
                ((HttpServletResponse) res).sendRedirect(
                  "jdict?ime_text=" +
                  URLEncoder.encode(num, "UTF-8") +
                  "&kana=none&dict=kanjidic"
                );
                return;  // Redirect abandons this response. Do not doFilter()
              }
              catch (NumberFormatException ex)  {  }
            }
            i = 0;  // flag
            break;
          default : // SKIP?
            try  {
              if (ime_text.matches("\\d-\\d{1,2}-\\d{1,2}"))  {
                doSkipQuery(req, ime_text);
                i = 1;
              }
            }
            catch (java.util.regex.PatternSyntaxException ex)  {  }
            //i = 0;  // flag
            break;
        }

        if (i == 0)  {  // Not done. Do english word search.
          if (dict.equals("kanjidic"))  {
            doKanjidicKeywordQuery(req, ime_text);
          }
          else if (dict.equals("edict"))  {
            doEdictKeywordQuery(req, ime_text);
          }
        }
      }
      catch (Exception ex)  {
        req.getServletContext().log(ex.toString());
      }
    }

    chain.doFilter(req, res);
  }

  private void doRadicalQuery(ServletRequest req, int number)  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();
    String radical = "";

    try  {
      // NOTE: kanji table name has changed to entry.
      // This depends on .derby\jdict database ..
      //sb.append("SELECT kanjidic.kanji.entry, kanjidic.kanji.s FROM kanjidic.kanji WHERE kanjidic.kanji.b = ");
      sb.append("SELECT kanji FROM kanjidic.entry WHERE b = ");
      sb.append(number);
      sb.append(" ORDER BY s");

      Statement stmnt = conn.createStatement();
      ResultSet rs = stmnt.executeQuery(sb.toString());
      ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
      Map<String, String> row;
      while (rs.next())  {
        row = new java.util.HashMap<String, String>();
        row.put("kanji", rs.getString(1));
        rows.add(row);
      }
      vc.put("kanjidic_radno_rows", rows);
      // Get radical record ..
      // First, the character ..
      sb = new StringBuilder();
      sb.append("SELECT kanjidic.radical.kanji FROM kanjidic.radical WHERE kanjidic.radical.b = ");
      sb.append(number);
      rs = stmnt.executeQuery(sb.toString());
      if (rs.next())  {
        radical = rs.getString(1);
      }
      // Now, query kanji table ..
      sb = new StringBuilder();
      // NOTE: kanji table name has changed to entry.
      // This depends on .derby\jdict database ..
      //sb.append(
      //  "SELECT kanjidic.kanji.entry, kanjidic.kanji.u, " +
      //  "kanjidic.kanji.f, kanjidic.kanji.g, kanjidic.kanji.p, kanjidic.kanji.s " +
      //  "FROM kanjidic.kanji WHERE kanjidic.kanji.entry = '");
      sb.append(
        "SELECT kanji, u, f, g, p, s " +
        "FROM kanjidic.entry WHERE kanji = '");
      sb.append(radical);
      sb.append("'");
      rs = stmnt.executeQuery(sb.toString());
      row = new java.util.HashMap<String, String>();
      if (rs.next())  {  // Some radicals are not in kanjidic.
        row.put("kanji", rs.getString(1));
        row.put("unicode", rs.getString(2));
        row.put("frequency",
          rs.getInt(3) == 0 ? "" : Integer.toString(rs.getInt(3)));
        row.put("grade",
          rs.getInt(4) == 0 ? "" : Integer.toString(rs.getInt(4)));
        row.put("skip", rs.getString(5));
        row.put("strokes", Integer.toString(rs.getInt(6)));
        row.put("radical", radical);

        // kun readings ..
        sb = new StringBuilder();
        sb.append("SELECT reading FROM kanjidic.kun WHERE kanji = '");
        sb.append(radical);
        sb.append("'");
        rs = stmnt.executeQuery(sb.toString());
        sb = new StringBuilder();
        while (rs.next())  {
          sb.append(rs.getString(1));
          sb.append("\u3000");  // Space character
        }
        row.put("kun_readings", sb.toString());
        // radical names ..
        sb = new StringBuilder();
        // NOTE: kanjidic.rad --> kanjidic.rad_name
        //sb.append("SELECT kanjidic.rad.reading FROM kanjidic.rad WHERE kanjidic.rad.kanji = '");
        sb.append("SELECT reading FROM kanjidic.radical_name WHERE kanji = '");
        sb.append(radical);
        sb.append("'");
        rs = stmnt.executeQuery(sb.toString());
        sb = new StringBuilder();
        while (rs.next())  {
          sb.append(rs.getString(1));
          sb.append("\u3000");  // Space character
        }
        row.put("radical_names", sb.toString());

        // ohn readings ..
        sb = new StringBuilder();
        sb.append("SELECT reading FROM kanjidic.ohn WHERE kanji = '");
        sb.append(radical);
        sb.append("'");
        //rs.close();
        rs = stmnt.executeQuery(sb.toString());
        sb = new StringBuilder();
        while (rs.next())  {
          sb.append(rs.getString(1));
          sb.append("\u3000");  // Space character
        }
        row.put("ohn_readings", sb.toString());
        // kanji meanings ..
        sb = new StringBuilder();
        sb.append("SELECT meaning FROM kanjidic.kanji_meaning WHERE kanji = '");
        sb.append(radical);
        sb.append("'");
        //rs.close();
        rs = stmnt.executeQuery(sb.toString());
        sb = new StringBuilder();
        while (rs.next())  {
          sb.append(rs.getString(1));
          sb.append("; ");
        }
        row.put("kanji_meanings",
          sb.substring(0,
            sb.length() > 2 ? sb.length() - 2 : 0));

        vc.put("kanjidic_radical_row", row);
      }

      stmnt.close();
    }
    catch (Exception ex)  {
      req.getServletContext().log(ex.toString());
    }
  }

  private static void doStrokesQuery(ServletRequest req, int number)  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();

    try  {
      // NOTE: kanji table name has changed to entry.
      // This depends on .derby\jdict database ..
      //sb.append("SELECT kanjidic.kanji.entry, kanjidic.kanji.b FROM kanjidic.kanji WHERE kanjidic.kanji.s = ");
      sb.append("SELECT kanji FROM kanjidic.entry WHERE s = ");
      sb.append(number);
      sb.append(" ORDER BY b");

      Statement stmnt = conn.createStatement();
      ResultSet rs = stmnt.executeQuery(sb.toString());
      ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
      Map<String, String> row;
      while (rs.next())  {
        row = new java.util.HashMap<String, String>();
        row.put("kanji", rs.getString(1));
        rows.add(row);
      }
      vc.put("kanjidic_strokes_rows", rows);
      stmnt.close();
    }
    catch (Exception ex)  {
      req.getServletContext().log(ex.toString());
    }
  }

  private static void doGradeQuery(ServletRequest req, int number)  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();

    try  {
      // NOTE: kanji table name has changed to entry.
      // This depends on .derby\jdict database ..
      // WRONG! Col 1 and 2 SAME. ..
      //sb.append("SELECT kanjidic.kanji.entry, kanjidic.kanji.entry FROM kanjidic.kanji WHERE kanjidic.kanji.g = ");
      sb.append("SELECT kanji FROM kanjidic.entry WHERE g = ");
      sb.append(number);
      sb.append(" ORDER BY b");

      Statement stmnt = conn.createStatement();
      ResultSet rs = stmnt.executeQuery(sb.toString());
      ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
      Map<String, String> row;
      while (rs.next())  {
        row = new java.util.HashMap<String, String>();
        row.put("kanji", rs.getString(1));
        rows.add(row);
      }
      vc.put("kanjidic_grade_rows", rows);
      stmnt.close();
    }
    catch (Exception ex)  {
      req.getServletContext().log(ex.toString());
    }
  }

  private static void doSkipQuery(ServletRequest req, String skip)  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();

    try  {
      // NOTE: kanji table name has changed to entry.
      // This depends on .derby\jdict database ..
      //sb.append("(SELECT kanjidic.kanji.entry, kanjidic.kanji.b FROM kanjidic.kanji WHERE kanjidic.kanji.p = '");
      //sb.append(skip);
      //sb.append("') UNION (SELECT kanjidic.mis_classified.kanji, 215 FROM kanjidic.mis_classified WHERE kanjidic.mis_classified.p = '");
      //sb.append(skip);
      //sb.append("') ORDER BY 2");
      sb.append("(SELECT kanji, b FROM kanjidic.entry WHERE p = '");
      sb.append(skip);
      sb.append("') UNION (SELECT kanji, 215 FROM kanjidic.mis_classified WHERE p = '");
      sb.append(skip);
      sb.append("') ORDER BY 2");

      Statement stmnt = conn.createStatement();
      ResultSet rs = stmnt.executeQuery(sb.toString());
      ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
      Map<String, String> row;
      while (rs.next())  {
        row = new java.util.HashMap<String, String>();
        row.put("kanji", rs.getString(1));
        rows.add(row);
      }
      vc.put("kanjidic_skip_rows", rows);
      stmnt.close();
    }
    catch (Exception ex)  {
      req.getServletContext().log(ex.toString());
    }
  }

  private static void doKanjidicKeywordQuery(ServletRequest req, String word)  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();

    try  {


      //sb.append("SELECT kanjidic.eidx.kanji_list FROM kanjidic.eidx WHERE kanjidic.eidx.keyword = '");
      sb.append("SELECT kanji_list FROM kanjidic.eidx WHERE keyword = '");
      sb.append(word);
      sb.append("'");

      Statement stmnt = conn.createStatement();
      ResultSet rs = stmnt.executeQuery(sb.toString());
      if (rs.next())  {

        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        Map<String, String> row;
        String kanji_list = rs.getString(1);


        //rs.close();
        for (int i = 0; i < kanji_list.length(); ++i)  {
          sb = new StringBuilder();
          sb.append("SELECT meaning from kanjidic.kanji_meaning WHERE kanji = '");
          sb.append(kanji_list.charAt(i));
          sb.append("'");
          rs = stmnt.executeQuery(sb.toString());

          sb = new StringBuilder();
          while (rs.next())  {
            sb.append(rs.getString(1));
            sb.append("; ");
          }

          row = new java.util.HashMap<String, String>();
          row.put("kanji", kanji_list);
          row.put("kanji", kanji_list.substring(i, i + 1));
          row.put("kanji_meanings",
            sb.substring(0,
              sb.length() > 2 ? sb.length() - 2 : 0));  // trim "; "

          rows.add(row);
        }
        vc.put("kanjidic_keyword_rows", rows);

      }
      stmnt.close();
    }
    catch (Exception ex)  {
      req.getServletContext().log(ex.toString());
    }
  }

  private static void doEdictKeywordQuery(ServletRequest req, String word)  {

    VelocityContext vc = (VelocityContext) req.getAttribute("velocity_context");
    Connection conn = (Connection) req.getServletContext().getAttribute("conn");
    StringBuilder sb = new StringBuilder();

    try  {

      sb.append("SELECT line_nos FROM edict.eidx WHERE keyword = '");
      sb.append(word);
      sb.append("'");

      Statement stmnt = conn.createStatement();
      ResultSet rs = stmnt.executeQuery(sb.toString());
      if (rs.next())  {

        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        Map<String, String> row;
        int[] line_nos = bytesToInts(rs.getBytes(1));
        java.sql.PreparedStatement ps =
          conn.prepareStatement(
            "SELECT phrase, reading, gloss FROM edict.entry WHERE line_no = ?"
          );


        for (int i = 0; i < line_nos.length; ++i)  {
          ps.setInt(1, line_nos[i]);
          rs = ps.executeQuery();
          if (rs.next())  {
            row = new java.util.HashMap<String, String>();
            row.put("phrase", rs.getString(1));
            row.put("reading", rs.getString(2) == null ? "" : rs.getString(2));
            row.put("gloss", rs.getString(3));
            rows.add(row);
          }
        }
        vc.put("edict_keyword_rows", rows);

      }
      stmnt.close();
    }
    catch (Exception ex)  {
      req.getServletContext().log(ex.toString());
    }
  }

  private static int[] bytesToInts(byte[] bytes)  {
    if (bytes == null) return null;
    //int b = bytes == null ? 0 : bytes.length;
    int b = bytes.length;
    int i = 0;
    int[] ints = b >= 4 ? new int[(int) b / 4] : null;
    b = 0;
    while (b <= bytes.length - 4)  {
      for (int c = b; c < b + 4; ++c)  {
        ints[i] <<= 8;
        ints[i] |= (bytes[c] & 0xFF);
      }
      ++i;
      b += 4;
    }
    return ints;
  }
}
