<?php

$link_init = '<link rel="stylesheet" href="';
$link_end = '">';


//Bootstrap 4.3.1
$bootstrap = '<link rel="stylesheet" href="Assets/css/bootstrap/bootstrap.min.css">';

//Index tema estilo
$index_theme = '<link rel="stylesheet" href="Assets/css/template_style/style.css">';

//Estilos propios
$mis_estilos ='<link rel="stylesheet" href="Assets/css/mis_estilos/estilos.css">';

//NO USADO
//Fontawesome 5.8.1
$fontawesome='<link rel="stylesheet" type="text/css" href="Fonts/Fontawesome-5.8.1/css/all.min.css">
';

//Estilos del panel de admin
$admin_style= "{$link_init}{$assets_css_admin}style.css{$link_end}";

//MaterialDesignIcons 3.6.95
$materialdesign_css= "{$link_init}{$Materialdesign_url}{$link_end}";

//Data tables -Usado en resumen.
$data_tables_css= "{$link_init}{$assets_css_data_tables}datatables.min.css{$link_end}";

//quill texto enriquecido
$quill_css= "{$link_init}{$assets_css_quill}quill.snow.css{$link_end}";





/*Switch por cada página vista*/
switch ($page){
	case "index":
	echo $animate.''.$bootstrap.''.$index_theme.''.$mis_estilos;
	break;
	case "Admin":
	echo $admin_style.''.$materialdesign_css.''.$data_tables_css.''.$quill_css;
	break;
	case "Login":
	case "Register":
	echo $admin_style.''.$materialdesign_css;
	break;
	default: 
	echo "//No se carga ningún css";
}

//var_dump($page);


?>	