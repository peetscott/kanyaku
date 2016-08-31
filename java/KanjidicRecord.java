import java.util.List;

public class KanjidicRecord  {

  public String KANJI;       // KANJI
  public String JIS;         // JIS encoding - HEX
  public String U;           // Unicode value - HEX
  public String B = null;    // Radical - Nelson (Bushu)
  public String C = null;    // Radical - Classical
  public String F = null;    // Frequency
  public String G = null;    // Jouyou grade
  public String H = null;    // Halpern index no.
  public String J = null;    // Japanese Language Proficiency Test (JLPT) level
  public String N = null;    // Nelson index no.
  public String V = null;    // New Nelson index no.
  public String DB = null;   // Japanese For Busy People
  public String DC = null;   // Crowley, Kanji Way to Japanese Power
  public String DG = null;   // Kodansha Compact Kanji Guide
  public String DH = null;   // Hensall - Guide to Reading and Writing Japanese
  public String DJ = null;   // Kanji in Context index
  public String DK = null;   // Kanji Learners Dictionary index no.
  public String DO = null;   // Essential Kanji (O'Neill) index no.
  public String DR = null;   // 2001 Kanji
  public String DS = null;   // Sakade - Guide to Reading and Writing Japanese
  public String DT = null;   // Tuttle Kanji Card no.
  public String P = null;    // SKIP pattern code
  public String S = null;    // Stroke count
  public String I = null;    // Tuttle Kanji Dictionary
  public String IN = null; 	 // Tuttle Kanji & Kana
  public String Q = null;    // Four Corner code
  public String MN = null;   // Morohashi index no.
  public String MP = null;   // Morohashi volume - page
  public String E = null;    // Guide to Remembering Japanese Characters - Henshall
  public String K = null;    // Gakken Kanji Dictionary
  public String L = null;    // Remembering the Kanji - Heisig
  public String O = null;    // Japanese Names - O'Neill
  public String X = null;    // cross-reference

  public List<String> meanings = new java.util.ArrayList<String>();
  public List<String> ohn_readings = new java.util.ArrayList<String>();
  public List<String> kun_readings = new java.util.ArrayList<String>();
  public List<String> name_readings = new java.util.ArrayList<String>();
  public List<String> radical_names = new java.util.ArrayList<String>();
  public List<String> korean_readings = new java.util.ArrayList<String>();
  public List<String> chinese_readings = new java.util.ArrayList<String>();
  public List<String> cross_refs = new java.util.ArrayList<String>();
  public List<String> mis_classifieds = new java.util.ArrayList<String>();

