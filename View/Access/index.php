<?php include '/home2/sivenati/public_html/View/Includes/url.php'; 

//Recibimos el valor del identificador
$identifier = $_POST['identifier'];

/*Switch por cada llave identificadora*/
switch ($identifier){
	
	/*Respuesta Ajax al logear*/
	case "login":
	require_once($model_user);
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
	break;

	/*Respuesta Ajax al deslogear*/
	case "logout":
	require_once($controller_login);
	$login_control=new LoginController();
	$result=$login_control->logout();

	if($result==1){
	session_start();
	$_SESSION["user"] = NULL;
	session_destroy();
	}


	echo $result;

	break;

	/*Respuesta default*/
	default:
	echo "//El valor no llega";
}


?>