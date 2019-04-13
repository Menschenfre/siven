<?php
require_once ('Conn.php');
/**
 * 
 */
class Crud extends Conn{

	protected $dateTimeNow;
	
	function __construct(){
		parent::__construct();
		// MySQL datetime format
		$this->dateTimeNow = new DateTime();
		
		    
	}

	
}


?>