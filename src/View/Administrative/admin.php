<?php
ob_start();
?>
<?php $page = "Admin";?><?php include '/var/www/html/View/Includes/header.php' ?>




  
<body>

  <div class="container-scroller">

    <!-- TOP NAVBAR -->
    
    <nav class="navbar default-layout col-lg-12 col-12 p-0 fixed-top d-flex flex-row">
      <div class="text-center navbar-brand-wrapper d-flex align-items-top justify-content-center">
        
        
      </div>
      <div class="navbar-menu-wrapper d-flex align-items-center">
        <ul class="navbar-nav navbar-nav-left header-links d-none d-md-flex">
          <li class="nav-item">
            <a href="#" class="nav-link">Calendario
              <span class="badge badge-primary ml-1">Fecha pronta(1)</span>
            </a>
          </li>
          <li class="nav-item active">
            <a href="#" class="nav-link">
              <i class="mdi mdi-elevation-rise"></i>Estadísticas</a>
          </li>
        </ul>
        <ul class="navbar-nav navbar-nav-right">
          <li class="nav-item dropdown">
            <a class="nav-link count-indicator dropdown-toggle" id="messageDropdown" href="#" data-toggle="dropdown" aria-expanded="false">
              <i class="mdi mdi-file-document-box"></i>
              <span class="count">7</span>
            </a>
            <div class="dropdown-menu dropdown-menu-right navbar-dropdown preview-list" aria-labelledby="messageDropdown">
              <div class="dropdown-item">
                <p class="mb-0 font-weight-normal float-left">Tienes 7 tareas pendientes
                </p>
                <span class="badge badge-info badge-pill float-right">Ver todas</span>
              </div>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item preview-item">
                <div class="preview-thumbnail">
                  
                </div>
                <div class="preview-item-content flex-grow">
                  <h6 class="preview-subject ellipsis font-weight-medium text-dark">Reunión
                    <span class="float-right font-weight-light small-text">1 Minutes ago</span>
                  </h6>
                  <p class="font-weight-light small-text">
                    Se cancela la reunión
                  </p>
                </div>
              </a>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item preview-item">
                <div class="preview-thumbnail">
                  
                </div>
                <div class="preview-item-content flex-grow">
                  <h6 class="preview-subject ellipsis font-weight-medium text-dark">Cuenta
                    <span class="float-right font-weight-light small-text">15 Minutes ago</span>
                  </h6>
                  <p class="font-weight-light small-text">
                    Pendiente el registro de cuentas (2)
                  </p>
                </div>
              </a>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item preview-item">
                <div class="preview-thumbnail">
                  
                </div>
                <div class="preview-item-content flex-grow">
                  <h6 class="preview-subject ellipsis font-weight-medium text-dark"> Johnson
                    <span class="float-right font-weight-light small-text">18 Minutes ago</span>
                  </h6>
                  <p class="font-weight-light small-text">
                    Upcoming board meeting
                  </p>
                </div>
              </a>
            </div>
          </li>
          <li class="nav-item dropdown">
            <a class="nav-link count-indicator dropdown-toggle" id="notificationDropdown" href="#" data-toggle="dropdown">
              <i class="mdi mdi-bell"></i>
              <span class="count">4</span>
            </a>
            <div class="dropdown-menu dropdown-menu-right navbar-dropdown preview-list" aria-labelledby="notificationDropdown">
              <a class="dropdown-item">
                <p class="mb-0 font-weight-normal float-left">Tienes 4 notificaciones
                </p>
                <span class="badge badge-pill badge-warning float-right">Ver todas</span>
              </a>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item preview-item">
                <div class="preview-thumbnail">
                  <div class="preview-icon bg-success">
                    <i class="mdi mdi-alert-circle-outline mx-0"></i>
                  </div>
                </div>
                <div class="preview-item-content">
                  <h6 class="preview-subject font-weight-medium text-dark">Error procedimiento BD</h6>
                  <p class="font-weight-light small-text">
                    Just now
                  </p>
                </div>
              </a>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item preview-item">
                <div class="preview-thumbnail">
                  <div class="preview-icon bg-warning">
                    <i class="mdi mdi-comment-text-outline mx-0"></i>
                  </div>
                </div>
                <div class="preview-item-content">
                  <h6 class="preview-subject font-weight-medium text-dark">Intentos fallidos</h6>
                  <p class="font-weight-light small-text">
                    Hace 3 horas 
                  </p>
                </div>
              </a>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item preview-item">
                <div class="preview-thumbnail">
                  <div class="preview-icon bg-info">
                    <i class="mdi mdi-email-outline mx-0"></i>
                  </div>
                </div>
                <div class="preview-item-content">
                  <h6 class="preview-subject font-weight-medium text-dark">Nuevos correos(2)</h6>
                  <p class="font-weight-light small-text">
                    2 days ago
                  </p>
                </div>
              </a>
            </div>
          </li>
          <li class="nav-item dropdown d-none d-xl-inline-block">
            <a class="nav-link dropdown-toggle" id="UserDropdown" href="#" data-toggle="dropdown" aria-expanded="false">
              <span class="profile-text">Cuenta</span>
              
            </a>
            <div class="dropdown-menu dropdown-menu-right navbar-dropdown" aria-labelledby="UserDropdown">
              <a class="dropdown-item p-0">
                <div class="d-flex border-bottom">
                  <div class="py-3 px-4 d-flex align-items-center justify-content-center">
                    <i class="mdi mdi-bookmark-plus-outline mr-0 text-gray"></i>
                  </div>
                  <div class="py-3 px-4 d-flex align-items-center justify-content-center border-left border-right">
                    <i class="mdi mdi-account-outline mr-0 text-gray"></i>
                  </div>
                  <div class="py-3 px-4 d-flex align-items-center justify-content-center">
                    <i class="mdi mdi-alarm-check mr-0 text-gray"></i>
                  </div>
                </div>
              </a>
              <a class="dropdown-item mt-2">
                Administrar cuenta
              </a>
              <a class="dropdown-item">
                Cambiar contraseña
              </a>
              
              <a class="dropdown-item" href="#" onclick="logout('logout')">
                Salir 
              </a>
            </div>
          </li>
        </ul>
        <button class="navbar-toggler navbar-toggler-right d-lg-none align-self-center" type="button" data-toggle="offcanvas">
          <span class="mdi mdi-menu"></span>
        </button>
      </div>
    </nav> 

    <!-- TOP NAVBAR -->



    <!-- partial -->
    <div class="container-fluid page-body-wrapper">

      <!--MENU Y PARTE IZQUIERDA -->

      <!-- partial:partials/_sidebar.html -->
      <nav class="sidebar sidebar-offcanvas" id="sidebar">
        <ul class="nav">
          <li class="nav-item nav-profile">
            <div class="nav-link">
              <div class="user-wrapper">
                <div class="profile-image">
                  <img src="<?php echo $assets_images ?>face9.jpg" alt="profile image">
                </div>
                <div class="text-wrapper">
                  <p class="profile-name">Sivenatico</p>
                  <div>
                    <small class="designation text-muted">New</small>
                    <span class="status-indicator online"></span>
                  </div>
                </div>
              </div>
              <button class="btn btn-success btn-block">Registrar cuenta
                <i class="mdi mdi-plus"></i>
              </button>
            </div>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/admin">
              <i class="menu-icon mdi mdi-television"></i>
              <span class="menu-title">Dashboard</span>
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="https://www.bootstrapdash.com/demo/star-admin-angular/dashboard">
              <i class="menu-icon mdi mdi-television"></i>
              <span class="menu-title">Templato</span>
            </a>
          </li>

 <!--Sección del menú notas -->
          <li class="nav-item">
            <a class="nav-link" data-toggle="collapse" href="#ui-basic3" aria-expanded="false" aria-controls="ui-basic">
              <i class="menu-icon mdi mdi-content-copy"></i>
              <span class="menu-title">Notas</span>
              <i class="menu-arrow"></i>
            </a>
            <div class="collapse" id="ui-basic3">
              <ul class="nav flex-column sub-menu">
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="addNoteAppears()">
                  Registrar 
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="list_noteAppears()">Lista de notas</a>
                </li>
              </ul>
            </div>
          </li>

 <!--Sección del menú historias -->
          <li class="nav-item">
            <a class="nav-link" data-toggle="collapse" href="#story_menu" aria-expanded="false" aria-controls="ui-basic">
              <i class="menu-icon mdi mdi-content-copy"></i>
              <span class="menu-title">Libros</span>
              <i class="menu-arrow"></i>
            </a>
            <div class="collapse" id="story_menu">
              <ul class="nav flex-column sub-menu">
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="addStoryAppears()">
                  Registrar libros 
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="list_storyAppears()">Listar libros</a>
                </li>
              </ul>
            </div>
          </li> 

          <li class="nav-item">
            <a class="nav-link" data-toggle="collapse" href="#ui-basic" aria-expanded="false" aria-controls="ui-basic">
              <i class="menu-icon mdi mdi-content-copy"></i>
              <span class="menu-title">Musick</span>
              <i class="menu-arrow"></i>
            </a>
            <div class="collapse" id="ui-basic">
              <ul class="nav flex-column sub-menu">
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="add_musicAppears()">Agregar</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="list_musicAppears()">Listados</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="playlistAppears(1)">Lista 1</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="playlistAppears(2)">Lista 2</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="playlistAppears(3)">Lista 3</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="playlistAppears(4)">Lista 4</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="playlistAppears(5)">Lista 5</a>
                </li>
              </ul>
            </div>
          </li>
          
          <li class="nav-item">
            <a class="nav-link" data-toggle="collapse" href="#ui-basic2" aria-expanded="false" aria-controls="ui-basic">
              <i class="menu-icon mdi mdi-content-copy"></i>
              <span class="menu-title">Cuentas</span>
              <i class="menu-arrow"></i>
            </a>
            <div class="collapse" id="ui-basic2">
              <ul class="nav flex-column sub-menu">
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="addProductAppears()">
                  Registrar 
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="list_productAppears()">Tabla de registros</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="#" onclick="statisticsAppears()">
                  Estadísticas 
                  </a>
                </li>
              </ul>
            </div>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#" onclick="logout('logout')">
              <i class="menu-icon mdi mdi-chart-line"></i>
              <span class="menu-title">Cerrar sesión</span>
            </a> 
          </li>





        </ul>
      </nav>

      <!-- partial -->
      <div class="main-panel">
          

        <div class="content-wrapper" id= "admin_content">
          <div class="row" id="dashboard_content">
            <div class="col-xl-3 col-lg-3 col-md-3 col-sm-6 grid-margin stretch-card">
              <div class="card card-statistics" onclick="list_productAppears()">
                <div class="card-body">
                  <div class="clearfix">
                    <div class="float-left">
                      <i class="mdi mdi-cube text-danger icon-lg"></i>
                    </div>
                    <div class="float-right">
                      <p class="mb-0 text-right" onclick="list_productAppears()">Resumen</p>
                      <div class="fluid-container">
                        <h3 class="font-weight-medium text-right mb-0">$65,650</h3>
                      </div>
                    </div>
                  </div>
                  <p class="text-muted mt-3 mb-0">
                    <i class="mdi mdi-alert-octagon mr-1" aria-hidden="true"></i> 65% lower growth
                  </p>
                </div>
              </div>
            </div>
            <div class="col-xl-3 col-lg-3 col-md-3 col-sm-6 grid-margin stretch-card">
              <div class="card card-statistics" onclick="addProductAppears()">
                <div class="card-body">
                  <div class="clearfix">
                    <div class="float-left">
                      <i class="mdi mdi-receipt text-warning icon-lg"></i>
                    </div>
                    <div class="float-right">
                      <p class="mb-0 text-right">Registro</p>
                      <div class="fluid-container">
                        <h3 class="font-weight-medium text-right mb-0">3455</h3>
                      </div>
                    </div>
                  </div>
                  <p class="text-muted mt-3 mb-0">
                    <i class="mdi mdi-bookmark-outline mr-1" aria-hidden="true"></i> Notas, recordatorios
                  </p>
                </div>
              </div>
            </div>

            <div class="col-xl-3 col-lg-3 col-md-3 col-sm-6 grid-margin stretch-card">
              <div class="card card-statistics" onclick="addNoteAppears()">
                <div class="card-body">
                  <div class="clearfix">
                    <div class="float-left">
                      <i class="mdi mdi-receipt text-warning icon-lg"></i>
                    </div>
                    <div class="float-right">
                      <p class="mb-0 text-right">Pendientes</p>
                      <div class="fluid-container">
                        <h3 class="font-weight-medium text-right mb-0">3455</h3>
                      </div>
                    </div>
                  </div>
                  <p class="text-muted mt-3 mb-0">
                    <i class="mdi mdi-bookmark-outline mr-1" aria-hidden="true"></i> Notas, recordatorios
                  </p>
                </div>
              </div>
            </div> 
            
            <div class="col-xl-3 col-lg-3 col-md-3 col-sm-6 grid-margin stretch-card">
              <div class="card card-statistics">
                <div class="card-body">
                  <div class="clearfix">
                    <div class="float-left">
                      <i class="mdi mdi-account-location text-info icon-lg"></i>
                    </div>
                    <div class="float-right">
                      <p class="mb-0 text-right">Employees</p>
                      <div class="fluid-container">
                        <h3 class="font-weight-medium text-right mb-0">246</h3>
                      </div>
                    </div>
                  </div>
                  <p class="text-muted mt-3 mb-0">
                    <i class="mdi mdi-reload mr-1" aria-hidden="true"></i> Product-wise sales
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        
        <!-- content-wrapper ends -->
        <!-- partial:partials/_footer.html -->

        <footer class="footer">
          <div class="container-fluid clearfix">
            <span class="text-muted d-block text-center text-sm-left d-sm-inline-block">Copyright © 2019
              <a href="http://www.bootstrapdash.com/" target="_blank">Sickven</a>. Derechos reservados.</span>
            <span class="float-none float-sm-right d-block mt-1 mt-sm-0 text-center">Hecho con
              <i class="mdi mdi-heart text-danger"></i>
            </span>
          </div>
        </footer>
        <!-- partial -->
      </div>
      <!-- main-panel ends -->
    </div>
    <!-- page-body-wrapper ends -->
  </div>
  <!-- container-scroller -->


  

  <!-- plugins:js -->
  <?php include $js ?>
