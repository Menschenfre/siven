
<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_product); ?>
<?php $prod_control=new ProductController();
//Invocamos la funcion que lista las categorías
$result = $prod_control->list_category();
?>

<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de productos</h4>
      <p class="card-description">
        Acá se registran los gastos por productos
      </p>
      
      <div class="form-group">
        <label>Nombre</label>
        <input type="text" id="product_name" class="form-control form-control-lg" placeholder="Nombre" aria-label="Nombre">
      </div>
      
      <div class="form-group">
        <label for="exampleFormControlSelect1">Categoría</label>
        <select class="form-control form-control-lg" id="product_category">
          <?php foreach ($result as $key) {?>
            <option value=<?php echo $key["id"]?>><?php echo $key["name"] ?></option>
          <?php }  ?>
          
        </select>
      </div>

      <div class="form-group">
        <label>Cantidad</label>
        <input type="text" id="product_total" class="form-control form-control-lg" placeholder="Cantidad" aria-label="Cantidad">
      </div>

      <div class="form-group">
        <label>Precio</label>
        <input type="text" id="product_price" class="form-control " placeholder="Precio" aria-label="Precio">
      </div>

      <div class="form-group">
        <label>Fecha</label>
        <input type="date" id="product_date" class="form-control " placeholder="Fecha" aria-label="Fecha">
      </div>

      <button class="btn btn-outline-primary" onclick="prod_reg()">Enviar</button>
    </div>
  </div>
</div>

<script type="text/javascript">
  
function prod_reg(){

  //Capturamos las id de los input
  var product_name = $("#product_name").val();
  var product_category = $("#product_category").val();
  var product_total = $("#product_total").val();
  var product_price = $("#product_price").val();
  var product_date = $("#product_date").val();

  $.ajax({
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"product_name":product_name,"product_category":product_category,"product_total":product_total,"product_price":product_price,"product_date":product_date}, 
          url:   '/View/Administrative/Bills/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          beforeSend: function () {
              alert("Enviando data...");
                  //$("#resultado").html("Procesando, espere por favor...");
          },
          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve

        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response==1){
                  alert("Llega la data");
                  //window.location = "/admin";

              }else{
                alert("No llega la data, fail");
              }
                  
          }
  });
}
</script>