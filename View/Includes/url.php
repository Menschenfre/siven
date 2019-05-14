<?php 
// Base URL
$Base_url="/home2/sivenati/public_html";
$Assets_origen= "/Assets";
$View_origen= "/View";
$Model_origen= "/Model";
//Obtiene el nombre del archivo actual.
//$page= basename( __FILE__ );
// Includes base URL
$Includes_url= "{$Base_url}/View/Includes";

//Material design url
$Materialdesign_url= "/Fonts/Materialdesignicons-3.6.95/css/materialdesignicons.min.css";


/*----------------------------------------------------------*/
$model_product= "{$Model_origen}/Product.php";

/*----------------------------------------------------------*/


/*----------------------------------------------------------*/
// Includes css
$css = "${Includes_url}/css.php";
// Includes js
$js = "${Includes_url}/js.php";

/*----------------------------------------------------------*/
// Assets css
$assets_css = "{$Assets_origen}/css/";
$assets_css_admin = "{$assets_css}admin_style/";
$assets_css_data_tables = "{$assets_css}data_tables/";
/*----------------------------------------------------------*/
// Assets js
$assets_js = "{$Assets_origen}/js/";
$assets_js_admin = "{$assets_js}admin_js/";
$assets_js_data_tables = "{$assets_js}data_tables/";
/*----------------------------------------------------------*/
// Assets images
$assets_images = "{$Assets_origen}/images/";

/*----------------------------------------------------------*/


?>