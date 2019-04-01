<?php
$file_type = "msword";
$file_ending = "rtf";
header("Content-Type: application/$file_type"); 
header("Content-Disposition: attachment; filename=documento_.$file_ending"); 
header("Pragma: no-cache"); 
header("Expires: 0");
//header("Content-type: application/ms-word");
//header("Content-Disposition: attachment; filename=Reporte_productos.rtf");
require_once('cliente.php');

?>
<!DOCTYPE html>
<html lang="es">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta charset="utf-8">
<title>lista de clientes</title>
</head>
<body>
<table width="100%" border="1" cellspacing="0" cellpadding="0">
  <tr>
    <td colspan="5" bgcolor="skyblue"><CENTER><strong>Reporte de los usuario</strong></CENTER></td>
  </tr>
  <tr bgcolor="red">
    <td align="center"><strong>CODIGO</strong></td>
    <td align="center"><strong>NOMBRE</strong></td>
    <td align="center"><strong>SEXO</strong></td>
    <td align="center"><strong>EDAD</strong></td>
    <td align="center"><strong>DEUDA</strong></td>
  </tr>
  
<?php
	
$respuesta=new cliente();
$reg=$respuesta->listar();  

foreach($reg as $row){
?>
  <tr>
    <td align="center"><?php echo $row['codigo']; ?></td>
    <td align="center"><?php echo $row['nombre']; ?></td>
    <td align="center"><?php echo $row['sexoo']; ?></td>
    <td align="center"><?php echo $row['edad']; ?></td>
    <td align="center">$/<?php echo $row['deuda']; ?></td>
    </tr>
  <?php
}
?>  
</table>
</body>
</html>