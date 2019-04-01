<?php

require_once('cliente.php');
$codigo=$_POST['codigo'];

$eliminar=new cliente();
$reg=$eliminar->eliminar($codigo);

if($reg){
 header('location:listar.php');
}else{
echo "no se elimino nada";
}

?>