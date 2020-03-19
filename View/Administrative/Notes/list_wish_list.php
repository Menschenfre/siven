<?php
ob_start();
?>
<?php $page = "Admin";?><?php $MAINPATH= $_SERVER['DOCUMENT_ROOT'];
include '' . $MAINPATH . '/View/Includes/url.php'; ?>

<?php 
if($_SESSION["user"] == NULL || $_SESSION["user"]!= 'siven'){
  header('Location: /login');
  exit();
}
?>

<?php //Llamamos el controlador de producto
require_once($controller_note); ?>
<?php $note_control=new NoteController(); 
//Invocamos la funcion que lista las notas
$list_note = $note_control->list();
$call_row = $list_note[7];
 
//var_dump($call_row);

$attribute_call_row = $call_row["content"];
$decode_attribute_call_row = json_decode($attribute_call_row, true);

$encode_decode_atribbute_call_row= json_encode($decode_attribute_call_row["ops"]);



 
?>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.7.1/katex.min.css" />

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/styles/monokai-sublime.min.css" />

<div class="col-lg-6 grid-margin stretch-card">
    <div class="card">
        <div class="card-body">
            <h4 class="card-title">Wish:</h4>
            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Título</th>
                            <th>Creado</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($list_note as $key ) {?>
                        <tr onclick="view_note(<?php echo $key["id"]  ?>)">
                            <td><?php echo $key["id"]?></td>

                            <td><?php
                            if(strlen($key["title"])>37){
                               echo substr($key["title"],0, 33)."...";
                            }else{
                              echo $key["title"];
                            }?></td>

                            <td><?php echo $key["created"]?></td>
                        </tr>
                        <?php }  ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
       
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
  quill.setContents(<?php echo $encode_decode_atribbute_call_row ?>);



  function view_note(note_id){
  //alert(note_id);
  $.ajax({
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"identifier":'view_note', "note_id":note_id}, 
          url:   '/View/Administrative/Notes/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          datatype: 'json',
          beforeSend: function () { 
             //alert("Enviando data...");
          },

          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve
                console.log(response);
                //Transformamos la respuesta en un JSON
                var aca2 = JSON.parse(response);
                //Mostramos el contenido
                quill.setContents(aca2);     
          }
    });
  };

</script>

<?php
ob_end_flush();
?>

