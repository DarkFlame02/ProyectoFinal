package dam.dad.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private int id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;
    private List<Vehiculo> vehiculos;
    
    public Usuario() {
        this.vehiculos = new ArrayList<>();
    }
    
    public Usuario(int id, String username, String password, String email, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.vehiculos = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(List<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }
    
    public void addVehiculo(Vehiculo vehiculo) {
        this.vehiculos.add(vehiculo);
    }

    @Override
    public String toString() {
        return username;
    }
} 