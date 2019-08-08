<?php 

class MasterController{

	public function __construct(){

	}

	public function save($save_data){
	
		$model=new $this->model_name($save_data);
		$result= $model->create();
		
		return $result;
	}




}

?>