<?php
include '/home2/sivenati/public_html/View/Includes/url.php'; 

require_once($model_product);

class ProductController{

	/**
	 * Class Constructor
	 */
	public function __construct(){

		 
		 
	}
	function Works(){
		return 1;
	}

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