<script type="text/javascript">

function playlistAppears(lista){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de bills y se muestra dentro del contenido 
  $.ajax({type: "POST", url: "/View/Administrative/Music/playlist.php", data:{"lista":lista}, success: function(result){
      $("#admin_content").html(result);
  }});
};  

function list_productAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de bills y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Bills/list_product.php", success: function(result){
      $("#admin_content").html(result);
      $("#year_select").selectpicker("refresh");
      $("#month_select").selectpicker("refresh");
      $("#category_select").selectpicker("refresh");
  }});
};

function addProductAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de agregar producto y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Bills/add_product.php", success: function(result){
      $("#admin_content").html(result);
  }});
};

function statisticsAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();

  var identifier= 'div';
  $.ajax({
    data: {"identifier":identifier},
    url: "/View/Administrative/Bills/statistics.php",
    type: 'post',
    success: function(result){
      $("#admin_content").html(result); 
  }})

};

function addNoteAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de agregar producto y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Notes/add_note.php", success: function(result){
      $("#admin_content").html(result);
  }});
};

function list_noteAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de bills y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Notes/list_note.php", success: function(result){
      $("#admin_content").html(result);
  }});
};


function addStoryAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de agregar producto y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Stories/add_story.php", success: function(result){
      $("#admin_content").html(result);
  }});
};

