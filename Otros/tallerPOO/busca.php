<?php
require_once('cliente.php');
?>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
	<title>consulta</title>
</head>
<body>
<h1 align="center">Consulta de Clientes</h1> 
<?php
$codigo=$_POST['codigo'];
$busca=new  cliente();
$resp=$busca->buscar($codigo);
if ($resp) {
	?>
<center>
      <?php foreach ($resp as $row): ?> 
         <table border="1" cellpadding="3" cellspacing="3"> 
            <tr>
            <td >Codigo:    
            <td ><?php echo $row['codigo'];?>    
            <tr>
            <td>Nombre:    
            <td ><?php echo $row['nombre'];?>
            <tr>
            <td>Sexo:    
            <td ><?php echo $row['sexoo'];?>
            <tr>
            <td>Edad:    
            <td ><?php echo $row['edad'];?>
            <tr>
            <td>Deuda:    
            <td ><?php echo $row['deuda'];?>
            <tr>
         </table>
     <?php endforeach ?>     
 </center>
	<?php
}else{
echo"<script> alert('el codigo: $codigo no exixste!! ')
window.location='buscar.php';
        </script>";
}

?>
</body>
</html>







        