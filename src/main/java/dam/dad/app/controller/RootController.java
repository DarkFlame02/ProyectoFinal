package dam.dad.app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        // Crear diálogo para opciones de listado
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Opciones de Listado");
        dialog.setHeaderText("Selecciona las opciones de ordenación");

        // Crear botones
        ButtonType aplicarButtonType = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(aplicarButtonType, ButtonType.CANCEL);

        // Crear contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Ordenación
        Label ordenLabel = new Label("Ordenar por:");
        ComboBox<String> ordenCombo = new ComboBox<>();
        ordenCombo.getItems().addAll("Matrícula", "Marca", "Modelo", "Año", "Kilómetros");
        ordenCombo.setValue("Matrícula");

        // Dirección de ordenación
        Label direccionLabel = new Label("Dirección:");
        ComboBox<String> direccionCombo = new ComboBox<>();
        direccionCombo.getItems().addAll("Ascendente", "Descendente");
        direccionCombo.setValue("Ascendente");

        // Añadir controles al grid
        grid.add(ordenLabel, 0, 0);
        grid.add(ordenCombo, 1, 0);
        grid.add(direccionLabel, 0, 1);
        grid.add(direccionCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(response -> {
            if (response == aplicarButtonType) {
                // Obtener vehículos
                List<Vehiculo> vehiculos = dbManager.getVehiculosByUsuario();
                
                // Aplicar ordenación
                String criterioOrden = ordenCombo.getValue();
                boolean ascendente = direccionCombo.getValue().equals("Ascendente");
                
                vehiculos.sort((v1, v2) -> {
                    int resultado = 0;
                    switch (criterioOrden) {
                        case "Matrícula":
                            resultado = v1.getMatricula().compareTo(v2.getMatricula());
                            break;
                        case "Marca":
                            resultado = v1.getMarca().compareTo(v2.getMarca());
                            break;
                        case "Modelo":
                            resultado = v1.getModelo().compareTo(v2.getModelo());
                            break;
                        case "Año":
                            resultado = Integer.compare(v1.getAnio(), v2.getAnio());
                            break;
                        case "Kilómetros":
                            resultado = Integer.compare(v1.getKilometros(), v2.getKilometros());
                            break;
                    }
                    return ascendente ? resultado : -resultado;
                });
                
                // Actualizar la tabla
                vehiculosData.clear();
                vehiculosData.addAll(vehiculos);
                vehiculosTable.setItems(vehiculosData);
                
                // Seleccionar el primer vehículo si hay alguno
                if (!vehiculosData.isEmpty()) {
                    vehiculosTable.getSelectionModel().select(0);
                    mostrarReparacionesDeVehiculo(vehiculosData.get(0));
                }
            }
        });
    }

    @FXML
    private void handleBuscarVehiculo() {
        // Crear diálogo para buscar vehículo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Buscar Vehículo");
        dialog.setHeaderText("Introduce los criterios de búsqueda");

        // Crear botones
        ButtonType buscarButtonType = new ButtonType("Buscar", ButtonBar.ButtonData.OK_DONE);
        ButtonType limpiarButtonType = new ButtonType("Limpiar", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(buscarButtonType, limpiarButtonType, ButtonType.CANCEL);

        // Crear contenido del diálogo
        VBox vbox = new VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20, 20, 20, 20));

        // Grupo de identificación
        TitledPane identificacionPane = new TitledPane();
        identificacionPane.setText("Identificación");
        identificacionPane.setExpanded(true);
        
        GridPane identificacionGrid = new GridPane();
        identificacionGrid.setHgap(10);
        identificacionGrid.setVgap(10);
        identificacionGrid.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

        TextField matriculaField = new TextField();
        matriculaField.setPromptText("Matrícula");
        matriculaField.setPrefWidth(200);
        
        TextField marcaField = new TextField();
        marcaField.setPromptText("Marca");
        marcaField.setPrefWidth(200);
        
        TextField modeloField = new TextField();
        modeloField.setPromptText("Modelo");
        modeloField.setPrefWidth(200);

        identificacionGrid.add(new Label("Matrícula:"), 0, 0);
        identificacionGrid.add(matriculaField, 1, 0);
        identificacionGrid.add(new Label("Marca:"), 0, 1);
        identificacionGrid.add(marcaField, 1, 1);
        identificacionGrid.add(new Label("Modelo:"), 0, 2);
        identificacionGrid.add(modeloField, 1, 2);

        identificacionPane.setContent(identificacionGrid);

        // Grupo de características
        TitledPane caracteristicasPane = new TitledPane();
        caracteristicasPane.setText("Características");
        caracteristicasPane.setExpanded(true);
        
        GridPane caracteristicasGrid = new GridPane();
        caracteristicasGrid.setHgap(10);
        caracteristicasGrid.setVgap(10);
        caracteristicasGrid.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

        TextField anioField = new TextField();
        anioField.setPromptText("Año");
        anioField.setPrefWidth(200);
        
        TextField kilometrosField = new TextField();
        kilometrosField.setPromptText("Kilómetros");
        kilometrosField.setPrefWidth(200);

        // Añadir validadores para campos numéricos
        anioField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                anioField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        kilometrosField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                kilometrosField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        caracteristicasGrid.add(new Label("Año:"), 0, 0);
        caracteristicasGrid.add(anioField, 1, 0);
        caracteristicasGrid.add(new Label("Kilómetros:"), 0, 1);
        caracteristicasGrid.add(kilometrosField, 1, 1);

        caracteristicasPane.setContent(caracteristicasGrid);

        // Añadir los paneles al VBox
        vbox.getChildren().addAll(identificacionPane, caracteristicasPane);

        // Añadir un separador
        Separator separator = new Separator();
        vbox.getChildren().add(separator);

        // Añadir un label informativo
        Label infoLabel = new Label("Deja los campos vacíos para ignorarlos en la búsqueda");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        vbox.getChildren().add(infoLabel);

        dialog.getDialogPane().setContent(vbox);

        // Manejar el botón Limpiar
        Button limpiarButton = (Button) dialog.getDialogPane().lookupButton(limpiarButtonType);
        limpiarButton.setOnAction(e -> {
            matriculaField.clear();
            marcaField.clear();
            modeloField.clear();
            anioField.clear();
            kilometrosField.clear();
        });

        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(response -> {
            if (response == buscarButtonType) {
                // Obtener valores de búsqueda
                String matricula = matriculaField.getText().trim().toLowerCase();
                String marca = marcaField.getText().trim().toLowerCase();
                String modelo = modeloField.getText().trim().toLowerCase();
                String anioStr = anioField.getText().trim();
                String kilometrosStr = kilometrosField.getText().trim();

                // Filtrar vehículos
                List<Vehiculo> resultados = vehiculosData.stream()
                    .filter(v -> matricula.isEmpty() || v.getMatricula().toLowerCase().contains(matricula))
                    .filter(v -> marca.isEmpty() || v.getMarca().toLowerCase().contains(marca))
                    .filter(v -> modelo.isEmpty() || v.getModelo().toLowerCase().contains(modelo))
                    .filter(v -> {
                        if (anioStr.isEmpty()) return true;
                        try {
                            int anio = Integer.parseInt(anioStr);
                            return v.getAnio() == anio;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .filter(v -> {
                        if (kilometrosStr.isEmpty()) return true;
                        try {
                            int kilometros = Integer.parseInt(kilometrosStr);
                            return v.getKilometros() == kilometros;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

                if (resultados.isEmpty()) {
                    mostrarAlerta("Búsqueda sin resultados", "No se encontraron vehículos que coincidan con los criterios de búsqueda.");
                } else {
                    // Actualizar la tabla con los resultados
                    vehiculosData.clear();
                    vehiculosData.addAll(resultados);
                    vehiculosTable.setItems(vehiculosData);
                    
                    // Seleccionar el primer resultado
                    vehiculosTable.getSelectionModel().select(0);
                    mostrarReparacionesDeVehiculo(resultados.get(0));
                }
            }
        });
    }

    @FXML
    private void handleHistorialReparaciones() {
        Vehiculo selectedVehiculo = vehiculosTable.getSelectionModel().getSelectedItem();
        if (selectedVehiculo == null) {
            mostrarAlerta("No hay vehículo seleccionado", "Por favor, selecciona un vehículo para ver su historial de reparaciones.");
            return;
        }
        
        // Crear diálogo para mostrar el historial
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de Reparaciones");
        dialog.setHeaderText("Historial de reparaciones para: " + selectedVehiculo.toString());
        
        // Botones
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        // Contenido principal
        VBox vbox = new VBox(5);
        vbox.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));
        
        // Panel de filtros y estadísticas en una fila
        HBox topBox = new HBox(10);
        topBox.setPadding(new javafx.geometry.Insets(0, 0, 10, 0));
        
        // Panel de filtros
        VBox filtrosBox = new VBox(5);
        filtrosBox.setPadding(new javafx.geometry.Insets(5));
        filtrosBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        
        Label filtrosLabel = new Label("Filtros y Ordenación");
        filtrosLabel.setStyle("-fx-font-weight: bold;");
        
        GridPane filtrosGrid = new GridPane();
        filtrosGrid.setHgap(5);
        filtrosGrid.setVgap(5);
        
        ComboBox<String> estadoFilter = new ComboBox<>(
            FXCollections.observableArrayList("Todos", "Pendiente", "En progreso", "Completada", "Cancelada")
        );
        estadoFilter.setValue("Todos");
        
        ComboBox<String> ordenCombo = new ComboBox<>(
            FXCollections.observableArrayList("Fecha", "Costo", "Estado")
        );
        ordenCombo.setValue("Fecha");
        
        ComboBox<String> direccionCombo = new ComboBox<>(
            FXCollections.observableArrayList("Ascendente", "Descendente")
        );
        direccionCombo.setValue("Descendente");
        
        filtrosGrid.add(new Label("Estado:"), 0, 0);
        filtrosGrid.add(estadoFilter, 1, 0);
        filtrosGrid.add(new Label("Ordenar:"), 0, 1);
        filtrosGrid.add(ordenCombo, 1, 1);
        filtrosGrid.add(new Label("Dirección:"), 0, 2);
        filtrosGrid.add(direccionCombo, 1, 2);
        
        filtrosBox.getChildren().addAll(filtrosLabel, filtrosGrid);
        
        // Panel de estadísticas
        VBox statsBox = new VBox(5);
        statsBox.setPadding(new javafx.geometry.Insets(5));
        statsBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 5;");
        
        Label statsLabel = new Label("Estadísticas");
        statsLabel.setStyle("-fx-font-weight: bold;");
        
        Label totalReparaciones = new Label();
        Label costoTotal = new Label();
        Label promedioCosto = new Label();
        Label estadoMasComun = new Label();
        
        statsBox.getChildren().addAll(
            statsLabel,
            totalReparaciones,
            costoTotal,
            promedioCosto,
            estadoMasComun
        );
        
        topBox.getChildren().addAll(filtrosBox, statsBox);
        
        // Tabla de reparaciones
        TableView<Reparacion> historialTable = new TableView<>();
        historialTable.setPrefHeight(200);
        
        TableColumn<Reparacion, LocalDate> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(new PropertyValueFactory<>("fechaReparacion"));
        fechaCol.setPrefWidth(100);
        
        TableColumn<Reparacion, String> descripcionCol = new TableColumn<>("Descripción");
        descripcionCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        descripcionCol.setPrefWidth(200);
        
        TableColumn<Reparacion, Double> costoCol = new TableColumn<>("Costo");
        costoCol.setCellValueFactory(new PropertyValueFactory<>("costo"));
        costoCol.setPrefWidth(80);
        
        TableColumn<Reparacion, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        estadoCol.setPrefWidth(100);
        
        historialTable.getColumns().addAll(fechaCol, descripcionCol, costoCol, estadoCol);
        
        // Función para actualizar la tabla y estadísticas
        Runnable updateTable = () -> {
            List<Reparacion> reparaciones = selectedVehiculo.getReparaciones();
            
            // Aplicar filtro de estado
            if (!estadoFilter.getValue().equals("Todos")) {
                reparaciones = reparaciones.stream()
                    .filter(r -> r.getEstado().equals(estadoFilter.getValue()))
                    .collect(Collectors.toList());
            }
            
            // Aplicar ordenación
            String criterioOrden = ordenCombo.getValue();
            boolean ascendente = direccionCombo.getValue().equals("Ascendente");
            
            reparaciones.sort((r1, r2) -> {
                int resultado = 0;
                switch (criterioOrden) {
                    case "Fecha":
                        resultado = r1.getFechaReparacion().compareTo(r2.getFechaReparacion());
                        break;
                    case "Costo":
                        resultado = Double.compare(r1.getCosto(), r2.getCosto());
                        break;
                    case "Estado":
                        resultado = r1.getEstado().compareTo(r2.getEstado());
                        break;
                }
                return ascendente ? resultado : -resultado;
            });
            
            // Actualizar tabla
            historialTable.setItems(FXCollections.observableArrayList(reparaciones));
            
            // Actualizar estadísticas
            totalReparaciones.setText("Total: " + reparaciones.size() + " reparaciones");
            
            double totalCosto = reparaciones.stream()
                .mapToDouble(Reparacion::getCosto)
                .sum();
            costoTotal.setText("Total: " + String.format("%.2f €", totalCosto));
            
            double promedio = reparaciones.isEmpty() ? 0 : totalCosto / reparaciones.size();
            promedioCosto.setText("Promedio: " + String.format("%.2f €", promedio));
            
            // Calcular estado más común
            String estadoMasFrecuente = reparaciones.stream()
                .collect(Collectors.groupingBy(Reparacion::getEstado, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No hay reparaciones");
            estadoMasComun.setText("Estado más común: " + estadoMasFrecuente);
        };
        
        // Añadir listeners a los filtros
        estadoFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateTable.run());
        ordenCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateTable.run());
        direccionCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateTable.run());
        
        // Añadir todo al VBox principal
        vbox.getChildren().addAll(topBox, historialTable);
        
        // Añadir un label informativo
        Label infoLabel = new Label("Los filtros y la ordenación se aplican en tiempo real");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        vbox.getChildren().add(infoLabel);
        
        dialog.getDialogPane().setContent(vbox);
        
        // Actualizar tabla inicialmente
        updateTable.run();
        
        // Mostrar diálogo
        dialog.showAndWait();
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