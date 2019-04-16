<?php 

//jQuery 3.3.1 (Cargarlo antes que bootstrap)
$jquery ='<script src="Assets/js/jquery/jquery-3.3.1.min.js"></script>';
//Bootstrap 4.3.1
$bootstrap ='<script src="Assets/js/bootstrap/bootstrap.min.js"></script>';
//Template index 
$main_index ='<script src="Assets/js/template_js/main.js"></script>';
//Sweet alert(alertas custome)
$sweet_alert ='<script src="Assets/js/sweetalert2/sweetalert2.all.min.js"></script>';
//Jquery waypoints (afecta el menu lateral del index)
$waypoints = '<script src="Assets/js/jwaypoints/jquery.waypoints.min.js"></script>';
//Popper
$popper = '<script src="Assets/js/popper/popper.min.js"></script>';
switch ($page){
	case "index":
	echo $jquery.''.$bootstrap.''.$main_index.''.$sweet_alert.''.$waypoints;
	break;
	case "login":
	echo $jquery.''.$bootstrap.''.$popper;
	break;
	default:
	echo "//No se carga ningún css";
}


?>