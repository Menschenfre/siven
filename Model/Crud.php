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

		return $this->table;
	} 

	public function read($customesql=null){
		//El nombre de la tabla se obtiene de la clase modelo, atributos
        $sql="SELECT * FROM $this->table";
        $result=$this->con->query($sql);
        //Inicializamos un array
        $list = array();

        //Por cada fila del resultado en la query se guarda dentro de la variable array $list
        while ($row = mysqli_fetch_array($result))
        $list[] = $row;

    	//Se retorna un arreglo con cada fila en la base de datos
        return $list;
    }

    public function createTEST(){

    	$sql="INSERT INTO $this->table($this->parameters) VALUES($this->values)";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();

        //Cerramos la consulta y la conexión 
        $this->con->close(); 
        return 1;
        //return $this->content;  
    }

}

?>