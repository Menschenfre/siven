<?php 
$script_init = '<script src="';
$script_end = '"></script>';

//jQuery 3.3.1 (Cargarlo antes que bootstrap)
$jquery ='<script src="/Assets/js/jquery/jquery-3.3.1.min.js"></script>';

//Bootstrap 4.3.1
$bootstrap ='<script src="/Assets/js/bootstrap/bootstrap.min.js"></script>';

//Template index 
$main_index ='<script src="Assets/js/template_js/main.js"></script>';

//NO USADO
//Sweet alert(alertas custome)
$sweet_alert ='<script src="Assets/js/sweetalert2/sweetalert2.all.min.js"></script>';

//Jquery waypoints (afecta el menu lateral del index)
$waypoints ='<script src="Assets/js/jwaypoints/jquery.waypoints.min.js"></script>';

//Admin js
$admin_dashboard_js = "{$script_init}{$assets_js_admin}dashboard.js{$script_end}";
$admin_base_js = "{$script_init}{$assets_js_admin}base.js{$script_end}";
$admin_offcanvas_js = "{$script_init}{$assets_js_admin}off-canvas.js{$script_end}";
$admin_misc_js = "{$script_init}{$assets_js_admin}misc.js{$script_end}";
$data_tables_js = "{$script_init}{$assets_js_data_tables}datatables.min.js{$script_end}";


switch ($page){
	case "index":
	echo $jquery.''.$bootstrap.''.$main_index.''.$sweet_alert.''.$waypoints;
	break;
	case "Admin":
	echo $jquery.''.$bootstrap.''.$admin_base_js.''.$admin_offcanvas_js.''.$admin_dashboard_js.''.$admin_misc_js.''.$data_tables_js;
	break;
	case "Register":
	case "Login":
	echo $jquery.''.$bootstrap.''.$admin_base_js.''.$admin_offcanvas_js.''.$admin_dashboard_js.''.$admin_misc_js;
	break;
	default:
	echo "//No se carga ningún js";
}


?>