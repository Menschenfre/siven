<?php 
$controlador=array(
	'usuario'=>['guardar']
);

if (array_key_exists($controller,  $controlador)) {
	if (in_array($action, $controlador[$controller])) {
		call($controller, $action);
	}
	else{
		call('usuario','error');
	}		
}else{
	call('usuario','error');
}

function call($controller, $action){
	require_once('Controlador/'.$controller.'Control.php');

	switch ($controller) {
		case 'usuario':
		require_once('Modelo/Usuario.php');
		$controller= new UsuarioControl();
		break;			
		default:
				# code...
		break;
	}
	$controller->{$action}();
}

?>