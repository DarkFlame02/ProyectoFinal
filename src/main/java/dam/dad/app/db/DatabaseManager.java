package dam.dad.app.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

import dam.dad.app.model.Vehiculo;
import dam.dad.app.model.Reparacion;
import dam.dad.app.model.Taller;
import dam.dad.app.model.Valoracion;
import dam.dad.app.model.MantenimientoDefault;
import dam.dad.app.model.NotificacionMantenimiento;

public class DatabaseManager {
    private static final String PROPERTIES_FILE = "/database.properties";
    private static DatabaseManager instance;
    private Connection connection;
    private int currentUserId = -1; // Para almacenar el ID del usuario autenticado

    private DatabaseManager() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream(PROPERTIES_FILE));
            
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        String sql = "INSERT INTO usuarios (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, username + "@example.com"); // Email temporal
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateUser(String username, String password) {
        String sql = "SELECT id, password FROM usuarios WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                boolean valid = BCrypt.checkpw(password, hashedPassword);
                if (valid) {
                    currentUserId = rs.getInt("id");
                }
                return valid;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public int getCurrentUserId() {
        return currentUserId;
    }
    
    public void logout() {
        currentUserId = -1;
    }
    
    // MÉTODOS PARA GESTIONAR VEHÍCULOS
    
    public List<Vehiculo> getVehiculosByUsuario() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos WHERE usuario_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Vehiculo vehiculo = new Vehiculo();
                vehiculo.setId(rs.getInt("id"));
                vehiculo.setUsuarioId(rs.getInt("usuario_id"));
                vehiculo.setMarca(rs.getString("marca"));
                vehiculo.setModelo(rs.getString("modelo"));
                vehiculo.setMatricula(rs.getString("matricula"));
                vehiculo.setAnio(rs.getInt("anio"));
                vehiculo.setKilometros(rs.getInt("kilometros"));
                vehiculo.setKmMensuales(rs.getInt("km_mensuales"));
                
                // Cargar reparaciones del vehículo
                vehiculo.setReparaciones(getReparacionesByVehiculo(vehiculo.getId()));
                
                vehiculos.add(vehiculo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return vehiculos;
    }
    
    public Vehiculo getVehiculoById(int vehiculoId) {
        String sql = "SELECT * FROM vehiculos WHERE id = ? AND usuario_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculoId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Vehiculo vehiculo = new Vehiculo();
                vehiculo.setId(rs.getInt("id"));
                vehiculo.setUsuarioId(rs.getInt("usuario_id"));
                vehiculo.setMarca(rs.getString("marca"));
                vehiculo.setModelo(rs.getString("modelo"));
                vehiculo.setMatricula(rs.getString("matricula"));
                vehiculo.setAnio(rs.getInt("anio"));
                vehiculo.setKilometros(rs.getInt("kilometros"));
                vehiculo.setKmMensuales(rs.getInt("km_mensuales"));
                
                // Cargar reparaciones del vehículo
                vehiculo.setReparaciones(getReparacionesByVehiculo(vehiculo.getId()));
                
                return vehiculo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean addVehiculo(Vehiculo vehiculo) {
        String sql = "INSERT INTO vehiculos (usuario_id, marca, modelo, matricula, anio, kilometros, km_mensuales) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, vehiculo.getMarca());
            stmt.setString(3, vehiculo.getModelo());
            stmt.setString(4, vehiculo.getMatricula());
            stmt.setInt(5, vehiculo.getAnio());
            stmt.setInt(6, vehiculo.getKilometros());
            stmt.setInt(7, vehiculo.getKmMensuales());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    vehiculo.setId(generatedKeys.getInt(1));
                    // Generar notificaciones de mantenimiento para el nuevo vehículo
                    generarNotificacionesMantenimiento(vehiculo);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean updateVehiculo(Vehiculo vehiculo) {
        String sql = "UPDATE vehiculos SET marca = ?, modelo = ?, matricula = ?, anio = ?, kilometros = ?, km_mensuales = ? WHERE id = ? AND usuario_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vehiculo.getMarca());
            stmt.setString(2, vehiculo.getModelo());
            stmt.setString(3, vehiculo.getMatricula());
            stmt.setInt(4, vehiculo.getAnio());
            stmt.setInt(5, vehiculo.getKilometros());
            stmt.setInt(6, vehiculo.getKmMensuales());
            stmt.setInt(7, vehiculo.getId());
            stmt.setInt(8, currentUserId);
            
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                // Actualizar notificaciones de mantenimiento
                actualizarNotificacionesMantenimiento(vehiculo);
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean deleteVehiculo(int vehiculoId) {
        // Primero eliminamos todas las reparaciones asociadas
        String sqlDeleteReparaciones = "DELETE FROM reparaciones WHERE vehiculo_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sqlDeleteReparaciones)) {
            stmt.setInt(1, vehiculoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Luego eliminamos el vehículo
        String sqlDeleteVehiculo = "DELETE FROM vehiculos WHERE id = ? AND usuario_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sqlDeleteVehiculo)) {
            stmt.setInt(1, vehiculoId);
            stmt.setInt(2, currentUserId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // MÉTODOS PARA GESTIONAR REPARACIONES
    
    public List<Reparacion> getReparacionesByVehiculo(int vehiculoId) {
        List<Reparacion> reparaciones = new ArrayList<>();
        String sql = "SELECT * FROM reparaciones WHERE vehiculo_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculoId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Reparacion reparacion = new Reparacion();
                reparacion.setId(rs.getInt("id"));
                reparacion.setVehiculoId(rs.getInt("vehiculo_id"));
                reparacion.setTallerId(rs.getInt("taller_id"));
                reparacion.setDescripcion(rs.getString("descripcion"));
                reparacion.setFechaReparacion(rs.getDate("fecha_reparacion").toLocalDate());
                reparacion.setCosto(rs.getDouble("costo"));
                reparacion.setEstado(rs.getString("estado"));
                
                reparaciones.add(reparacion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return reparaciones;
    }
    
    public boolean addReparacion(Reparacion reparacion) {
        String sql = "INSERT INTO reparaciones (vehiculo_id, taller_id, descripcion, fecha_reparacion, costo, estado) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reparacion.getVehiculoId());
            stmt.setInt(2, reparacion.getTallerId());
            stmt.setString(3, reparacion.getDescripcion());
            stmt.setObject(4, reparacion.getFechaReparacion());
            stmt.setDouble(5, reparacion.getCosto());
            stmt.setString(6, reparacion.getEstado());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reparacion.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean updateReparacion(Reparacion reparacion) {
        String sql = "UPDATE reparaciones SET taller_id = ?, descripcion = ?, fecha_reparacion = ?, costo = ?, estado = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reparacion.getTallerId());
            stmt.setString(2, reparacion.getDescripcion());
            stmt.setObject(3, reparacion.getFechaReparacion());
            stmt.setDouble(4, reparacion.getCosto());
            stmt.setString(5, reparacion.getEstado());
            stmt.setInt(6, reparacion.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean deleteReparacion(int reparacionId) {
        String sql = "DELETE FROM reparaciones WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reparacionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // MÉTODOS PARA GESTIONAR TALLERES
    
    public List<Taller> getAllTalleres() {
        List<Taller> talleres = new ArrayList<>();
        String sql = "SELECT * FROM talleres WHERE activo = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Taller taller = new Taller();
                taller.setId(rs.getInt("id"));
                taller.setNombre(rs.getString("nombre"));
                taller.setDireccion(rs.getString("direccion"));
                taller.setTelefono(rs.getString("telefono"));
                taller.setEmail(rs.getString("email"));
                taller.setEspecialidad(rs.getString("especialidad"));
                taller.setActivo(rs.getBoolean("activo"));
                
                // Cargar la valoración media desde la base de datos
                taller.setValoracionMedia(rs.getDouble("valoracion_media"));
                
                talleres.add(taller);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return talleres;
    }
    
    public double calcularValoracionPromedio(int tallerId) {
        String sql = "SELECT AVG(puntuacion) AS promedio FROM valoraciones WHERE taller_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tallerId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("promedio");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }

    public int getLoggedInUserId() {
        return currentUserId;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // MÉTODOS PARA GESTIONAR TALLERES Y VALORACIONES
    
    public List<Valoracion> getValoracionesByTaller(int tallerId) {
        List<Valoracion> valoraciones = new ArrayList<>();
        String sql = "SELECT * FROM valoraciones WHERE taller_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tallerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Valoracion valoracion = new Valoracion();
                valoracion.setId(rs.getInt("id"));
                valoracion.setTallerId(rs.getInt("taller_id"));
                valoracion.setUsuarioId(rs.getInt("usuario_id"));
                valoracion.setPuntuacion(rs.getInt("puntuacion"));
                valoracion.setComentario(rs.getString("comentario"));
                
                valoraciones.add(valoracion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return valoraciones;
    }
    
    public boolean addTaller(Taller taller) {
        String sql = "INSERT INTO talleres (nombre, direccion, telefono) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, taller.getNombre());
            stmt.setString(2, taller.getDireccion());
            stmt.setString(3, taller.getTelefono());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    taller.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean updateTaller(Taller taller) {
        String sql = "UPDATE talleres SET nombre = ?, direccion = ?, telefono = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, taller.getNombre());
            stmt.setString(2, taller.getDireccion());
            stmt.setString(3, taller.getTelefono());
            stmt.setInt(4, taller.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteTaller(int tallerId) {
        // Verificar si hay reparaciones asociadas a este taller
        String checkSql = "SELECT COUNT(*) FROM reparaciones WHERE taller_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, tallerId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Hay reparaciones asociadas, no se puede eliminar
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Eliminar primero las valoraciones asociadas
        String deleteValoracionesSql = "DELETE FROM valoraciones WHERE taller_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteValoracionesSql)) {
            stmt.setInt(1, tallerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Luego eliminar el taller
        String deleteTallerSql = "DELETE FROM talleres WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteTallerSql)) {
            stmt.setInt(1, tallerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addValoracion(Valoracion valoracion) {
        String sql = "INSERT INTO valoraciones (taller_id, usuario_id, puntuacion, comentario) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, valoracion.getTallerId());
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, valoracion.getPuntuacion());
            stmt.setString(4, valoracion.getComentario());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    valoracion.setId(generatedKeys.getInt(1));
                    
                    // Actualizar la valoración media del taller
                    actualizarValoracionMedia(valoracion.getTallerId());
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private void actualizarValoracionMedia(int tallerId) {
        String sql = "UPDATE talleres SET valoracion_media = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            double valoracionMedia = calcularValoracionPromedio(tallerId);
            stmt.setDouble(1, valoracionMedia);
            stmt.setInt(2, tallerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // MÉTODOS PARA GESTIONAR MANTENIMIENTOS PREDETERMINADOS

    public List<MantenimientoDefault> getAllMantenimientosDefault() {
        List<MantenimientoDefault> mantenimientos = new ArrayList<>();
        String sql = "SELECT * FROM mantenimientos_default ORDER BY intervalo_km";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MantenimientoDefault mantenimiento = new MantenimientoDefault();
                mantenimiento.setId(rs.getInt("id"));
                mantenimiento.setTipo(rs.getString("tipo"));
                mantenimiento.setDescripcion(rs.getString("descripcion"));
                mantenimiento.setIntervaloKm(rs.getInt("intervalo_km"));
                mantenimiento.setCriticidad(rs.getString("criticidad"));
                
                mantenimientos.add(mantenimiento);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return mantenimientos;
    }

    public MantenimientoDefault getMantenimientoDefaultById(int mantenimientoId) {
        String sql = "SELECT * FROM mantenimientos_default WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mantenimientoId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                MantenimientoDefault mantenimiento = new MantenimientoDefault();
                mantenimiento.setId(rs.getInt("id"));
                mantenimiento.setTipo(rs.getString("tipo"));
                mantenimiento.setDescripcion(rs.getString("descripcion"));
                mantenimiento.setIntervaloKm(rs.getInt("intervalo_km"));
                mantenimiento.setCriticidad(rs.getString("criticidad"));
                
                return mantenimiento;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // MÉTODOS PARA GESTIONAR NOTIFICACIONES DE MANTENIMIENTO

    public List<NotificacionMantenimiento> getNotificacionesByVehiculo(int vehiculoId) {
        List<NotificacionMantenimiento> notificaciones = new ArrayList<>();
        String sql = "SELECT n.*, m.tipo, m.descripcion, m.intervalo_km, m.criticidad " +
                    "FROM notificaciones_mantenimiento n " +
                    "JOIN mantenimientos_default m ON n.mantenimiento_id = m.id " +
                    "WHERE n.vehiculo_id = ? " +
                    "ORDER BY n.km_estimados_restantes, n.fecha_estimada";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculoId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                NotificacionMantenimiento notificacion = new NotificacionMantenimiento();
                notificacion.setId(rs.getInt("id"));
                notificacion.setVehiculoId(rs.getInt("vehiculo_id"));
                notificacion.setMantenimientoId(rs.getInt("mantenimiento_id"));
                notificacion.setKmEstimadosRestantes(rs.getInt("km_estimados_restantes"));
                
                // Convertir fecha SQL a LocalDate
                Date fechaSql = rs.getDate("fecha_estimada");
                if (fechaSql != null) {
                    notificacion.setFechaEstimada(fechaSql.toLocalDate());
                }
                
                notificacion.setEstado(rs.getString("estado"));
                
                // Cargar el mantenimiento asociado
                MantenimientoDefault mantenimiento = new MantenimientoDefault();
                mantenimiento.setId(rs.getInt("mantenimiento_id"));
                mantenimiento.setTipo(rs.getString("tipo"));
                mantenimiento.setDescripcion(rs.getString("descripcion"));
                mantenimiento.setIntervaloKm(rs.getInt("intervalo_km"));
                mantenimiento.setCriticidad(rs.getString("criticidad"));
                
                notificacion.setMantenimiento(mantenimiento);
                
                notificaciones.add(notificacion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notificaciones;
    }

    public List<NotificacionMantenimiento> getNotificacionesPendientes() {
        List<NotificacionMantenimiento> notificaciones = new ArrayList<>();
        
        try {
            // Verificar si hay tablas de notificaciones y mantenimientos en la base de datos
            // Esta consulta es más compatible con diferentes motores de base de datos
            String sql = "SELECT n.*, m.tipo, m.descripcion, m.intervalo_km, m.criticidad, " +
                        "v.marca, v.modelo, v.matricula, v.anio, v.kilometros, v.km_mensuales " +
                        "FROM notificaciones_mantenimiento n " +
                        "JOIN mantenimientos_default m ON n.mantenimiento_id = m.id " +
                        "JOIN vehiculos v ON n.vehiculo_id = v.id " +
                        "WHERE v.usuario_id = ? AND (n.km_estimados_restantes <= 500 OR n.fecha_estimada <= ?) " +
                        "AND n.estado = 'Pendiente' " +
                        "ORDER BY n.km_estimados_restantes, n.fecha_estimada";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, currentUserId);
                // Calcular la fecha dentro de 30 días
                LocalDate fechaLimite = LocalDate.now().plusDays(30);
                stmt.setDate(2, Date.valueOf(fechaLimite));
                
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    NotificacionMantenimiento notificacion = new NotificacionMantenimiento();
                    notificacion.setId(rs.getInt("id"));
                    notificacion.setVehiculoId(rs.getInt("vehiculo_id"));
                    notificacion.setMantenimientoId(rs.getInt("mantenimiento_id"));
                    notificacion.setKmEstimadosRestantes(rs.getInt("km_estimados_restantes"));
                    
                    // Convertir fecha SQL a LocalDate
                    Date fechaSql = rs.getDate("fecha_estimada");
                    if (fechaSql != null) {
                        notificacion.setFechaEstimada(fechaSql.toLocalDate());
                    }
                    
                    notificacion.setEstado(rs.getString("estado"));
                    
                    // Cargar el mantenimiento asociado
                    MantenimientoDefault mantenimiento = new MantenimientoDefault();
                    mantenimiento.setId(rs.getInt("mantenimiento_id"));
                    mantenimiento.setTipo(rs.getString("tipo"));
                    mantenimiento.setDescripcion(rs.getString("descripcion"));
                    mantenimiento.setIntervaloKm(rs.getInt("intervalo_km"));
                    mantenimiento.setCriticidad(rs.getString("criticidad"));
                    
                    notificacion.setMantenimiento(mantenimiento);
                    
                    // Cargar el vehículo asociado
                    Vehiculo vehiculo = new Vehiculo();
                    vehiculo.setId(rs.getInt("vehiculo_id"));
                    vehiculo.setMarca(rs.getString("marca"));
                    vehiculo.setModelo(rs.getString("modelo"));
                    vehiculo.setMatricula(rs.getString("matricula"));
                    vehiculo.setAnio(rs.getInt("anio"));
                    vehiculo.setKilometros(rs.getInt("kilometros"));
                    vehiculo.setKmMensuales(rs.getInt("km_mensuales"));
                    
                    notificacion.setVehiculo(vehiculo);
                    
                    notificaciones.add(notificacion);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener notificaciones pendientes: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado al obtener notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
        
        return notificaciones;
    }

    public boolean actualizarEstadoNotificacion(int notificacionId, String nuevoEstado) {
        String sql = "UPDATE notificaciones_mantenimiento SET estado = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, notificacionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // Método para generar notificaciones de mantenimiento para un vehículo
    private void generarNotificacionesMantenimiento(Vehiculo vehiculo) {
        try {
            List<MantenimientoDefault> mantenimientos = getAllMantenimientosDefault();
            
            if (mantenimientos.isEmpty()) {
                System.err.println("No hay mantenimientos por defecto configurados");
                return;
            }
            
            for (MantenimientoDefault mantenimiento : mantenimientos) {
                // Calcular km restantes hasta el próximo mantenimiento
                int ultimoMantenimiento = buscarUltimoMantenimiento(vehiculo.getId(), mantenimiento.getTipo());
                int kmRestantes = mantenimiento.getIntervaloKm() - (vehiculo.getKilometros() - ultimoMantenimiento);
                
                // Si los km restantes son negativos, significa que ya se debería haber hecho el mantenimiento
                if (kmRestantes < 0) {
                    kmRestantes = 0;
                }
                
                // Calcular fecha estimada basada en los km mensuales
                LocalDate fechaEstimada = null;
                if (vehiculo.getKmMensuales() > 0) {
                    int mesesEstimados = kmRestantes / vehiculo.getKmMensuales();
                    fechaEstimada = LocalDate.now().plusMonths(mesesEstimados);
                }
                
                // Insertar la notificación
                String sql = "INSERT INTO notificaciones_mantenimiento (vehiculo_id, mantenimiento_id, km_estimados_restantes, fecha_estimada, estado) " +
                            "VALUES (?, ?, ?, ?, 'Pendiente')";
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, vehiculo.getId());
                    stmt.setInt(2, mantenimiento.getId());
                    stmt.setInt(3, kmRestantes);
                    
                    if (fechaEstimada != null) {
                        stmt.setDate(4, Date.valueOf(fechaEstimada));
                    } else {
                        stmt.setNull(4, java.sql.Types.DATE);
                    }
                    
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al generar notificaciones: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado al generar notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para actualizar las notificaciones de mantenimiento de un vehículo
    private void actualizarNotificacionesMantenimiento(Vehiculo vehiculo) {
        try {
            // Primero obtenemos todas las notificaciones existentes del vehículo
            List<NotificacionMantenimiento> notificaciones = getNotificacionesByVehiculo(vehiculo.getId());
            
            // Obtenemos todos los mantenimientos por defecto
            List<MantenimientoDefault> todosMantenimientos = getAllMantenimientosDefault();
            
            // Creamos un conjunto para rastrear los IDs de mantenimientos que ya tienen notificación
            java.util.Set<Integer> mantenimientosConNotificacion = new java.util.HashSet<>();
            
            // Actualizar las notificaciones existentes
            for (NotificacionMantenimiento notificacion : notificaciones) {
                // No actualizamos las notificaciones ya completadas
                if ("Completado".equals(notificacion.getEstado())) {
                    continue;
                }
                
                mantenimientosConNotificacion.add(notificacion.getMantenimientoId());
                
                // Recalcular km restantes
                int ultimoMantenimiento = buscarUltimoMantenimiento(vehiculo.getId(), notificacion.getMantenimiento().getTipo());
                int kmRestantes = notificacion.getMantenimiento().getIntervaloKm() - (vehiculo.getKilometros() - ultimoMantenimiento);
                
                // Si los km restantes son negativos, significa que ya se debería haber hecho el mantenimiento
                if (kmRestantes < 0) {
                    kmRestantes = 0;
                }
                
                // Calcular fecha estimada actualizada basada en los km mensuales
                LocalDate fechaEstimada = null;
                if (vehiculo.getKmMensuales() > 0) {
                    int mesesEstimados = kmRestantes / vehiculo.getKmMensuales();
                    fechaEstimada = LocalDate.now().plusMonths(mesesEstimados);
                }
                
                // Actualizar la notificación
                String sql = "UPDATE notificaciones_mantenimiento SET km_estimados_restantes = ?, fecha_estimada = ? WHERE id = ?";
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, kmRestantes);
                    
                    if (fechaEstimada != null) {
                        stmt.setDate(2, Date.valueOf(fechaEstimada));
                    } else {
                        stmt.setNull(2, java.sql.Types.DATE);
                    }
                    
                    stmt.setInt(3, notificacion.getId());
                    
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
            // Crear notificaciones para mantenimientos que aún no tienen
            for (MantenimientoDefault mantenimiento : todosMantenimientos) {
                // Si este mantenimiento ya tiene una notificación, lo saltamos
                if (mantenimientosConNotificacion.contains(mantenimiento.getId())) {
                    continue;
                }
                
                // Calcular km restantes hasta el próximo mantenimiento
                int ultimoMantenimiento = buscarUltimoMantenimiento(vehiculo.getId(), mantenimiento.getTipo());
                int kmRestantes = mantenimiento.getIntervaloKm() - (vehiculo.getKilometros() - ultimoMantenimiento);
                
                // Si los km restantes son negativos, significa que ya se debería haber hecho el mantenimiento
                if (kmRestantes < 0) {
                    kmRestantes = 0;
                }
                
                // Calcular fecha estimada basada en los km mensuales
                LocalDate fechaEstimada = null;
                if (vehiculo.getKmMensuales() > 0) {
                    int mesesEstimados = kmRestantes / vehiculo.getKmMensuales();
                    fechaEstimada = LocalDate.now().plusMonths(mesesEstimados);
                }
                
                // Insertar la nueva notificación
                String sql = "INSERT INTO notificaciones_mantenimiento (vehiculo_id, mantenimiento_id, km_estimados_restantes, fecha_estimada, estado) " +
                            "VALUES (?, ?, ?, ?, 'Pendiente')";
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, vehiculo.getId());
                    stmt.setInt(2, mantenimiento.getId());
                    stmt.setInt(3, kmRestantes);
                    
                    if (fechaEstimada != null) {
                        stmt.setDate(4, Date.valueOf(fechaEstimada));
                    } else {
                        stmt.setNull(4, java.sql.Types.DATE);
                    }
                    
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Error al crear nueva notificación: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar notificaciones de mantenimiento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para buscar el último mantenimiento realizado de un tipo específico
    private int buscarUltimoMantenimiento(int vehiculoId, String tipoMantenimiento) {
        String sql = "SELECT r.*, v.kilometros " +
                    "FROM reparaciones r " +
                    "JOIN vehiculos v ON r.vehiculo_id = v.id " +
                    "WHERE r.vehiculo_id = ? AND r.descripcion LIKE ? AND r.estado = 'Completada' " +
                    "ORDER BY r.fecha_reparacion DESC LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculoId);
            stmt.setString(2, "%" + tipoMantenimiento + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Si encontramos una reparación que coincide con el tipo de mantenimiento,
                // estimamos los km en el momento de la reparación
                int kmActuales = rs.getInt("kilometros");
                
                // Obtenemos la fecha de la reparación
                Date fechaReparacion = rs.getDate("fecha_reparacion");
                
                // Calculamos los km aproximados en el momento de la reparación
                return calcularKmEnFecha(vehiculoId, fechaReparacion, kmActuales);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Si no hay reparaciones previas de este tipo, asumimos que el vehículo nunca ha tenido este mantenimiento
        // Por lo tanto, devolvemos 0, lo que significa que el mantenimiento será necesario cuando se alcance el intervalo
        return 0;
    }

    // Método auxiliar para calcular los km aproximados en una fecha determinada
    private int calcularKmEnFecha(int vehiculoId, Date fecha, int kmActuales) {
        // Obtenemos el vehículo para conocer sus km mensuales
        Vehiculo vehiculo = getVehiculoById(vehiculoId);
        
        if (vehiculo != null && vehiculo.getKmMensuales() > 0) {
            // Calculamos la diferencia de tiempo entre la fecha actual y la fecha de la reparación
            long diffInDays = ChronoUnit.DAYS.between(fecha.toLocalDate(), LocalDate.now());
            long diffInMonths = diffInDays / 30; // Aproximación de meses
            
            // Calculamos los km aproximados en el momento de la reparación
            return kmActuales - (int)(diffInMonths * vehiculo.getKmMensuales());
        }
        
        // Si no podemos calcular, devolvemos los km actuales (asumimos que no ha cambiado)
        return kmActuales;
    }
} 