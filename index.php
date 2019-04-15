<!DOCTYPE html>
<?php
$page = 'index';
?>
    <html class="no-js">
	<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<title>Siven</title>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="author" content="Siven" />


	<!-- Place favicon.ico and apple-touch-icon.png in the root directory -->
	<link rel="shortcut icon" href="favicon.ico">

	<link href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700" rel="stylesheet">

<!--=======================================Estilos CSS==============================================-->
<?php include 'View/Includes/css.php' ?>
	
	</head>
	<body>
	<div id="fh5co-page">
		<a href="#" class="js-fh5co-nav-toggle fh5co-nav-toggle"><i></i></a>
		<aside id="fh5co-aside" role="complementary" class="border js-fullheight">

			<h1 id="fh5co-logo"><a id="nickChange" href="https://www.sivenatic.com"></a></h1>
<!-- Menú -->			
			<nav id="fh5co-main-menu" role="navigation">
				<ul>
					<li class="fh5co-active"><a href="https://www.sivenatic.com">Main</a></li>
					<li><a href="#" onclick="musicaAparicion()" id="music">Musicc</a></li>
					<li><a href="#" onclick="randomVideosAparicion()" id="random">IsMagic</a></li>
					<li><a href="#" onclick="loginAparicion()" id="login">AcscesON</a></li>
					<li><a href="#" onclick="kill_cookie()" id="logout">AcscesOFF</a></li>
					<li><a href="#" onclick="nadaAparicion()" id="nada">Nada</a></li>
				</ul>
			</nav>
<!-- Footer -->
			<div class="fh5co-footer">
				<p><small>&copy; 2019 All Rights Reserved.</span> <span>designed by <strong><a class="color-rojito" href="https://siven.cl/" target="_blank">Sivenatic</a></strong></span> </small></p>
				
			</div>

		</aside>

<!-- Contenedor -->
		<div id="fh5co-main">
			<div class="fh5co-narrow-content">
<!-- Contenido cambiante -->				
				<div class="row row-bottom-padded-md" id="contenido">
					<div class="col-md-6 animate-box" data-animate-effect="fadeInLeft" id= "index2">
						<img class="img-responsive" src="Assets/images/wanted.png">
					</div>
					<div class="col-md-6 animate-box" data-animate-effect="fadeInLeft" id="index">
						<h2 class="fh5co-heading">About "Siven"</h2>
						<p>Far far away, behind the word mountains, far from the countries Vokalia and Consonantia, there live the blind texts. Quisque sit amet efficitur nih. Interdum et malesuada fames ac ante ipsum primis in faucibus interda et malesuada parturient.</p>
						<p>Quisque sit amet efficitur nih. Ut oblivioni tradita est bonum modo, sed difficile.</p>
					<!--<?php
					while ($columna = mysqli_fetch_array($resultado))
						{
 							echo $columna['nombre'] . $columna['nick'];
						}
					?>	-->
					</div>
				</div>
			</div>
			<!--<input type="button" value="Go" onClick="setcookie(7)">-->
		</div>
	</div>

<!-- Imports funciones template -->
	<!-- jQuery 3.3.1 (Cargarlo antes que bootstrap) -->
	<script src="Assets/js/jquery/jquery-3.3.1.min.js"></script>
	<!-- Bootstrap -->
	<script src="Assets/js/bootstrap/bootstrap.min.js"></script>
	<!-- Template js -->
	<script src="Assets/js/template_js/main.js"></script>
	<!-- Template js -->
	<script src="Assets/js/sweetalert2/sweetalert2.all.min.js"></script>
	<!-- jquery waypoints (afecta el menu lateral) -->
	<script src="Assets/js/jwaypoints/jquery.waypoints.min.js"></script>

	

	</body>

<!-- Imports funciones propias -->
	<script src="Assets/js/mis_funciones/logicas.js"></script>
	<script src="Assets/js/mis_funciones/login.js"></script>
	<script src="Assets/js/mis_funciones/cookie.js"></script>

	<script>
		/*
		$("#boton").click(function(){
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



</html>

