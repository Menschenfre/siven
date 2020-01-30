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

	//$test_list = $product_control->list_test();

	echo json_encode($list_product);

	//echo json_encode($list_product);


	//$data=array("wooooooorks","System Architect","Edinburgh","5421","2011/04/26","$3,120");
	//$cars = array("Volvo", "BMW", "Toyota");
	//echo json_encode($data); 
	//echo implode(",",$data);
	break;

	case "month_select":
	//Requerimos el controlador de producto
	require_once($controller_product);
	//Obtenemos los filtros definidos en los select
	$product_filters = $_POST['product_filters'];
	//Inicializamos el controlador de producto
	$product_control=new ProductController();
	//Obtenemos la lista de productos desde el controlador
	$list_product = $product_control->list_product_custome($product_filters);
	//Obtenemos la suma del total de la lista
	$total_product = $product_control->total_product_custome($product_filters);
	//Obtenemos la suma del total de la lista por categoria
	$total_category = $product_control->total_category_custome($product_filters);
	//Seguna variable a entregar hacia el ajax
	$a = "text";
	//Codificamos la lista de productos a json
	$encode_list = json_encode($list_product);
	//Creamos un array para pasar al ajax más de una variable
	$array_result = array("listado"=>$encode_list, "texto"=>$total_product, "total_category"=>$total_category);
	//Enviamos al ajax el array transformado a json
	echo (json_encode($array_result));

	break;
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}