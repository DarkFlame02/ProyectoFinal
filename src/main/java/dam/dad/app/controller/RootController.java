package dam.dad.app.controller;

import java.io.IOException;
import java.util.Optional;

import dam.dad.app.db.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RootController {
    
    @FXML
    private BorderPane root;
    
    @FXML
    private Label tituloSeccionLabel;
    
    @FXML
    private Button notificacionesButton;
    
    @FXML
    private Label notificacionesContador;
    
    private Stage primaryStage;
    private DatabaseManager dbManager;
    
    // Controladores de las vistas
    private VehiculoController vehiculoController;
    private ReparacionController reparacionController;
    private TallerController tallerController;
    private InformesController informesController;
    private NotificacionesController notificacionesController;
    
    // Vistas cargadas
    private Parent vehiculosView;
    private Parent talleresView;
    private Parent informesView;
    private Parent notificacionesView;
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        cargarVistas();
        mostrarVistaPrincipal();
        
        // Verificar notificaciones pendientes al iniciar la aplicación
        if (notificacionesController != null) {
            try {
                notificacionesController.verificarNotificacionesPendientes();
                actualizarContadorNotificaciones();
            } catch (Exception e) {
                System.err.println("Error al verificar notificaciones: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Actualiza el contador de notificaciones pendientes
     */
    public void actualizarContadorNotificaciones() {
        if (notificacionesController != null && notificacionesContador != null) {
            try {
                // Obtener el número de notificaciones críticas (km <= 500)
                int numNotificaciones = notificacionesController.getNumeroNotificacionesCriticas();
                
                // Actualizar el texto del contador
                if (numNotificaciones > 0) {
                    notificacionesContador.setText("(" + numNotificaciones + ")");
                    notificacionesContador.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else {
                    notificacionesContador.setText("");
                }
            } catch (Exception e) {
                System.err.println("Error al actualizar contador de notificaciones: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    private void cargarVistas() {
        try {
            // Cargar vista de vehículos (que a su vez carga la vista de reparaciones)
            FXMLLoader vehiculosLoader = new FXMLLoader(getClass().getResource("/fxml/VehiculosView.fxml"));
            vehiculosView = vehiculosLoader.load();
            vehiculoController = vehiculosLoader.getController();
            
            // Cargar vista de talleres
            FXMLLoader talleresLoader = new FXMLLoader(getClass().getResource("/fxml/TalleresView.fxml"));
            talleresView = talleresLoader.load();
            tallerController = talleresLoader.getController();
            
            // Cargar vista de informes
            FXMLLoader informesLoader = new FXMLLoader(getClass().getResource("/fxml/InformesView.fxml"));
            informesView = informesLoader.load();
            informesController = informesLoader.getController();
            
            // Obtener el controlador de reparaciones
            reparacionController = vehiculoController.getReparacionController();
            
            // Inicializar controlador de notificaciones
            try {
                notificacionesController = new NotificacionesController();
                notificacionesController.setDatabaseManager(dbManager);
                notificacionesController.setRootController(this);
                notificacionesView = notificacionesController.getView();
                
                if (notificacionesView == null) {
                    mostrarAlerta("Error en notificaciones", "No se pudo cargar la vista de notificaciones correctamente.");
                }
            } catch (Exception e) {
                mostrarAlerta("Error en notificaciones", "Error al inicializar el sistema de notificaciones: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            mostrarAlerta("Error al cargar vistas", "No se pudieron cargar las vistas de la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void mostrarVistaPrincipal() {
        // Por defecto, muestra la vista de vehículos y reparaciones
        root.setCenter(vehiculosView);
        tituloSeccionLabel.setText("Vehículos y Reparaciones");
    }

    @FXML
    public void handleMostrarVehiculos() {
        root.setCenter(vehiculosView);
        tituloSeccionLabel.setText("Vehículos y Reparaciones");
        primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Vehículos y Reparaciones");
        actualizarContadorNotificaciones();
    }

    @FXML
    public void handleMostrarTalleres() {
        root.setCenter(talleresView);
        tituloSeccionLabel.setText("Catálogo de Talleres Colaboradores");
        primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Talleres");
        actualizarContadorNotificaciones();
    }

    @FXML
    public void handleMostrarInformes() {
        // Actualizar datos de informes antes de mostrar
        informesController.cargarInformes();
        root.setCenter(informesView);
        tituloSeccionLabel.setText("Informes y Estadísticas");
        primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Informes");
        actualizarContadorNotificaciones();
    }
    
    @FXML
    public void handleMostrarNotificaciones() {
        if (notificacionesView != null && notificacionesController != null) {
            try {
                // Actualizar datos de notificaciones antes de mostrar
                notificacionesController.cargarNotificaciones();
                actualizarContadorNotificaciones();
                root.setCenter(notificacionesView);
                tituloSeccionLabel.setText("Notificaciones de Mantenimiento");
                primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Notificaciones");
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo mostrar la vista de notificaciones: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "La vista de notificaciones no está disponible");
        }
    }

    @FXML
    public void handleActualizarDatos() {
        vehiculoController.cargarVehiculos();
        tallerController.cargarTalleres();
        
        if (notificacionesController != null) {
            try {
                notificacionesController.cargarNotificaciones();
                actualizarContadorNotificaciones();
            } catch (Exception e) {
                System.err.println("Error al actualizar notificaciones: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleCerrarSesion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cerrar sesión");
        confirmacion.setHeaderText("¿Estás seguro de que quieres cerrar la sesión?");
        confirmacion.setContentText("Se perderán los cambios no guardados.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Cerrar sesión en el gestor de base de datos
                dbManager.logout();
                
                // Cargar vista de login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Login");
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo cargar la vista de login: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleSalir() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Salir");
        confirmacion.setHeaderText("¿Estás seguro de que quieres salir de la aplicación?");
        confirmacion.setContentText("Se perderán los cambios no guardados.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
        }
    }
    
    private void mostrarAlerta(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 