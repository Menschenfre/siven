<?php //Invocación de urls útiles
include '/home2/sivenati/public_html/View/Includes/url.php'; 

//Se carga el controlador padre, que contiene las funciones útiles para todos los controladores
require_once($controller_master);

//Se carga el modelo correspondiente, este es utilizado por el controlador padre
require_once($model_product);

//Cargamos el modelo hijo, que contiene las categorías de los productos.
require_once($model_products_category);

class ProductController extends MasterController{

	//Nombre del modelo para el controlador padre
    public $model_name= "Product";

	public function __construct(){ 
	} 
	
	function Works(){
		return 1;
	}
	//Listado de productos, este es reflejado en el listado  "list_product.php"
	function list_product(){
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

	/*function save($id_category,$name,$total,$price,$created){
		$model=new Product($id_category,$name,$total,$price,NULL,$created,NULL,NULL);
		$result= $model->create();
		
		return $result;
	}*/
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
	//Integra una query requerida por cada caso 
	function list_for_params(){
		$model= new Product();
		$customesql= $model->queryDelivery($year);
		$result= $model->read($customesql);
	}

}
?>