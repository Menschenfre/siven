<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_note); ?>
<?php $note_control=new NoteController();
//Invocamos la funcion que lista las categorías
$result = $note_control->list_category();
?>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.7.1/katex.min.css" />

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/styles/monokai-sublime.min.css" />

<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de notas</h4>
      <p class="card-description">
        Acá se registran notas ideas etc.
      </p>
       
      <div class="form-group">
        <label>Título</label>
        <input type="text" id="title" class="form-control form-control-lg" placeholder="Título" aria-label="Nombre"> 
      </div>
      <div class="form-group">
        <label for="exampleFormControlSelect1">Categoría</label>
        <select class="form-control form-control-lg" id="note_category">
          <?php foreach ($result as $key) {?>
            <option value=<?php echo $key["id"]?>><?php echo $key["name"] ?></option>
          <?php }  ?>
          
        </select>
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


<script type="text/javascript">
  
function note_reg(identifier){

  //Capturamos las id de los input
  var title = $("#title").val();
  //todo el contenido del texto enriquecido
  var content = quill.getContents();

  //Solo texto
  //var text = quill.getText(0, 1000);
  
  //Transformamos el contenido en texto plano
  var content= JSON.stringify(content);

  alert(content);
  //exit();
  //Metemos los valores obtenidos a un array
  var note = {"title":title, "content":content};

  

  //alert(JSON.stringify(note, null, 4));
  //alert(JSON.stringify(content, null, 4));
  //alert(test);
  //exit(); 
   
  $.ajax({ 
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"note":note, "identifier":identifier}, 
          url:   '/View/Administrative/Notes/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          beforeSend: function () {
              alert("Enviando data...");
              
                  //$("#resultado").html("Procesando, espere por favor...");
          },
          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve
            alert(JSON.stringify(response));
              //exit();
        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response==1){
                  //alert(JSON.stringify(response));
                  alert("Llega la data");
                  //alert(response);
                  //window.location = "/admin";

              }else{
                alert("No llega la data");
                alert(response);
                //alert(JSON.stringify(response));
              }
                  
          }
  });
}
</script>


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

  /*Seteando valores por defecto
  quill.setContents([
  { insert: 'Hello ' },
  { insert: 'World!', attributes: { bold: true } },
  { insert: '\n' }
]);*/
</script>



