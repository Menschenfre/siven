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

//Gráficos canvasjs 2.3.2
$canvasjs = "{$script_init}{$assets_js_canvasjs}canvasjs.min.js{$script_end}";

//quill, texto enriquecido(katex y highlight requerido para complemento)
$quill_js = "{$script_init}{$assets_js_quill}quill.min.js{$script_end}";
$katex_js = "{$script_init}{$assets_js_quill}katex.min.js{$script_end}";
$highlight_js = "{$script_init}{$assets_js_quill}highlight.min.js{$script_end}";


//popper, para dropdown, debe ser invocado antes de bootstrap 
$popper_js = "{$script_init}{$assets_js_popper}popper.js{$script_end}";


//bootstrap select, más opciones de select
$bootstrap_select_js = "{$script_init}{$assets_js_bootstrap_select}bootstrap-select.min.js{$script_end}";



switch ($page){
	case "index":
	echo $jquery.''.$bootstrap.''.$main_index.''.$sweet_alert.''.$waypoints;
	break;
	case "Admin":
	echo $popper_js.''.$jquery.''.$bootstrap.''.$admin_base_js.''.$admin_offcanvas_js.''.$admin_dashboard_js.''.$admin_misc_js.''.$data_tables_js.''.$canvasjs.''.$katex_js.''.$highlight_js.''.$quill_js;
	break;
	case "Register":
	case "Login":
	echo $jquery.''.$bootstrap.''.$admin_base_js.''.$admin_offcanvas_js.''.$admin_dashboard_js.''.$admin_misc_js;
	break;
<<<<<<< HEAD
	case "Test":
	echo $popper_js.''.$jquery.''.$bootstrap.''.$bootstrap_select_js;
	break;
=======
>>>>>>> parent of 7a41690... Cambios testing
	default:
	echo "//No se carga ningún js";
} 


?>