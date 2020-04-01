<?php
require_once('../Model/Story.php');
$a="jamaica";
$b="localeconv";
$c=1;
$d="";
$e="";
$story=new Story($a,$b,$c,$d,$e);
$reg=$story->save();
if ($reg) {
	echo "bien";
}else{
    echo "fallo"; 
}  

?>