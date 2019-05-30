<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de productos</h4>
      <p class="card-description">
        Acá se registran los gastos por productos
      </p>
      
      <div class="form-group">
        <label>Nombre</label>
        <input type="text" id="prod_name" class="form-control form-control-lg" placeholder="Nombre" aria-label="Nombre">
      </div>
      <div class="form-group">
        <label for="exampleFormControlSelect1">Categoría</label>
        <select class="form-control form-control-lg" id="prod_category">
          <option>1</option>
          <option>2</option>
          <option>3</option>
          <option>4</option>
          <option>5</option>
        </select>
      </div>

      <div class="form-group">
        <label>Cantidad</label>
        <input type="text" id="prod_total" class="form-control form-control-lg" placeholder="Cantidad" aria-label="Cantidad">
      </div>

      <div class="form-group">
        <label>Precio</label>
        <input type="text" id="prod_price" class="form-control " placeholder="Precio" aria-label="Precio">
      </div>
      
    </div>
  </div>
</div>

<script type="text/javascript">
  
function login(){

  //Capturamos las id de los input
      var prod_name = $("#prod_name").val();
      var prod_category = $("#prod_category").val();
      var prod_total = $("#prod_total").val();
      var prod_price = $("#prod_price").val();


      $.ajax({
          //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
              data:  {"prod_name":prod_name,"prod_category":prod_category,"prod_total":prod_total,"prod_price":prod_price}, 
              url:   '/View/Bills/index.php', //archivo que recibe la peticion
              type:  'post', //método de envio
              beforeSend: function () {
                  alert("Logeando...");
                      //$("#resultado").html("Procesando, espere por favor...");
              },
              //response es lo primero que se retorna en el controller
              success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve

            //Si el controlador retorna un positivo se devuelve mensaje exitoso 
                  if(response==1){
                      alert("Sesión activa");
                      window.location = "/admin";

                  }else{
                    alert("No existe usuario, sesión fallida");
                  }
                      
              }
      });
}
</script>