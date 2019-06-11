<?php  
require_once('Controller/ProductController.php');

$bill=new ProductController();
$Muestro= $bill->Test();

echo "hello, muestro";
echo $Muestro;


?>
