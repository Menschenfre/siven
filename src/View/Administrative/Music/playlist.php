<?php
//Recibimos la variable con el número de lista a mostrar
$lista = $_POST['lista'];

//Variables para los inicios y fin de etiquetas de video
$playlist_init='<div class="embed-responsive embed-responsive-16by9">
  <iframe class="embed-responsive-item" src="';
$playlist_end='" allowfullscreen></iframe></div>';

//Listado de reproducción de video:
$op_playlist="{$playlist_init}https://www.youtube.com/embed?v=JwQZQygg3Lk&list=PL7us9dtxevSqbslKo7tW-JCoHHWTJa33a{$playlist_end}";

$animatio_playlist="{$playlist_init}https://www.youtube.com/embed?v=opoLHWg55RE&list=PL7us9dtxevSoPZvYa9EeTsJhKqzMtUDlh{$playlist_end}";

$rocklasic_playlist="{$playlist_init}https://www.youtube.com/embed?v=Soa3gO7tL-c&list=PL7us9dtxevSqXmmdfRJGwFDuEv2A_ssit{$playlist_end}";

$stage_playlist="{$playlist_init}https://www.youtube.com/embed?v=8Ttz_TMGDZc&list=PL7us9dtxevSqG5rajxmc0xPQDpIQPYgM3{$playlist_end}";
$clasico_playlist="{$playlist_init}https://www.youtube.com/embed?v=MmPdt4UUsoA&list=PL7us9dtxevSr88kJi00wfJJxWOOxLlC6O{$playlist_end}";


/*Switch por variable $lista*/
switch ($lista){
	case "1":
	echo $op_playlist;
	break;
	case "2":
	echo $animatio_playlist;
	break;
	case "3":
	echo $rocklasic_playlist;
	break;
	case "4":
	echo $stage_playlist;
	break;
	case "5":
	echo $clasico_playlist;
	break;
	default:
	echo "No se carga ninguna lista de reproducción";
}
?>