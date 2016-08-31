// Requires document

function IME(id, tabindex)  {
  var container = document.getElementById(id);
  var _self = this;
  this._tabindex = tabindex;
  this._listeners = [];
  this._inputBox = document.createElement("INPUT");
  this._inputBox.name = "ime_text";
  this._inputBox.type = "text";
  this._inputBox.size = "30";
  this._inputBox.maxlength = "40";
  this._inputBox.tabindex = tabindex;
  this._inputBox._self = this;
  // List of indicies.
  this._cache = null;  // Holds the result of recent Edict search search.
  container.appendChild(this._inputBox);
  this._inputBox.addEventListener("keyup",
    function (event)  {
      _self._onkeyup(event);
    }
  );
  this._inputBox.focus();
  // Setup the default input type: hiragana
  this.setInputTypeControl(
    {
      getInputType: function () { return IME.INPUT_TYPE.HIRAGANA; }
    }
  );
}

// control must implement getInputType() -> {0, 1, 2}
// This is optional. If not set, defaultInputType will be used.
IME.prototype.setInputTypeControl = function (control)  {
  if (!(control && control.getInputType instanceof Function))  {
    throw new Error("IME.setInputTypeControl(): control ("
              + control.constructor + ") does not implement getInputType()." )
  }
  this._inputTypeControl = control;
};

IME.INPUT_TYPE =  {
  KATAKANA: 0,
  HIRAGANA: 1,
  ROMAJI: 2,
  NONE: 3
};

IME.prototype._onkeyup = function (event)  {
  var str = event.target.value;
  var ch = event.keyCode;
  switch (ch)  {
  case 0x20 :  // Spacebar
    // Remove the trailing space character:
    event.target.value = str.substr(0, str.length - 1);
    break;
  case 0x0d :  // Enter/Return
    this._raiseEvent();
    this._inputBox.value = "";
    break;
  default :
    var inputType = this._inputTypeControl.getInputType();
    if (inputType == IME.INPUT_TYPE.ROMAJI)  {
      str = IME.romanToRomaji(str);
    }
    else if (inputType == IME.INPUT_TYPE.HIRAGANA ||
             inputType == IME.INPUT_TYPE.KATAKANA)  {
      str = IME.romanToKana(str, inputType);
    }
    event.target.value = str;
    break;
  }
};

IME.prototype.getValue = function ()  {
  return this._inputBox.value;
};

IME.prototype.focus = function ()  {
  this._inputBox.focus();
};

// There's only one event: 'Enter' keypress.
// The callback will recieve one argument of type string - the
// value of the input box.
IME.prototype.addEventListener = function (callback)  {
  this._listeners.push(callback);
};

IME.prototype.removeEventListener = function (callback)  {
  for (var i = 0; i < this._listeners.length; i++)  {
    if (this._listeners[i] = callback)  {
      break;
    }
  }
  if (i < this._listeners.length)  {
    this._listeners.splice(i, 1);
  }
};

IME.prototype.removeAllEventListeners = function ()  {
  this._listeners = [];
};

IME.prototype._raiseEvent = function ()  {
  var value = this._inputBox.value;
  for (var i in this._listeners)  {
    this._listeners[i](value);
  }
};


// Test if string is all kana.
// Readings will be all kana.
// Katakana entries MAY have no reading field. MAY not test true.
// Main entries can be a mix of kana, kanji and romaji.

IME._isKana = function (str)  {
  // 0x3041 (12353) small hiragana 'a'
  // 0x30fc (12540) kana prolonged sound mark
  var allKana = true;
  for (var i = 0; i < str.length; i++)  {
    var ch = str.charCodeAt(i);
    if (ch < 12353 || ch > 12540)  {
      allKana = false;
      break;
    }
  }
  return allKana;
}

/*****************************************************************/

