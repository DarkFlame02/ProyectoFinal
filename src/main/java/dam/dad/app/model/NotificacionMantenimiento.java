package dam.dad.app.model;

import java.time.LocalDate;

public class NotificacionMantenimiento {
    private int id;
    private int vehiculoId;
    private int mantenimientoId;
    private int kmEstimadosRestantes;
    private LocalDate fechaEstimada;
    private String estado;
    private Vehiculo vehiculo;
    private MantenimientoDefault mantenimiento;
    
    public NotificacionMantenimiento() {
    }
    
    public NotificacionMantenimiento(int id, int vehiculoId, int mantenimientoId, int kmEstimadosRestantes, 
                                    LocalDate fechaEstimada, String estado) {
        this.id = id;
        this.vehiculoId = vehiculoId;
        this.mantenimientoId = mantenimientoId;
        this.kmEstimadosRestantes = kmEstimadosRestantes;
        this.fechaEstimada = fechaEstimada;
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
    
    public int getMantenimientoId() {
        return mantenimientoId;
    }
    
    public void setMantenimientoId(int mantenimientoId) {
        this.mantenimientoId = mantenimientoId;
    }
    
    public int getKmEstimadosRestantes() {
        return kmEstimadosRestantes;
    }
    
    public void setKmEstimadosRestantes(int kmEstimadosRestantes) {
        this.kmEstimadosRestantes = kmEstimadosRestantes;
    }
    
    public LocalDate getFechaEstimada() {
        return fechaEstimada;
    }
    
    public void setFechaEstimada(LocalDate fechaEstimada) {
        this.fechaEstimada = fechaEstimada;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Vehiculo getVehiculo() {
        return vehiculo;
    }
    
    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
        if (vehiculo != null) {
            this.vehiculoId = vehiculo.getId();
        }
    }
    
    public MantenimientoDefault getMantenimiento() {
        return mantenimiento;
    }
    
    public void setMantenimiento(MantenimientoDefault mantenimiento) {
        this.mantenimiento = mantenimiento;
        if (mantenimiento != null) {
            this.mantenimientoId = mantenimiento.getId();
        }
    }
    
    @Override
    public String toString() {
        if (mantenimiento != null) {
            return mantenimiento.getTipo() + " - " + (estado != null ? estado : "Pendiente");
        }
        return "Notificación #" + id;
    }
} 