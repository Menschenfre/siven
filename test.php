<?php  
require_once('Controller/ProductController.php');

$bill=new ProductController();
$Muestro= $bill->Test();
$Muestro2= $bill->Test2();

echo "hello, muestro";
echo $Muestro;
echo $Muestro2;


?>
