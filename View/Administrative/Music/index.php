<?php $MAINPATH= $_SERVER['DOCUMENT_ROOT'];
include '' . $MAINPATH . '/View/Includes/url.php';

//Recibimos el valor del identificador
$identifier = $_POST['identifier'];

/*Switch por cada llave identificadora*/
switch ($identifier){ 
	
	/*Respuesta Ajax al agregar una nota*/
	case "add_music":
	require_once($controller_music);
	$music_control=new MusicController();

	//Se recibe el array con la data de la vista
	$music = $_POST['music'];

	//capturamos la url ingresada
	$url = $music["url"];
	//obtenemos el nombre del video
	$content = file_get_contents("http://youtube.com/get_video_info?video_id=$url");
	parse_str($content, $ytarr);
	$jsondec = json_decode($ytarr['player_response'],true);
	$name = $jsondec['videoDetails'][title];

	//Pasamos el nombre del video al arreglo "music"
	$music += ["name" => $name];

	//Pasamos la variable al método save del controlador(master)
	$result=$music_control->saveTest($music);

	echo $result;
	break;
  
	
	/*Respuesta default*/
	default:
	echo "//No se ha seteado ningun valor atravéz del botón";
}
