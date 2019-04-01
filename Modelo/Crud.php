<?php
require_once ('ConexionMysqli.php');
/**
 * 
 */
class Crud extends ConexionMysqli{
	
	function __construct(){
		parent::__construct();
	}

	public function save($nombre){

		$sql="INSERT INTO usuarios(nombre,nick,pass,creado) VALUES('test2','test','test','31-03-2019')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();
	}
}


?>