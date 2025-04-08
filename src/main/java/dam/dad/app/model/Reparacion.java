package dam.dad.app.model;

import java.time.LocalDate;

public class Reparacion {
    private int id;
    private int vehiculoId;
    private int tallerId;
    private String descripcion;
    private LocalDate fechaReparacion;
    private double costo;
    private String estado;
    
    public Reparacion() {
    }
    
    public Reparacion(int id, int vehiculoId, int tallerId, String descripcion, LocalDate fechaReparacion, 
                      double costo, String estado) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.tallerId = tallerId;
        this.descripcion = descripcion;
        this.fechaReparacion = fechaReparacion;
        this.costo = costo;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(int vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public int getTallerId() {
        return tallerId;
    }

    public void setTallerId(int tallerId) {
        this.tallerId = tallerId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaReparacion() {
        return fechaReparacion;
    }

    public void setFechaReparacion(LocalDate fechaReparacion) {
        this.fechaReparacion = fechaReparacion;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return descripcion + " (" + fechaReparacion + ")";
    }
} 