<div class="col-md-7 grid-margin stretch-card">
  <div class="card">
    <div class="card-body">
      <h4 class="card-title">Ingreso de notas</h4>
      <p class="card-description">
        Acá se registran notas ideas etc.
      </p>
      
      <div class="form-group">
        <label>Título</label>
        <input type="text" id="product_name" class="form-control form-control-lg" placeholder="Nombre" aria-label="Nombre">
      </div>
      
      <div class="form-group">
        <label>Cantidad</label>
        <input type="text" id="product_total" class="form-control form-control-lg" placeholder="Cantidad" aria-label="Cantidad">
      </div>

      <div class="form-group">
        <label>Precio</label>
        <input type="text" id="product_price" class="form-control " placeholder="Precio" aria-label="Precio">
      </div>

      <div class="form-group">
        <label>Fecha</label>
        <input type="date" id="product_date" class="form-control " placeholder="Fecha" aria-label="Fecha">
      </div>

      <button class="btn btn-outline-primary" onclick="prod_reg()">Enviar</button>
    </div>
  </div>
</div>