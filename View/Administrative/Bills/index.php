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
	
	$result = $product_control->save($product);
	echo $result;
	
	break;

	case "list_product":
	//Requerimos el controlador de producto
	require_once($controller_product);
	//Inicializamos el controlador de producto
	$product_control=new ProductController();
	$list_product = $product_control->list_product();

	$test_list = $product_control->list_test();

	echo json_encode($test_list);

	//echo json_encode($list_product);


	//$data=array("wooooooorks","System Architect","Edinburgh","5421","2011/04/26","$3,120");
	//$cars = array("Volvo", "BMW", "Toyota");
	//echo json_encode($data); 
	//echo implode(",",$data);
	break;
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}