function list_storyAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de bills y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Stories/list_story.php", success: function(result){
      $("#admin_content").html(result);
  }});
};

function add_musicAppears(){
  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de bills y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Music/add_music.php", success: function(result){
      $("#admin_content").html(result);
  }});
};

function list_musicAppears(){

  //Ocultamos el contenido actual
  $('#dashboard_content').hide();
  //Se trae el contenido de bills y se muestra dentro del contenido 
  $.ajax({url: "/View/Administrative/Music/list_music.php", success: function(result){
      $("#admin_content").html(result);

  }});
};


function logout(identifier){
        $.ajax({
            /*datos que se envian a traves de ajax, primer valor nombre de la variable, segundo valor del input declarado previamente; en este caso se entregan 3 valores como data, nombre de usuario
            contraseña de usuario y el valor identificador para llamado de funciones
            */
                data:  {"identifier":identifier}, 
                url:   '/View/Access/index.php', //archivo que recibe la peticion
                type:  'post', //método de envio
                /*beforeSend: function () {
                    alert("Saliendo...");
                        
                },*/
                //response es lo primero que se retorna en el controller
                success:  function (response) { //una vez que el archivo recibe el request lo procesa y lo devuelve

              //Si el controlador retorna un positivo se devuelve mensaje exitoso 
                    if(response==1){
                        //alert("Sesión activa");
                        //alert("prueba de guardadooo"); 
                        window.location = "/login";
                        
                    }else{
                      //alert(response);
                      alert("No salida");
                    }
                        
                }
        });
}






</script>






  
  

</body>

</html>
<?php
ob_end_flush();
?>