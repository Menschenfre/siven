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
    public $table= "products";
//Atributos
	private $id;
	private $id_category;
	private $name;
	private $total;
	private $price;
	private $status;
	private $created;
	private $modified;
	private $deleted;



	/**
	 * Class Constructor
	 * @param    $id   
	 * @param    $id_category   
	 * @param    $name   
	 * @param    $total   
	 * @param    $price   
	 * @param    $status   
	 * @param    $created   
	 * @param    $modified   
	 * @param    $deleted   
	 */

	//Inicializamos los atributos nulos para simular un constructor vacío
	public function __construct($id_category = null, $name = null, $total = null, $price = null, $status = null, $created = null, $modified = null, $deleted = null){

		//Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
		$this->id_category = $id_category;
		$this->name = $name;
		$this->total = $total;
		$this->price = $price;
		$this->status = $status;
		$this->created = $created;
		$this->modified = $modified;
		$this->deleted = $deleted;
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
    public function getTotal()
    {
        return $this->total;
    }

    /**
     * @param mixed $total
     *
     * @return self
     */
    public function setTotal($total)
    {
        $this->total = $total;

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

        $sql="INSERT INTO products(id_category,name,total,price,status,created) VALUES('$this->id_category','$this->name','$this->total','$this->price', 1, '$this->created')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();
        
        return 1;
    }

    public function total(){

        //Consulta obteniendo la suma del precio
        $sql="SELECT SUM(price) FROM $this->table";
        $total=$this->con->query($sql);
        //obtenemos la fila afectada
        $total=$total->fetch_row();
  

        //Consulta obteniendo la suma del precio de categoría comida
        $sql="SELECT SUM(price) FROM $this->table WHERE id_category=1";
        $total_food=$this->con->query($sql);
        $total_food=$total_food->fetch_row();

        //Consulta obteniendo la suma del precio de categoría virtual
        $sql="SELECT SUM(price) FROM $this->table WHERE id_category=2";
        $total_virtual=$this->con->query($sql);
        $total_virtual=$total_virtual->fetch_row();

        //Consulta obteniendo la suma del precio de categoría tecnología
        $sql="SELECT SUM(price) FROM $this->table WHERE id_category=3";
        $total_technology=$this->con->query($sql);
        $total_technology=$total_technology->fetch_row();

        //Consulta obteniendo la suma del precio de categoría salud
        $sql="SELECT SUM(price) FROM $this->table WHERE id_category=4";
        $total_health=$this->con->query($sql);
        $total_health=$total_health->fetch_row();

        //Consulta obteniendo la suma del precio de categoría fijos
        $sql="SELECT SUM(price) FROM $this->table WHERE id_category=5";
        $total_immovable=$this->con->query($sql);
        $total_immovable=$total_immovable->fetch_row();

        //Consulta obteniendo la suma del precio de categoría otros
        $sql="SELECT SUM(price) FROM $this->table WHERE id_category=6";
        $total_others=$this->con->query($sql);
        $total_others=$total_others->fetch_row();

       
        /*Creamos un array con las variables de totales obtenidas $keys guarda las cabeceras
        Results guarda las posiciones unicas de las consultas, $array combina entre posiciones
        y resultados.*/
        $keys = ['total','total_food','total_virtual','total_technology','total_health','total_immovable','total_others'];
        $results = [$total[0],$total_food[0],$total_virtual[0],$total_technology[0],$total_health[0],$total_immovable[0],$total_others[0]];
        $array = array_combine($keys, $results);

        //retornamos el array armado.
        return $array;
    }


    public function testTotal(){
        $sql="SELECT * FROM products_category";
        $result=$this->con->query($sql);
        //Inicializamos un array
        $list = array();

        while ($row = mysqli_fetch_array($result)){
            $count++;

            $sql="SELECT SUM(price) FROM $this->table WHERE id_category='$count'";
            $total=$this->con->query($sql);
            $total=$total->fetch_row();
            $list[] = $total;
        }
        return $list;
    }



    /*array(8) { 
[0]=> array(1) { [0]=> string(5) "45623" } 
[1]=> array(1) { [0]=> NULL } 
[2]=> array(1) { [0]=> NULL } 
[3]=> array(1) { [0]=> NULL } 
[4]=> array(1) { [0]=> string(6) "241630" } 
[5]=> array(1) { [0]=> string(5) "10000" } 
[6]=> array(1) { [0]=> string(5) "10000" } 
[7]=> array(1) { [0]=> NULL } }


SELECT SUM(price) FROM products 
INNER JOIN products_category
ON products.id_category = products_category.id
WHERE id_category=1
GROUP BY products.id 
ORDER BY products.id DESC;*/
}

?>