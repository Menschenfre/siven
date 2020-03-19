<?php
$MAINPATH= $_SERVER['DOCUMENT_ROOT'];
include '' . $MAINPATH . '/View/Includes/url.php';  

require_once($model_user);

class UserController{

	/**
	 * Class Constructor
	 */
	public function __construct(){ 
	}
	
	function save($user){
		$model=new User2($user);
		$result= $model->create();
		
		return $result;
	}
}
?>