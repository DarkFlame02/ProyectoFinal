package dam.dad.app.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import dam.dad.app.db.DatabaseManager;
import dam.dad.app.model.Reparacion;
import dam.dad.app.model.Taller;
import dam.dad.app.model.Vehiculo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleStringProperty;

public class ReparacionController {
    
    @FXML
    private TableView<Reparacion> reparacionesTable;
    
    @FXML
    private TableColumn<Reparacion, LocalDate> fechaColumn;
    
    @FXML
    private TableColumn<Reparacion, String> descripcionColumn;
    
    @FXML
    private TableColumn<Reparacion, Double> costoColumn;
    
    @FXML
    private TableColumn<Reparacion, String> tallerColumn;
    
    @FXML
    private TableColumn<Reparacion, String> estadoColumn;
    
    private DatabaseManager dbManager;
    private ObservableList<Reparacion> reparacionesData = FXCollections.observableArrayList();
    
    private VehiculoController vehiculoController;
    
    public void setVehiculoController(VehiculoController vehiculoController) {
        this.vehiculoController = vehiculoController;
    }
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        // Configurar columnas de reparaciones
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaReparacion"));
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        costoColumn.setCellValueFactory(new PropertyValueFactory<>("costo"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar celdas para mostrar taller en las reparaciones
        tallerColumn.setCellValueFactory(cellData -> {
            Reparacion reparacion = cellData.getValue();
            String nombreTaller = "[No disponible]";
            for (Taller taller : dbManager.getAllTalleres()) {
                if (taller.getId() == reparacion.getTallerId()) {
                    nombreTaller = taller.getNombre();
                    break;
                }
            }
            return new SimpleStringProperty(nombreTaller);
        });
    }
    
    public void mostrarReparacionesDeVehiculo(Vehiculo vehiculo) {
        reparacionesData.clear();
        if (vehiculo != null && vehiculo.getReparaciones() != null) {
            reparacionesData.addAll(vehiculo.getReparaciones());
        }
        reparacionesTable.setItems(reparacionesData);
    }
    
    @FXML
    public void handleNuevaReparacion() {
        if (vehiculoController == null) {
            mostrarAlerta("Error", "No se ha configurado el controlador de vehículos.");
            return;
        }
        
        Vehiculo vehiculoSeleccionado = vehiculoController.getSelectedVehiculo();
        if (vehiculoSeleccionado == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Selecciona un vehículo antes de añadir una reparación.");
            return;
        }
        
        // Crear diálogo para nueva reparación
        Dialog<Reparacion> dialog = new Dialog<>();
        dialog.setTitle("Nueva Reparación");
        dialog.setHeaderText("Introduce los datos de la nueva reparación");
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker fechaPicker = new DatePicker(LocalDate.now());
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción de la reparación");
        TextField costoField = new TextField();
        costoField.setPromptText("Costo");
        
        ComboBox<Taller> tallerCombo = new ComboBox<>();
        tallerCombo.setItems(FXCollections.observableArrayList(dbManager.getAllTalleres()));
        tallerCombo.setPromptText("Selecciona un taller");
        
        ComboBox<String> estadoCombo = new ComboBox<>();
        estadoCombo.setItems(FXCollections.observableArrayList("Pendiente", "En progreso", "Completada"));
        estadoCombo.setValue("Pendiente");
        
        grid.add(new Label("Fecha:"), 0, 0);
        grid.add(fechaPicker, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Costo:"), 0, 2);
        grid.add(costoField, 1, 2);
        grid.add(new Label("Taller:"), 0, 3);
        grid.add(tallerCombo, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(estadoCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Taller taller = tallerCombo.getValue();
                    if (taller == null) {
                        mostrarAlerta("Error en los datos", "Debes seleccionar un taller.");
                        return null;
                    }
                    
                    Reparacion reparacion = new Reparacion();
                    reparacion.setVehiculoId(vehiculoSeleccionado.getId());
                    reparacion.setFechaReparacion(fechaPicker.getValue());
                    reparacion.setDescripcion(descripcionArea.getText().trim());
                    reparacion.setCosto(Double.parseDouble(costoField.getText().trim()));
                    reparacion.setTallerId(taller.getId());
                    reparacion.setEstado(estadoCombo.getValue());
                    
                    return reparacion;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error en los datos", "El costo debe ser un valor numérico válido.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Reparacion> result = dialog.showAndWait();
        
        result.ifPresent(reparacion -> {
            if (dbManager.addReparacion(reparacion)) {
                vehiculoController.cargarVehiculos(); // Recargar para actualizar las reparaciones del vehículo
            } else {
                mostrarAlerta("Error al guardar", "No se pudo guardar la reparación.");
            }
        });
    }

    @FXML
    public void handleEditarReparacion() {
        Reparacion selectedReparacion = reparacionesTable.getSelectionModel().getSelectedItem();
        if (selectedReparacion == null) {
            mostrarAlerta("No hay reparación seleccionada", "Por favor, selecciona una reparación para editar.");
            return;
        }
        
        // Crear diálogo para editar reparación
        Dialog<Reparacion> dialog = new Dialog<>();
        dialog.setTitle("Editar Reparación");
        dialog.setHeaderText("Edita los datos de la reparación");
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker fechaPicker = new DatePicker(selectedReparacion.getFechaReparacion());
        TextArea descripcionArea = new TextArea(selectedReparacion.getDescripcion());
        TextField costoField = new TextField(String.valueOf(selectedReparacion.getCosto()));
        
        ComboBox<Taller> tallerCombo = new ComboBox<>();
        tallerCombo.setItems(FXCollections.observableArrayList(dbManager.getAllTalleres()));
        
        // Seleccionar el taller actual
        for (Taller taller : tallerCombo.getItems()) {
            if (taller.getId() == selectedReparacion.getTallerId()) {
                tallerCombo.setValue(taller);
                break;
            }
        }
        
        ComboBox<String> estadoCombo = new ComboBox<>();
        estadoCombo.setItems(FXCollections.observableArrayList("Pendiente", "En progreso", "Completada"));
        estadoCombo.setValue(selectedReparacion.getEstado());
        
        grid.add(new Label("Fecha:"), 0, 0);
        grid.add(fechaPicker, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Costo:"), 0, 2);
        grid.add(costoField, 1, 2);
        grid.add(new Label("Taller:"), 0, 3);
        grid.add(tallerCombo, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(estadoCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Taller taller = tallerCombo.getValue();
                    if (taller == null) {
                        mostrarAlerta("Error en los datos", "Debes seleccionar un taller.");
                        return null;
                    }
                    
                    // Crear copia con los nuevos valores pero manteniendo el id
                    Reparacion reparacionEditada = new Reparacion();
                    reparacionEditada.setId(selectedReparacion.getId());
                    reparacionEditada.setVehiculoId(selectedReparacion.getVehiculoId());
                    reparacionEditada.setFechaReparacion(fechaPicker.getValue());
                    reparacionEditada.setDescripcion(descripcionArea.getText().trim());
                    reparacionEditada.setCosto(Double.parseDouble(costoField.getText().trim()));
                    reparacionEditada.setTallerId(taller.getId());
                    reparacionEditada.setEstado(estadoCombo.getValue());
                    
                    return reparacionEditada;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error en los datos", "El costo debe ser un valor numérico válido.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Reparacion> result = dialog.showAndWait();
        
        result.ifPresent(reparacion -> {
            if (dbManager.updateReparacion(reparacion)) {
                vehiculoController.cargarVehiculos(); // Recargar para actualizar las reparaciones del vehículo
            } else {
                mostrarAlerta("Error al guardar", "No se pudo actualizar la reparación.");
            }
        });
    }
    
    @FXML
    public void handleEliminarReparacion() {
        Reparacion selectedReparacion = reparacionesTable.getSelectionModel().getSelectedItem();
        if (selectedReparacion == null) {
            mostrarAlerta("No hay reparación seleccionada", "Por favor, selecciona una reparación para eliminar.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Reparación");
        alert.setHeaderText("¿Estás seguro de que deseas eliminar esta reparación?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteReparacion(selectedReparacion.getId())) {
                vehiculoController.cargarVehiculos(); // Recargar para actualizar las reparaciones del vehículo
            } else {
                mostrarAlerta("Error al eliminar", "No se pudo eliminar la reparación.");
            }
        }
    }
    
    private Vehiculo getVehiculoById(int id) {
        List<Vehiculo> vehiculos = dbManager.getVehiculosByUsuario(); 
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getId() == id) {
                return vehiculo;
            }
        }
        return null;
    }
    
    private void mostrarAlerta(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 