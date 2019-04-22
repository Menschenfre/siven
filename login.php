<?php
// Start the session
session_start();
var_dump($_SESSION["user"]);
?>

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

				<form class="login100-form validate-form" method="POST">
					<span class="login100-form-title">
						Login
					</span>

					<div class="wrap-input100">
						<input class="input100" type="text" name="user" id="user" placeholder="Usuario o email">
						<span class="focus-input100"></span>
						<span class="symbol-input100">
							<i class="fa fa-envelope" aria-hidden="true"></i>
						</span>
					</div>

					<div class="wrap-input100">
						<input class="input100" type="password" name="pass" id="pass" placeholder="Contraseña">
						<span class="focus-input100"></span>
						<span class="symbol-input100">
							<i class="fa fa-lock" aria-hidden="true"></i>
						</span>
					</div>
					
					<div class="container-login100-form-btn">
						<button class="login100-form-btn" id="loginbtn" onclick="login()">
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
	function login(){

		//Capturamos las id de los input
        var username = $("#user").val();
		var password = $("#pass").val();

        $.ajax({
        		//datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
                data:  {"username":username,"password":password}, 
                url:   'Controller/LoginController.php', //archivo que recibe la peticion
                type:  'post', //método de envio
                beforeSend: function () {
                		alert("Logeando...");
                        //$("#resultado").html("Procesando, espere por favor...");
                },
                //response es lo primero que se retorna en el controller
                success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve

    					//Si el controlador retorna un positivo se devuelve mensaje exitoso	
                		if(response==1){
                		//$("#resultado").html(response);
                        alert("Sesión activa");

                		}else{
                			alert("No existe usuario, sesión fallida");
                		}
                        
                }
        });
}
	</script>

	<script type="text/javascript">
	$(document).ready(function(){
		$('#btnguardar').click(function(){
			var datos=$('#frmajax').serialize();
			$.ajax({
				type:"POST",
				url:"insertar.php",
				data:datos,
				success:function(r){
					if(r==1){
						alert("agregado con exito");
					}else{
						alert("Fallo el server");
					}
				}
			});

			return false;
		});
	});
	</script>




</body>
</html>