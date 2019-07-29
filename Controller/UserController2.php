<?php
include '/home2/sivenati/public_html/View/Includes/url.php'; 

require_once($model_product);
require_once($model_products_category);

class UserController2{

	/**
	 * Class Constructor
	 */
	public function __construct(){ 
	}
	
	function Works(){
		return 1;
	}
	

	function save($id_category,$name,$total,$price,$created){
		$model=new Product($id_category,$name,$total,$price,NULL,$created,NULL,NULL);
		$result= $model->create();
		
		return $result;
	}

}
?>