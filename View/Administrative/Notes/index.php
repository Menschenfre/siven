<?php include '/home2/sivenati/public_html/View/Includes/url.php';

//Recibimos el valor del identificador
$identifier = $_POST['identifier'];

/*Switch por cada llave identificadora*/
switch ($identifier){ 
	
	/*Respuesta Ajax al agregar una nota*/
	case "add_note":
	require_once($controller_note);
	$note_control=new NoteController();

	//Se recibe el array con la data de la vista
	$note = $_POST['note'];

	//$varee= implode($note);
	
	//Pasamos la variable al método save del controlador(master)
	$result=$note_control->save($note);


	echo $result;
	break;

	/*Respuesta Ajax al ver una nota*/
	case "view_note":
	require_once($controller_note);
	$note_control=new NoteController();

	//Se recibe el array con la data de la vista
	$id = $_POST['note_id'];

	//Invocamos la funcion que lista las notas por id
	$list_note_by_id = $note_control->list_by_id($id);

	foreach ($list_note_by_id as $key) {
		//Obtenemos la fila por la id recibida
		$call_row = $key["content"];
	}
	$decode_call_row = json_decode($call_row, true);
	//Codificamos el contenido del json para obtener el contenido de "ops"
	$encode_decode_call_row= json_encode($decode_call_row["ops"]);

	//Mansamos los valores de "ops" hacia el ajax
	echo $encode_decode_call_row; 
	break;
  
	
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}
