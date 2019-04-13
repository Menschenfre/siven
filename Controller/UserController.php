<?php
 
require_once('../Model/User.php');
$nick="test1";
$pass="testpass";
$status="";
$created="";
$modified="";
$user=new User($nick,$pass,$status,$created,$modified);
$reg=$user->save();
if ($reg) {
	echo "bien";
}else{
    echo "fallo"; 
}     

?>