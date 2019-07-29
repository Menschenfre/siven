<?php $page = "Register";?>
<?php include '/home2/sivenati/public_html/View/Includes/header.php' ?>

<body>
<div class="container-scroller">
<div class="container-fluid page-body-wrapper full-page-wrapper auth-page">
    <div class="content-wrapper d-flex align-items-center auth auth-bg-1 theme-one">
        <div class="row w-100">
            <div class="col-lg-4 mx-auto">

                <div class="grid-margin stretch-card">
                    <div class="card">
                        <div class="card-body">
                            <h4 class="card-title">Registro de usuario</h4>
                      
                            <div class="form-group">
                                <label>Nombre</label>
                                <input type="text" id="user_name" class="form-control form-control-lg" placeholder="Nombre" aria-label="Nombre">
                            </div>

                            <div class="form-group">
                                <label>Usuario</label>
                                <input type="text" id="nick_name" class="form-control form-control-lg" placeholder="Usuario" aria-label="Nombre">
                            </div>

                            <div class="form-group">
                                <label>Contraseña</label>
                                <input type="password" id="password" class="form-control form-control-lg" placeholder="Contraseña" aria-label="Nombre">
                            </div>

                            <div class="form-group">
                                <label for="exampleFormControlSelect1">Rol</label>
                                <select class="form-control form-control-lg" id="role">
                                    <?php foreach ($result as $key) {?>
                                        <option value=<?php echo $key[ "id"]?>>
                                            <?php echo $key["name"] ?>
                                        </option>
                                        <?php }  ?>

                                </select>
                            </div>

                            <button class="btn btn-outline-primary" onclick="user_reg('register')">Registro</button>
                        </div>
                    </div>
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

</body>


<script type="text/javascript">
  
function user_reg(identifier){

  //Capturamos las id de los input
  var user_name = $("#user_name").val();
  var nick_name = $("#nick_name").val();
  var password = $("#password").val();
  var role = $("#role").val();

  var user = {"user_name":user_name, "nick_name":nick_name, "password":password, "role":role};

  alert(JSON.stringify(user, null, 4));

  $.ajax({
      //datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente
          data:  {"user":user, "identifier":identifier}, 
          url:   '/View/Access/index.php', //archivo que recibe la peticion
          type:  'post', //método de envio
          beforeSend: function () {
              alert("Enviando data...");
                  //$("#resultado").html("Procesando, espere por favor...");
          },
          //response es lo primero que se retorna en el controller
          success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve

        //Si el controlador retorna un positivo se devuelve mensaje exitoso 
              if(response){
              	  //alert(JSON.stringify(response));
                  alert("Llega la data");
                  alert(response);
                  //window.location = "/admin";

              }else{
                alert("No llega la data, fail");
              }
                  
          }
  });
}
</script>

</html>