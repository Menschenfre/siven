<?php
require_once ('Crud.php');


class Usuario extends Crud{

	private $id;
	private $nombre;
	private $nick;
	private $pass;
	private $estado;
	private $creado;
	private $modificado;
	private $eliminado;


	function __construct($nombre){
        parent::__construct();
        $this->nombre=$nombre;
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
    public function getNombre()
    {
        return $this->nombre;
    }

    /**
     * @param mixed $nombre
     *
     * @return self
     */
    public function setNombre($nombre)
    {
        $this->nombre = $nombre;

        return $this;
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
    public function getEstado()
    {
        return $this->estado;
    }

    /**
     * @param mixed $estado
     *
     * @return self
     */
    public function setEstado($estado)
    {
        $this->estado = $estado;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getCreado()
    {
        return $this->creado;
    }

    /**
     * @param mixed $creado
     *
     * @return self
     */
    public function setCreado($creado)
    {
        $this->creado = $creado;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getModificado()
    {
        return $this->modificado;
    }

    /**
     * @param mixed $modificado
     *
     * @return self
     */
    public function setModificado($modificado)
    {
        $this->modificado = $modificado;

        return $this;
    }

    /**
     * @return mixed
     */
    public function getEliminado()
    {
        return $this->eliminado;
    }

    /**
     * @param mixed $eliminado
     *
     * @return self
     */
    public function setEliminado($eliminado)
    {
        $this->eliminado = $eliminado;

        return $this;
    }



    public function guardar($nombre){

		$sql="INSERT INTO usuarios(nombre,nick,pass,creado) VALUES('test2','test','test','31-03-2019')";
        $resultado=$this->con->prepare($sql);
        $re=$resultado->execute();
	}




}




?>