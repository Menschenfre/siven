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

//Se Establece en nombre de la cookie con los valores otorgados
    function setcookie(session){
    	document.cookie="cookie="+session
	}

//IF que inicia lo guardado en la cookie en la sesión actual
	//if (get_cookie("cookie")!="")
    //alert(get_cookie("cookie"));