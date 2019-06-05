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
	//Listado de categorías, estas son reflejadas en la vista "add_product.php"
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

	function save2(){
		if (!isset($_POST['estado'])) {
			$estado="of";
		}else{
			$estado="on";
		}
		$alumno= new Alumno(null, $_POST['nombres'],$_POST['apellidos'],$estado);

		Alumno::save($alumno);
		$this->show();
	}
/*
	function save(){
		if (!isset($_POST['estado'])) {
			$estado="of";
		}else{
			$estado="on";
		}
		$alumno= new Alumno(null, $_POST['nombres'],$_POST['apellidos'],$estado);

		Alumno::save($alumno);
		$this->show();
	}

*/
 
}
?>