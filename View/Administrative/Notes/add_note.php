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
      
      <button class="btn btn-outline-primary" onclick="note_reg('add_note')">Enviar</button>
    </div>
  </div>
</div> 


<script type="text/javascript">
  
function note_reg(identifier){

  //Capturamos las id de los input
  var title = $("#title").val();
  var content = $("#content").val();

  var note = {"title":title, "content":content};

  alert(JSON.stringify(note, null, 4));

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