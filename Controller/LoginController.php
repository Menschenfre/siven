<?php

class LoginController{

	public function __construct(){ 
	}

	function logout(){
		//session_start();
		//session_destroy();
		//header("location:/login");
		//exit();
		return 1;
	}

	function works(){
		echo "works!";
	}

}

?> 