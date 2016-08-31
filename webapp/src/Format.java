

public class Format  {

  public static String edictPhraseHTML(String phrase)  {
    String[] variants = phrase.split("; ");
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < variants.length; ++i)  {
      sb.append("<div>");
      sb.append(hyperlinkPhrase(variants[i]));
      sb.append("</div>");
    }
    return sb.toString();
  }

  public static String edictReadingHTML(String reading)  {
    String[] variants = reading.split("; ");
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < variants.length; ++i)  {
      sb.append("<div>");
      sb.append(variants[i]);
      sb.append("</div>");
    }
    return sb.toString();
  }

  public static String edictGlossHTML(String gloss)  {
    String[] glosses = gloss.split("/");
    StringBuilder sb = new StringBuilder();

    sb.append("<div>");
    for (int i = 1; i < glosses.length - 1; ++i)  {

      sb.append(hyperlinkGloss(glosses[i]));
      if (glosses[i + 1].charAt(0) == '(')  {
        sb.append("</div><div>");
      }
      else if (i < glosses.length - 2)  {
        sb.append("; ");
      }
    }
    sb.append("</div>");
    return sb.toString();
  }


  private static String hyperlinkPhrase(String str)  {
    StringBuilder result = new StringBuilder();
    int start;
    int end = 0;
    int len = str.length();

    while (end < len)  {
      start = -1;
      while (end < len &&
             (str.charAt(end) < 0x4e00 ||
              str.charAt(end) > 0x9fa0))  {
        result.append(str.charAt(end));
        ++end;
      }
      if (end < len)  {
        start = end;
      }
      while (end < len &&
             str.charAt(end) >= 0x4e00 &&
             str.charAt(end) <= 0x9fa0)  {
        ++end;
      }
      if (start >= 0)  {
        result.append("<a href=\"jdict?ime_text=");
        try  {
        // InternetExplorer requires? encoded URLs.
        // Check Internet Options for URL encoding.
        result.append(java.net.URLEncoder.encode(str.substring(start, end), "UTF-8"));
        }
        catch (java.io.UnsupportedEncodingException ex)  { }
        result.append("&kana=hira&dict=kanjidic\">");
        result.append(str.substring(start, end));
        result.append("</a>");
      }
    }

    return result.toString();
  }

  /* TO DO: Missing 0x30FC Prolonged Sound Mark.   OK*/
  /* TO DO: Middle Dot is used as a separator. */

  // private static String hyperlinkGloss(String str)  {
    // StringBuilder result = new StringBuilder();
    // int start;
    // int end = 0;
    // int len = str.length();
    // while (end < len)  {
      // start = -1;
      // while (end < len &&
             // (str.charAt(end) < 0x3041 ||
             // // Consume leading Middle Dot, Prolonged Sound Mark ..
             // // 0x30fb, 0x30fc
              // (str.charAt(end) > 0x30fa &&
               // str.charAt(end) < 0x4e00
              // ) ||
              // str.charAt(end) > 0x9fa0
             // )
            // )  {
        // result.append(str.charAt(end));
        // ++end;
      // }
      // if (end < len)  {
        // start = end;
      // }
      // while (end < len &&
             // ((str.charAt(end) >= 0x3041 &&
             // // Retain Middle Dot, Prolonged Sound Mark ..
               // str.charAt(end) <= 0x30fc) ||
              // (str.charAt(end) >= 0x4e00 &&
              // str.charAt(end) <= 0x9fa0)))  {
        // // .. Unless Middle Dot not in katakana string ..
        // if (end > 0 && end < len - 1 &&
            // str.charAt(end) == 0x30fb &&
             // (
               // (str.charAt(end - 1) < 0x30a2 ||
                // str.charAt(end - 1) > 0x30f9
               // ) ||
               // (str.charAt(end + 1) < 0x30a2 ||
                // str.charAt(end + 1) > 0x30f9
               // )
             // )
           // )  {
          // break;  // Leave behind Middle Dot.
        // }
        // ++end;
      // }
      // if (start >= 0)  {
        // result.append("<a href=\"jdict?ime_text=");
        // try  {
          // // IE will not handle unencoded UTF-8 in URLs. Other browsers do.
          // result.append(
            // java.net.URLEncoder.encode(str.substring(start, end), "UTF-8")
          // );
        // }
        // catch (java.io.UnsupportedEncodingException ex)  { }
        // result.append("&kana=hira&dict=edict\">");
        // result.append(str.substring(start, end));
        // result.append("</a>");
      // }
    // }
    // return result.toString();
  // }

  private static String hyperlinkGloss(String str)  {
    StringBuilder result = new StringBuilder();
    int start;
    int end = 0;
    int len = str.length();
    char ch;

    while (end < len)  {
      start = -1;
      //ch = str.charAt(end);
      while (end < len)  {
        ch = str.charAt(end);
        if (ch < 0x3041 ||
              (ch > 0x30fa &&  // Check this.
               ch < 0x4e00
              ) ||
              ch > 0x9fa0
            )  {
          result.append(ch);
          ++end;
        }
        else  {
          break;
        }
      }
      if (end < len)  {
        start = end;
      }
      while (end < len)  {
        ch = str.charAt(end);
        if ((ch >= 0x3041 &&
              ch <= 0x30fc) ||
              (ch >= 0x4e00 &&
              ch <= 0x9fa0))  {
          // Is Middle Dot not in katakana string? ..
          if (end > 0 && end < len - 1 &&
              ch == 0x30fb &&
               (
                 (str.charAt(end - 1) < 0x30a2 ||
                  str.charAt(end - 1) > 0x30f9
                 ) ||
                 (str.charAt(end + 1) < 0x30a2 ||
                  str.charAt(end + 1) > 0x30f9
                 )
               )
             )  {
            break;
          }
          ++end;
        }
        else  {
          break;
        }
      }
      if (start >= 0)  {
        result.append("<a href=\"jdict?ime_text=");
        try  {
          // IE will not handle unencoded UTF-8 in URLs. Other browsers do.
          result.append(
            java.net.URLEncoder.encode(str.substring(start, end), "UTF-8")
          );
        }
        catch (java.io.UnsupportedEncodingException ex)  { }
        result.append("&kana=hira&dict=edict\">");
        result.append(str.substring(start, end));
        result.append("</a>");
      }
    }
    return result.toString();
  }
}
