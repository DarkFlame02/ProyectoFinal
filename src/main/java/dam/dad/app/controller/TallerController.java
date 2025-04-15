package dam.dad.app.controller;

import java.util.List;
import java.util.Optional;

import dam.dad.app.db.DatabaseManager;
import dam.dad.app.model.Taller;
import dam.dad.app.model.Valoracion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class TallerController {
    
    @FXML
    private TableView<Taller> talleresTable;
    
    @FXML
    private TableColumn<Taller, String> nombreColumn;
    
    @FXML
    private TableColumn<Taller, String> direccionColumn;
    
    @FXML
    private TableColumn<Taller, String> telefonoColumn;
    
    @FXML
    private TableColumn<Taller, Double> valoracionColumn;
    
    @FXML
    private TableView<Valoracion> valoracionesTable;
    
    @FXML
    private TableColumn<Valoracion, Integer> puntuacionColumn;
    
    @FXML
    private TableColumn<Valoracion, String> comentarioColumn;
    
    private DatabaseManager dbManager;
    private ObservableList<Taller> talleresData = FXCollections.observableArrayList();
    private ObservableList<Valoracion> valoracionesData = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        // Configurar columnas de talleres
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        direccionColumn.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        valoracionColumn.setCellValueFactory(new PropertyValueFactory<>("valoracionMedia"));
        
        // Configurar columnas de valoraciones
        puntuacionColumn.setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        comentarioColumn.setCellValueFactory(new PropertyValueFactory<>("comentario"));
        
        // Cargar datos iniciales
        cargarTalleres();
        
        // Añadir listener para mostrar valoraciones del taller seleccionado
        talleresTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarValoracionesDeTaller(newSelection);
            }
        });
    }
    
    public void cargarTalleres() {
        talleresData.clear();
        List<Taller> talleres = dbManager.getAllTalleres();
        talleresData.addAll(talleres);
        talleresTable.setItems(talleresData);
        
        if (!talleresData.isEmpty()) {
            talleresTable.getSelectionModel().select(0);
            mostrarValoracionesDeTaller(talleresData.get(0));
        }
    }
    
    private void mostrarValoracionesDeTaller(Taller taller) {
        valoracionesData.clear();
        List<Valoracion> valoraciones = dbManager.getValoracionesByTaller(taller.getId());
        valoracionesData.addAll(valoraciones);
        valoracionesTable.setItems(valoracionesData);
    }
    
    @FXML
    public void handleNuevoTaller() {
        // Crear diálogo para nuevo taller
        Dialog<Taller> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Taller");
        dialog.setHeaderText("Introduce los datos del nuevo taller");
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextField direccionField = new TextField();
        direccionField.setPromptText("Dirección");
        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfono");
        
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(direccionField, 1, 1);
        grid.add(new Label("Teléfono:"), 0, 2);
        grid.add(telefonoField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                Taller taller = new Taller();
                taller.setNombre(nombreField.getText().trim());
                taller.setDireccion(direccionField.getText().trim());
                taller.setTelefono(telefonoField.getText().trim());
                return taller;
            }
            return null;
        });
        
        Optional<Taller> result = dialog.showAndWait();
        
        result.ifPresent(taller -> {
            if (dbManager.addTaller(taller)) {
                cargarTalleres();
            } else {
                mostrarAlerta("Error al guardar", "No se pudo guardar el taller.");
            }
        });
    }
    
    @FXML
    public void handleEditarTaller() {
        Taller selectedTaller = talleresTable.getSelectionModel().getSelectedItem();
        if (selectedTaller == null) {
            mostrarAlerta("No hay taller seleccionado", "Por favor, selecciona un taller para editar.");
            return;
        }
        
        // Crear diálogo para editar taller
        Dialog<Taller> dialog = new Dialog<>();
        dialog.setTitle("Editar Taller");
        dialog.setHeaderText("Edita los datos del taller");
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nombreField = new TextField(selectedTaller.getNombre());
        TextField direccionField = new TextField(selectedTaller.getDireccion());
        TextField telefonoField = new TextField(selectedTaller.getTelefono());
        
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(direccionField, 1, 1);
        grid.add(new Label("Teléfono:"), 0, 2);
        grid.add(telefonoField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                Taller taller = new Taller();
                taller.setId(selectedTaller.getId());
                taller.setNombre(nombreField.getText().trim());
                taller.setDireccion(direccionField.getText().trim());
                taller.setTelefono(telefonoField.getText().trim());
                taller.setValoracionMedia(selectedTaller.getValoracionMedia());
                return taller;
            }
            return null;
        });
        
        Optional<Taller> result = dialog.showAndWait();
        
        result.ifPresent(taller -> {
            if (dbManager.updateTaller(taller)) {
                cargarTalleres();
            } else {
                mostrarAlerta("Error al actualizar", "No se pudo actualizar el taller.");
            }
        });
    }
    
    @FXML
    public void handleEliminarTaller() {
        Taller selectedTaller = talleresTable.getSelectionModel().getSelectedItem();
        if (selectedTaller == null) {
            mostrarAlerta("No hay taller seleccionado", "Por favor, selecciona un taller para eliminar.");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar este taller?");
        confirmacion.setContentText("Esta acción eliminará también todas las valoraciones asociadas y podría afectar a las reparaciones.");
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteTaller(selectedTaller.getId())) {
                cargarTalleres();
            } else {
                mostrarAlerta("Error al eliminar", "No se pudo eliminar el taller. Podría tener reparaciones asociadas.");
            }
        }
    }
    
    @FXML
    public void handleNuevaValoracion() {
        Taller selectedTaller = talleresTable.getSelectionModel().getSelectedItem();
        if (selectedTaller == null) {
            mostrarAlerta("No hay taller seleccionado", "Por favor, selecciona un taller para valorar.");
            return;
        }
        
        // Crear diálogo para nueva valoración
        Dialog<Valoracion> dialog = new Dialog<>();
        dialog.setTitle("Nueva Valoración");
        dialog.setHeaderText("Valora el taller: " + selectedTaller.getNombre());
        
        // Botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);
        
        // Contenido del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Slider puntuacionSlider = new Slider(1, 5, 3);
        puntuacionSlider.setShowTickLabels(true);
        puntuacionSlider.setShowTickMarks(true);
        puntuacionSlider.setMajorTickUnit(1);
        puntuacionSlider.setMinorTickCount(0);
        puntuacionSlider.setSnapToTicks(true);
        
        Label puntuacionLabel = new Label("3");
        puntuacionSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            puntuacionLabel.setText(String.valueOf(newVal.intValue()));
        });
        
        TextArea comentarioArea = new TextArea();
        comentarioArea.setPromptText("Comentario (opcional)");
        
        grid.add(new Label("Puntuación:"), 0, 0);
        grid.add(puntuacionSlider, 1, 0);
        grid.add(puntuacionLabel, 2, 0);
        grid.add(new Label("Comentario:"), 0, 1);
        grid.add(comentarioArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir el resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                Valoracion valoracion = new Valoracion();
                valoracion.setTallerId(selectedTaller.getId());
                valoracion.setPuntuacion(puntuacionSlider.valueProperty().intValue());
                valoracion.setComentario(comentarioArea.getText().trim());
                valoracion.setUsuarioId(dbManager.getLoggedInUserId());
                return valoracion;
            }
            return null;
        });
        
        Optional<Valoracion> result = dialog.showAndWait();
        
        result.ifPresent(valoracion -> {
            if (dbManager.addValoracion(valoracion)) {
                cargarTalleres(); // Recarga para actualizar valoración media
                mostrarValoracionesDeTaller(selectedTaller);
            } else {
                mostrarAlerta("Error al guardar", "No se pudo guardar la valoración.");
            }
        });
    }
    
    private void mostrarAlerta(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 