IME.isKanaTest = function ()  {
  var report = "";
  var str = "";
  for (var i = 12353; i < 12541; i++)  {
    str += String.fromCharCode(i);
  }
  report += "isKanaTest (1): "
  if (IME._isKana(str))  {
    report += "OK.\n";
  }
  else  {
    report += "failed!\n"
  }
  var failStr = str + String.fromCharCode(12541);
  report += "isKanaTest (2): "
  if (IME._isKana(failStr))  {
    report += "failed!\n"
  }
  else  {
    report += "OK.\n";
  }
  failStr = String.fromCharCode(12352) + str;
  report += "isKanaTest (3): "
  if (IME._isKana(failStr))  {
    report += "failed!\n"
  }
  else  {
    report += "OK.\n";
  }
  failStr = str + String.fromCharCode(65296); // full-width 0
  report += "isKanaTest (4): "
  if (IME._isKana(failStr))  {
    report += "failed!\n"
  }
  else  {
    report += "OK.\n";
  }
  failStr = str + String.fromCharCode(19968); // ichi (one)
  report += "isKanaTest (5): "
  if (IME._isKana(failStr))  {
    report += "failed!\n"
  }
  else  {
    report += "OK.\n";
  }

  return report;
}
/********************************************************************/

//-- Peter Jacob

//-- Set up the state transition table:
//-- There are 31 intermediate states - more input required to complete a syllable.
//-- 26 valid inputs [a - z] -> [0 - 25]. An input transitions to either another
//-- intermediate state, or a final state.
//-- State values are pretty much arbitrary, except that final states are
//-- relative to actual code points. Two-character syllables (-KYA-) do not
//-- correspond directly.


IME.state_table = new Array(31);

