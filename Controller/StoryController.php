<?php
require_once('../Model/Story.php');

$story=new Story($title,$content,$state,$created,$modified);
$reg=$story->save();
if ($reg) {
	echo "bien";
}else{
    echo "fallo"; 
}  

?>