<?php
//Conexión Mysqli POO
	class Conn{
		protected $con;
		private $dbhost="localhost";
		private $dbuser="sivenati_sickven";
		private $dbpass="1342993nonoaccessbbbrbrbbb29";
		private $dbname="sivenati_siven";
		private $dbcharset ="utf8";

		public function __construct(){
			//$this->connect_db();
			$this->con = new mysqli('localhost', $this->dbuser, $this->dbpass, $this->dbname);
			if($this->con->connect_errno){
				echo "fallo al conectar la bd".$this->con->connect_errno;
				return;
			}
			$this->con->set_charset($dbcharset);

		}
		/*public function connect_db(){
			$this->con = new mysqli($dbhost, $dbuser, $dbpass, $dbname);
			if($this->con->connect_errno){
				echo "fallo al conectar la bd".$this->con->connect_errno;
				return;
			}
			$this->con->set_charset($dbcharset);
		}*/

}

?>