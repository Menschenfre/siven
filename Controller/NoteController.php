<?php 
include '/home2/sivenati/public_html/View/Includes/url.php'; 

require_once($model_note);

class NoteController extends MasterController{


	//Nombre del modelo
    public $model_name= "Note";

	/**
	 * Class Constructor
	 */
	public function __construct(){ 
		//Constructor padre
		parent::__construct();
	}



	/*function save($note){
		$model=new User2($user);
		$result= $model->create();
		
		return $result;
	}*/
}

?>