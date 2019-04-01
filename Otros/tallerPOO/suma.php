<?php
require_once('cliente.php');

$s=new cliente();
$resultado=$s->suma();
	echo "el resultado es: ".$resultado;
?>
<br><br>
<?php

$respuesta=$s->cantidad();
echo "la cantidad es:".$respuesta;




?> 