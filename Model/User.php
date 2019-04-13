<?php
require_once ('Crud.php');

class User extends Crud{

	private $id;
    private $nick;
    private $pass;
    private $status;
    private $created;
    private $modified;


    /**
     * Class Constructor 
     * @param    $nick   
     * @param    $pass   
     * @param    $status   
     * @param    $created   
     * @param    $modified   
     */
    public function __construct($nick, $pass, $status, $created, $modified){

        //Herencia de constructor padre
        parent::__construct();

        //Constructor de atributos
        $this->nick = $nick;
        $this->pass = $pass;
        $this->setStatus($status);
        $this->setCreated($created);
        $this->modified = $modified;
    }

    /**
     * @return mixed
     */
    public function getNick()
    {
        return $this->nick;
    }

    /**
     * @param mixed $nick
     *
     * @return self
     */
    public function setNick($nick)
    {
        $this->nick = $nick;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getPass()
    {
        return $this->pass;
    }

    /**
     * @param mixed $pass
     *
     * @return self
     */
    public function setPass($pass)
    {
        $this->pass = $pass;

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

    public function save(){
        $sql="INSERT INTO users(nick,pass,status,created) VALUES('$this->nick','$this->pass','$this->status','$this->created')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();
    }

}




?>