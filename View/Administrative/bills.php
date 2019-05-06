<?php  
require_once('/home2/sivenati/public_html/Controller/BillController.php');

$bill=new BillController();


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
            <th>Categoría</th>
            <th>Producto</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>1</td>
            <td>Comida</td>
            <td>Pollo Asado</td>

        </tr>
        <tr>
            <td>2</td>
            <td>Virtual</td>
            <td>Internet</td>
        </tr>
    </tbody>
</table>
