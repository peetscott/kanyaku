
class KanaUtil  {

  public static final int OTHER_TYPE = 0;
  public static final int HIRAGANA_TYPE = 1;
  public static final int KATAKANA_TYPE = 2;
  public static final int ASCII_TYPE = 3;


  /*
  Examines a string to determine if it is hiragana, katakana,
  ascii or some other mix of characters.
  */
  public static int getStringType(String str)  {
    boolean hasHiragana = false;
    boolean hasKatakana = false;
    boolean hasAscii = false;
    boolean hasOther = false;

    for (int i = 0; i < str.length(); ++i)  {
      char ch = str.charAt(i);
      if (ch > 0x3040 && ch < 0x3094)  {
        hasHiragana = true;
      }
      else if (ch > 0x30a0 && ch < 0x3200)  {
        hasKatakana = true;
      }
      else if (ch < 0x7f)  {
        hasAscii = true;
      }
      else  {
        hasOther = true;
      }
    }
    if (hasHiragana)  {
      if (hasKatakana || hasAscii || hasOther)  {
        return KanaUtil.OTHER_TYPE;
      }
      else  {
        return KanaUtil.HIRAGANA_TYPE;
      }
    }
    else if (hasKatakana)  {
      if (hasAscii || hasOther)  {
        return KanaUtil.OTHER_TYPE;
      }
      else  {
        return KanaUtil.KATAKANA_TYPE;
      }
    }
    else if (hasAscii)  {
      if (hasOther)  {
        return KanaUtil.OTHER_TYPE;
      }
      else  {
        return KanaUtil.ASCII_TYPE;
      }
    }
    return KanaUtil.OTHER_TYPE;
  }
}
