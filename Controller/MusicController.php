<?php
/*Controlador de musica---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 21-09-2019
Comentario: Clase MusicController.php, controlador que contiene las diferentes funciones y validaciones para el módulo de musica.
-----------------------------------------------------------------------------------------------------------*/

//Invocamos todas las url útiles
include '/home2/sivenati/public_html/View/Includes/url.php'; 
//Se carga el controlador padre
require_once($controller_master);
//Se carga el modelo correspondiente, este es utilizado por el controlador padre
require_once($model_music);

class MusicController extends MasterController{


	//Nombre del modelo para el controlador maestro
    public $model_name= "Music";

	public function __construct(){ 
		//Constructor padre, necesario para cargar funciones del padre
		parent::__construct();
	}
}