<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_product); ?>
<?php $prod_control=new ProductController();
//Invocamos la funcion que lista las categorías
$list_product = $prod_control->list_product();
$total_product = $prod_control->total_product();

$total_category_product = $prod_control->total_category_product();
//var_dump($test);

//Invocamos la función que lista el total por mes
$total_month = $prod_control->total_month();

 
//var_dump($list_product);
?>

<?php 
//Codificamos el array obtenido para que sea consumido por la tabla fija de datatable en jquery.
$list_list= json_encode($list_product);
?>
 
<!-- Search form -->

<div class="card">
<div class="card-body">
    
<select id="year_select" class="selectpicker" title="Filtrar por año" multiple>
  <option value="2019">2019</option>
  <option value="2020">2020</option>
  <option value="2021">2021</option>  
</select>
<select id="month_select" class="selectpicker" title="Filtrar por mes" multiple>
  <option value="1">Enero</option>
  <option value="2">Febrero</option>
  <option value="3">Marzo</option> 
  <option value="4">Abril</option>
  <option value="5">Mayo</option>
  <option value="6">Junio</option>
  <option value="7">Julio</option>
  <option value="8">Agosto</option>
  <option value="9">Septiembre</option>
  <option value="10">Octubre</option>
  <option value="11">Noviembre</option>
  <option value="12">Diciembre</option> 
</select>
<select id="category_select" class="selectpicker" title="Filtrar por categoría" multiple>
  <option>Enero</option>
  <option>Febrero</option>
  <option>Marzo</option> 
  <option>Abril</option> 
</select>

</br></br>

<table id="table_product" class="display" style="width:100%">
</table>
    

</div>
</div>

<label>General : <?php echo number_format($total_product[0],'0', ',','.')?></label><br>


<?php foreach ($total_category_product as $key) {?>
<label><?php echo $key[0] ?> : <?php  echo number_format($key[1],'0', ',','.')?></label><br>
<?php }  ?>

<label>Total por mes : <?php echo number_format($total_month[5],'0', ',','.')?></label><br> 
<?php 
echo var_dump($total_month); ?>




<script>
  $("#year_select").change(function(){
    //Capturamos las id de los input
    var year = $("#year_select").val();
    //alert(year);
    var identifier = "list_product";

    $.ajax({
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"identifier":identifier}, 
          url:   '/View/Administrative/Bills/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          datatype: 'json',
          beforeSend: function () { 
             // alert("Enviando data...");
          },

          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve
        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response==1){
                //  alert("Llega la data");
                  //window.location = "/admin";

              }else{

              var aca2 = JSON.parse(response);
              $('#table_product').dataTable( {
                "destroy": true,
                "language": {
                "search": "Buscar:",
                "info": "Mostrando registro _START_ al _END_ de _TOTAL_ resultados",
                "lengthMenu": "Mostrando _MENU_ resultados",
                "paginate": {
                    "first":      "Primero",
                    "last":       "Último",
                    "next":       "Siguiente",
                    "previous":   "Anterior"
                    },
                },
                 data: aca2,
                  columns: [
                      { title: "ID" },
                      { title: "Nombre" },
                      { title: "Precio" },
                      { title: "Fecha" }
                  ]
              } );
            }     
          }
    });    
  });
</script>
<!-- Cambio de mes por cbo -->
<script>
  $("#month_select").change(function(){
    //Capturamos las id de los input
    var month = $("#month_select").val();
    var year = $("#year_select").val();
    //alert(year);
    var identifier = "month_product";

    //Metemos los valores obtenidos a un array asociativo
    var product_filters = {"month":month, "year":year};

    $.ajax({
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"product_filters":product_filters,"identifier":identifier}, 
          url:   '/View/Administrative/Bills/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          beforeSend: function () { 
             // alert("Enviando data...");
          },

          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve
        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response==1){
                //  alert("Llega la data");
                  //window.location = "/admin";

              }else{

              var product_filter_month = JSON.parse(response);
              $('#table_product').dataTable( {
                "destroy": true,
                "language": {
                "search": "Buscar:",
                "info": "Mostrando registro _START_ al _END_ de _TOTAL_ resultados",
                "lengthMenu": "Mostrando _MENU_ resultados",
                "paginate": {
                    "first":      "Primero",
                    "last":       "Último",
                    "next":       "Siguiente",
                    "previous":   "Anterior"
                    },
                },
                 data: product_filter_month,
                  columns: [
                      { title: "ID" },
                      { title: "Nombre" },
                      { title: "Precio" },
                      { title: "Fecha" }
                  ]
              } );
            }     
          }
    });    
  });
</script>


<!-- Script de la tabla estática inicial con todos los registros -->
<script>
$(document).ready(function(){
  var aca2= <?php echo $list_list; ?>

  $('#table_product').dataTable( {
    "destroy": true,
    "language": {
    "search": "Buscar:",
    "info": "Mostrando registro _START_ al _END_ de _TOTAL_ resultados",
    "lengthMenu": "Mostrando _MENU_ resultados",
    "paginate": {
        "first":      "Primero",
        "last":       "Último",
        "next":       "Siguiente",
        "previous":   "Anterior"
    },
  },
     data: aca2,
      columns: [
          { title: "ID" },
          { title: "Nombre" },
          { title: "Precio" },
          { title: "Fecha" }
      ]
  } );
  
});
</script>


