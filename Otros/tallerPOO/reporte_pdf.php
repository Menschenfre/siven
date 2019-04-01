<?php
	require_once("dompdf/dompdf_config.inc.php");
	require_once("cliente.php");

$codigoHTML='
<!DOCTYPE html >
<html lang="es">
<head>               
<meta charset="utf-8">
<title>REPORTE</title>
</head>
<body>
<table width="100%" border="1" cellspacing="0" cellpadding="2">
  <tr>
    <td colspan="5" bgcolor="skyblue"><CENTER><strong>Reporte de los usuario</strong></CENTER></td>
  </tr>
  <tr bgcolor="red">
    <td align="center"><strong>CODIGO</strong></td>
    <td align="center"><strong>NOMBRE</strong></td>
    <td align="center"><strong>SEXO</strong></td>
    <td align="center"><strong>EDAD</strong></td>
    <td align="center"><strong>DEUDA</strong></td>
  </tr>';
$respuesta=new cliente();
$reg=$respuesta->listar();  

foreach($reg as $row){
$codigoHTML.='	
	<tr>
		<td align="center">'.$row['codigo'].'</td>
		<td align="center">'.$row['nombre'].'</td>
    <td align="center">'.$row['sexoo'].'</td>
    <td align="center">'.$row['edad'].'</td>
    <td align="center">$/'.$row['deuda'].'</td>
    </tr>';
	
}
$codigoHTML.='
</table>
</body>
</html>';
$codigoHTML=utf8_decode(utf8_encode($codigoHTML));
$dompdf=new DOMPDF();
$dompdf->load_html($codigoHTML);
ini_set("memory_limit","128M");
$dompdf->render();
$dompdf->stream("Reporte_producto.pdf");
?>