IME.state_table[0] = new Array(  // START
//    0,   1,   2,   3,    4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,    21,  22,  23,  24,  25
    140,  12,   7,   8,  146,  11,   2,  10, 142,   4,   1, 258,  14,   9, 148,  13, 258,  16,   3,   6, 144, 12532,  17, 258,  15,   5
);
IME.state_table[1] = new Array(  // K
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    149, 258, 258, 258, 155, 258, 258, 258, 151, 258, 173, 258, 258, 258, 157, 258, 258, 258, 258, 258, 153, 258, 258, 258,  18, 258
);
IME.state_table[2] = new Array(  // G
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    150, 258, 258, 258, 156, 258, 173, 258, 152, 258, 258, 258, 258, 258, 158, 258, 258, 258, 258, 258, 154, 258, 258, 258,  19, 258
);
IME.state_table[3] = new Array(  // S
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    159, 258, 258, 258, 165, 258, 258,  20, 161, 258, 258, 258, 258, 258, 167, 258, 258, 258, 173, 258, 163, 258, 258, 258, 258, 258
);
IME.state_table[4] = new Array(  // J
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    231, 258, 258, 258, 258, 258, 258, 258, 162, 173, 258, 258, 258, 258, 233, 258, 258, 258, 258, 258, 232, 258, 258, 258,  21, 258
);
IME.state_table[5] = new Array(  // Z
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    160, 258, 258, 258, 166, 258, 258, 258, 162, 258, 258, 258, 258, 258, 168, 258, 258, 258, 258, 258, 164, 258, 258, 258, 258, 173
);
IME.state_table[6] = new Array(  // T
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    169, 258, 258, 258, 176, 258, 258, 258, 171, 258, 258, 258, 258, 258, 178, 258, 258, 258, 23, 173, 174, 258, 258, 258, 258, 258
);
IME.state_table[7] = new Array(  // C
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    258, 258, 173, 258, 258, 258, 258,  22, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258
);
IME.state_table[8] = new Array(  // D
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    170, 258, 258, 173, 177, 258, 258, 258, 172, 258, 258, 258, 258, 258, 179, 258, 258, 258, 258, 258, 175, 258, 258, 258,  24, 258
);
IME.state_table[9] = new Array(  // N
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    180, 258, 258, 258, 183, 258, 258, 258, 181, 258, 258, 258, 258, 221, 184, 258, 258, 258, 258, 258, 182, 258, 258, 258,  25, 258
);
IME.state_table[10] = new Array(  // H
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    185, 258, 258, 258, 194, 258, 258, 173, 188, 258, 258, 258, 258, 258, 197, 258, 258, 258, 258, 258, 191, 258, 258, 258,  26, 258
);
IME.state_table[11] = new Array(  // F
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    258, 258, 258, 258, 258, 173, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 191, 258, 258, 258, 258, 258
);
IME.state_table[12] = new Array(  // B
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    186, 173, 258, 258, 195, 258, 258, 258, 189, 258, 258, 258, 258, 258, 198, 258, 258, 258, 258, 258, 192, 258, 258, 258,  27, 258
);
IME.state_table[13] = new Array(  // P
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    187, 258, 258, 258, 196, 258, 258, 258, 190, 258, 258, 258, 258, 258, 199, 173, 258, 258, 258, 258, 193, 258, 258, 258,  28, 258
);
IME.state_table[14] = new Array(  // M
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    200, 258, 258, 258, 203, 258, 258, 258, 201, 258, 258, 258, 173, 258, 204, 258, 258, 258, 258, 258, 202, 258, 258, 258,  29, 258
);
IME.state_table[15] = new Array(  // Y
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    206, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 210, 258, 258, 258, 258, 258, 208, 258, 258, 258, 173, 258
);
IME.state_table[16] = new Array(  // R
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    211, 258, 258, 258, 214, 258, 258, 258, 212, 258, 258, 258, 258, 258, 215, 258, 258, 173, 258, 258, 213, 258, 258, 258,  30, 258
);
IME.state_table[17] = new Array(  // W
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    217, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 220, 258, 258, 258, 258, 258, 258, 258, 173, 258, 258, 258
);
IME.state_table[18] = new Array(  // KY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    222, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 224, 258, 258, 258, 258, 258, 223, 258, 258, 258, 258, 258
);
IME.state_table[19] = new Array(  // GY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    225, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 227, 258, 258, 258, 258, 258, 226, 258, 258, 258, 258, 258
);
IME.state_table[20] = new Array(  // SH
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    228, 258, 258, 258, 258, 258, 258, 258, 161, 258, 258, 258, 258, 258, 230, 258, 258, 258, 258, 258, 229, 258, 258, 258, 258, 258
);
IME.state_table[21] = new Array(  // JY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    231, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 233, 258, 258, 258, 258, 258, 232, 258, 258, 258, 258, 258
);
IME.state_table[22] = new Array(  // CH
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    234, 258, 258, 258, 258, 258, 258, 258, 171, 258, 258, 258, 258, 258, 236, 258, 258, 258, 258, 258, 235, 258, 258, 258, 258, 258
);
IME.state_table[23] = new Array(  // TS
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 233, 258, 258, 258, 258, 258, 174, 258, 258, 258, 258, 258
);
IME.state_table[24] = new Array(  // DY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    237, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 239, 258, 258, 258, 258, 258, 238, 258, 258, 258, 258, 258
);
IME.state_table[25] = new Array(  // NY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    240, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 242, 258, 258, 258, 258, 258, 241, 258, 258, 258, 258, 258
);
IME.state_table[26] = new Array(  // HY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    243, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 245, 258, 258, 258, 258, 258, 244, 258, 258, 258, 258, 258
);
IME.state_table[27] = new Array(  // BY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    246, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 248, 258, 258, 258, 258, 258, 247, 258, 258, 258, 258, 258
);
IME.state_table[28] = new Array(  // PY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    249, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 251, 258, 258, 258, 258, 258, 250, 258, 258, 258, 258, 258
);
IME.state_table[29] = new Array(  // MY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    252, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 254, 258, 258, 258, 258, 258, 253, 258, 258, 258, 258, 258
);
IME.state_table[30] = new Array(  // RY
//    0,   1,   2,   3,   4,   5,   6,   7,   8,   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,  22,  23,  24,  25
    255, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 257, 258, 258, 258, 258, 258, 256, 258, 258, 258, 258, 258
);

// ,,,,,,,,,,,,,,,,,,,,,,,,,

