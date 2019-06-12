<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_product); ?>
<?php $prod_control=new ProductController();
//Invocamos la funcion que lista las categorías
$result = $prod_control->list_product();
$select_totals = $prod_control->total_product();

$test = $prod_control->testo();
var_dump($test);
?>

<script type="text/javascript">
  $(document).ready( function () {
    $('#table_id').DataTable();
} );
</script>
<table id="table_id" class="display">
    <thead>
        <tr>
            <th>ID #</th>
            <th>Producto</th>
            <th>Cantidad</th>
            <th>Valor</th>
            <th>Fecha gasto</th>
        </tr>
    </thead>
    <tbody>
        <?php foreach ($result as $key ) {?>
        <tr>
            <td><?php echo $key["id"]?></td>
            <td><?php echo $key["name"]?></td>
            <td><?php echo $key["total"]?></td>
            <td><?php echo $key["price"]?></td>
            <td><?php echo $key["created"]?></td>
        </tr>
        <?php }  ?>
    </tbody>
</table>

<label>Total General : <?php echo $select_totals["total"]?></label><br>
<label>Total Comida : <?php echo $select_totals["total_food"]?></label><br>
<label>Total Virtual : <?php echo $select_totals["total_virtual"]?></label><br>
<label>Total Tecnología : <?php echo $select_totals["total_technology"]?></label><br>
<label>Total Salud : <?php echo $select_totals["total_health"]?></label><br>
<label>Total Fijos : <?php echo $select_totals["total_immovable"]?></label><br>
<label>Total Otros : <?php echo $select_totals["total_others"]?></label><br>

<?php foreach ($test as $key) {?>
<label><?php echo $key[0] ?> : <?php  echo $key[0]?></label><br>
<?php }  ?>
