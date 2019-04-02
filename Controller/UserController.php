<?php
 
require_once('../Model/User.php');
$nombre = 'caca';
$usuario=new User($nombre);
$reg=$usuario->guardar($nombre);
if ($reg) {
	echo "bien";
}else{
    echo "fallo"; 
}    

?>