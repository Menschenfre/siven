<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de música</h4>
      <p class="card-description">
        Acá se registra la música.
      </p>
      
      <div class="form-group">
        <label>Categoría</label>
        <input type="text" id="category" class="form-control form-control-lg" placeholder="Categoría" aria-label="Nombre"> 
      </div>
      <div class="form-group">
        <label>Url</label>
        <input type="text" id="url" class="form-control form-control-lg" placeholder="Url" aria-label="Nombre"> 
      </div>

      
      <button class="btn btn-outline-primary" onclick="note_reg('add_music')">Enviar</button>
    </div>
  </div>
</div> 


<script type="text/javascript">
  
function note_reg(identifier){

  //Capturamos las id de los input
  var category = $("#category").val();
  var url = $("#url").val();



  var separador = "=";
  var arregloDeSubCadenas = url.split(separador);
  //obtenemos el string despues del = y le aplicamos trim para eliminar espacios vacios
  var url = arregloDeSubCadenas[1].trim();
  alert(url);


  //Metemos los valores obtenidos a un array
  var music = {"category":category, "url":url};

   
  $.ajax({ 
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"music":music, "identifier":identifier}, 
          url:   '/View/Administrative/Music/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          beforeSend: function () {
              alert("Enviando data...");
          },
          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve
        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response==1){
                  alert("Llega la data");
              }else{
                alert("No llega la data");
                alert(response);
              }
                  
          }
  });
}
</script>