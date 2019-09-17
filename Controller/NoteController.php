<?php
/*Controlador de notas---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 17-09-2019
Comentario: Clase NoteController.php, controlador que contiene las diferentes funciones y validaciones para el módulo de notas.
-----------------------------------------------------------------------------------------------------------*/

//Invocamos todas las url útiles
include '/home2/sivenati/public_html/View/Includes/url.php'; 
//Se carga el controlador padre
require_once($controller_master);
//Se carga el modelo correspondiente, este es utilizado por el controlador padre
require_once($model_note);

class NoteController extends MasterController{


	//Nombre del modelo para el controlador maestro
    public $model_name= "Notes";

	public function __construct(){ 
		//Constructor padre, necesario para cargar funciones del padre
		parent::__construct();
	}
}