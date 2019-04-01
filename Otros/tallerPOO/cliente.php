<?php

require_once ('modelo.php');

class cliente extends modelo{
protected $codigo;	
protected $nombre;
protected $sexo;
protected $edad;
protected $deuda;

public	function __construct(){
    parent::__construct();
}

public function registro($nombre,$sexo,$edad,$deuda){

$sql="INSERT INTO clientes(nombre,sexoo,edad,deuda) VALUES('".$nombre."','".$sexo."',$edad,$deuda)";
$resultado=$this->_db->prepare($sql);
$re=$resultado->execute();
	if (!$re) {
	echo "fallo el ingreso de datos ";
}else{
	return $re;
	$re->close();
	$this->_db->close();
}
}

public function buscar($codigo){

$sql="SELECT *FROM clientes WHERE codigo='".$codigo."'";
$busca=$this->_db->query($sql);
$respuesta=$busca->fetch_all(MYSQLI_ASSOC);
	if ($respuesta) {
    
 return $respuesta;
	$respuesta->close();
	$this->_db->close();
}
}


public function eliminar($codigo){

 $sql="DELETE FROM clientes where codigo='".$codigo."'";
 $elimina=$this->_db->prepare($sql);
 $re=$elimina->execute();
 if (!$re) {
	echo "fallo la eliminacion de datos ";
}else{
	return $re;
	$re->close();
	$this->_db->close();
}
}

public function modificar($codigo,$nombre,$sexo,$edad,$deuda){

	$sql="UPDATE clientes SET nombre='".$nombre."',sexoo='".$sexo."',edad=$edad,deuda=$deuda WHERE codigo='".$codigo."'";
   $modifica=$this->_db->query($sql);

  if(!$modifica){
   echo "fallo la modificacion de datos";
  }else{
  	return $modifica;
	$modifica->close();
	$this->_db->close();
  } 	
}


public function listar(){

$registros = 7; 
@$pagina = $_GET ['pagina']; 
if (!isset($pagina)) { 
$pagina = 1; 
$inicio = 0; 
}else { 
$inicio = ($pagina-1) * $registros; 
}
$total_registros=$this->paginacion(); 
$total_paginas = ceil($total_registros / $registros); 
$sql="SELECT*FROM clientes order by codigo ASC LIMIT ".$inicio." , ".$registros." ";
$resultado=$this->_db->query($sql);
$usuarios=$resultado->fetch_all(MYSQLI_ASSOC);
if ($usuarios) {
 return $usuarios;
$usuarios->close();
$this->_db->close();
}
}


public function paginacion(){
  $sql="SELECT*FROM clientes";
  $resultado=$this->_db->query($sql);
  $respuesta=$resultado->num_rows;
  return $respuesta;
  $respuesta->close();
  $this->_db->close();
}



public function suma(){

$sql="SELECT sum(deuda) as suma from clientes";
$suma=$this->_db->query($sql);
$respuesta=$suma->fetch_all(MYSQLI_ASSOC);
foreach ($respuesta as $row ) {
$suma=$row['suma'];
$cambio=number_format($suma,2);	
 return $cambio;
$cambio->close();
$this->_db->close(); 
}
}

public function cantidad(){
	
$sql="SELECT count(*) as cantidad from clientes";
$cantidad=$this->_db->query($sql);
$respuesta=$cantidad->fetch_all(MYSQLI_ASSOC);
foreach ($respuesta as $row) {
$cant=$row['cantidad'];
return $cant;
$cant->close();
$this->_db->close();	
}
}

}
?>