  public static KanjidicRecord fromLine(String line)  {
    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(line);
    KanjidicRecord rec = new KanjidicRecord();
    StringBuffer token;
    boolean T1;
    boolean T2;

    rec.KANJI = tokenizer.nextToken();
    rec.JIS = tokenizer.nextToken();
    T1 = T2 = false;  // flags. following token of class T1 or T2 ?
    while (tokenizer.hasMoreTokens())  {
      char ch;
      token = new StringBuffer(tokenizer.nextToken());
      ch = token.charAt(0);
      switch (ch)  {
      case 'U' :
        token.deleteCharAt(0);
        rec.U = token.toString();
        break;
      case 'B' :
        token.deleteCharAt(0);
        rec.B = token.toString();
        break;
      case 'C' :
        token.deleteCharAt(0);
        rec.C = token.toString();
        break;
      case 'F' :
        token.deleteCharAt(0);
        rec.F = token.toString();
        break;
      case 'G' :
        token.deleteCharAt(0);
        rec.G = token.toString();
        break;
      case 'H' :
        token.deleteCharAt(0);
        rec.H = token.toString();
        break;
      case 'J' :  // JLPT Level
        token.deleteCharAt(0);
        rec.J = token.toString();
        break;
      case 'N' :
        token.deleteCharAt(0);
        rec.N = token.toString();
        break;
      case 'V' :
        token.deleteCharAt(0);
        rec.V = token.toString();
        break;
      case 'D' :
        ch = token.charAt(1);
        token.deleteCharAt(0);
        token.deleteCharAt(0);
        switch (ch)  {
        case 'B' :
          rec.DB = token.toString();
          break;
        case 'C' :
          rec.DC = token.toString();
          break;
        case 'G' :
          rec.DG = token.toString();
          break;
        case 'H' :
          rec.DH = token.toString();
          break;
        case 'J' :
          rec.DJ = token.toString();
          break;
        case 'K' :
          rec.DK = token.toString();
          break;
        case 'O' :
          rec.DO = token.toString();
          break;
        case 'R' :
          rec.DR = token.toString();
          break;
        case 'S' :
          rec.DS = token.toString();
          break;
        case 'T' :
          rec.DT = token.toString();
          break;
        }
        break;
      case 'P' :
        token.deleteCharAt(0);
        rec.P = token.toString();
        break;
      case 'S' :
        token.deleteCharAt(0);
        rec.S = token.toString();
        break;
      case 'I' :
        ch = token.charAt(1);
        token.deleteCharAt(0);
        if (ch == 'N')  {
          token.deleteCharAt(0);
          rec.IN = token.toString();
        }
        else  {
          rec.I = token.toString();
        }
        break;
      case 'Q' :
        token.deleteCharAt(0);
        rec.Q = token.toString();
        break;
      case 'M' :
        ch = token.charAt(1);
        token.deleteCharAt(0);
        token.deleteCharAt(0);
        switch (ch)  {
        case 'N' :
          rec.MN = token.toString();
          break;
        case 'P' :
          rec.MP = token.toString();
          break;
        }
        break;
      case 'E' :
        token.deleteCharAt(0);
        rec.E = token.toString();
        break;
      case 'K' :
        token.deleteCharAt(0);
        rec.K = token.toString();
        break;
      case 'L' :
        token.deleteCharAt(0);
        rec.L = token.toString();
        break;
      case 'O' :
        token.deleteCharAt(0);
        rec.O = token.toString();
        break;
      case 'W' :
        token.deleteCharAt(0);
        rec.korean_readings.add(token.toString());
        break;
      case 'Y' :
        // TO DO: Check JIS 6124 ... Yqian1wa3 Typo I think. Yes.
        // Should be corrected in future kanjidic files.
        token.deleteCharAt(0);
        rec.chinese_readings.add(token.toString());
        break;
      case 'X' :
        token.deleteCharAt(0);
        rec.cross_refs.add(token.toString());
        break;
      case 'Z' :
        token.deleteCharAt(0);
        rec.mis_classifieds.add(token.toString());
        break;
      case 'T' :
        ch = token.charAt(1);
        switch (ch)  {
        case '1' :
          T1 = true;
          T2 = false;
          break;
        case '2' :
          T2 = true;
          T1 = false;
          break;
        }
        break;
      case '{' :  // Meanings enclosed in { ... }
        while (token.charAt(token.length() - 1) != '}')  {
          token.append(' ');    // nextToken() strips space character.
          token.append(tokenizer.nextToken());
        }
        token.deleteCharAt(0);  // Remove '{'
        token.deleteCharAt(token.length() - 1);  // Remove '}'
        rec.meanings.add(token.toString());
        break;
      default :
        if (T1 == true)  {
          rec.name_readings.add(token.toString());
          break;
        }
        if (T2 == true)  {
          rec.radical_names.add(token.toString());
          break;
        }
        if (ch == '-')  {
          ch = token.charAt(1);
        }
        if (ch < 0x30A1)  { // hiragana
          rec.kun_readings.add(token.toString());
        }
        else  {  // should be katakana
          rec.ohn_readings.add(token.toString());
        }
        break;
      }
    }

    return rec;
  }
}
