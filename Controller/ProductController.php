<?php
include '/home2/sivenati/public_html/View/Includes/url.php'; 

require_once($model_product);
require_once($model_products_category);

class ProductController{

	/**
	 * Class Constructor
	 */
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

	function save($id_category,$name,$total,$price,$created){
		$model=new Product($id_category,$name,$total,$price,NULL,$created,NULL,NULL);
		$result= $model->create();
		
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

}
?>