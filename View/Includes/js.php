<?php 
$script_init = '<script src="';
$script_end = '"></script>';

//jQuery 3.3.1 (Cargarlo antes que bootstrap)
$jquery ='<script src="Assets/js/jquery/jquery-3.3.1.min.js"></script>';

//Bootstrap 4.3.1
$bootstrap ='<script src="Assets/js/bootstrap/bootstrap.min.js"></script>';

//Template index 
$main_index ='<script src="Assets/js/template_js/main.js"></script>';

//Sweet alert(alertas custome)
$sweet_alert ='<script src="Assets/js/sweetalert2/sweetalert2.all.min.js"></script>';

//Jquery waypoints (afecta el menu lateral del index)
$waypoints ='<script src="Assets/js/jwaypoints/jquery.waypoints.min.js"></script>';

//Popper
$popper ='<script src="Assets/js/popper/popper.min.js"></script>';

//Select2
$select2 ='<script src="Assets/js/select2/select2.min.js"></script>';

//Tilt Jquery
$tilt_jquery ='<script src="Assets/js/tilt_jquery/tilt.jquery.min.js"></script>';

//Js de la página login
$login_js ='<script src="Assets/js/login_js/main.js"></script>';

//Admin js
$admin_dashboard = "{$script_init}{$assets_js_admin}dashboard.js{$script_end}";


switch ($page){
	case "index":
	echo $jquery.''.$bootstrap.''.$main_index.''.$sweet_alert.''.$waypoints;
	break;
	case "login":
	echo $jquery.''.$popper.''.$bootstrap.''.$select2.''.$tilt_jquery.''.$login_js;
	break;
	case "admin":
	echo $admin_dashboard;
	break;
	default:
	echo "//No se carga ningún js";
}


?>