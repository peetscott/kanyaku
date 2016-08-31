import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Map;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 *
 * @author Peter Jacob
 */
public class JDictSQL  {

  /* These static properties might be used to construct
     the CREATE TABLE statements from a template --
     to avoid possible overflow errors ..
   */

  /* kanjidic properties .. */
  public static int max_ohn_len;
  public static int max_kun_len;
  public static int max_na_len;
  public static int max_rad_len;  // --> rad_name
  public static int max_kor_len;
  public static int max_pin_len;
  public static int max_meaning_len;
  // TO DO ..
  //public static int max_kanjidic_keyword_len;
  //public static int max_kanjidic_kanji_list_len;

  /* edict2 properties .. */
  public static int max_phrase_len;
  public static int max_reading_len;
  public static int max_gloss_len;
  public static int max_edict_eidx_len;
  public static int max_edict_eidx_keyword_len;
  // TO DO ..
  //public static int max_edict_keyword_len;
  //public static int max_edict_keyword_list_len;



  public static void writeKanjidicTables() throws IOException  {

    BufferedReader reader = null;
    OutputStreamWriter kanji = null;
    OutputStreamWriter kanji_meaning = null;;
    OutputStreamWriter ohn = null;
    OutputStreamWriter kun = null;
    OutputStreamWriter na = null;  // --> proper_name
    OutputStreamWriter rad_name = null;  // --> rad_name
    OutputStreamWriter korean = null;
    OutputStreamWriter pinyin = null;
    OutputStreamWriter cross_ref = null;
    OutputStreamWriter mis_classified = null;

    String path;
    File input_dir = null;
    File output_dir = null;

    max_ohn_len =
    max_kun_len =
    max_na_len =
    max_rad_len =
    max_kor_len =
    max_pin_len =
    max_meaning_len = 0;

    try  {
      path = System.getProperty("edict.input.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      path = System.getProperty("edict.output.dir");
      output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "kanjidic")
          ),
          "EUC-JP"));
      kanji = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "kanji_entry.sql")
        ),
        "EUC-JP");
      kanji_meaning = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "kanji_meaning.sql")
        ),
        "EUC-JP");
      ohn = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "ohn.sql")
        ),
        "EUC-JP");
      kun = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "kun.sql")
        ),
        "EUC-JP");
      na = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "name_reading.sql")
        ),
        "EUC-JP");
      rad_name = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "radical_name.sql")  // --> rad_name
        ),
        "EUC-JP");
      korean = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "korean.sql")
        ),
        "EUC-JP");
      pinyin = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "pinyin.sql")
        ),
        "EUC-JP");
      cross_ref = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "cross_ref.sql")
        ),
        "EUC-JP");
      mis_classified = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "mis_classified.sql")
        ),
        "EUC-JP");

      String line = reader.readLine();  // Throw away first line of file.
      while ((line = reader.readLine()) != null)  {
        KanjidicRecord rec = KanjidicRecord.fromLine(line);
        String field;
        kanji.write(kanjiInsertStatement(rec));
        kanji.write("\n");

        for (int i = 0; i < rec.meanings.size(); ++i)  {
          field = rec.meanings.get(i);
          if (field.length() > max_meaning_len)  {
            max_meaning_len = field.length();
          }
          kanji_meaning.write("INSERT INTO kanjidic.kanji_meaning VALUES ('");
          kanji_meaning.write(rec.KANJI);
          kanji_meaning.write("','");
          // Have to escape single quote ..
          kanji_meaning.write(field.replace("'", "''"));
          kanji_meaning.write("');\n");
        }
        for (int i = 0; i < rec.ohn_readings.size(); ++i)  {
          field = rec.ohn_readings.get(i);
          if (field.length() > max_ohn_len)  {
            max_ohn_len = field.length();
          }
          ohn.write("INSERT INTO kanjidic.ohn VALUES ('");
          ohn.write(rec.KANJI);
          ohn.write("','");
          ohn.write(field);
          ohn.write("');\n");
        }
        for (int i = 0; i < rec.kun_readings.size(); ++i)  {
          field = rec.kun_readings.get(i);
          if (field.length() > max_kun_len)  {
            max_kun_len = field.length();
          }
          kun.write("INSERT INTO kanjidic.kun VALUES ('");
          kun.write(rec.KANJI);
          kun.write("','");
          kun.write(field);
          kun.write("');\n");
        }
        for (int i = 0; i < rec.name_readings.size(); ++i)  {
          field = rec.name_readings.get(i);
          if (field.length() > max_na_len)  {
            max_na_len = field.length();
          }
          na.write("INSERT INTO kanjidic.name_reading VALUES ('");
          na.write(rec.KANJI);
          na.write("','");
          na.write(field);
          na.write("');\n");
        }
        for (int i = 0; i < rec.radical_names.size(); ++i)  {
          field = rec.radical_names.get(i);
          if (field.length() > max_rad_len)  {
            max_rad_len = field.length();
          }
          rad_name.write("INSERT INTO kanjidic.radical_name VALUES ('");  // --> kanjidic.rad_name
          rad_name.write(rec.KANJI);
          rad_name.write("','");
          rad_name.write(field);
          rad_name.write("');\n");
        }
        for (int i = 0; i < rec.korean_readings.size(); ++i)  {
          field = rec.korean_readings.get(i);
          if (field.length() > max_kor_len)  {
            max_kor_len = field.length();
          }
          korean.write("INSERT INTO kanjidic.korean VALUES ('");
          korean.write(rec.KANJI);
          korean.write("','");
          korean.write(field);
          korean.write("');\n");
        }
        for (int i = 0; i < rec.chinese_readings.size(); ++i)  {
          field = rec.chinese_readings.get(i);
          if (field.length() > max_pin_len)  {
            max_pin_len = field.length();
          }
          pinyin.write("INSERT INTO kanjidic.pinyin VALUES ('");
          pinyin.write(rec.KANJI);
          pinyin.write("','");
          pinyin.write(field);
          pinyin.write("');\n");
        }
        for (int i = 0; i < rec.cross_refs.size(); ++i)  {
          cross_ref.write("INSERT INTO kanjidic.cross_ref VALUES ('");
          cross_ref.write(rec.KANJI);
          cross_ref.write("','");
          cross_ref.write(rec.cross_refs.get(i));
          cross_ref.write("');\n");
        }
        for (int i = 0; i < rec.mis_classifieds.size(); ++i)  {
          mis_classified.write("INSERT INTO kanjidic.mis_classified VALUES ('");
          mis_classified.write(rec.KANJI);
          mis_classified.write("','");
          mis_classified.write(rec.mis_classifieds.get(i));
          mis_classified.write("');\n");
        }
      }
    }
    finally  {
      if (reader != null) reader.close();
      if (kanji != null) kanji.close();
      if (kanji_meaning != null) kanji_meaning.close();
      if (ohn != null) ohn.close();
      if (kun != null) kun.close();
      if (na != null) na.close();
      if (rad_name != null) rad_name.close();  // --> rad_name
      if (korean != null) korean.close();
      if (pinyin != null) pinyin.close();
      if (cross_ref != null) cross_ref.close();
      if (mis_classified != null) mis_classified.close();
    }
  }

  public static void writeKanjidicEidxTable() throws IOException  {

    BufferedReader reader = null;
    OutputStreamWriter eidx = null;

    String path;
    File input_dir = null;
    File output_dir = null;
    try  {
      path = System.getProperty("edict.input.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      path = System.getProperty("edict.output.dir");
      output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "kanjidic")
          ),
          "EUC-JP"));
      eidx = new OutputStreamWriter(
        new FileOutputStream(
          new File(output_dir, "kanjidic_eidx.sql")
        ),
        "EUC-JP");

      String line = reader.readLine();  // Throw away first line of file.
      Map<String, List<KanjidicCandidateValue>> keywords =
        new java.util.HashMap<String, List<KanjidicCandidateValue>>();

      while ((line = reader.readLine()) != null)  {
        KanjidicRecord rec = KanjidicRecord.fromLine(line);
        collectKanjidicEnglishKeys(rec, keywords);
      }
      for (String key : keywords.keySet())  {
        StringBuilder sb = new StringBuilder();
        KanjidicCandidateValue[] values =
          keywords.get(key).toArray(new KanjidicCandidateValue[0]);
        java.util.Arrays.sort(values,
            new KanjidicCandidateValue());
        for (int i = 0; i < values.length; ++i)  {
          sb.append(values[i].kanji);
        }
        eidx.write("INSERT INTO kanjidic.eidx VALUES ('" );
        eidx.write(key.replace("'", "''"));
        eidx.write("','");
        eidx.write(sb.toString());
        eidx.write("');\n");
      }
    }
    finally  {
      if (eidx != null) eidx.close();
    }
  }

  public static void writeEdictEntryTable() throws IOException  {
    BufferedReader reader = null;
    PrintWriter writer = null;
    int line_no = 1;
    String path;
    File input_dir = null;
    File output_dir = null;

    max_phrase_len =
    max_reading_len =
    max_gloss_len = 0;

    try  {
      path = System.getProperty("edict.input.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      path = System.getProperty("edict.output.dir");
      output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "edict2")
          ),
          "EUC-JP"));
      writer = new PrintWriter(
        new OutputStreamWriter(
          new FileOutputStream(
            new File(output_dir, "edict_entry.sql")
          ),
          "EUC-JP"));

      String line = reader.readLine();  // Read and ignore header.
      while ((line = reader.readLine()) != null)  {
        ++line_no;
        Edict2Record rec = Edict2Record.fromLine(line);
        writer.write("INSERT INTO edict.entry VALUES\n(" + line_no + ",");
        int len = 0;
        StringBuilder buff = new StringBuilder(256);
        // Do phrase ..
        buff.append("'");
        for (int i = 0; i < rec.entries.size(); ++i)  {
          if (i > 0)  {
            buff.append("; ");
            len += 2;
          }
          buff.append(rec.entries.get(i));
          len += rec.entries.get(i).length();
        }
        buff.append("',");
        if (len > max_phrase_len)  {
          max_phrase_len = len;
        }
        // Do reading ..
        if (rec.readings.size() == 0)  {
          buff.append("NULL,'");  // Opening single quote here.
        }
        else  {
          len = 0;
          buff.append("'");
          for (int i = 0; i < rec.readings.size(); ++i)  {
            if (i > 0)  {
              buff.append("; ");
              len += 2;
            }
            buff.append(rec.readings.get(i));
            len += rec.readings.get(i).length();
          }
          buff.append("','");  // Close, open single quote here.
          if (len > max_reading_len)  {
            max_reading_len = len;
          }
        }
        // Do gloss ..
        len = rec.glosses.length();
        if (len > max_gloss_len)  {
          max_gloss_len = len;
        }
        writer.write(buff.toString());
        writer.write(rec.glosses.replace("'", "''"));
        writer.write("');\n");
      }
    }
    catch (Exception ex)  {
      throw new IOException("Problem with edict2 file? Current line: " + line_no, ex);
    }
    finally  {
      if (reader != null)  {
        reader.close();
      }
      if (writer != null)  {
        writer.close();
      }
    }
  }

  public static void writePidxTable() throws IOException  {
    BufferedReader reader = null;
    PrintWriter writer = null;
    int line_no = 1;
    String path;
    File input_dir = null;
    File output_dir = null;
    max_phrase_len = 0;

    try  {
      path = System.getProperty("edict.input.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      path = System.getProperty("edict.output.dir");
      output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "edict2")
          ),
          "EUC-JP"));
      writer = new PrintWriter(
        new OutputStreamWriter(
          new FileOutputStream(
            new File(output_dir, "pidx.sql")
          ),
          "EUC-JP"));
      String line = reader.readLine();  // Read and ignore header.
      while ((line = reader.readLine()) != null)  {
        ++line_no;
        Edict2Record rec = Edict2Record.fromLine(line);
        if (rec.readings.size() == 0)  {
          // Kana only. Ignore.
          continue;
        }
        for (String entry : rec.entries)  {
          int last = entry.indexOf("(");  // Cut annotations ..
          if (last < 0)  {                // stripParens() ??
            last = entry.length();
          }
          if (last > max_phrase_len)  {
            max_phrase_len = last;
          }
          entry = entry.substring(0, last);
          writer.print("INSERT INTO edict.pidx VALUES ('");
          writer.write(entry);
          writer.write("',");
          writer.write(Integer.toString(line_no));
          writer.write(");\n");
        }
      }
    }
    catch (Exception ex)  {
      throw new IOException("Problem with edict2 file? Current line: " + line_no, ex);
    }
    finally  {
      if (reader != null)  {
        reader.close();
      }
      if (writer != null)  {
        writer.close();
      }
    }
  }

  public static void writeRidxTable() throws IOException  {
    BufferedReader reader = null;
    PrintWriter writer = null;
    int line_no = 1;
    String path;
    File input_dir = null;
    File output_dir = null;
    max_reading_len = 0;



    try  {
      path = System.getProperty("edict.input.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      path = System.getProperty("edict.output.dir");
      output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "edict2")
          ),
          "EUC-JP"));
      writer = new PrintWriter(
        new OutputStreamWriter(
          new FileOutputStream(
            new File(output_dir, "ridx.sql")
          ),
          "EUC-JP"));

      String line = reader.readLine();  // Read and ignore header.
      while ((line = reader.readLine()) != null)  {
        ++line_no;
        Edict2Record rec = Edict2Record.fromLine(line);
        java.util.List<String> list = rec.readings;
        if (list.size() == 0)  {
          list = rec.entries;
        }
        for (String reading : list)  {
          int last = reading.indexOf("(");  // Cut annotations ..
          if (last < 0)  {                  // stripParens() ??
            last = reading.length();
          }
          if (last > max_reading_len)  {
            max_reading_len = last;
          }
          reading = reading.substring(0, last);
          writer.print("INSERT INTO edict.ridx VALUES ('");
          writer.write(reading);
          writer.write("',");
          writer.write(Integer.toString(line_no));
          writer.write(");\n");
        }
      }
    }
    catch (Exception ex)  {
      throw new IOException("Problem with edict2 file? Current line: " + line_no, ex);
    }
    finally  {
      if (reader != null)  {
        reader.close();
      }
      if (writer != null)  {
        writer.close();
      }
    }
  }

  public static void prepareEdictEidx() throws IOException  {

    BufferedReader reader = null;
    PrintWriter writer = null;
    int line_no = 1;
    String path;
    File input_dir = null;
    File output_dir = null;


    try  {
      path = System.getProperty("edict.input.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      path = System.getProperty("edict.output.dir");
      output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "edict2")
          ),
          "EUC-JP"));
      writer = new PrintWriter(
        new OutputStreamWriter(
          new FileOutputStream(
            new File(output_dir, "edict_eidx")
          ),
          "EUC-JP"));

      Map<String, List<EdictCandidateValue>> keywords =
        new java.util.HashMap<String, List<EdictCandidateValue>>();

      String line = reader.readLine();  // Read and ignore header.
      while ((line = reader.readLine()) != null)  {
        ++line_no;
        Edict2Record rec = Edict2Record.fromLine(line);
        collectEdictEnglishKeys(rec, line_no, keywords);

      }
      // TO DO: Remove these ..
      int lim = 1000;
      int trunk = 0;
      // ..
      for (String key : keywords.keySet())  {
        StringBuilder sb = new StringBuilder();
        EdictCandidateValue[] values =
          keywords.get(key).toArray(new EdictCandidateValue[0]);
        java.util.Arrays.sort(values,
            new EdictCandidateValue());
        // TO DO: Trim results to a certain length??
        for (int i = 0; i < values.length; ++i)  {
          sb.append(Integer.toString(values[i].line_no) + " ");
        }
        if (key.length() > max_edict_eidx_keyword_len)  {
          max_edict_eidx_keyword_len = key.length();
        }
        if (sb.length() > max_edict_eidx_len)  {
          max_edict_eidx_len = sb.length();
          if (max_edict_eidx_len > lim)  {
            //System.err.println(key);
            lim += 1000;
          }
        }
        // Remove this. Database table now limits the number
        // of results returned ..
        if (sb.length() > 4096)  {
          trunk++;
          sb = new StringBuilder(sb.substring(0, 4096));
          sb = new StringBuilder(sb.substring(0, sb.lastIndexOf(" ") + 1));
        }
        //writer.write("INSERT INTO edict.writer VALUES ('" );
        writer.write(key.replace("'", "''"));
        writer.write("\t");
        //writer.write("','");
        ////writer.write(sb.toString());
        writer.write(sb.substring(0, sb.length() - 1));
        //writer.write("');\n");
        writer.write("\n");
      }
      //System.err.print(max_edict_eidx_len);
      //System.err.println("trunk: " + trunk);
      //System.err.println("key len: " + max_edict_eidx_keyword_len);
    }
    catch (Exception ex)  {
      throw new IOException("Problem with edict2 file? Current line: " + line_no, ex);
    }
    finally  {
      if (reader != null)  {
        reader.close();
      }
      if (writer != null)  {
        writer.close();
      }
    }
  }

  private static void collectKanjidicEnglishKeys(
    KanjidicRecord rec,
    Map<String, List<KanjidicCandidateValue>> keywords)  {


    String[] exclude =
      {"&", "a", "an", "and", "at", "as", "be", "for", "has", "in", "into", "is",
       "of", "off", "on", "or", "out", "the",  "to", "with"};


    for (int i = 0; i < rec.meanings.size(); ++i)  {
      String[] words = stripParens(rec.meanings.get(i)).split(" ");
      for (int j = 0; j < words.length; ++j)  {
        String word = words[j].toLowerCase();
        if (java.util.Arrays.binarySearch(exclude, word) >= 0)  {
          continue;
        }
        if (word.equals(""))  {
          continue;
        }
        KanjidicCandidateValue candidate = new KanjidicCandidateValue();
        List<KanjidicCandidateValue> candidates;
        candidate.kanji = rec.KANJI;
        candidate.position = j + 1;
        candidate.phrase_len = words.length;
        if (keywords.containsKey(word))  {
          candidates = keywords.get(word);
          int index = candidates.indexOf(candidate);
          if (index >= 0)  {
            // Is the current candidate a better match? ..
            KanjidicCandidateValue duplicate = candidates.get(index);
            if (candidate.compare(candidate, duplicate) < 0)  {
              candidates.remove(duplicate);
              candidates.add(candidate);
            }
          }
          else  {
            candidates.add(candidate);
          }
        }
        else  {
          candidates = new java.util.ArrayList<KanjidicCandidateValue>();
          candidates.add(candidate);
          keywords.put(word, candidates);
        }
      }
    }
  }

  private static void collectEdictEnglishKeys(
    Edict2Record rec,
    int line_no,
    Map<String, List<EdictCandidateValue>> keywords)  {

    String[] phrases = rec.glosses.split("/");
    /*
    String[] exclude = {"about", "after", "book", "chinese",
                        "day", "end", "etc", "family",
                        "first", "fish", "food",
                        "get", "go", "good", "great", "house", "japanese",
                        "life", "line", "long", "make", "man", "much", "name",
                        "new", "old", "paper", "people", "person", "place",
                        "point", "rice", "school", "small", "species", "sumo",
                        "system", "take", "time", "water", "way", "work", "year"};
    */
    // The number of results returned for a keyword search is now limited by
    // the size of the database column: VARCHAR (bytes)

    String[] exclude_gram = {
      "#", "&", "-", ".", "a", "all", "an", "and", "any", "as", "at", "back", "be", "being", "by",
      "do", "down", "e.g.", "etc.", "for", "from", "get", "has", "have", "i", "if", "in", "into", "is", "it",
      "me", "my", "no", "not", "of", "off", "on", "one", "one's", "oneself", "or", "other", "out", "over", "some", "someone",
      "something", "that", "the", "them", "themselves", "this", "to", "up", "used", "we", "who", "with", "without", "you",
      "yourself"
    };


    for (int i = 1; i < phrases.length; ++i)  {
      String[] words = stripBrackets(stripParens(phrases[i])).trim().split(" ");

      for (int j = 0; j < words.length; ++j)  {
        String word = words[j].toLowerCase();

        word = word.replace(",", "");
        word = word.replace("?", "");
        word = word.replace("\"", "");
        word = word.replace("!", "");
        word = word.replace("...", "");
        word = word.replace("'s$", "");

        if (word.equals("") ||
            word.startsWith("entl") ||
            word.matches("\\d+") ||
            word.matches("^\\{.+\\}$"))  {
          continue;
        }

        if (java.util.Arrays.binarySearch(exclude_gram, word) >=0
                && words.length > 1)  {
          continue;
        }
        //if (java.util.Arrays.binarySearch(exclude, word) >=0
        //        && j > 3)  {
        //  continue;
        //}
        EdictCandidateValue candidate = new EdictCandidateValue();
        List<EdictCandidateValue> candidates;
        candidate.line_no = line_no;
        candidate.position = j + 1;
        candidate.phrase_len = words.length;

        if (keywords.containsKey(word))  {
          candidates = keywords.get(word);
          int index = candidates.indexOf(candidate);
          if (index >= 0)  {
            // Is the current candidate a better match? ..
            EdictCandidateValue duplicate = candidates.get(index);
            if (candidate.compare(candidate, duplicate) < 0)  {
              candidates.remove(duplicate);
              candidates.add(candidate);
            }
          }
          else  {
            candidates.add(candidate);
          }
        }
        else  {
          candidates = new java.util.ArrayList<EdictCandidateValue>();
          candidates.add(candidate);
          keywords.put(word, candidates);
        }
      }
    }
  }

  public static void loadEdictEidxTable()  throws IOException  {
    BufferedReader reader = null;
    int line_no = 0;
    String path;
    File input_dir = null;
    Connection conn = null;

    try  {
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      conn = java.sql.DriverManager.getConnection("jdbc:derby:jdict_db");

      // TO DO: Change the names of these system properies -> jdict.dict.dir ... ??
      path = System.getProperty("edict.output.dir");
      //path = System.getProperty("edict.output.dir");
      input_dir = new File(path != null ? path : System.getProperty("user.dir"));
      //path = System.getProperty("edict.output.dir");
      //output_dir = new File(path != null ? path : System.getProperty("user.dir"));

      reader = new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(input_dir, "edict_eidx")
          ),
          "EUC-JP"));

      PreparedStatement ps =
        conn.prepareStatement(
          "INSERT INTO edict.eidx VALUES (?, ?)");
      String line;

      while ((line = reader.readLine()) != null)  {
        ++line_no;
        String[] str_list = line.split("\t");
        String keyword = str_list[0];
        // The line numbers as strings ..
        str_list = str_list[1].split(" ");// The line numbers.
        int[] int_list = new int[str_list.length];
        for (int i = 0; i < str_list.length; ++i)  {
          int_list[i] = Integer.parseInt(str_list[i]);
        }

        // TEST THIS ..
        byte[] byte_list = intsToBytes(int_list);
        ps.setString(1, keyword);
        // This depends on the CREATE TABLE definition in create_tables.sql ..
        // Limit the number of results returned ..
        if (byte_list.length > 1024)  {
          byte_list = java.util.Arrays.copyOfRange(byte_list, 0, 1024);
        }
        ps.setBytes(2, byte_list);
        // ..

        ps.execute();
      }
      conn.close();
    }
    catch (java.sql.SQLException ex)  {
      throw new IOException("SQL Error:" + ex + " Line: " + line_no, ex);
    }
    catch (Exception ex)  {
      throw new IOException("Error loading edict.eidx. Line: " + line_no, ex);
    }
    finally  {
      if (reader != null)  {
        reader.close();
      }
    }
  }

  private static String kanjiInsertStatement(KanjidicRecord rec)  {
    StringBuffer buff = new StringBuffer();

    buff.append("INSERT INTO kanjidic.entry VALUES\n('");
    buff.append(rec.KANJI);
    buff.append("','");
    buff.append(rec.JIS);
    buff.append("','");
    buff.append(rec.U);
    buff.append("',");
    if (rec.B == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.B + ",");
    }
    if (rec.C == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.C + ",");
    }
    if (rec.F == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.F + ",");
    }
    if (rec.G == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.G + ",");
    }
    if (rec.H == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.H + ",");
    }
    if (rec.J == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.J + ",");
    }
    if (rec.N == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.N + ",");
    }
    if (rec.V == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.V + ",");
    }
    if (rec.DB == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append("'" + rec.DB + "',");
    }
    if (rec.DC == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DC + ",");
    }
    if (rec.DG == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DG + ",");
    }
    if (rec.DH == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DH + ",");
    }
    if (rec.DJ == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DJ + ",");
    }
    if (rec.DK == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DK + ",");
    }
    if (rec.DO == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DO + ",");
    }
    if (rec.DR == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DR + ",");
    }
    if (rec.DS == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DS + ",");
    }
    if (rec.DT == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.DT + ",");
    }
    if (rec.P == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append("'" + rec.P + "',");
    }
    if (rec.S == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.S + ",");
    }
    if (rec.I == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append("'" + rec.I + "',");
    }
    if (rec.IN == null)  {  // Column ikk
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.IN + ",");
    }
    if (rec.Q == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append("'" + rec.Q + "',");
    }
    if (rec.MN == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append("'" + rec.MN + "',");
    }
    if (rec.MP == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append("'" + rec.MP + "',");
    }
    if (rec.E == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.E + ",");
    }
    if (rec.K == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.K + ",");
    }
    if (rec.L == null)  {
      buff.append("NULL,");
    }
    else  {
      buff.append(rec.L + ",");
    }
    if (rec.O == null)  {
      buff.append("NULL);");
    }
    else  {
      buff.append("'" + rec.O + "');");
    }
    return buff.toString();
  }

  /* This will fail if parentheses are not properly matched. */
  private static String stripParens(String str)  {
    StringBuilder sb = new StringBuilder();
    int openParen = 0;

    for (int i = 0; i < str.length(); ++i)  {
      char ch = str.charAt(i);
      if (ch == '(')  {
        ++openParen;
        continue;
      }
      if (ch == ')')  {
        --openParen;
        continue;
      }
      // Not sure about this. Where does this occur? ..
      //if (ch == ' ' && i > 0 && str.charAt(i - 1) == ' ')  {
      //  continue;
      //}
      // Prevent double space ..
      // Better ..
      if (ch == ' ' &&
          sb.length() > 0 &&
          sb.charAt(sb.length() - 1) == ' ')  {
        continue;
      }
      if (openParen == 0)  {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /*
   * edict glosses have annotations enclosed in brackets: /... {...} .../
   * The annotations are not useful for the index, so we remove them.
   */
  private static String stripBrackets(String str)  {
    StringBuilder sb = new StringBuilder();
    int openBrack = 0;
    // There should not be any nested brackets. Could probably get
    // away with a search and replace.
    for (int i = 0; i < str.length(); ++i)  {
      char ch = str.charAt(i);
      if (ch == '{')  {
        ++openBrack;
        continue;
      }
      if (ch == '}')  {
        --openBrack;
        continue;
      }
      // Prevent double space ..
      if (ch == ' ' &&
          sb.length() > 0 &&
          sb.charAt(sb.length() - 1) == ' ')  {
        continue;
      }
      if (openBrack == 0)  {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /*
   TO DO: Move this outside class.
          Have this return an array of bytes.
   */

  private static byte[] intsToBytes(int[] ints)  {
    if (ints == null)  {
      return null;
    }
    int i = 0;
    int b = 0;
    byte[] bytes = new byte[ints.length * 4];
    while (i < ints.length)  {
      int tmp = ints[i];
      for (int c = b + 3; c >= b; --c)  {
        bytes[c] = (byte) (tmp & 0xFF);
        tmp >>= 8;
      }
      b += 4;
      ++i;
    }
    return bytes;
  }
}
