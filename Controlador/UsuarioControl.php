<?php

require_once('../Modelo/Usuario.php');
$nombre = 'caca';
$usuario=new Usuario($nombre);
$reg=$usuario->guardar($nombre);
if ($reg) {
	echo "bien";
}else{
    echo "fallo"; 
}    

?>