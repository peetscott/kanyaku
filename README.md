#    kanyaku    #

This is a user interface for the EDICT and KANJIDIC Japanese-English
dictionaries. It consists of a Derby database and a Java servlet. You
can use it to:

* Search EDICT by reading, phrase, pattern, or English keyword.
* Search KANJIDIC by _ohn_ reading, _kun_ reading, radical, grade,
  stroke count, SKIP number, or English keyword.

EDICT and KANJIDIC are both plain text files that you can read with
any EUC-JP enabled text editor or web browser. kanyaku aims to make
doing so a little easier and cleaner. To learn more about the dictionaries
visit the
[EDICT home page](http://www.csse.monash.edu.au/~jwb/edict.html),
the
[Electronic Dictionary Research and Development Group](http://www.edrdg.org/)
website, and the
[KANJIDIC home page](http://www.csse.monash.edu.au/~jwb/kanjidic.html).

##    System Requirements    ##

1.  Both the database and the servlet require Java. If it is not already
    installed on your system you can obtain the Java Runtime Environment (JRE)
    from the downloads section of the Oracle website.

2.  The dictionary files are not included with this distribution. You will
    have to download them yourself from the Monash University server. They
    are available in gzipped and uncompressed formats. (Note: The file name
    is edict2. The edict file is similar - but different.)

    [edict2.gz](http://ftp.monash.edu.au/pub/nihongo/edict2.gz)

    [edict2](http://ftp.monash.edu.au/pub/nihongo/edict2)

    [kanjidic.gz](http://ftp.monash.edu.au/pub/nihongo/kanjidic.gz)

    [kanjidic](http://ftp.monash.edu.au/pub/nihongo/kanjidic)

    To read the files in your browser, remember to set the page encoding
    to EUC-JP.

3.  A servlet container to run the web application. Two options are

    [Jetty](http://download.eclipse.org/jetty)

    and

    [Apache Tomcat](http://tomcat.apache.org)

    Both of these come with extensive manuals that will guide you through
    the setup process. The kanyaku web application uses only basic
    functionality, so you will not need to read all the details - just
    enough to start and stop the server. Other servlet containers
    should work equally well.

4.  If you want to use the build script you will need
    [Apache Ant](http://ant.apache.org).

5.  To compile the Java source code (not required) you will need the
    Java Development Kit (JDK), also available from Oracle.

For more information about usage and setup see webapp/web/index.html
