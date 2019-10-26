<?php 
/*Modelo de producto---------------------------------------------------------------------------------------
Versión: 1.0
Fecha última modificación: 11-06-2019
Comentario: Clase Product.php se hace la modificación agregando el atributo $table con un valor estático para 
el pase a Crud.php
-----------------------------------------------------------------------------------------------------------*/
require_once ('Crud.php');

class Product extends Crud{

    //Nombre de la tabla
    public $table;
    //Parámetros de lectura "solo los requeridos" para la función read del crud
    public $read_parameters;    
    //Atributos
	private $id;
	private $id_category;
	private $name;
	private $price;
	private $status;
	private $created;
	private $modified;
	private $deleted;


  
	//Inicializamos los atributos nulos para simular un constructor vacío
	public function __construct($product = null){

		//Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
		$this->id_category = $product["product_category"];
		$this->name = $product["product_name"];
		$this->price = $product["product_price"];
		$this->status = $status;
		$this->created = $product["product_date"];
		$this->modified = $modified;
		$this->deleted = $deleted;
        //Valores usados en funciones
        $this->table = "products";
        $this->read_parameters = "id,name,price,created";
	}
 
    /**
     * @return mixed
     */
    public function getIdCategory()
    {
        return $this->id_category;
    }

    /**
     * @param mixed $id_category
     *
     * @return self
     */
    public function setIdCategory($id_category)
    {
        $this->id_category = $id_category;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getName()
    {
        return $this->name;
    }

    /**
     * @param mixed $name
     *
     * @return self
     */
    public function setName($name)
    {
        $this->name = $name;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getPrice()
    {
        return $this->price;
    }

    /**
     * @param mixed $price
     *
     * @return self
     */
    public function setPrice($price)
    {
        $this->price = $price;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getStatus()
    {
        return $this->status;
    }

    /**
     * @param mixed $status
     *
     * @return self
     */
    public function setStatus($status)
    {
        $this->status = $status;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getCreated()
    {
        return $this->created;
    }

    /**
     * @param mixed $created
     *
     * @return self
     */
    public function setCreated($created)
    {
        $this->created = $created;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getModified()
    {
        return $this->modified;
    }

    /**
     * @param mixed $modified
     *
     * @return self
     */
    public function setModified($modified)
    {
        $this->modified = $modified;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getDeleted()
    {
        return $this->deleted;
    }

    /**
     * @param mixed $deleted
     *
     * @return self
     */
    public function setDeleted($deleted)
    {
        $this->deleted = $deleted;

        return $this;
    }


    //Función buscar por nick, recibe 2 parámetros de comparación
    public function validateUser($nick, $pass){

        //Generamos la consulta en una variable reutilizable
        $sql="SELECT * FROM users WHERE nick='$nick' AND pass='$pass'";
 
        //Preparamos la consulta ejecutando la query palabra reservada "query"
        $result=$this->con->query($sql);
    
        //Inicializamos un array
        //$userList = array();

        //Por cada row del resultado en la query se guarda dentro de la variable array $userList
        //while ($row_user = mysqli_fetch_array($resultado))
        //$userList[] = $row_user;
        
        //Variable booleana 0 = no,  1 = si.
        $flag= 0;

        //Si el numero de filas es mayor a 0 se retorna un verdadero
        if($result->num_rows>0){
            //Cerramos la consulta
            $result->close();
            //Cerramos la conexión
            $this->con->close();
            //Retornamos 1
            return $flag = 1;
        }else{
            //Cerramos la consulta
            $result->close();
            //Cerramos la conexión
            $this->con->close();
            //Retornamos 0
            return $flag = 0;
        }
        

    }

    public function create(){

        $sql="INSERT INTO products(id_category,name,price,status,created) VALUES('$this->id_category','$this->name','$this->price', 1, '$this->created')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();
        
        return 1;
    }

    public function generalTotal(){

        //Consulta obteniendo la suma del precio
        $sql="SELECT SUM(price) FROM $this->table";
        $total=$this->con->query($sql);
        //obtenemos la fila afectada
        $total=$total->fetch_row();
  
        //retornamos el array armado.
        return $total;
    }


    public function categoryTotal(){
        $sql="SELECT * FROM products_category";
        $result=$this->con->query($sql);
        //Inicializamos un array
        $list = array();

        while ($row = mysqli_fetch_array($result)){
            $count++;

            $sql="SELECT products_category.name, IFNULL(SUM(products.price),0) FROM products_category 
            INNER JOIN $this->table 
            ON products.id_category = products_category.id 
            WHERE products_category.id='$count'";

            $total=$this->con->query($sql);
            $total=$total->fetch_row();
            $list[] = $total;
        }
        return $list;
    }

    
    public function monthTotal($year, $month){
        
        $count= 1;
        $month= 10;
        $acumulated[] = "SELECT SUM(price) FROM products WHERE created BETWEEN '$year-01-01' AND '$year-01-31'
    ) AS MONTH1,";

        while ($count <= $month) {
            $count++;
            $acumulated[] = "(SELECT SUM(price) FROM products WHERE created BETWEEN '$year-$count-01' AND '$year-$count-31'
    ) AS MONTH$count,";

        }
        $acumulated[] = "(SELECT SUM(price) FROM products WHERE created BETWEEN '$year-12-01' AND '$year-12-31'
    ) AS MONTH12";
        $implodeado= implode($acumulated);

        $sql="SELECT( $implodeado";


        $total= $this->con->query($sql);
        //obtenemos un array con la data
        $total=$total->fetch_array();
        //retornamos el array armado.
        return $total;
        
    } 

    public function queryDelivery($year){

        $sql= "SELECT * FROM $this->table WHERE created BETWEEN '$year-01-01' AND '$year-12-31'";
        return $sql;
    }


    
    public function testQuery(){
        //El nombre de la tabla se obtiene de la clase modelo, atributos
        $sql="SELECT * FROM $this->table";
        $result=$this->con->query($sql);
        //Inicializamos un array
        $list = array();

        //Por cada fila del resultado en la query se guarda dentro de la variable array $list
        while ($row = mysqli_fetch_row($result))
        $list[] = $row;

        //Se retorna un arreglo con cada fila en la base de datos
        return $list;
    }
 
}

