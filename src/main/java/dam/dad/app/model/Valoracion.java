package dam.dad.app.model;

import java.time.LocalDateTime;

public class Valoracion {
    private int id;
    private int usuarioId;
    private int tallerId;
    private int puntuacion;
    private String comentario;
    private LocalDateTime fecha;
    
    public Valoracion() {
    }
    
    public Valoracion(int id, int usuarioId, int tallerId, int puntuacion, String comentario, LocalDateTime fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tallerId = tallerId;
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getTallerId() {
        return tallerId;
    }

    public void setTallerId(int tallerId) {
        this.tallerId = tallerId;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Valoración: " + puntuacion + "/5 - " + comentario;
    }
} 