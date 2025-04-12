package dam.dad.app.controller;

import java.io.IOException;
import java.util.List;

import dam.dad.app.db.DatabaseManager;
import dam.dad.app.model.NotificacionMantenimiento;
import dam.dad.app.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class NotificacionesController {

    @FXML
    private TableView<NotificacionMantenimiento> notificacionesTable;
    
    @FXML
    private TableColumn<NotificacionMantenimiento, String> vehiculoColumn;
    
    @FXML
    private TableColumn<NotificacionMantenimiento, String> mantenimientoColumn;
    
    @FXML
    private TableColumn<NotificacionMantenimiento, Integer> kmRestantesColumn;
    
    @FXML
    private TableColumn<NotificacionMantenimiento, String> fechaEstimadaColumn;
    
    @FXML
    private TableColumn<NotificacionMantenimiento, String> criticidadColumn;
    
    @FXML
    private VBox detallesContainer;
    
    @FXML
    private Label vehiculoLabel;
    
    @FXML
    private Label mantenimientoLabel;
    
    @FXML
    private Label descripcionLabel;
    
    @FXML
    private Label kmRestantesLabel;
    
    @FXML
    private Label fechaEstimadaLabel;
    
    @FXML
    private Label criticidadLabel;
    
    @FXML
    private Button marcarCompletadoButton;
    
    private DatabaseManager dbManager;
    private ObservableList<NotificacionMantenimiento> notificacionesData = FXCollections.observableArrayList();
    private Parent root;
    private RootController rootController;
    
    public NotificacionesController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NotificacionesView.fxml"));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            System.err.println("Error al cargar vista de notificaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void initialize() {
        try {
            // Configurar la tabla de notificaciones
            vehiculoColumn.setCellValueFactory(cellData -> {
                NotificacionMantenimiento notificacion = cellData.getValue();
                if (notificacion.getVehiculo() != null) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> 
                        notificacion.getVehiculo().getMarca() + " " + notificacion.getVehiculo().getModelo() + 
                        " (" + notificacion.getVehiculo().getMatricula() + ")");
                }
                return javafx.beans.binding.Bindings.createStringBinding(() -> "");
            });
            
            mantenimientoColumn.setCellValueFactory(cellData -> {
                NotificacionMantenimiento notificacion = cellData.getValue();
                if (notificacion.getMantenimiento() != null) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> 
                        notificacion.getMantenimiento().getTipo());
                }
                return javafx.beans.binding.Bindings.createStringBinding(() -> "");
            });
            
            kmRestantesColumn.setCellValueFactory(new PropertyValueFactory<>("kmEstimadosRestantes"));
            
            fechaEstimadaColumn.setCellValueFactory(cellData -> {
                NotificacionMantenimiento notificacion = cellData.getValue();
                if (notificacion.getFechaEstimada() != null) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> 
                        notificacion.getFechaEstimada().toString());
                }
                return javafx.beans.binding.Bindings.createStringBinding(() -> "No disponible");
            });
            
            criticidadColumn.setCellValueFactory(cellData -> {
                NotificacionMantenimiento notificacion = cellData.getValue();
                if (notificacion.getMantenimiento() != null) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> 
                        notificacion.getMantenimiento().getCriticidad());
                }
                return javafx.beans.binding.Bindings.createStringBinding(() -> "");
            });
            
            // Escuchar cambios en la selección de la tabla
            notificacionesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetallesNotificacion(newSelection);
                } else {
                    ocultarDetalles();
                }
            });
            
            // Configurar acción del botón marcar como completado
            marcarCompletadoButton.setOnAction(event -> {
                NotificacionMantenimiento notificacion = notificacionesTable.getSelectionModel().getSelectedItem();
                if (notificacion != null) {
                    marcarNotificacionComoCompletada(notificacion);
                }
            });
            
            // Inicialmente ocultar los detalles
            ocultarDetalles();
        } catch (Exception e) {
            System.err.println("Error en inicialización de NotificacionesController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        cargarNotificaciones();
    }
    
    public Parent getView() {
        return root;
    }
    
    public void cargarNotificaciones() {
        if (dbManager != null) {
            try {
                notificacionesData.clear();
                List<NotificacionMantenimiento> notificaciones = dbManager.getNotificacionesPendientes();
                notificacionesData.addAll(notificaciones);
                notificacionesTable.setItems(notificacionesData);
            } catch (Exception e) {
                System.err.println("Error al cargar notificaciones: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void mostrarDetallesNotificacion(NotificacionMantenimiento notificacion) {
        detallesContainer.setVisible(true);
        
        if (notificacion.getVehiculo() != null) {
            Vehiculo vehiculo = notificacion.getVehiculo();
            vehiculoLabel.setText(vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getMatricula() + ")");
        } else {
            vehiculoLabel.setText("No disponible");
        }
        
        if (notificacion.getMantenimiento() != null) {
            mantenimientoLabel.setText(notificacion.getMantenimiento().getTipo());
            descripcionLabel.setText(notificacion.getMantenimiento().getDescripcion());
            criticidadLabel.setText(notificacion.getMantenimiento().getCriticidad());
        } else {
            mantenimientoLabel.setText("No disponible");
            descripcionLabel.setText("No disponible");
            criticidadLabel.setText("No disponible");
        }
        
        kmRestantesLabel.setText(String.valueOf(notificacion.getKmEstimadosRestantes()));
        
        if (notificacion.getFechaEstimada() != null) {
            fechaEstimadaLabel.setText(notificacion.getFechaEstimada().toString());
        } else {
            fechaEstimadaLabel.setText("No disponible");
        }
    }
    
    private void ocultarDetalles() {
        detallesContainer.setVisible(false);
    }
    
    /**
     * Permite establecer una referencia al controlador principal
     * para poder actualizar el contador de notificaciones
     */
    public void setRootController(RootController rootController) {
        this.rootController = rootController;
    }
    
    private void marcarNotificacionComoCompletada(NotificacionMantenimiento notificacion) {
        if (dbManager.actualizarEstadoNotificacion(notificacion.getId(), "Completado")) {
            // Actualizar la tabla
            cargarNotificaciones();
            
            // Actualizar el contador de notificaciones en la interfaz
            if (rootController != null) {
                rootController.actualizarContadorNotificaciones();
            }
            
            // Mostrar mensaje de confirmación
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Notificación actualizada");
            alert.setHeaderText("Notificación completada");
            alert.setContentText("La notificación de mantenimiento ha sido marcada como completada.");
            alert.showAndWait();
        } else {
            // Mostrar mensaje de error
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al actualizar");
            alert.setContentText("No se pudo actualizar el estado de la notificación.");
            alert.showAndWait();
        }
    }
    
    /**
     * Obtiene el número de notificaciones críticas
     * (aquellas con kilómetros restantes <= 500)
     * 
     * @return Número de notificaciones críticas pendientes
     */
    public int getNumeroNotificacionesCriticas() {
        if (dbManager == null) return 0;
        
        try {
            List<NotificacionMantenimiento> notificacionesUrgentes = dbManager.getNotificacionesPendientes();
            
            // Filtrar solo las notificaciones realmente urgentes (km restantes <= 500)
            List<NotificacionMantenimiento> notificacionesCriticas = notificacionesUrgentes.stream()
                .filter(n -> n.getKmEstimadosRestantes() <= 500)
                .collect(java.util.stream.Collectors.toList());
            
            return notificacionesCriticas.size();
        } catch (Exception e) {
            System.err.println("Error al obtener número de notificaciones críticas: " + e.getMessage());
            return 0;
        }
    }
    
    // Método para verificar y mostrar notificaciones pendientes
    public void verificarNotificacionesPendientes() {
        if (dbManager != null) {
            try {
                List<NotificacionMantenimiento> notificacionesUrgentes = dbManager.getNotificacionesPendientes();
                
                // Filtrar solo las notificaciones realmente urgentes (km restantes <= 500)
                List<NotificacionMantenimiento> notificacionesCriticas = notificacionesUrgentes.stream()
                    .filter(n -> n.getKmEstimadosRestantes() <= 500)
                    .collect(java.util.stream.Collectors.toList());
                
                if (!notificacionesCriticas.isEmpty()) {
                    StringBuilder mensaje = new StringBuilder();
                    mensaje.append("Tienes las siguientes notificaciones de mantenimiento URGENTES:\n\n");
                    
                    for (NotificacionMantenimiento notificacion : notificacionesCriticas) {
                        String vehiculoInfo = "No disponible";
                        String mantenimientoInfo = "No disponible";
                        
                        if (notificacion.getVehiculo() != null) {
                            Vehiculo vehiculo = notificacion.getVehiculo();
                            vehiculoInfo = vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getMatricula() + ")";
                        }
                        
                        if (notificacion.getMantenimiento() != null) {
                            mantenimientoInfo = notificacion.getMantenimiento().getTipo();
                        }
                        
                        mensaje.append("• ").append(vehiculoInfo).append(": ")
                               .append(mantenimientoInfo);
                        
                        if (notificacion.getKmEstimadosRestantes() <= 0) {
                            mensaje.append(" - ¡URGENTE! Mantenimiento necesario AHORA");
                        } else {
                            mensaje.append(" - Faltan SOLO ").append(notificacion.getKmEstimadosRestantes()).append(" km");
                        }
                        
                        mensaje.append("\n");
                    }
                    
                    mensaje.append("\nEs importante realizar estos mantenimientos lo antes posible para garantizar " +
                                "el buen funcionamiento y seguridad de tus vehículos.");
                    
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Notificaciones de mantenimiento URGENTES");
                    alert.setHeaderText("¡Atención! Mantenimientos críticos pendientes");
                    alert.setContentText(mensaje.toString());
                    alert.showAndWait();
                }
                
                // Cargar las notificaciones en la tabla para poder ver todas
                cargarNotificaciones();
                
            } catch (Exception e) {
                System.err.println("Error al verificar notificaciones pendientes: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 