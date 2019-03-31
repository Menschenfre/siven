<?php
//Conexión Mysqli POO
	class ConexionMysqli{
		protected $con;
		private $dbhost="localhost";
		private $dbuser="sivenati_sickven";
		private $dbpass="1342993nonoaccessbbbrbrbbb29";
		private $dbname="sivenati_siven";
		private $dbcharset ="utf8";
		//protected $con;

		public function __construct(){
			//$this->connect_db();
			$this->con = new mysqli('localhost', $this->dbuser, '1342993nonoaccessbbbrbrbbb29', 'sivenati_siven');
			if($this->con->connect_errno){
				echo "fallo al conectar la bd".$this->con->connect_errno;
				return;
			}
			$this->con->set_charset($dbcharset);

		}
		public function connect_db(){
			$this->con = new mysqli($dbhost, $dbuser, $dbpass, $dbname);
			if($this->con->connect_errno){
				echo "fallo al conectar la bd".$this->con->connect_errno;
				return;
			}
			$this->con->set_charset($dbcharset);
		}

}

?>