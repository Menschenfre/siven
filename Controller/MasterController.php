<?php 

class MasterController{

	public function __construct(){

	}

	public function save($save_data){
		$model=new $model_name($save_data);
		$result= $model->create();
		
		return $result;
	}




}

?>