<?php

require_once('cliente.php');

$nombre=$_POST['nombre'];
$edad=$_POST['edad'];
$sexo=$_POST['sexo'];
$deuda=$_POST['deuda'];

$cliente=new cliente();
$reg=$cliente->registro($nombre,$sexo,$edad,$deuda);
if ($reg) {
	header("location:listar.php");
}else{
    echo "fallo"; 
}    

?>
