<?php 
/*Controlador de producto---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 17-09-2019
Comentario: Clase ProductController.php, controlador que invoca las diversas funciones del modelo de producto.
-----------------------------------------------------------------------------------------------------------*/

//Invocación de urls útiles
include '/home2/sivenati/public_html/View/Includes/url.php'; 

//Se carga el controlador padre, que contiene las funciones útiles para todos los controladores
require_once($controller_master);

//Se carga el modelo correspondiente, este es utilizado por el controlador padre
require_once($model_product);

//Cargamos el modelo hijo, que contiene las categorías de los productos.
require_once($model_products_category);

class ProductController extends MasterController{

	//Nombre del modelo ocupado por el controlador maestro
    public $model_name= "Product";

	public function __construct(){ 
	} 
	//Listado de productos, este es reflejado en el listado  "list_product.php"
	function list_product($result_type=null){
		$model=new Product();
		$result= $model->read();
		return $result;
	}
	//Listado de productos, estas son reflejadas en la vista "add_product.php"
	function list_category(){
		$model=new Products_category();
		$result= $model->read();
		return $result;
	} 
	//Suma de totales por categoría
	function total_category_product(){
		$model=new Product();
		$result= $model->categoryTotal();
		return $result;
	}
	//Suma del total, este es reflejado en el listado  "list_product.php"
	function total_product(){
		$model=new Product();
		$result= $model->generalTotal();
		return $result;
	}
	//Suma de totales por mes
	function total_month(){
		$model=new Product();
		$result= $model->monthTotal(2019,6);
		return $result;
	}
	
	//Listado de productos custom query, este es usado para el AJAX, recibe los parámetros de select en un array"
	function list_product_custome($product_filters){
		$model=new Product();
		$result= $model->custome_read($product_filters);
		return $result;
	}

	//Listado de productos custom query, este es usado para el AJAX, recibe los parámetros de select en un array"
	function total_product_custome($product_filters){
		$model=new Product();
		$result= $model->custome_total($product_filters);
		return $result;
	}

	//Listado de categorias y totales custom query, este es usado para el AJAX, recive los parámetros de select en un array"
	function total_category_custome($product_filters){
		$model= new Product();
		$result= $model->custome_total_category($product_filters);
		return $result;
	}

}