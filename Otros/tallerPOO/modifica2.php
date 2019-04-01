<?php

require_once('cliente.php');
$codigo=$_POST['codigo'];
$nombre=$_POST['nombre'];
$sexo=$_POST['sexo'];
$edad=$_POST['edad'];
$deuda=$_POST['deuda'];

$modifica=new cliente();
$resultado=$modifica->modificar($codigo,$nombre,$sexo,$edad,$deuda);

if($resultado){
 header('location:listar.php');
}else{
echo "no se modifico nada";
}

?>