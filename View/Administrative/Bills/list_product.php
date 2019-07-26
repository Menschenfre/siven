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
        <?php foreach ($list_product as $key ) {?>
        <tr>
            <td><?php echo $key["id"]?></td>
            <td><?php echo $key["name"]?></td>
            <td><?php echo $key["total"]?></td>
            <td><?php echo number_format($key["price"],'0', ',','.')?></td>
            <td><?php echo $key["created"]?></td>
        </tr>
        <?php }  ?>
    </tbody>
</table>

<label>General : <?php echo number_format($total_product[0],'0', ',','.')?></label><br>


<?php foreach ($total_category_product as $key) {?>
<label><?php echo $key[0] ?> : <?php  echo number_format($key[1],'0', ',','.')?></label><br>
<?php }  ?>