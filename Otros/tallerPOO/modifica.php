<?php
require_once('cliente.php');
?>

<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
	<title>modifica  clientes</title>
</head>
<body>
<h1 align="center">Consulta de Clientes que sera modificaso</h1> 
<?php
$codigo=$_POST['codigo'];
$busca=new  cliente();
$resp=$busca->buscar($codigo);
if ($resp) {
	?>
<center>
      <?php foreach ($resp as $row): ?> 
        <form action="modifica2.php" method="POST">
         <table border="1" cellpadding="3" cellspacing="3"> 
            <tr>
            <td>Codigo:    
            <td><input type="text" name="codigo" value="<?php echo $row['codigo'];?>" readonly>
            <tr>
            <td>Nombre:    
            <td><input type="text" name="nombre" value="<?php echo $row['nombre'];?>" >
            <tr>
            <td>Sexo:    
            <td><input type="text" name="sexo" value="<?php echo $row['sexoo'];?>" >                         
            <tr>
            <td>Edad:    
            <td><input type="text" name="edad" value="<?php echo $row['edad'];?>" >
            <tr>
            <td>Deuda:    
            <td><input type="text" name="deuda" value="<?php echo $row['deuda'];?>" >
            <tr>
         </table>
         <BR>
         <button type="submit">Modificar</button>
        <form> 
     <?php endforeach ?>     
 </center>
	<?php

}else{

echo"<script>
       alert('el codigo: $codigo no exixste!! ')
        window.location='modificar.php';
        </script>";
}

?>
</body>
</html>
