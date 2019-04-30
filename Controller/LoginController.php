<?php
require_once('../Model/User.php');
$user=new User();
//$nick = "siven";
//$pass = "siveng";
$nick = $_POST['username'];
$pass = $_POST['password'];
$result=$user->validateUser($nick,$pass);

//Si se encuentra el usuario, se guardan las variables de sesión 
if($result==1){
	session_start();
	$_SESSION["user"] = $nick;
}

//Retornamos positivo o negativo desde la clase
echo $result;
//Validamos ambos caminos
/*if ($result==1) {
	echo $result;
	//echo "Se debe abrir la sesión y permitir el acceso";
}else{
	echo $result;
    echo "Se debe retornar al login mostrando el mensaje pertinente"; 
}  */

public function logout(){
	session_destroy();
	header('Location: /login');

}
?>