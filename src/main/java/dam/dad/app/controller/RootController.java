package dam.dad.app.controller;

import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RootController {
    @FXML
    private TableView<Vehiculo> vehiculosTable;
    @FXML
    private TableView<Reparacion> reparacionesTable;
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
    private TableColumn<Reparacion, LocalDate> fechaColumn;
    @FXML
    private TableColumn<Reparacion, String> vehiculoColumn;
    @FXML
    private TableColumn<Reparacion, String> descripcionColumn;
    @FXML
    private TableColumn<Reparacion, Double> costoColumn;
    @FXML
    private TableColumn<Reparacion, String> tallerColumn;
    @FXML
    private TableColumn<Reparacion, String> estadoColumn;
    
    private DatabaseManager dbManager;
    private ObservableList<Vehiculo> vehiculosData = FXCollections.observableArrayList();
    private ObservableList<Reparacion> reparacionesData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        // Configurar columnas de vehículos
        matriculaColumn.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        marcaColumn.setCellValueFactory(new PropertyValueFactory<>("marca"));
        modeloColumn.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        anioColumn.setCellValueFactory(new PropertyValueFactory<>("anio"));
        kilometrosColumn.setCellValueFactory(new PropertyValueFactory<>("kilometros"));
        
        // Configurar columnas de reparaciones
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaReparacion"));
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        costoColumn.setCellValueFactory(new PropertyValueFactory<>("costo"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar celdas para mostrar vehículo y taller en las reparaciones
        vehiculoColumn.setCellValueFactory(cellData -> {
            Reparacion reparacion = cellData.getValue();
            Vehiculo vehiculo = getVehiculoById(reparacion.getVehiculoId());
            String descripcionVehiculo = "[No disponible]";
            if (vehiculo != null) {
                descripcionVehiculo = vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getMatricula() + ")";
            }
            return new javafx.beans.property.SimpleStringProperty(descripcionVehiculo);
        });
        
        tallerColumn.setCellValueFactory(cellData -> {
            Reparacion reparacion = cellData.getValue();
            String nombreTaller = "[No disponible]";
            for (Taller taller : dbManager.getAllTalleres()) {
                if (taller.getId() == reparacion.getTallerId()) {
                    nombreTaller = taller.getNombre();
                    break;
                }
            }
            return new javafx.beans.property.SimpleStringProperty(nombreTaller);
        });
        
        // Cargar datos iniciales
        cargarVehiculos();
        
        // Añadir listener para mostrar reparaciones del vehículo seleccionado
        vehiculosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarReparacionesDeVehiculo(newSelection);
            }
        });
    }
    
    private void cargarVehiculos() {
        vehiculosData.clear();
        List<Vehiculo> vehiculos = dbManager.getVehiculosByUsuario();
        vehiculosData.addAll(vehiculos);
        vehiculosTable.setItems(vehiculosData);
        
        // Seleccionar el primer vehículo si hay alguno
        if (!vehiculosData.isEmpty()) {
            vehiculosTable.getSelectionModel().select(0);
            mostrarReparacionesDeVehiculo(vehiculosData.get(0));
        }
    }
    
    private void mostrarReparacionesDeVehiculo(Vehiculo vehiculo) {
        reparacionesData.clear();
        if (vehiculo != null && vehiculo.getReparaciones() != null) {
            reparacionesData.addAll(vehiculo.getReparaciones());
        }
        reparacionesTable.setItems(reparacionesData);
    }

    @FXML
    private void handleNuevoVehiculo() {
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
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
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
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error en los datos");
                    alert.setContentText("Por favor, introduce valores numéricos válidos para Año y Kilómetros.");
                    alert.showAndWait();
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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al guardar");
                alert.setContentText("No se pudo guardar el vehículo. La matrícula podría estar duplicada.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleEditarVehiculo() {
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
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
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
                mostrarAlerta("Error al guardar", "No se pudo actualizar el vehículo. La matrícula podría estar duplicada.");
            }
        });
    }

    @FXML
    private void handleEliminarVehiculo() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para eliminar.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar este vehículo?");
        alert.setContentText("Se eliminarán también todas las reparaciones asociadas a este vehículo.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteVehiculo(selectedVehiculo.getId())) {
                cargarVehiculos();
            } else {
                mostrarAlerta("Error al eliminar", "No se pudo eliminar el vehículo.");
            }
        }
    }

    @FXML
    private void handleNuevaReparacion() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para añadir una reparación.");
            return;
        }
        
        // Obtener lista de talleres
        List<Taller> talleres = dbManager.getAllTalleres();
        if (talleres.isEmpty()) {
            mostrarAlerta("No hay talleres disponibles", "No hay talleres registrados en el sistema.");
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
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        ComboBox<Taller> tallerCombo = new ComboBox<>(FXCollections.observableArrayList(talleres));
        tallerCombo.setPromptText("Selecciona un taller");
        
        TextArea descripcionArea = new TextArea();
        descripcionArea.setPromptText("Descripción de la reparación");
        descripcionArea.setPrefRowCount(3);
        
        DatePicker fechaPicker = new DatePicker(LocalDate.now());
        
        TextField costoField = new TextField();
        costoField.setPromptText("Costo");
        
        ComboBox<String> estadoCombo = new ComboBox<>(
            FXCollections.observableArrayList("Pendiente", "En progreso", "Completada", "Cancelada")
        );
        estadoCombo.setValue("Pendiente");
        
        grid.add(new Label("Taller:"), 0, 0);
        grid.add(tallerCombo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Fecha:"), 0, 2);
        grid.add(fechaPicker, 1, 2);
        grid.add(new Label("Costo:"), 0, 3);
        grid.add(costoField, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(estadoCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    if (tallerCombo.getValue() == null) {
                        mostrarAlerta("Taller no seleccionado", "Por favor, selecciona un taller.");
                        return null;
                    }
                    
                    Reparacion reparacion = new Reparacion();
                    reparacion.setVehiculoId(selectedVehiculo.getId());
                    reparacion.setTallerId(tallerCombo.getValue().getId());
                    reparacion.setDescripcion(descripcionArea.getText().trim());
                    reparacion.setFechaReparacion(fechaPicker.getValue());
                    reparacion.setCosto(Double.parseDouble(costoField.getText().trim()));
                    reparacion.setEstado(estadoCombo.getValue());
                    
                    return reparacion;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error en los datos", "Por favor, introduce un valor numérico válido para el costo.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Reparacion> result = dialog.showAndWait();
        
        result.ifPresent(reparacion -> {
            if (dbManager.addReparacion(reparacion)) {
                // Actualizar el vehículo seleccionado para mostrar la nueva reparación
                Vehiculo vehiculo = dbManager.getVehiculoById(selectedVehiculo.getId());
                vehiculosTable.getSelectionModel().select(vehiculo);
                mostrarReparacionesDeVehiculo(vehiculo);
            } else {
                mostrarAlerta("Error al guardar", "No se pudo guardar la reparación.");
            }
        });
    }

    @FXML
    private void handleEditarReparacion() {
        Reparacion selectedReparacion = reparacionesTable.getSelectionModel().getSelectedItem();
        if (selectedReparacion == null) {
            mostrarAlerta("No hay reparación seleccionada", "Por favor, selecciona una reparación para editar.");
            return;
        }
        
        // Obtener lista de talleres
        List<Taller> talleres = dbManager.getAllTalleres();
        
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
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        ComboBox<Taller> tallerCombo = new ComboBox<>(FXCollections.observableArrayList(talleres));
        talleres.stream()
                .filter(t -> t.getId() == selectedReparacion.getTallerId())
                .findFirst()
                .ifPresent(tallerCombo::setValue);
        
        TextArea descripcionArea = new TextArea(selectedReparacion.getDescripcion());
        descripcionArea.setPrefRowCount(3);
        
        DatePicker fechaPicker = new DatePicker(selectedReparacion.getFechaReparacion());
        
        TextField costoField = new TextField(String.valueOf(selectedReparacion.getCosto()));
        
        ComboBox<String> estadoCombo = new ComboBox<>(
            FXCollections.observableArrayList("Pendiente", "En progreso", "Completada", "Cancelada")
        );
        estadoCombo.setValue(selectedReparacion.getEstado());
        
        grid.add(new Label("Taller:"), 0, 0);
        grid.add(tallerCombo, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionArea, 1, 1);
        grid.add(new Label("Fecha:"), 0, 2);
        grid.add(fechaPicker, 1, 2);
        grid.add(new Label("Costo:"), 0, 3);
        grid.add(costoField, 1, 3);
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(estadoCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    if (tallerCombo.getValue() == null) {
                        mostrarAlerta("Taller no seleccionado", "Por favor, selecciona un taller.");
                        return null;
                    }
                    
                    Reparacion reparacion = new Reparacion();
                    reparacion.setId(selectedReparacion.getId());
                    reparacion.setVehiculoId(selectedReparacion.getVehiculoId());
                    reparacion.setTallerId(tallerCombo.getValue().getId());
                    reparacion.setDescripcion(descripcionArea.getText().trim());
                    reparacion.setFechaReparacion(fechaPicker.getValue());
                    reparacion.setCosto(Double.parseDouble(costoField.getText().trim()));
                    reparacion.setEstado(estadoCombo.getValue());
                    
                    return reparacion;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error en los datos", "Por favor, introduce un valor numérico válido para el costo.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Reparacion> result = dialog.showAndWait();
        
        result.ifPresent(reparacion -> {
            if (dbManager.updateReparacion(reparacion)) {
                // Actualizar el vehículo seleccionado para mostrar la reparación actualizada
                Vehiculo vehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
                Vehiculo actualizado = dbManager.getVehiculoById(vehiculo.getId());
                mostrarReparacionesDeVehiculo(actualizado);
            } else {
                mostrarAlerta("Error al guardar", "No se pudo actualizar la reparación.");
            }
        });
    }

    @FXML
    private void handleEliminarReparacion() {
        Reparacion selectedReparacion = reparacionesTable.getSelectionModel().getSelectedItem();
        if (selectedReparacion == null) {
            mostrarAlerta("No hay reparación seleccionada", "Por favor, selecciona una reparación para eliminar.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de eliminar esta reparación?");
        alert.setContentText("Esta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteReparacion(selectedReparacion.getId())) {
                // Actualizar el vehículo seleccionado para mostrar las reparaciones actualizadas
                Vehiculo vehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
                Vehiculo actualizado = dbManager.getVehiculoById(vehiculo.getId());
                mostrarReparacionesDeVehiculo(actualizado);
            } else {
                mostrarAlerta("Error al eliminar", "No se pudo eliminar la reparación.");
            }
        }
    }

    @FXML
    private void handleListarVehiculos() {
        cargarVehiculos();
    }

    @FXML
    private void handleBuscarVehiculo() {
        // Crear diálogo para buscar vehículo
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Vehículo");
        dialog.setHeaderText("Introduce la matrícula del vehículo");
        dialog.setContentText("Matrícula:");
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(matricula -> {
            // Filtrar la lista de vehículos por la matrícula
            vehiculosData.stream()
                .filter(v -> v.getMatricula().toLowerCase().contains(matricula.toLowerCase()))
                .findFirst()
                .ifPresentOrElse(
                    vehiculo -> vehiculosTable.getSelectionModel().select(vehiculo),
                    () -> mostrarAlerta("Vehículo no encontrado", "No se encontró ningún vehículo con esa matrícula.")
                );
        });
    }

    @FXML
    private void handleHistorialReparaciones() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para ver su historial de reparaciones.");
            return;
        }
        
        mostrarReparacionesDeVehiculo(selectedVehiculo);
    }

    @FXML
    private void handleActualizarVehiculos() {
        cargarVehiculos();
    }
    
    @FXML
    private void handleActualizarReparaciones() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo != null) {
            // Obtener datos actualizados del vehículo
            Vehiculo actualizado = dbManager.getVehiculoById(selectedVehiculo.getId());
            if (actualizado != null) {
                // Actualizar la selección en la tabla
                int index = vehiculosData.indexOf(selectedVehiculo);
                if (index >= 0) {
                    vehiculosData.set(index, actualizado);
                    vehiculosTable.getSelectionModel().select(index);
                }
                // Mostrar reparaciones actualizadas
                mostrarReparacionesDeVehiculo(actualizado);
            }
        } else {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para ver sus reparaciones.");
        }
    }

    @FXML
    private void handleSalir() {
        Stage stage = (Stage) vehiculosTable.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleCerrarSesion() {
        // Cerrar la sesión actual
        dbManager.logout();
        
        try {
            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            // Obtener la ventana actual y cambiar su contenido
            Stage stage = (Stage) vehiculosTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestión de Reparaciones de Vehículos - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta("Error", "Error al cargar la pantalla de login.");
            e.printStackTrace();
        }
    }
    
    private void mostrarAlerta(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private Vehiculo getVehiculoById(int id) {
        for (Vehiculo vehiculo : vehiculosData) {
            if (vehiculo.getId() == id) {
                return vehiculo;
            }
        }
        return null;
    }
} 