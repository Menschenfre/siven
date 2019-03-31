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
<form action="../Controlador/UsuarioControl.php" method="post">
<button type="submit">Grabar</button>
</form>
</body>
</html>