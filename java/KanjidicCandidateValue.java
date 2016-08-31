class KanjidicCandidateValue extends KeywordCandidateValue  {

  String kanji;

  public boolean equals(Object val)  {

    if (val != null &&
       (val instanceof KanjidicCandidateValue))  {

      if (this.kanji.equals(((KanjidicCandidateValue) val).kanji))  {
        return true;
      }
      return false;
    }
    return false;
  }
}
