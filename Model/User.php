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

    //Inicializamos los atributos nulos para simular un constructor vacío
    public function __construct($nick = null, $pass= null, $status= null, $created= null, $modified= null){

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

        //Cerramos la consulta y la conexión 
        $this->con->close();
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


}




?>