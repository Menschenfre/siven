<?php 
// Base URL
$Base_url="/home2/sivenati/public_html";
$Assets_origen= "/Assets";
$View_origen= "/View";
$Function_origen= "/Function";
//Obtiene el nombre del archivo actual.
//$page= basename( __FILE__ );
// Includes base URL
$Includes_url= "{$Base_url}/View/Includes";

//Material design url
$Materialdesign_url= "/Fonts/Materialdesignicons-3.6.95/css/materialdesignicons.min.css";


/*----------------------------------------------------------*/
// Includes css
$css = "${Includes_url}/css.php";
// Includes js
$js = "${Includes_url}/js.php";
// Includes function
$function = "${Includes_url}/function.php";

/*----------------------------------------------------------*/
// Assets css
$assets_css = "{$Assets_origen}/css/";
$assets_css_admin = "{$assets_css}admin_style/";
/*----------------------------------------------------------*/
// Assets js
$assets_js = "{$Assets_origen}/js/";
$assets_js_admin = "{$assets_js}admin_js/";
/*----------------------------------------------------------*/
// Assets images
$assets_images = "{$Assets_origen}/images/";

?>