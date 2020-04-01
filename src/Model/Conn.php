<?php
/*Conexión Mysqli POO--------------------------------------------------------------------------------------
Versión: 1.0
Fecha de creación: 02-06-2019
Fecha última modificación: 23-08-2019
Comentario: Clase de conexión a la base de datos, se crean los atributos y constructor que son heredados
por la clase "Crud.php".
-----------------------------------------------------------------------------------------------------------*/
class Conn{
	//Atributos con sus valores estatáticos.
	protected $con;
	private $dbhost="10.108.214.85:80";
	private $dbuser="root";
	private $dbpass="my-super-secret-password";
	private $dbname="sivenati_siven";
	private $dbcharset ="utf8";

	//Constructor que se heredará a otras clases con la conexión a la bd
	public function __construct(){
		$this->con = new mysqli($this->dbhost, $this->dbuser, $this->dbpass, $this->dbname);
		if($this->con->connect_errno){
			echo "falla al conectar la bd".$this->con->connect_errno;
			return;
		}
		//Cambiamos a utf8 para tildes
		$this->con->set_charset($this->dbcharset);
	} 
}

?>