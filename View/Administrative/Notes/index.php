<?php include '/home2/sivenati/public_html/View/Includes/url.php';

//Recibimos el valor del identificador
$identifier = $_POST['identifier'];

/*Switch por cada llave identificadora*/
switch ($identifier){ 
	
	/*Respuesta Ajax al agregar una nota*/
	case "add_note":
	require_once($controller_note);
	$control_note=new NoteController();

	//Se recibe el array con la data de la vista
	$note = $_POST['note'];

	//$varee= implode($note);
	
	//Pasamos la variable al método save del controlador(master)
	$result=$control_note->save($note);


	echo $result;
	break;
 
	
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}

 
?>