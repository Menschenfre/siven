<?php 
/*Modelo de producto---------------------------------------------------------------------------------------
Versión: 1.0
Fecha de creación: 07-08-2019
Fecha última modificación: 07-08-2019
Comentario: Clase Notes.php, utilizada para almacenar notas de todo tipo.
-----------------------------------------------------------------------------------------------------------*/

require_once ('Crud.php');
/**
 * 
 */
class Notes extends Crud{

	//Nombre de la tabla
	public $table= "notes";

	//Atributos
	private $id;
	private $title;
	private $content;
	private $status;
	private $created;
	private $modified;


	//Inicializamos los atributos nulos para simular un constructor vacío, recibimos un array
	public function __construct($note = null){

		//Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
		$this->title = $note["title"];
		$this->content = $note["content"];
		$this->setStatus($status);
        $this->setCreated($created);
		$this->modified = $modified;
		
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
    public function getTitle()
    {
        return $this->title;
    }

    /**
     * @param mixed $title
     *
     * @return self
     */
    public function setTitle($title)
    {
        $this->title = $title;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getContent()
    {
        return $this->content;
    }

    /**
     * @param mixed $content
     *
     * @return self
     */
    public function setContent($content)
    {
        $this->content = $content;

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
        $this->status = 0;

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


     //Función guardar 
    public function create(){
        $sql="INSERT INTO notes(title,content,status,created) VALUES('$this->title','$this->content','$this->status','$this->created')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();

        //Cerramos la consulta y la conexión 
        $this->con->close(); 
        return 1;
    }
}
?>