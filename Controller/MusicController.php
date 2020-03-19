<?php
/*Controlador de musica---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 21-09-2019
Resumen: Clase MusicController.php, controlador que contiene las diferentes funciones y validaciones para el módulo de musica.
Último comentario: Se agrega main path.
-----------------------------------------------------------------------------------------------------------*/

//Invocamos todas las url útiles
$MAINPATH= $_SERVER['DOCUMENT_ROOT'];
include '' . $MAINPATH . '/View/Includes/url.php'; 
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