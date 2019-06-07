<?php //Obtenemos las url estáticas
include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de producto
require_once($controller_product); ?>
<?php $prod_control=new ProductController();
//Invocamos la funcion que lista las categorías
$result = $prod_control->list_product();
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