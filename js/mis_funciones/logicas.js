$(document).ready(function(){

//Lógica música web
    $('#music').click(function(){
    	$('#index').hide();
    	$('#index2').hide();
    });
//Lógica random video
    $('#random').click(function(){
    	$('#index').hide();
    	$('#index2').hide();
    });
//Lógica login
    $('#login').click(function(){
    	$('#index').hide();
    	$('#index2').hide();
    });  

//Lógica Sesión activa login
    //IF que indica el guardado de la cookie

/*Sesión activa cambio de nick menú*/
    if (nick!=""){
        $('#nickChange').text(nick);
    }else{
        $('#nickChange').text("Siven.CL");
    }
      
});






// "nada" carga en contenido index
function nadaAparicion(){
    $.ajax({url: "vistas/nada.html", success: function(result){
        $("#contenido").html(result);
    }});
};

// "login" carga en contenido index
function loginAparicion(){
    $.ajax({url: "vistas/login.html", success: function(result){
        $("#contenido").html(result);
    }});
};

// random videos carga en contenido index
function randomVideosAparicion(){
    $.ajax({url: "vistas/random.html", success: function(result){
        $("#contenido").html(result);
        var videos_collection = [
        "https://www.youtube.com/embed/ioOzsi9aHQQ", 
        "https://www.youtube.com/embed/dQw4w9WgXcQ",
        "https://www.youtube.com/embed/bOsKJpCR9Fo",
        "https://www.youtube.com/embed/71Gt46aX9Z4",
        "https://www.youtube.com/embed/UiHmeHZAc0s",
        "https://www.youtube.com/embed/HzTlB-TjAzM",
        "https://www.youtube.com/embed/yyDUC1LUXSU",
        "https://www.youtube.com/embed/pBkHHoOIIn8",
        "https://www.youtube.com/embed/eAVl2cpFKyQ",
        "https://www.youtube.com/embed/H1KBHFXm2Bg"
        ];
        var playerDiv = document.getElementById("random_player");
        var player = document.createElement("IFRAME");
        var randomVideoUrl = videos_collection[Math.floor(Math.random() * videos_collection.length)];
        player.setAttribute('class', 'embed-responsive-item');
        player.setAttribute('src', randomVideoUrl);
        playerDiv.appendChild(player);
        }});
};

// musica video carga en contenido index
function musicaAparicion(){
    $.ajax({url: "vistas/music.html", success: function(result){
        $("#contenido").html(result);
        }});
}

//Función login click function respaldo

   /* $("#login").click(function(){
        $.ajax({url: "vistas/login.html", success: function(result){
        $("#contenido").html(result);
        }});
    });*/