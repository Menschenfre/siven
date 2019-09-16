<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_product); ?>
<?php $prod_control=new ProductController();
//Invocamos la funcion que lista las categorías
//$list_product = $prod_control->list_product();
//$total_product = $prod_control->total_product();

//$total_category_product = $prod_control->total_category_product();
//var_dump($test);

//Invocamos la función que lista el total por mes
//$total_month = $prod_control->total_month();

 
//var_dump($list_product);
?>
 
<script type="text/javascript">
  $(document).ready( function () {
    $('#table_id').DataTable();
} );
</script>
<!-- Search form -->

<div class="card">
<div class="card-body">
    
<select id="year_select" class="selectpicker" title="Filtrar por año" multiple>
  <option value="2019">2019</option>
  <option value="2020">2020</option>
  <option value="2021">2021</option>  
</select>
<select id="month_select" class="selectpicker" title="Filtrar por mes" multiple>
  <option>Enero</option>
  <option>Febrero</option>
  <option>Marzo</option> 
  <option>Abril</option> 
</select>
<select id="category_select" class="selectpicker" title="Filtrar por categoría" multiple>
  <option>Enero</option>
  <option>Febrero</option>
  <option>Marzo</option> 
  <option>Abril</option> 
</select>

</br></br>
    
<table id="table_id" class="display">
    <thead>
        <tr>
            <th>ID #</th>
            <th>Producto</th>
            <!--<th>Cantidad</th>-->
            <th>Valor</th>
            <th>Fecha gasto</th>
        </tr> 
    </thead>
    <tbody>
        <?php foreach ($list_product as $key ) {?>
        <tr>
            <td><?php echo $key["id"]?></td>
            <td><?php echo $key["name"]?></td>
            <!--<td><?php echo $key["total"]?></td>-->
            <td><?php echo number_format($key["price"],'0', ',','.')?></td>
            <td><?php echo $key["created"]?></td>
        </tr>
        <?php }  ?>
    </tbody>
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


<table id="table_test" class="display" style="width:100%">
</table>


<script>

  $("#year_select").change(function(){
    //Capturamos las id de los input
    var year = $("#year_select").val();
    alert(year);
    var identifier = "list_product";

    $.ajax({
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"identifier":identifier}, 
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
                var data = [
    [
        "Tiger caca works",
        "System Architect",
        "Edinburgh",
        "5421", 
        "2011/04/25",
        "$3,120"
    ]
];
var works= response.split(',');
//console.log(JSON.stringify(works));
//alert(data);
alert(works);
alert(response);
var datal = [
    [
        works
    ]
];
console.log(response);

                alert("No llega la data, fail");
                //alert(response);
                $('#table_test').dataTable( {
        "destroy": true,
        data: datal,
        columns: [
            { title: "Namem" },
            { title: "Position" },
            { title: "Office" },
            { title: "Extn." },
            { title: "Start date" },
            { title: "Salary" }
        ]
} );

              }
                  
          }
  });

    

    
    //table.destroy();
    
    
    

    
  });

  $("#month_select").change(function(){
     var data = [
    [
        "Tiger caca works",
        "System Architect",
        "Edinburgh",
        "5421",
        "2011/04/25",
        "$3,120"
    ]
];
alert(data);


$('#table_test').dataTable( {
        "destroy": true,
        data: data,
        columns: [
            { title: "Namem" },
            { title: "Position" },
            { title: "Office" },
            { title: "Extn." },
            { title: "Start date" },
            { title: "Salary" }
        ]
} );
  });

</script>

