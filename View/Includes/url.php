<?php 
// Base URL 
$Base_url="/home2/sivenati/public_html";
$Assets_origen= "/Assets";
$View_origen= "/View";
$Model_origen= "/Model";
$Controller_origen= "/Controller";
//Obtiene el nombre del archivo actual.
//$page= basename( __FILE__ );
// Includes base URL
$Includes_url= "{$Base_url}/View/Includes";

//Material design url
$Materialdesign_url= "/Fonts/Materialdesignicons-3.6.95/css/materialdesignicons.min.css";


/*----------------------------------------------------------*/
// Includes Models
$model_product= "{$Base_url}{$Model_origen}/Product.php";
$model_products_category= "{$Base_url}{$Model_origen}/Products_category.php";
$model_user= "{$Base_url}{$Model_origen}/User.php";
/*----------------------------------------------------------*/
//Includes Controllers
$controller_product= "{$Base_url}{$Controller_origen}/ProductController.php";
$controller_login= "{$Base_url}{$Controller_origen}/LoginController.php";
$controller_user= "{$Base_url}{$Controller_origen}/UserController2.php";

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