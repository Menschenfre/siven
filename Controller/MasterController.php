<?php 

class MasterController{

	public function __construct(){

	}


	public function list(){
	
		$model=new $this->model_name();
		$result= $model->read();
		return $result;
	}




}

?>