IME.conversion_table = new Array(
    // KI,  GI, SHI,  JI, CHI,  DI,  NI,  HI,  BI,  PI,  MI,  RI
      151, 152, 161, 162, 171, 172, 181, 188, 189, 190, 201, 212
);

/*--------------------------------------------------------------------*/

IME.isSpecialChar = function (ch)  {
  var unicode;
  switch (ch)  {
  case 0x2d :          // '-'
    unicode = 0x30fc;
    break;
  case 0x2c :          // ','
    unicode = 0x3001;
    break;
  case 0x2e :          // '.'
    unicode = 0x3002;
    break;
  case 0x5b :          // '['
    unicode = 0x300c;
    break;
  case 0x5d :           // ']'
    unicode = 0x300d;
    break;
  case 0x20 :           // ' '
    unicode = 0x3000;
    break;
  //case 0x2a :           // '*'
  //  unicode = 0x30fb;   // Middle Dot
  //  break;
  case 0x30 :           // '0' ... '9'
  case 0x31 :
  case 0x32 :
  case 0x33 :
  case 0x34 :
  case 0x35 :
  case 0x36 :
  case 0x37 :
  case 0x38 :
  case 0x39 :  // 0xFF10 Full-Width Zero
    unicode = 0xff10 + (ch - 0x30);
    break;
  default :
    unicode = 0;
    break;
  }
  return unicode;
};

/*--------------------------------------------------------------------*/

/*

  _____ IME.romanToKana() __________________________________________________
 |
 | str         : String
 | inputType   : IME.INPUT_TYPE
 |
 | Return      : String
 |
 | Takes an ascii string and transforms it to hiragana or katakana.
 |
 | When inputType = IME.INPUT_TYPE.HIRAGANA, returns hiragana;
 | IME.INPUT_TYPE.KATAKANA, katakana.
 | Non-alphabetic characters are mostly ignored. A few special
 | characters like '.' are transformed to Japanese equivalents.
 | Unicode characters can be inserted into the return string using
 | the escape sequence \uXXXX (XXXX being the Unicode hex value of
 | the character.)
 |_____________________________________________________________________

 */
