<?php
//Incluimos la master de URLs estáticas
include '/home2/sivenati/public_html/View/Includes/url.php';
//Recibimos el valor del identificador
$identifier = $_POST['identifier'];
switch ($identifier){ 
	case "add_product":
	//Requerimos el controlador de producto
	require_once($controller_product);
	//Inicializamos el controlador de producto
	$product_control=new ProductController();
	//Se recibe el array con la data de la vista
	$product = $_POST['product'];
	/*
	$product_name = "nameTest1";
	$product_category = 1;
	$product_total = 10;
	$product_price = 100;*/
	$result = $product_control->save($product);
	echo $result;
	//$result = $prod_control->Works();
	//echo $result;
	//var_dump($result);
	break;
	case "list_product":
	//Requerimos el controlador de producto
	require_once($controller_product);
	//Inicializamos el controlador de producto
	$product_control=new ProductController();
	$list_product = $product_control->list_product();
	$data=array("fdfd","System Architect","Edinburgh","5421","2011/04/25","$3,120");
	$cars = array("Volvo", "BMW", "Toyota");
	echo $UNA ="UNA";
	//echo json_encode($data); 
	//echo implode(",",$data);
	break;
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}