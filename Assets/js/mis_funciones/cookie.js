
//var pass= get_cookie("pass");
//var code= get_cookie("cookie");


/* Se obtiene la cookie */
function get_cookie(cookieSession) {
	var search = cookieSession + "="
	var returnvalue = "";
	if (document.cookie.length > 0) {
    	offset = document.cookie.indexOf(search)
    	// Si la cookie existe
    	if (offset != -1) { 
       	 offset += search.length
        // set index of beginning of value
        // Setea el index al inicio del valor
         end = document.cookie.indexOf(";", offset);
        // set index of end of cookie value
        if (end == -1) end = document.cookie.length;
        returnvalue=unescape(document.cookie.substring(offset, end))
    }
}
return returnvalue;
}

/*Sesión activa lógica*/

function kill_cookie(){
    //Se mata la cookie solo con el user
    document.cookie="nick="+';expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    //Se mata la cookie solo con la pass
    document.cookie="pass="+';expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    //Se mata la cookie con user+id
    document.cookie="cookie="+';expires=Thu, 01 Jan 1970 00:00:01 GMT;';

    //Reload para reflejar cambios en la web
    location.reload();
}





//IF que inicia lo guardado en la cookie en la sesión actual
	//if (get_cookie("cookie")!="")
    //alert(get_cookie("cookie"));
