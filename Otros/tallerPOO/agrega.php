<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
	<title>agrega</title>

<style type="text/css">
	input:required:invalid{
		border: 1px solid red;
	}
	input:required:valid {
        border: 1px solid green;
    }

    input:focus{
    	color: blue;
    }
</style>
</head>
<body>
<form action="agregar.php" method="post">
<label>Nombre:</label><input type="text" name="nombre" title="se necesita el nombre"  required><br><br>
<label>Sexo:</label><input type="text" name="sexo" title="se necesita el sexo"  required><br><br>	
<label>Edad:</label><input type="text" name="edad" title="se necesita la edad"  required ><br><br>		
<label>Deuda:</label><input type="text" name="deuda" title="se necesita el monto"  required><br><br>
<button type="submit">Grabar</button>
</form>
</body>
</html>