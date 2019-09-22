<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de música</h4>
      <p class="card-description">
        Acá se registra la música.
      </p>
       
      <div class="form-group">
        <label>Nombre</label>
        <input type="text" id="name" class="form-control form-control-lg" placeholder="Nombre" aria-label="Nombre"> 
      </div>
      <div class="form-group">
        <label>Categoría</label>
        <input type="text" id="category" class="form-control form-control-lg" placeholder="Categoría" aria-label="Nombre"> 
      </div>
      <div class="form-group">
        <label>Url</label>
        <input type="text" id="url" class="form-control form-control-lg" placeholder="Url" aria-label="Nombre"> 
      </div>

      
      <button class="btn btn-outline-primary" onclick="note_reg('add_music')">Enviar</button>
    </div>
  </div>
</div> 