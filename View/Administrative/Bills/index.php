<?php
//Incluimos la master de URLs estáticas
include '/home2/sivenati/public_html/View/Includes/url.php';

//Requerimos el controlador de producto
require_once($controller_product);

//Inicializamos el controlador de producto
$product_control=new ProductController();

//Obtenemos la data desde el ajax mediante POST
$product_name = $_POST['product_name'];
$product_category = $_POST['product_category'];
$product_total = $_POST['product_total'];
$product_price = $_POST['product_price'];

/*
$product_name = "nameTest1";
$product_category = 1;
$product_total = 10;
$product_price = 100;*/

$result = $product_control->save($product_category,$product_name,$product_total,$product_price);
echo $result;


//$result = $prod_control->Works();
//echo $result;

//var_dump($result);
 
?>