<?php
//Incluimos la master de URLs estáticas
include '/home2/sivenati/public_html/View/Includes/url.php';

//Requerimos el controlador de producto
require_once($controller_product);

//Inicializamos el controlador de producto
$prod_control=new ProductController();

//Obtenemos la data desde el ajax mediante POST
$product_name = $_POST['product_name'];
$product_category = $_POST['product_category'];
$product_total = $_POST['product_total'];
$product_price = $_POST['product_price'];

$result = $prod_control->save();
echo $result;
//$result = $prod_control->Works();
//echo $result;

//var_dump($result);
 
?>