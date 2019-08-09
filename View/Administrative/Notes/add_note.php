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
        <label>Contenido</label>
        <input type="text" id="content" class="form-control form-control-lg" placeholder="contenido" aria-label="Nombre">
      </div>

      <div>
      <textarea style="width: 345px; height: 100px;" id="myArea2"></textarea>
      <br />
      <button onClick="addArea2();">+</button> <button onClick="removeArea2();">-</button> 
      </div>
      <div style="clear: both;"></div>


      
      <button class="btn btn-outline-primary" onclick="note_reg('add_note')">Enviar</button>
    </div>
  </div>
</div> 


<script type="text/javascript">
  
function note_reg(identifier){

  //Capturamos las id de los input
  var title = $("#title").val();
  var content = $("#content").val();
  nicEditors.findEditor("#myArea2").saveContent();

  var myArea2 = $("#myArea2").val();

  var note = {"title":title, "content":content};

  alert(myArea2);

  alert(JSON.stringify(note, null, 4));
  exit();

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

        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response==1){
                  //alert(JSON.stringify(response));
                  alert("Llega la data");
                  alert(response);
                  //window.location = "/admin";

              }else{
                alert("No llega la data");
                alert(response);
                alert(JSON.stringify(response));
              }
                  
          }
  });
}
</script>

<script>
var area1, area2;

function toggleArea1() {
  if(!area1) {
    area1 = new nicEditor({fullPanel : true}).panelInstance('myArea1',{hasPanel : true});
  } else {
    area1.removeInstance('myArea1');
    area1 = null;
  }
}

function addArea2() {
  area2 = new nicEditor({fullPanel : true}).panelInstance('myArea2');
}
function removeArea2() {
  area2.removeInstance('myArea2');
}

bkLib.onDomLoaded(function() { toggleArea1(); });
</script> 