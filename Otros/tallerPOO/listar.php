<?php   
require_once "cliente.php"; 
 $registros = 7; 
@$pagina = $_GET ['pagina']; 
if (!isset($pagina)) { 
$pagina = 1; 
$inicio = 0; 
}else { 
$inicio = ($pagina-1) * $registros; 
} 
$usuarioModel = new cliente(); 
$reg = $usuarioModel->listar();
$total_registros=$usuarioModel->paginacion();
$total_paginas = ceil($total_registros / $registros); 

if($reg){
?> 
   <!DOCTYPE html> 
   <html lang="es"> 
   <head> 
   <meta charset="utf-8">
   <link rel="shortcut icon" type="image/x-icon" href="imagenes/favicon.ico" />
    <title>Usuarios registrados</title> 
   </head> 
    <body> 
    <h1 align="center">Lista de los clientes</h1>
    <center>
    <table width="100%" border="1"> 
             <tr>
                <td bgcolor="#9ED0DB" >Nro. 
                <td bgcolor="#9ED0DB">codigo 
                <td bgcolor="#9ED0DB">Nombre
                <td bgcolor="#9ED0DB">sexo     
                <td bgcolor="#9ED0DB">edad  
                <td bgcolor="#9ED0DB">deuda
                     
<?php
            $n=0;
    foreach ($reg as $row):
             $n++;
    if($n%2 == 0){
 ?>

    <tr bgcolor="blue">  
<?php
    }else{
?> 
    <tr bgcolor="blue">
<?php   
    } 
            ?> 
         
            <tr>
                <td><?php echo $n;?> 
                <td><?php echo $row['codigo']; ?>
                <td><?php echo $row['nombre']; ?> 
                <td><?php echo $row['sexoo']; ?>
                <td><?php echo $row['edad']; ?> 
                <td><?php echo $row['deuda']; ?> 
            </tr>
            <?php 
            endforeach 
            ?>  

     </table><br> 
     <?php

if($total_registros>$registros){ 
if(($pagina - 1) > 0) { 
echo "<span><a  href='?pagina=".($pagina-1)."'>&laquo; Anterior</a></span> "; 
} 
// Numero de paginas a mostrar 
$num_paginas=10; 
//limitando las paginas mostradas 
$pagina_intervalo=ceil($num_paginas/2)-1; 
// Calculamos desde que numero de pagina se mostrara 
$pagina_desde=$pagina-$pagina_intervalo; 
$pagina_hasta=$pagina+$pagina_intervalo; 
// Verificar que pagina_desde sea negativo 
if($pagina_desde<1){ // le sumamos la cantidad sobrante para mantener el numero de enlaces mostrados $pagina_hasta-=($pagina_desde-1); $pagina_desde=1; } // Verificar que pagina_hasta no sea mayor que paginas_totales if($pagina_hasta>$total_paginas){ 
$pagina_desde-=($pagina_hasta-$total_paginas); 
$pagina_hasta=$total_paginas; 
if($pagina_desde<1){ 
$pagina_desde=1; 
} 
} 
for ($i=$pagina_desde; $i<=$pagina_hasta; $i++){ 
if ($pagina == $i){ 
echo "<span>".$pagina."</span> "; 
}else{ 
echo "<span><a  href='?pagina=$i'>$i</a></span> "; 
} 
} 
if(($pagina + 1)<=$total_paginas) { 
echo " <span><a href='?pagina=".($pagina+1)."'>Siguiente &raquo;</a></span>"; 
} 
} 
?> 
</center>
<br><br>
<center>
<button><a href="suma.php">Detalles</a></button>
<button><a href="index.php">Menu</a></button>  
</center>
 </body> 
 </html> 
<?php
}else{

echo "<script>
       alert('no exixste registros!! ')
        window.location='listar.php';
        </script>";
}

?>



