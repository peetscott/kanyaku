/**
  A driver program for the JDictSQL class.
 */

public class JDictPrepare  {


  public static void main(String[] args)  {

    try  {
      JDictSQL.writeKanjidicTables();
      JDictSQL.writeKanjidicEidxTable();
      JDictSQL.prepareEdictEidx();
      JDictSQL.loadEdictEidxTable();
      JDictSQL.writeEdictEntryTable();
      JDictSQL.writePidxTable();
      JDictSQL.writeRidxTable();
    }
    catch (java.io.IOException ex)  {
      System.err.println("Could not write the SQL files: " + ex);
    }
  }
}
