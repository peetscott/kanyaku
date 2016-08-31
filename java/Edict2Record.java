/**
 *  @author Peter Jacob
 */

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Edict2Record  {


  public List<String> entries = new ArrayList<String>();

  // The list of readings may be empty.
  public List<String> readings = new ArrayList<String>();

  // A string that looks like "/.../.../.../".
  public String glosses;

  /*
  NOTE: edict2 uses a 2-3 byte character encoding. The byte-ranges for
        this encoding (extended EUC-JP) include values which are NOT
        printable ISO-8859-1 characters. MUST use an InputStreamReader/Writer
        configured for EUC-JP.
   */

   /* TO DO. Have this throw Exception. IOException ?? */
  public static Edict2Record fromLine(String line)  {
    Edict2Record rec = new Edict2Record();

    try  {
      StringTokenizer tokens = new StringTokenizer(line, " ", true);
      // Read to the first space char --> entry
      String token = tokens.nextToken();
      String[] list = token.split(";");
      for (int i = 0; i < list.length; ++i)  {
        rec.entries.add(list[i]);
      }
      tokens.nextToken();  // read the space char
      // Now look for either the reading or the glosses.
      token = tokens.nextToken("[/");
      if (token.equals("["))  {
        // reading
        token = tokens.nextToken("]");
        list = token.split(";");
        for (int i = 0; i < list.length; ++i)  {
          rec.readings.add(list[i]);
        }
        token = tokens.nextToken("]");  // "] /" follows reading
        token = tokens.nextToken(" ");
        token = tokens.nextToken("/");
      }
      // Already read the initial '/'. Replace it.
      rec.glosses = "/" + tokens.nextToken("");  // gobble rest of tokens
    }
    catch (java.util.NoSuchElementException ex)  {
      System.err.println("Format error? : " + line + "  " + ex);
    }
    catch (NullPointerException ex)  {
      // Should never occur.
    }

    return rec;
  }
}
