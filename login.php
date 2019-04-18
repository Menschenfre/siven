<!DOCTYPE html>
<?php
$page = 'login';
?>

<html lang="en">
<head>
	<title>Login</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
<!--===============================================================================================-->	
	<link rel="icon" type="image/png" href="favicon.ico"/>
<!--=======================================Estilos CSS==============================================-->
<?php include 'View/Includes/css.php' ?>
</head>
<body>
	
	<div class="limiter">
		<div class="container-login100">
			<div class="wrap-login100">
				<div class="login100-pic js-tilt" data-tilt>
					<img src="Assets/images/img-01.png" alt="IMG">
				</div>

				<form class="login100-form validate-form">
					<span class="login100-form-title">
						Login
					</span>

					<div class="wrap-input100">
						<input class="input100" type="text" name="email" placeholder="Usuario o email">
						<span class="focus-input100"></span>
						<span class="symbol-input100">
							<i class="fa fa-envelope" aria-hidden="true"></i>
						</span>
					</div>

					<div class="wrap-input100">
						<input class="input100" type="password" name="pass" placeholder="Contraseña">
						<span class="focus-input100"></span>
						<span class="symbol-input100">
							<i class="fa fa-lock" aria-hidden="true"></i>
						</span>
					</div>
					
					<div class="container-login100-form-btn">
						<button class="login100-form-btn" id="loginbtn">
							Ingresar
						</button>
					</div>

					<div class="text-center p-t-136">
						<a class="txt2" href="#">
							Crear cuenta
							<i class="fa fa-long-arrow-right m-l-5" aria-hidden="true"></i>
						</a>
					</div>
				</form>
			</div>
		</div>
	</div>
	
	
<!--===============================================================================================-->	
<?php include 'View/Includes/js.php' ?>	

<!--===============================================================================================-->	
	<script >
		$('.js-tilt').tilt({
			scale: 1.1
		})
	</script>
	<script>/*
		$("#loginbtn").click(function(){
		var data;
		var especifico;
 		$.getJSON("data/termonuclear.json", function(respuesta){
 			data = respuesta;
 			$.each(data.usuarios, function(index, data) {
 		  	if(data.id == "1"){
 		  	alert(data.id);
 		  }
			});
		});
	});*/
	</script>


</body>
</html>