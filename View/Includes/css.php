<?php

//Animate.css, animaciones simples
$animate = '<link rel="stylesheet" href="Assets/css/animate/animate.css">';
//Bootstrap 4.3.1
$bootstrap = '<link rel="stylesheet" href="Assets/css/bootstrap/bootstrap.min.css">';
//Index tema estilo
$index_theme = '<link rel="stylesheet" href="Assets/css/template_style/style.css">';
//Estilos propios
$mis_estilos ='<link rel="stylesheet" href="Assets/css/mis_estilos/estilos.css">';

switch ($page){
	case "index":
	echo $animate.''.$bootstrap.''.$index_theme.''.$mis_estilos;
	break;
	case "login":
	echo $bootstrap.''.$animate;
	break;
	default:
	echo "//No se carga ningún css";
}

var_dump($page);


?>	