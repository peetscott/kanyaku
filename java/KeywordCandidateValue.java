/**
    KeywordCandidateValue is a structure that represents a possible match
    for a keyword search of edict or kanjidic. The indexing algorithm
    associates with each keyword a list of dictionary records in which
    the keyword appears. An attempt is made to order the list according
    to the relevancy of the record to the keyword. The most relevant records
    are at the front of the list and will appear at the top of the keyword
    search results.

    position    Is the index of a keyword within a phrase.
    phrase_len  Is the length of the phrase in which the keyword appears.

    compare()   Decides the relevance of a record to a keyword.
                Records with single word phrases are the most relevant to
                that particular keyword.
                Keywords with a lesser position are considered more relevant.
                Keywords at the same position are sorted according to the
                length of their respective phrases. Shorter phrases are
                considered more relevant.


 */
abstract class KeywordCandidateValue implements java.util.Comparator<KeywordCandidateValue>{

  int position;
  int phrase_len;

  public int compare(KeywordCandidateValue v1, KeywordCandidateValue v2)  {
    if (v1.position < v2.position)  {
      return -1;
    }
    if (v1.position == v2.position)  {
      if (v1.phrase_len < v2.phrase_len)  {
        return -1;
      }
      if (v1.phrase_len == v2.phrase_len)  {
        return 0;
      }
      return 1;
    }
    return 1;
  }

  abstract public boolean equals(Object val);
}
