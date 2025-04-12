package dam.dad.app.model;

public class MantenimientoDefault {
    private int id;
    private String tipo;
    private String descripcion;
    private int intervaloKm;
    private String criticidad;
    
    public MantenimientoDefault() {
    }
    
    public MantenimientoDefault(int id, String tipo, String descripcion, int intervaloKm, String criticidad) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.intervaloKm = intervaloKm;
        this.criticidad = criticidad;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public int getIntervaloKm() {
        return intervaloKm;
    }
    
    public void setIntervaloKm(int intervaloKm) {
        this.intervaloKm = intervaloKm;
    }
    
    public String getCriticidad() {
        return criticidad;
    }
    
    public void setCriticidad(String criticidad) {
        this.criticidad = criticidad;
    }
    
    @Override
    public String toString() {
        return tipo;
    }
} 