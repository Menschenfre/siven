<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
<title>reporte</title>
<link rel="shortcut icon" type="image/x-icon" href="imagenes/favicon.ico" />
<style type="text/css">
	.todo{
width: 100%;
margin-top: 100px;
}

.i{
	width: 35%;
	float: left;

}
.ccc{
	width: 35%;
	float: left;	
}
.d{
	width: 30%;
	float: left;
}

.redonda{
	border-radius: 144px 144px 144px 144px;
-moz-border-radius: 144px 144px 144px 144px;
-webkit-border-radius: 144px 144px 144px 144px;
}	
</style>
</head>
<body>
<header>
<h1 align="center">Reporte de productos!</h1>
<p class="inicio"><a href="index.php"><span class="icon-forward"></span>Inicio</a></p>
</header>
<div class="todo">
<div class="i"><img class="redonda" style="width:200px;height:200px;" src="imagenes/pdf.png"><a href="reporte_pdf.php"> <span class="icon-printer"></span> Descarga en pdf</a></div>
<div class="ccc"><img class="redonda" style="width:200px;height:200px;" src="imagenes/word.png"><a href="reporte_word.php"> <span class="icon-printer"></span> Descarga en word</a></div>
<div class="d"><img class="redonda" style="width:200px;height:200px;" src="imagenes/excel.png"><a href="reporte_excel.php"> <span class="icon-printer"></span> Descarga en excel</a></div>	
</div>
</body>
</html>