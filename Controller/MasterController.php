<?php 

class MasterController{

	public function __construct(){

	}

	public function save($save_data){
	
		$model=new $this->model_name($save_data);
		$result= $model->create();
		
		return $result;
	}

	public function list(){
	
		$model=new $this->model_name();
		$result= $model->read();
		return $result;
	}




}

?>