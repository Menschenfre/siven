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
            <th>Producto</th>
            <th>Valor</th>
            <th>Fecha gasto</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>1</td>
            <td>Pollo con papas</td>
            <td>$4300</td>
            <td>11/04/2019</td>
        </tr>
        <tr>
            <td>2</td>
            <td>Internet</td>
            <td>$24000</td>
            <td>07/05/2019</td>
        </tr>
    </tbody>
</table>
