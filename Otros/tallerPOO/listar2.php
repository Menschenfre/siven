<?php   
require_once "cliente.php"; 
$usuarioModel = new cliente(); 
$reg = $usuarioModel->listar();
if($reg){
?> 
   <!DOCTYPE html> 
   <html lang="es"> 
   <head><meta charset="utf-8"><title>Usuarios registrados</title></head> 
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
        <?php foreach ($reg as $row): ?> 
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
       </table>
     </center>
</body> 
 </html> 
<?php
}else{
echo "<script>alert('no exixste registros!! ')window.location='listar.php';</script>";
}
?>



