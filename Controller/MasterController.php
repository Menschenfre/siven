<?php
/*Controlador Maestro---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 17-09-2019
Comentario: Clase MasterController.php, diseñada para ahorrar código con funciones genéricas ocupadas por todos los controladores.
-----------------------------------------------------------------------------------------------------------*/

class MasterController{
	public function __construct(){
	}

//Función que recibe como parámetro un arreglo con la info a guardar atravéz del modelo.
	public function save($save_data){
		$model=new $this->model_name($save_data);
		$result= $model->create();
		
		return $result;
	}
//Función que invoca una lista con todos los datos del modelo.	
	public function list(){
		$model=new $this->model_name();
		$result= $model->read();
		return $result;
	}

	//Función que recibe como parámetro un arreglo con la info a guardar atravéz del modelo.
	public function saveTest($save_data){
		$model=new $this->model_name($save_data);
		$result= $model->createTEST();
		
		return $result;
	}
} 
