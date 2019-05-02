<?php 


//$LoginFunction = include '{$Base_url}{$Function_origen}/LoginFunction.php';
$LoginFunction = include '/home2/sivenati/public_html/Function/LoginFunction.php';



switch ($page){
	case "Login":
	echo $LoginFunction;
	break;
	default:
	echo "//No se carga ninguna función";
}
?>