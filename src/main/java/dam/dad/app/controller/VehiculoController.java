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
            mostrarAlerta("Error de carga", "No se pudo cargar la vista de reparaciones: " + e.getMessage());
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
        
        // Seleccionar el primer vehículo si hay alguno
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
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Vehiculo vehiculo = new Vehiculo();
                    vehiculo.setMarca(marcaField.getText().trim());
                    vehiculo.setModelo(modeloField.getText().trim());
                    vehiculo.setMatricula(matriculaField.getText().trim());
                    vehiculo.setAnio(Integer.parseInt(anioField.getText().trim()));
                    vehiculo.setKilometros(Integer.parseInt(kilometrosField.getText().trim()));
                    
                    return vehiculo;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error en los datos", "Por favor, introduce valores numéricos válidos para Año y Kilómetros.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Vehiculo> result = dialog.showAndWait();
        
        result.ifPresent(vehiculo -> {
            if (dbManager.addVehiculo(vehiculo)) {
                cargarVehiculos();
            } else {
                mostrarAlerta("Error al guardar", "No se pudo guardar el vehículo. La matrícula podría estar duplicada.");
            }
        });
    }

    @FXML
    public void handleEditarVehiculo() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para editar.");
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
        
        TextField marcaField = new TextField(selectedVehiculo.getMarca());
        TextField modeloField = new TextField(selectedVehiculo.getModelo());
        TextField matriculaField = new TextField(selectedVehiculo.getMatricula());
        TextField anioField = new TextField(String.valueOf(selectedVehiculo.getAnio()));
        TextField kilometrosField = new TextField(String.valueOf(selectedVehiculo.getKilometros()));
        
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
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Vehiculo vehiculo = new Vehiculo();
                    vehiculo.setId(selectedVehiculo.getId());
                    vehiculo.setUsuarioId(selectedVehiculo.getUsuarioId());
                    vehiculo.setMarca(marcaField.getText().trim());
                    vehiculo.setModelo(modeloField.getText().trim());
                    vehiculo.setMatricula(matriculaField.getText().trim());
                    vehiculo.setAnio(Integer.parseInt(anioField.getText().trim()));
                    vehiculo.setKilometros(Integer.parseInt(kilometrosField.getText().trim()));
                    
                    return vehiculo;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error en los datos", "Por favor, introduce valores numéricos válidos para Año y Kilómetros.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Vehiculo> result = dialog.showAndWait();
        
        result.ifPresent(vehiculo -> {
            if (dbManager.updateVehiculo(vehiculo)) {
                cargarVehiculos();
            } else {
                mostrarAlerta("Error al actualizar", "No se pudo actualizar el vehículo. La matrícula podría estar duplicada.");
            }
        });
    }

    @FXML
    public void handleEliminarVehiculo() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para eliminar.");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar este vehículo?");
        confirmacion.setContentText("Esta acción eliminará también todas las reparaciones asociadas.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteVehiculo(selectedVehiculo.getId())) {
                cargarVehiculos();
            } else {
                mostrarAlerta("Error al eliminar", "No se pudo eliminar el vehículo.");
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
                mostrarAlerta("Sin resultados", "No se encontraron vehículos que coincidan con la búsqueda.");
            }
        });
    }

    @FXML
    public void handleHistorialReparaciones() {
        Vehiculo selectedVehiculo = getSelectedVehiculo();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para ver su historial de reparaciones.");
            return;
        }
        
        // Crear diálogo para mostrar el historial
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de Reparaciones");
        dialog.setHeaderText("Historial de reparaciones para: " + selectedVehiculo.getMarca() + " " + selectedVehiculo.getModelo() + " (" + selectedVehiculo.getMatricula() + ")");
        
        // Botones
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Contenido principal - simplificado para pruebas
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        
        TableView<Reparacion> historialTable = new TableView<>();
        
        // Columnas simplificadas
        TableColumn<Reparacion, LocalDate> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fechaReparacion"));
        
        TableColumn<Reparacion, String> descripcionCol = new TableColumn<>("Descripción");
        descripcionCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        TableColumn<Reparacion, Double> costoCol = new TableColumn<>("Costo");
        costoCol.setCellValueFactory(new PropertyValueFactory<>("costo"));
        
        TableColumn<Reparacion, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        historialTable.getColumns().addAll(fechaCol, descripcionCol, costoCol, estadoCol);
        
        // Cargar datos
        if (selectedVehiculo.getReparaciones() != null) {
            historialTable.setItems(FXCollections.observableArrayList(selectedVehiculo.getReparaciones()));
        }
        
        vbox.getChildren().add(historialTable);
        
        dialog.getDialogPane().setContent(vbox);
        dialog.showAndWait();
    }

    private void mostrarAlerta(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 