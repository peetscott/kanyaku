// Requires ime.js

window.addEventListener("load", init, false);


function init()  {
  var ime = new IME("ime", 1);

  ime.setInputTypeControl(new InputTypeControl());
  ime.focus();


  /*****************************************/
  function InputTypeControl()  {
    var selectElement = document.getElementById("select_input_type");
    this.getInputType = function ()  {
      switch (selectElement.value)  {
      case "hira" :
        return IME.INPUT_TYPE.HIRAGANA;
      case "kata" :
        return IME.INPUT_TYPE.KATAKANA;
      case "roma" :
        return IME.INPUT_TYPE.ROMAJI;
      default :
        return IME.INPUT_TYPE.NONE;
      }
    };
  }
}
