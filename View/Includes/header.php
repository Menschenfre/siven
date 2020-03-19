<?php session_start(); ?> 
<?php //Include URL PATH ORIGEN
$MAINPATH= $_SERVER['DOCUMENT_ROOT'];
include '' . $MAINPATH . '/View/Includes/url.php';
?>

<!-- Main head -->
<!DOCTYPE html>
<html lang="en">
<head>
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <!-- Titulo de página obtenido del estático en la página -->
  <title><?php echo $page ?></title>
  <!-- Include CSS -->
  <?php include $css ?>
  <link rel="shortcut icon" href="/favicon.ico" />
</head>