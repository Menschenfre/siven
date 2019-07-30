<?php
include '/home2/sivenati/public_html/View/Includes/url.php'; 

require_once($model_user2);

class UserController2{

	/**
	 * Class Constructor
	 */
	public function __construct(){ 
	}
	
	function Works(){
		return 1;
	}
	

	function save($user){
		$model=new User2($user);
		$result= $model->create();
		
		return $result;
	}

}
?>