IME.romanToKana = function (str, inputType)  {

  var buffer = new String("");  // Return string.
  var ch;                     // Current char.
//  var chBuff = new Array(3);  // Save chars while looking ahead.
  var state = 0;
  var i = 0;                  // str index.
  var j = 0;                  // chBuff index.
  //var komoji = false;         // flag. character is 'lower case'
  var unicodeA;

  /* TO DO: kana WI WE */

  if (inputType == IME.INPUT_TYPE.HIRAGANA)  {
    unicodeA = 12354;  // 0x3042
  }
  else  {  // katakana -A-
    unicodeA = 12450;  // 0x30a2
  }

  while (i < str.length) {
    j = i; // Save this index.
           // On error, ignore this char and try parsing from next index.
    while (state < 140 && i < str.length)  {  // 140 == kana:A
      ch = str.charAt(i).toLowerCase();
      if ((ch.charCodeAt(0) - 97) < 0 || (ch.charCodeAt(0) - 97) > 25)  {
        state = 258;
      }
      else  {
        state = IME.state_table[state][ch.charCodeAt(0) - 97];
      }
      i++;
    }
    if (state == 258)  { // Error/Ignore/Escape/Special state
      // Either an invalid character sequence, already transliterated, special char or escape char.
      // Try parsing from the next position in str.
      // Just copy one char directly to the result.

      ch = IME.isSpecialChar(str.charCodeAt(j));
      if (ch > 0)  {
        buffer += String.fromCharCode(ch);
      }
      else if (str.charCodeAt(j) == 0x5c && j < str.length - 1)  { // '\' escape
        //i = j + 1;
        ch = "\\";
        ++j;
        // TO DO: \u30FB KATAKANA MIDDLE DOT
        switch (str.charCodeAt(j))  {
        case 0x6b :  // 'k'
          //ch = "";
          //++j;
          ch += str.charAt(j);  // ch == "\k"
          ++j;
          state = 0;
          while (state < 140 && j < str.length)  {
            state = IME.state_table[state][str.charCodeAt(j) - 97];
            ch += str.charAt(j);
            ++j;
          }
          switch (state)  {  // Only these states have 'small' forms.
          case 140 :
          case 142 :
          case 144 :
          case 146 :
          case 148 :
          case 206 :
          case 208 :
          case 210 :
          case 217 :
            ch = String.fromCharCode((state - 1 - 140) +  unicodeA);
            break;
          case 149 :  // ka
            ch = String.fromCharCode(83 + unicodeA);
            break;
          case 155 :  // ke
            ch = String.fromCharCode(84 + unicodeA);
            break;
          }
          break;
        case 0x75 :  // 'u'
          ch += str.charAt(j);  // ch == "\u"
          ++j;
          var k = 0;
          var decimal = 0;

          while (j < str.length && k < 4)  {
            if (((str.charCodeAt(j) >= 0x30) && (str.charCodeAt(j) <= 0x39)))  {
              // digit
              decimal = decimal * 16 + (str.charCodeAt(j) - 0x30);
            }
            else if (((str.charCodeAt(j) >= 0x61) && (str.charCodeAt(j) <= 0x66)))  {
              // lower case
              decimal = decimal * 16 + (str.charCodeAt(j) - 0x57);  // '0x0a' == 10dec
            }
            else if (((str.charCodeAt(j) >= 0x41) && (str.charCodeAt(j) <= 0x46)))  {
              // upper case
              decimal = decimal * 16 + (str.charCodeAt(j) - 0x37);
            }
            else  {
              // malformed hex value. abandon
              break;
            }
            ++k;
            ch += str.charAt(j);
            ++j;
          }
          if (k == 4)  {
            ch = String.fromCharCode(decimal);
          }
          break;
        case 0x2e  :  // '.'
          j++;
          ch = String.fromCharCode(0x30fb);  // KATAKANA MIDDLE DOT
          break;
        default :
          break;
        }
        // j is either beyond end of string or points to next char.
        // Because i is set to j + 1 below, reset j.
        --j;
        buffer += ch;
      }
      else  {
        buffer += str.charAt(j);
      }
      i = j + 1;
    }
    else if (state < 140)  {     // Intermediate state.
      while (j < i)  {           // Only get here if the string ends before completing
        buffer += str.charAt(j); // a syllable.
        j++;
      }
    }
    else  {  // Final state.
      // State -A- == 140
      if (state < 222)  {
        buffer += String.fromCharCode((state - 140) +  unicodeA);
      }
      else if (state < 258)  {
        // States > 221 -KYA- require two character sequence.
        // -KYA- becomes -KI- + -YA-
        j = IME.conversion_table[Math.floor((state - 222) / 3)];
        buffer += String.fromCharCode((j - 140) +  unicodeA);
        j = (state - 222) % 3;
        switch(j)  { // -YA- -YU- -YO-
        case 0 :
          state = 205;
          break;
        case 1 :
          state = 207;
          break;
        case 2 :
          state = 209;
          break;
        }
        buffer += String.fromCharCode((state - 140) +  unicodeA);
      }
      else  {  // Special Cases: state > 258  -V-  Directly encoded as 12532 (0x30f4).
        buffer += String.fromCharCode(state);
      }
    }
    state = 0;
  }
  return buffer;
};

IME.romanToRomaji = function (str)  {
  // ! 0x21 (33)  -> 0xff01 (65128)
  // ~ 0x7e (126) -> 0xff5e (65374)
  var buffer = new String();
  for (var i in str)  {
    var code = str[i].charCodeAt(0);
    if (code < 33 || code > 126)  { // ! ... ~
      // Just copy directly (ignore).
      buffer += String.fromCharCode(code);
    }
    else  {
      code -= 33;    // ! 0x21
      code += 65281; // ! 0xff01
      buffer += String.fromCharCode(code);
    }
  }
  return buffer;
};

/******************************************************************************/
