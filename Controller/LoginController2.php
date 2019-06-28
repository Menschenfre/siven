<?php
include '/home2/sivenati/public_html/View/Includes/url.php';

class LoginController2{

	public function __construct(){ 
	}

	function logout(){
		session_start();
		session_destroy();
		header("location:/login");
		exit();
	}

	function works(){
		echo "works!";
	}

}

?>