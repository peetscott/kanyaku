class EdictCandidateValue extends KeywordCandidateValue  {

  int line_no;

  public boolean equals(Object val)  {

    if (val != null &&
       (val instanceof EdictCandidateValue))  {

      if (this.line_no == (((EdictCandidateValue) val).line_no))  {
        return true;
      }
      return false;
    }
    return false;
  }
}
