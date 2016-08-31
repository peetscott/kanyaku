-- DO NOT DELETE THIS FILE. --

CREATE SCHEMA kanjidic;

CREATE SCHEMA edict;

CREATE TABLE kanjidic.entry (
  kanji CHAR (1),
  jis CHAR (4) NOT NULL,
  u CHAR (4) NOT NULL,
  b SMALLINT NOT NULL,
  c SMALLINT,
  f SMALLINT,
  g SMALLINT,
  h SMALLINT,
  j SMALLINT,
  n SMALLINT,
  v SMALLINT,
  db CHAR (4),
  dc SMALLINT,
  dg SMALLINT,
  dh SMALLINT,
  dj SMALLINT,
  dk SMALLINT,
  don SMALLINT,
  dr SMALLINT,
  ds SMALLINT,
  dt SMALLINT,
  p VARCHAR (7),
  s SMALLINT,
  i CHAR (10),
  ikk SMALLINT,
  q CHAR (6),
  mn CHAR (7),
  mp CHAR (7),
  e SMALLINT,
  k SMALLINT,
  l SMALLINT,
  o CHAR (5),
  
  PRIMARY KEY ( kanji )
);

CREATE TABLE kanjidic.kun (
  kanji CHAR (1),
  reading VARCHAR (12),
  PRIMARY KEY ( kanji, reading )
);

CREATE TABLE kanjidic.ohn (
  kanji CHAR (1),
  reading VARCHAR (12),
  PRIMARY KEY ( kanji, reading )
);

CREATE TABLE kanjidic.radical_name (
  kanji CHAR (1),
  reading VARCHAR (8),
  PRIMARY KEY ( kanji, reading )
);

CREATE TABLE kanjidic.radical (
  b SMALLINT,
  kanji CHAR ( 1 ),
  PRIMARY KEY ( b )
);

CREATE TABLE kanjidic.name_reading (
  kanji CHAR (1),
  reading VARCHAR (4),
  PRIMARY KEY ( kanji, reading )
);

CREATE TABLE kanjidic.korean (
  kanji CHAR (1),
  reading VARCHAR (6),
  PRIMARY KEY ( kanji, reading )
);

CREATE TABLE kanjidic.pinyin (
  kanji CHAR (1),
  reading VARCHAR (8),
  PRIMARY KEY ( kanji, reading )
);

CREATE TABLE kanjidic.mis_classified (
  kanji CHAR (1),
  p VARCHAR (9),
  PRIMARY KEY ( kanji, p )
);

CREATE TABLE kanjidic.cross_ref (
  kanji CHAR (1),
  code VARCHAR (6),
  PRIMARY KEY ( kanji, code )
);

CREATE TABLE kanjidic.kanji_meaning (
  kanji CHAR (1),
  meaning VARCHAR (75),
  PRIMARY KEY ( kanji, meaning )
);

CREATE TABLE kanjidic.eidx (
  keyword VARCHAR (24),
  kanji_list VARCHAR (256),
  PRIMARY KEY ( keyword )
);

CREATE TABLE edict.entry (
  line_no INT,
  phrase VARCHAR (130) NOT NULL,
  reading VARCHAR (130),
  gloss VARCHAR (1350),
  PRIMARY KEY ( line_no )
);

CREATE TABLE edict.pidx (
  phrase VARCHAR (48),
  line_no INT,
  PRIMARY KEY ( phrase, line_no )
);

CREATE TABLE edict.ridx (
  reading VARCHAR (48),
  line_no INT,
  PRIMARY KEY ( reading, line_no )
);

CREATE TABLE edict.eidx (
  keyword VARCHAR (48),
  line_nos VARCHAR (1024) FOR BIT DATA,
  PRIMARY KEY ( keyword )
);
