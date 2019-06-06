<?php
require_once ('Crud.php');

class Products_category extends Crud{

	private $id;
	private $name;
	private $description;
	private $status;
	private $created;
	private $modified;
	private $deleted;


	/**
	 * Class Constructor
	 * @param    $id   
	 * @param    $name   
	 * @param    $description   
	 * @param    $status   
	 * @param    $created   
	 * @param    $modified   
	 * @param    $deleted   
	 */
	public function __construct($id = null, $name = null, $description = null, $status = null, $created = null, $modified = null, $deleted = null){

		//Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
		$this->id = $id;
		$this->name = $name;
		$this->description = $description;
		$this->status = $status;
		$this->created = $created;
		$this->modified = $modified;
		$this->deleted = $deleted;
	}





    /**
     * @return mixed
     */
    public function getId()
    {
        return $this->id;
    }

    /**
     * @param mixed $id
     *
     * @return self
     */
    public function setId($id)
    {
        $this->id = $id;

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
    public function getDescription()
    {
        return $this->description;
    }

    /**
     * @param mixed $description
     *
     * @return self
     */
    public function setDescription($description)
    {
        $this->description = $description;

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


    //CRUD
    public function read(){
        $sql="SELECT * FROM products_category";
        $result=$this->con->query($sql);
        //Inicializamos un array
        $categoryList = array();

        //Por cada row del resultado en la query se guarda dentro de la variable array $XList
        while ($row_category = mysqli_fetch_array($result))
        $categoryList[] = $row_category;

        return $categoryList;
    }

}

?>