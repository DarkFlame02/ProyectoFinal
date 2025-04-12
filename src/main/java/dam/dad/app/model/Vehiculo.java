package dam.dad.app.model;

import java.util.ArrayList;
import java.util.List;

public class Vehiculo {
    private int id;
    private int usuarioId;
    private String marca;
    private String modelo;
    private String matricula;
    private int anio;
    private int kilometros;
    private int kmMensuales;
    private List<Reparacion> reparaciones;
    
    public Vehiculo() {
        this.reparaciones = new ArrayList<>();
    }
    
    public Vehiculo(int id, int usuarioId, String marca, String modelo, String matricula, int anio, int kilometros, int kmMensuales) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.marca = marca;
        this.modelo = modelo;
        this.matricula = matricula;
        this.anio = anio;
        this.kilometros = kilometros;
        this.kmMensuales = kmMensuales;
        this.reparaciones = new ArrayList<>();
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getKilometros() {
        return kilometros;
    }

    public void setKilometros(int kilometros) {
        this.kilometros = kilometros;
    }
    
    public int getKmMensuales() {
        return kmMensuales;
    }
    
    public void setKmMensuales(int kmMensuales) {
        this.kmMensuales = kmMensuales;
    }

    public List<Reparacion> getReparaciones() {
        return reparaciones;
    }

    public void setReparaciones(List<Reparacion> reparaciones) {
        this.reparaciones = reparaciones;
    }
    
    public void addReparacion(Reparacion reparacion) {
        this.reparaciones.add(reparacion);
    }

    @Override
    public String toString() {
        return marca + " " + modelo + " (" + matricula + ")";
    }
} 