<?php 

class BD
{
	private static $instance=NULL;
	
	function __construct(){}

	public static function getConnect(){
		if (!isset(self::$instance)) {
			$pdo_options[PDO::ATTR_ERRMODE]=PDO::ERRMODE_EXCEPTION;
			self::$instance= new PDO('mysql:host=localhost;dbname=sivenati_siven','sivenati_sickven','1342993nonoaccessbbbrbrbbb29',$pdo_options);
		} 
		return self::$instance;
	}
}

?>