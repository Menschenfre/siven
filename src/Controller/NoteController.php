<?php
/*Controlador de notas---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 17-09-2019
Comentario: Clase NoteController.php, controlador que contiene las diferentes funciones y validaciones para el módulo de notas.
Último comentario: Se agrega main path.
-----------------------------------------------------------------------------------------------------------*/

//Invocamos todas las url útiles
$MAINPATH= $_SERVER['DOCUMENT_ROOT'];
include '' . $MAINPATH . '/View/Includes/url.php'; 
//Se carga el controlador padre
require_once($controller_master);
//Se carga el modelo correspondiente, este es utilizado por el controlador padre
require_once($model_note);
//Cargamos el modelo hijo, que contiene las categorías de las notas.
require_once($model_notes_category);

class NoteController extends MasterController{


	//Nombre del modelo para el controlador maestro
    public $model_name= "Notes";

	public function __construct(){ 
		//Constructor padre, necesario para cargar funciones del padre
		parent::__construct();
	}

	//Listado categorías de notas , estas son reflejadas en la vista "add_note.php"
	function list_category(){
		$model=new Notes_category();
		$result= $model->read();
		return $result;
	} 
}