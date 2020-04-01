<?php
require_once ('Crud.php');

class Story extends Crud{

	private $id;
	private $title;
	private $content;
	private $state;
	private $created;
	private $modified;

	/**
	 * Class Constructor
	 * @param    $id   
	 * @param    $title   
	 * @param    $content   
	 * @param    $state   
	 * @param    $created   
	 * @param    $modified   
	 */
	public function __construct($title, $content, $state, $created, $modified){
        //Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
		$this->setTitle($title);
		$this->content = $content;
		$this->state = $state;
		$this->created = $created;
		$this->modified = $modified;
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
        $this->title = 'caca';

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
    public function getState()
    {
        return $this->state;
    }

    /**
     * @param mixed $state
     *
     * @return self
     */
    public function setState($state)
    {
        $this->state = $state;

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


    public function save(){
        var_dump($this->getTitle());
        $sql="INSERT INTO stories(title,content,state,created) VALUES('$this->title','test',1,'31-03-2019')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();
    }

  



}


?>