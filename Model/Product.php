<?php
require_once ('Crud.php');

class Product extends Crud{

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
	public function __construct($id = null, $id_category = null, $name = null, $total = null, $price = null, $status = null, $created = null, $modified = null, $deleted = null){

		//Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
		$this->id = $id;
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
}

?>