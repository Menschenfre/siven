function validaForm(){	
	var username = $("#user").val();
	var password = $("#pass").val();
	$.getJSON("data/termonuclear.json", function(respuesta){
		data = respuesta;
		$.each(data.usuarios, function(index, data) {
	  	if(data.user == username && data.pass == password) {
	  		alert("Bienvenido");
	  		
	  		//Se inicializa la cookie solo con el nombre	
	  		document.cookie="nick="+data.user
	  		//Se inicializa la cookie solo con la pass
	  		document.cookie="pass="+data.pass
	  		//Se inicializa la cookie con user+id
	  		document.cookie="cookie="+data.user+data.id
	    }else{
	    	//loginAparicion();
	    }
 	 	});
 	 	alert(get_cookie("nick"));
 	 	//Se compara sesión y valores rescatados del formulario login
		if (get_cookie("nick")!=username && get_cookie("pass")!=password){
			alert("Usuario terrible inválido");
		}else{
			if (get_cookie("cookie")=="")
			{ 
				alert("Usuario terrible inválido");
			}else{
				//Se refresca para activar la cookie
	  			location.reload();

			}
			
		}
	});
};