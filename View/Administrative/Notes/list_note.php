<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_note); ?>
<?php $note_control=new NoteController(); 
//Invocamos la funcion que lista las notas
$list_note = $note_control->list();
$call_row = $list_note[16];
$call_detail_of_row = (string)$call_row["content"];
$call_detail_of_row2 = $call_detail_of_row;



//$explode = explode(' ',$call_detail_of_row);
$explodeOnly1= $explode;

$json = '{"a":1,"b":2,"c":3,"d":4,"e":5}';
$json2= '{"ops":[{"attributes":{"size":"huge"},"insert":"Note"},{"insert":" "}]}';
$deco = json_decode($json2, true);
$deco2 = json_decode($call_detail_of_row2, true);
$enco = json_encode($deco["ops"]);

//var_dump(json_decode($json, true));
//var_dump(json_decode($json2, true));
//var_dump(json_decode($explode[0], true));
//var_dump($enco);
var_dump($json2); 
var_dump($call_detail_of_row);

var_dump($deco);
var_dump($deco2);

var_dump($call_detail_of_row2);

//$decode = json_decode('ops',$call_detail_of_row, true);

//$cal = 'decoding'; 

//var_dump($list_note);
//var_dump($json);
//var_dump($explode[0]);
//var_dump($call_detail_of_row);
//var_dump(json_decode($call_detail_of_row["ops"], true));

?>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.7.1/katex.min.css" />

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/styles/monokai-sublime.min.css" />

<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de test</h4>
      <p class="card-description">
        Acá se registran notas ideas etc.
      </p>
       
      <div class="form-group">
        <label>Título</label>
        <input type="text" id="title" class="form-control form-control-lg" placeholder="Título" aria-label="Nombre"> 
      </div>
<style> 
  body > #standalone-container {
    margin: 50px auto;
    max-width: 720px;
  }
  #editor-container {
    height: 350px;
  }
</style>
<div class="form-group">
        <label>Contenido:</label>
</div>
  <div id="standalone-container">
    <div id="toolbar-container">
      <span class="ql-formats">
        <select class="ql-font"></select>
        <select class="ql-size"></select>
      </span>
      <span class="ql-formats">
        <button class="ql-bold"></button>
        <button class="ql-italic"></button>
        <button class="ql-underline"></button>
        <button class="ql-strike"></button>
      </span>
      <span class="ql-formats">
        <select class="ql-color"></select>
        <select class="ql-background"></select>
      </span>
      <span class="ql-formats">
        <button class="ql-script" value="sub"></button>
        <button class="ql-script" value="super"></button>
      </span>
      <span class="ql-formats">
        <button class="ql-header" value="1"></button>
        <button class="ql-header" value="2"></button>
        <button class="ql-blockquote"></button>
        <button class="ql-code-block"></button>
      </span>
      <span class="ql-formats">
        <button class="ql-list" value="ordered"></button>
        <button class="ql-list" value="bullet"></button>
        <button class="ql-indent" value="-1"></button>
        <button class="ql-indent" value="+1"></button>
      </span>
      <span class="ql-formats">
        <button class="ql-direction" value="rtl"></button>
        <select class="ql-align"></select>
      </span>
      <span class="ql-formats">
        <button class="ql-link"></button>
        <button class="ql-image"></button>
        <button class="ql-video"></button>
        <button class="ql-formula"></button>
      </span>
      <span class="ql-formats">
        <button class="ql-clean"></button>
      </span>
    </div>
    <div id="editor-container"></div>

</div>

      
      <button class="btn btn-outline-primary" onclick="note_reg('add_note')">Enviar</button>
    </div>
  </div>
</div> 




<script>
  var quill = new Quill('#editor-container', {
    modules: {
      formula: true,
      syntax: true,
      toolbar: '#toolbar-container'
    },
    placeholder: 'Escribir',
    theme: 'snow'
  });

  //Seteando valores por defecto
  quill.setContents(<?php echo $enco ?>);

  //alert(<?php echo $json2 ?>);
  //alert(<?php echo $call_detail_of_row2 ?>);
  alert(JSON.stringify(<?php echo $call_detail_of_row2 ?>));
  alert(JSON.stringify(<?php echo $json2 ?>));
  

  
</script>



