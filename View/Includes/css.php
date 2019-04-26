<?php

$link_init = '<link rel="stylesheet" href="';
$link_end = '">';


//Animate.css, animaciones simples
$animate = '<link rel="stylesheet" href="Assets/css/animate/animate.css">';

//Bootstrap 4.3.1
$bootstrap = '<link rel="stylesheet" href="Assets/css/bootstrap/bootstrap.min.css">';

//Index tema estilo
$index_theme = '<link rel="stylesheet" href="Assets/css/template_style/style.css">';

//Estilos propios
$mis_estilos ='<link rel="stylesheet" href="Assets/css/mis_estilos/estilos.css">';

//Hamburgers is a collection of tasty CSS-animated hamburger icons
$hamburgers ='<link rel="stylesheet" href="Assets/css/hamburgers/hamburgers.min.css">';

//The jQuery replacement for select boxes
$select2 ='<link rel="stylesheet" href="Assets/css/select2/select2.min.css">';

//Estilos del login
$login_main='<link rel="stylesheet" href="Assets/css/login_style/main.css">';
$login_util='<link rel="stylesheet" href="Assets/css/login_style/util.css">';

//Fontawesome 5.8.1
$fontawesome='<link rel="stylesheet" type="text/css" href="Fonts/Fontawesome-5.8.1/css/all.min.css">
';

//Estilos del panel de admin
$admin_style= "{$link_init}{$assets_css_admin}style.css{$link_end}";
$admin_addons= "{$link_init}{$assets_css_admin}addons.css{$link_end}";

/*Switch por cada página vista*/
switch ($page){
	case "index":
	echo $animate.''.$bootstrap.''.$index_theme.''.$mis_estilos;
	break;
	case "login":
	echo $bootstrap.''.$animate.''.$hamburgers.''.$select2.''.$login_util.''.$login_main.''.$fontawesome;
	break;
	case "admin":
	echo $admin_style.''.$admin_addons;
	break;
	default:
	echo "//No se carga ningún css";
}

//var_dump($page);


?>	