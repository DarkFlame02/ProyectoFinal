package dam.dad.app.model;

public class Taller {
    private int id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String especialidad;
    private boolean activo;
    private double valoracionPromedio;
    
    public Taller() {
    }
    
    public Taller(int id, String nombre, String direccion, String telefono, String email, 
                 String especialidad, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.especialidad = especialidad;
        this.activo = activo;
        this.valoracionPromedio = 0.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public double getValoracionPromedio() {
        return valoracionPromedio;
    }
    
    public void setValoracionPromedio(double valoracionPromedio) {
        this.valoracionPromedio = valoracionPromedio;
    }

    @Override
    public String toString() {
        return nombre + " - " + especialidad;
    }
} 