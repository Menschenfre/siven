<?php //Invocamos todas las url útiles
include '/home2/sivenati/public_html/View/Includes/url.php'; 
//Se carga el controlador padre
require_once($controller_master);

//Se carga el modelo correspondiente, este es utilizado por el controlador padre
require_once($model_note);

class NoteController extends MasterController{


	//Nombre del modelo para el controlador padre
    public $model_name= "Notes";

	public function __construct(){ 
		//Constructor padre
		parent::__construct();
	}

}

?>