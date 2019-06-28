<?php $page = "Login";?>
<?php include '/home2/sivenati/public_html/View/Includes/header.php' ?>
<?php include '/home2/sivenati/public_html/View/Includes/url.php'; ?>
<?php //Llamamos el controlador de login
require_once($controller_login); ?>
<?php $prod_control=new ProductController();
?>

<body>
  <div class="container-scroller">
    <div class="container-fluid page-body-wrapper full-page-wrapper auth-page">
      <div class="content-wrapper d-flex align-items-center auth auth-bg-1 theme-one">
        <div class="row w-100">
          <div class="col-lg-4 mx-auto">
            <div class="auto-form-wrapper">

              <form method="POST">
                <div class="form-group">
                  <label class="label">Usuario</label>
                  <div class="input-group">
                    <input type="text" class="form-control" id="user" placeholder="Usuario o correo">
                    <div class="input-group-append">
                      <span class="input-group-text">
                        <i class="mdi mdi-check-circle-outline"></i>
                      </span>
                    </div>
                  </div>
                </div>
                <div class="form-group">
                  <label class="label">Contraseña</label>
                  <div class="input-group">
                    <input type="password" class="form-control" id="pass" placeholder="*********">
                    <div class="input-group-append">
                      <span class="input-group-text">
                        <i class="mdi mdi-check-circle-outline"></i>
                      </span>
                    </div>
                  </div>
                </div>
                <div class="form-group">
                  <button class="btn btn-primary submit-btn btn-block" onclick="login()">Acceder</button>
                </div>
                <div class="form-group d-flex justify-content-between">
                  <div class="form-check form-check-flat mt-0">
                    <label class="form-check-label">
                      <input type="checkbox" class="form-check-input" checked> Recuérdame
                    </label>
                  </div>
                  <a href="#" class="text-small forgot-password text-black">¿Olvidaste la contraseña?</a>
                </div>
                <div class="text-block text-center my-3">
                  <span class="text-small font-weight-semibold">¿No registrado?</span>
                  <a href="register.html" class="text-black text-small">Crear un usuario</a>
                </div>
              </form>
            </div>
            <ul class="auth-footer">
              <li>
                <a href="#">Condiciones</a>
              </li>
              <li>
                <a href="#">Ayuda</a>
              </li>
              <li>
                <a href="#">Terminos</a>
              </li>
            </ul>
            <p class="footer-text text-center">copyright © 2019 Sivenatico. Derechos e izquierdos reservados.</p>
          </div>
        </div>
      </div>
      <!-- content-wrapper ends -->
    </div>
    <!-- page-body-wrapper ends -->
  </div>
  <!-- container-scroller -->
  <!-- plugins:js -->

  <!-- endinject -->
  <?php include $js ?>

  <script>/*
    $("#loginbtn").click(function(){
    var data;
    var especifico;
    $.getJSON("data/termonuclear.json", function(respuesta){
      data = respuesta;
      $.each(data.usuarios, function(index, data) {
        if(data.id == "1"){
        alert(data.id);
      }
      });
    });
  });*/
  function login(){

    //Capturamos las id de los input
        var username = $("#user").val();
    var password = $("#pass").val();

        $.ajax({
            //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
                data:  {"username":username,"password":password}, 
                url:   '/Controller/LoginController.php', //archivo que recibe la peticion
                type:  'post', //método de envio
                beforeSend: function () {
                    alert("Logeando...");
                        //$("#resultado").html("Procesando, espere por favor...");
                },
                //response es lo primero que se retorna en el controller
                success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve

              //Si el controlador retorna un positivo se devuelve mensaje exitoso 
                    if(response==1){
                        //alert("Sesión activa");
                        //alert("prueba de guardadooo"); 
                        window.location = "/admin";
                        
                    }else{
                      alert("No existe usuario, sesión fallida");
                    }
                        
                }
        });
}
  </script>

  <script type="text/javascript">
  $(document).ready(function(){
    $('#btnguardar').click(function(){
      var datos=$('#frmajax').serialize();
      $.ajax({
        type:"POST",
        url:"insertar.php",
        data:datos,
        success:function(r){
          if(r==1){
            alert("agregado con exito");
          }else{
            alert("Fallo el server");
          }
        }
      });

      return false;
    });
  });
  </script> 
</body>

</html>