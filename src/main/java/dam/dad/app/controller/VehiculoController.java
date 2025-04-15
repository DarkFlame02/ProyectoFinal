package dam.dad.app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import dam.dad.app.db.DatabaseManager;
import dam.dad.app.model.Reparacion;
import dam.dad.app.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VehiculoController {
    
    @FXML
    private TableView<Vehiculo> vehiculosTable;
    
    @FXML
    private TableColumn<Vehiculo, String> matriculaColumn;
    
    @FXML
    private TableColumn<Vehiculo, String> marcaColumn;
    
    @FXML
    private TableColumn<Vehiculo, String> modeloColumn;
    
    @FXML
    private TableColumn<Vehiculo, Integer> anioColumn;
    
    @FXML
    private TableColumn<Vehiculo, Integer> kilometrosColumn;
    
    @FXML
    private VBox reparacionesContainer;
    
    private DatabaseManager dbManager;
    private ObservableList<Vehiculo> vehiculosData = FXCollections.observableArrayList();
    
    private ReparacionController reparacionController;
    
    public void setReparacionController(ReparacionController reparacionController) {
        this.reparacionController = reparacionController;
    }
    
    public ReparacionController getReparacionController() {
        return reparacionController;
    }
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        // Configurar columnas de vehículos
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        marcaColumn.setCellValueFactory(new PropertyValueFactory<>("marca"));
        modeloColumn.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        anioColumn.setCellValueFactory(new PropertyValueFactory<>("anio"));
        kilometrosColumn.setCellValueFactory(new PropertyValueFactory<>("kilometros"));
        
        // Cargar vista de reparaciones
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReparacionesView.fxml"));
            Parent reparacionesView = loader.load();
            reparacionController = loader.getController();
            
            // Añadir a la vista
            reparacionesContainer.getChildren().add(reparacionesView);
            VBox.setVgrow(reparacionesView, javafx.scene.layout.Priority.ALWAYS);
            
            // Configurar el controlador de reparaciones
            reparacionController.setVehiculoController(this);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error de carga", "No se pudo cargar la vista de reparaciones: " + e.getMessage());
        }
        
        // Cargar datos iniciales
        cargarVehiculos();
        
        // Añadir listener para mostrar reparaciones del vehículo seleccionado
        vehiculosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && reparacionController != null) {
                reparacionController.mostrarReparacionesDeVehiculo(newSelection);
            }
        });
    }
    
    public void cargarVehiculos() {
        vehiculosData.clear();
        List<Vehiculo> vehiculos = dbManager.getVehiculosByUsuario();
        vehiculosData.addAll(vehiculos);
        vehiculosTable.setItems(vehiculosData);
        
        if (!vehiculosData.isEmpty()) {
            vehiculosTable.getSelectionModel().select(0);
            if (reparacionController != null) {
                reparacionController.mostrarReparacionesDeVehiculo(vehiculosData.get(0));
            }
        }
    }
    
    public Vehiculo getSelectedVehiculo() {
        return vehiculosTable.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    public void handleNuevoVehiculo() {
        // Crear diálogo para nuevo vehículo
        Dialog<Vehiculo> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Vehículo");
        dialog.setHeaderText("Introduce los datos del nuevo vehículo");
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField marcaField = new TextField();
        marcaField.setPromptText("Marca");
        TextField modeloField = new TextField();
        modeloField.setPromptText("Modelo");
        TextField matriculaField = new TextField();
        matriculaField.setPromptText("Matrícula");
        TextField anioField = new TextField();
        anioField.setPromptText("Año");
        TextField kilometrosField = new TextField();
        kilometrosField.setPromptText("Kilómetros");
        TextField kmMensualesField = new TextField();
        kmMensualesField.setPromptText("Kilómetros mensuales estimados");
        
        grid.add(new Label("Marca:"), 0, 0);
        grid.add(marcaField, 1, 0);
        grid.add(new Label("Modelo:"), 0, 1);
        grid.add(modeloField, 1, 1);
        grid.add(new Label("Matrícula:"), 0, 2);
        grid.add(matriculaField, 1, 2);
        grid.add(new Label("Año:"), 0, 3);
        grid.add(anioField, 1, 3);
        grid.add(new Label("Kilómetros:"), 0, 4);
        grid.add(kilometrosField, 1, 4);
        grid.add(new Label("Km mensuales:"), 0, 5);
        grid.add(kmMensualesField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado al hacer clic en Guardar
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    String marca = marcaField.getText().trim();
                    String modelo = modeloField.getText().trim();
                    String matricula = matriculaField.getText().trim();
                    int anio = Integer.parseInt(anioField.getText().trim());
                    int kilometros = Integer.parseInt(kilometrosField.getText().trim());
                    int kmMensuales = Integer.parseInt(kmMensualesField.getText().trim());
                    
                    if (marca.isEmpty() || modelo.isEmpty() || matricula.isEmpty()) {
                        showErrorAlert("Campos obligatorios", "Todos los campos son obligatorios.");
                        return null;
                    }
                    
                    Vehiculo nuevoVehiculo = new Vehiculo();
                    nuevoVehiculo.setMarca(marca);
                    nuevoVehiculo.setModelo(modelo);
                    nuevoVehiculo.setMatricula(matricula);
                    nuevoVehiculo.setAnio(anio);
                    nuevoVehiculo.setKilometros(kilometros);
                    nuevoVehiculo.setKmMensuales(kmMensuales);
                    
                    return nuevoVehiculo;
                } catch (NumberFormatException e) {
                    showErrorAlert("Error de formato", "El año, los kilómetros y los kilómetros mensuales deben ser números enteros.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Vehiculo> result = dialog.showAndWait();
        
        result.ifPresent(vehiculo -> {
            if (dbManager.addVehiculo(vehiculo)) {
                cargarVehiculos();
                
                // Mostrar mensaje de confirmación
                showInfoAlert("Vehículo añadido", "El vehículo ha sido añadido correctamente.");
            } else {
                showErrorAlert("Error", "No se pudo añadir el vehículo. Es posible que la matrícula ya exista.");
            }
        });
    }

    @FXML
    public void handleEditarVehiculo() {
        Vehiculo vehiculoSeleccionado = vehiculosTable.getSelectionModel().getSelectedItem();
        
        if (vehiculoSeleccionado == null) {
            showErrorAlert("Sin selección", "Por favor, selecciona un vehículo para editar.");
            return;
        }
        
        // Crear diálogo para editar vehículo
        Dialog<Vehiculo> dialog = new Dialog<>();
        dialog.setTitle("Editar Vehículo");
        dialog.setHeaderText("Edita los datos del vehículo");
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField marcaField = new TextField(vehiculoSeleccionado.getMarca());
        TextField modeloField = new TextField(vehiculoSeleccionado.getModelo());
        TextField matriculaField = new TextField(vehiculoSeleccionado.getMatricula());
        TextField anioField = new TextField(String.valueOf(vehiculoSeleccionado.getAnio()));
        TextField kilometrosField = new TextField(String.valueOf(vehiculoSeleccionado.getKilometros()));
        TextField kmMensualesField = new TextField(String.valueOf(vehiculoSeleccionado.getKmMensuales()));
        
        grid.add(new Label("Marca:"), 0, 0);
        grid.add(marcaField, 1, 0);
        grid.add(new Label("Modelo:"), 0, 1);
        grid.add(modeloField, 1, 1);
        grid.add(new Label("Matrícula:"), 0, 2);
        grid.add(matriculaField, 1, 2);
        grid.add(new Label("Año:"), 0, 3);
        grid.add(anioField, 1, 3);
        grid.add(new Label("Kilómetros:"), 0, 4);
        grid.add(kilometrosField, 1, 4);
        grid.add(new Label("Km mensuales:"), 0, 5);
        grid.add(kmMensualesField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado al hacer clic en Guardar
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    String marca = marcaField.getText().trim();
                    String modelo = modeloField.getText().trim();
                    String matricula = matriculaField.getText().trim();
                    int anio = Integer.parseInt(anioField.getText().trim());
                    int kilometros = Integer.parseInt(kilometrosField.getText().trim());
                    int kmMensuales = Integer.parseInt(kmMensualesField.getText().trim());
                    
                    if (marca.isEmpty() || modelo.isEmpty() || matricula.isEmpty()) {
                        showErrorAlert("Campos obligatorios", "Todos los campos son obligatorios.");
                        return null;
                    }
                    
                    // Crear copia del vehículo con los datos actualizados
                    Vehiculo vehiculoActualizado = new Vehiculo();
                    vehiculoActualizado.setId(vehiculoSeleccionado.getId());
                    vehiculoActualizado.setUsuarioId(vehiculoSeleccionado.getUsuarioId());
                    vehiculoActualizado.setMarca(marca);
                    vehiculoActualizado.setModelo(modelo);
                    vehiculoActualizado.setMatricula(matricula);
                    vehiculoActualizado.setAnio(anio);
                    vehiculoActualizado.setKilometros(kilometros);
                    vehiculoActualizado.setKmMensuales(kmMensuales);
                    
                    return vehiculoActualizado;
                } catch (NumberFormatException e) {
                    showErrorAlert("Error de formato", "El año, los kilómetros y los kilómetros mensuales deben ser números enteros.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Vehiculo> result = dialog.showAndWait();
        
        result.ifPresent(vehiculoActualizado -> {
            if (dbManager.updateVehiculo(vehiculoActualizado)) {
                cargarVehiculos();
                
                // Mostrar mensaje de confirmación
                showInfoAlert("Vehículo actualizado", "El vehículo ha sido actualizado correctamente.");
                
                // Mostrar un mensaje adicional si se actualizaron las notificaciones
                if (vehiculoActualizado.getKilometros() != vehiculoSeleccionado.getKilometros()) {
                    Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
                    confirmacion.setTitle("Notificaciones actualizadas");
                    confirmacion.setHeaderText("Notificaciones de mantenimiento actualizadas");
                    confirmacion.setContentText("Se han actualizado las notificaciones de mantenimiento según los nuevos valores de kilometraje.");
                    confirmacion.showAndWait();
                }
            } else {
                showErrorAlert("Error", "No se pudo actualizar el vehículo.");
            }
        });
    }

    @FXML
    public void handleEliminarVehiculo() {
        Vehiculo vehiculoSeleccionado = vehiculosTable.getSelectionModel().getSelectedItem();
        
        if (vehiculoSeleccionado == null) {
            showErrorAlert("Sin selección", "Por favor, selecciona un vehículo para eliminar.");
            return;
        }
        
        // Confirmar eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro que deseas eliminar este vehículo?");
        confirmacion.setContentText("Se eliminarán todas las reparaciones asociadas a este vehículo.\nEsta acción no se puede deshacer.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteVehiculo(vehiculoSeleccionado.getId())) {
                // Eliminar vehículo de la lista observable
                vehiculosData.remove(vehiculoSeleccionado);
                // Actualizar la tabla
                vehiculosTable.refresh();
                showInfoAlert("Vehículo eliminado", "El vehículo ha sido eliminado correctamente junto con todas sus reparaciones.");
            } else {
                showErrorAlert("Error", "No se pudo eliminar el vehículo.");
            }
        }
    }
    
    @FXML
    public void handleBuscarVehiculo() {
        // Crear diálogo de búsqueda
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Buscar Vehículo");
        dialog.setHeaderText("Introduce la matrícula o parte del modelo/marca a buscar");
        
        // Botones
        ButtonType buscarButtonType = new ButtonType("Buscar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buscarButtonType, ButtonType.CANCEL);
        
        // Contenido
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField busquedaField = new TextField();
        busquedaField.setPromptText("Matrícula, Marca o Modelo");
        
        grid.add(new Label("Texto a buscar:"), 0, 0);
        grid.add(busquedaField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buscarButtonType) {
                return busquedaField.getText().trim();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(texto -> {
            if (texto.isEmpty()) {
                cargarVehiculos();
                return;
            }
            
            // Filtrar vehículos
            List<Vehiculo> vehiculosFiltrados = dbManager.getVehiculosByUsuario().stream()
                    .filter(v -> v.getMatricula().toLowerCase().contains(texto.toLowerCase()) || 
                            v.getMarca().toLowerCase().contains(texto.toLowerCase()) ||
                            v.getModelo().toLowerCase().contains(texto.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            
            vehiculosData.clear();
            vehiculosData.addAll(vehiculosFiltrados);
            
            if (vehiculosData.isEmpty()) {
                showErrorAlert("Sin resultados", "No se encontraron vehículos que coincidan con la búsqueda.");
            }
        });
    }

    // Muestra una alerta de información al usuario
    private void showInfoAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    
    // Muestra una alerta de error al usuario
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 