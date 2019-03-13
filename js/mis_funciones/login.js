function validaForm(){	
	var username = $("#user").val();
	var password = $("#pass").val();
	$.getJSON("data/termonuclear.json", function(respuesta){
		data = respuesta;
		$.each(data.usuarios, function(index, data) {
	  	if(data.user == username && data.pass == password) {
	  		alert("Bienvenido");
	  		//Se inicializa la cookie con user+id
	  		document.cookie="cookie="+data.user+data.id
	  		//Se redirige
	    }else{
	    	//loginAparicion();
	    }
 	 	});
		if (get_cookie("cookie")==""){
			alert("Usuario terrible inválido");
		}else{
			//window.location.href = "vistas/login";
			nadaAparicion();
		}
	});
};