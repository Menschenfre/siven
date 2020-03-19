<?php 
// Base URL 
$Base_url= $_SERVER['DOCUMENT_ROOT'];
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
$model_note= "{$Base_url}{$Model_origen}/Notes.php";
$model_notes_category= "{$Base_url}{$Model_origen}/Notes_category.php";
$model_music= "{$Base_url}{$Model_origen}/Music.php";
/*----------------------------------------------------------*/
//Includes Controllers
$controller_product= "{$Base_url}{$Controller_origen}/ProductController.php";
$controller_login= "{$Base_url}{$Controller_origen}/LoginController.php";
$controller_user= "{$Base_url}{$Controller_origen}/UserController.php";
$controller_note= "{$Base_url}{$Controller_origen}/NoteController.php";
$controller_master= "{$Base_url}{$Controller_origen}/MasterController.php";
$controller_music= "{$Base_url}{$Controller_origen}/MusicController.php";

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
$assets_css_quill = "{$assets_css}quill/";
$assets_css_bootstrap_select = "{$assets_css}bootstrap_select/";
/*----------------------------------------------------------*/
// Assets js
$assets_js = "{$Assets_origen}/js/";
$assets_js_admin = "{$assets_js}admin_js/";
$assets_js_data_tables = "{$assets_js}data_tables/";
$assets_js_canvasjs = "{$assets_js}canvasjs/";
$assets_js_quill = "{$assets_js}quill/";
$assets_js_popper = "{$assets_js}popper/";
$assets_js_bootstrap_select = "{$assets_js}bootstrap_select/";
/*----------------------------------------------------------*/
// Assets images
$assets_images = "{$Assets_origen}/images/";

/*----------------------------------------------------------*/
