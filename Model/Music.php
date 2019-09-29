<?php
/*Modelo de música---------------------------------------------------------------------------------------
Versión: 1.0
Fecha de creación: 21-09-2019
Fecha última modificación: 21-09-2019
Comentario: Clase Notes.php, utilizada para almacenar musica cuya fuente de origen es youtube.
-----------------------------------------------------------------------------------------------------------*/

//Importamos funciones comunes del crud
require_once ('Crud.php');

class Music extends Crud{

	//Nombre de la tabla,parametros,valores, utilizados por el crud.
	public $table;
	//parametros que se usarán para guardar en la tabla.
	public $parameters;
	//valores que pasaran a guardar, se inicializan en el constructor por dependencia.
	public $values;

	//Atributos en la bd
	private $id;
	private $id_user;
	private $name;
	private $category;
	private $url;
	private $status;
	private $created;
	private $modified;

	//Inicializamos los atributos nulos para simular un constructor vacío, recibimos un array
	public function __construct($music = null){

		//Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos, recibimos valores de array declarado en el constructor
		//$this->id_user = $music["id_user"];
		$this->name = mysqli_real_escape_string($this->con, $music["name"]);
		$this->category = $music["category"];
		$this->url = $music["url"];
		$this->id_user = 1;
		//$this->name = "TESTNAME";
		//$this->category = "TESTCATEGORY";
		
        //$this->url = "TESTURL";
		$this->setStatus($status);
        $this->setCreated($created);
		$this->modified = $modified;

		$this->table = "music";
		$this->parameters = "id_user,name,category,url";
		$this->values = "'$this->id_user','$this->name','$this->category','$this->url'";

		
	}


    /**
     * @return mixed
     */
    public function getTable()
    {
        return $this->table;
    }

    /**
     * @param mixed $table
     *
     * @return self
     */
    public function setTable($table)
    {
        $this->table = $table;

        return $this;
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
    public function getIdUser()
    {
        return $this->id_user;
    }

    /**
     * @param mixed $id_user
     *
     * @return self
     */
    public function setIdUser($id_user)
    {
        $this->id_user = $id_user;

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
    public function getCategory()
    {
        return $this->category;
    }

    /**
     * @param mixed $category
     *
     * @return self
     */
    public function setCategory($category)
    {
        $this->category = $category;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getUrl()
    {
        return $this->url;
    }

    /**
     * @param mixed $url
     *
     * @return self
     */
    public function setUrl($url)
    {
        $this->url = $url;

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
        $this->status = 1;

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
        $this->created = $this->dateTimeNow->format('Y-m-d H:i:s');

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
}