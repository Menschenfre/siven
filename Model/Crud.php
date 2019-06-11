<?php
/*Clase CRUD(crear, leer, actualizar, eliminar)-------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 02-06-2019
Comentario: Clase CRUD, se implementa la fecha que es heredada por las clases del modelo.
-----------------------------------------------------------------------------------------------------------*/
require_once ('Conn.php');

class Crud extends Conn{

	protected $dateTimeNow;
	
	function __construct(){
		parent::__construct();
		// MySQL datetime format
		$this->dateTimeNow = new DateTime();    
	}

	public function test(){

		return "Estoy retornando una funcion heredada del CRUD CTM";
	} 

}

?>