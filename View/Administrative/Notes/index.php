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

	//Invocamos la funcion que lista las notas
	$list_note = $note_control->list();

	//Obtenemos la fila por la id recibida
	$call_row = $list_note[$id];
	//Obtenemos el contenido de la fila
	$attribute_call_row = $call_row["content"];
	//Decodificamos el contenido obtenido ya que viene como json
	$decode_attribute_call_row = json_decode($attribute_call_row, true);
	//Codificamos el contenido del json para obtener el contenido de "ops"
	$encode_decode_atribbute_call_row= json_encode($decode_attribute_call_row["ops"]);

	//Mansamos los valores de "ops" hacia el ajax
	echo $encode_decode_atribbute_call_row; 
	break;
  
	
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}
