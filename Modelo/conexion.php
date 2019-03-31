<?php 

//Declaramos las variables
$usuario = "sivenati_sickven";
$pass = "1342993nonoaccessbbbrbrbbb29";
$servidor = "localhost";
$basededatos = "sivenati_siven";

//Creamos la conexión a la base de datos
$conexion = mysqli_connect($servidor, $usuario, $pass) or die ( "Algo ha ido mal con la conexión a la base de datos");

//Seteamos a utf8 para carácteres especiales
mysqli_set_charset($conexion,"utf8");

//Seleccionamos la bd
$db = mysqli_select_db($conexion, $basededatos);

// Creamos la consulta
$query = "SELECT * FROM usuarios";

// Ejecutamos la consulta
$resultado = mysqli_query($conexion, $query) or die ( "Algo ha ido mal en la consulta a la base de datos");

// Cerramos la conexión posterior a la consulta
//mysqli_close($conexion);

?>