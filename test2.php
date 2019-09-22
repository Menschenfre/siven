<?php 
echo "works";
$table = "music";
$parameters = "id_user,name,category,url";
$values = "'this->id_user','this->name','this->category','this->urle'";

$array = array("id", "id_solar", "name", "desc");

echo $array[0];

foreach($array as $value){
  echo "Valores: $value<br>";
}

$sql="nada $table($parameters)";

echo $sql;

$sql2="caca($values)";

echo $sql2;
 ?>