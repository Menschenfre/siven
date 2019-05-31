<?php
include '/home2/sivenati/public_html/View/Includes/url.php';

require_once($controller_product);

$prod_control=new ProductController();

$result = $prod_control->Works();
echo $result;

//var_dump($result);

?>