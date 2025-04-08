package dam.dad.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RootController {
    @FXML
    private TableView<?> vehiculosTable;
    @FXML
    private TableView<?> reparacionesTable;
    @FXML
    private TableColumn<?, ?> matriculaColumn;
    @FXML
    private TableColumn<?, ?> marcaColumn;
    @FXML
    private TableColumn<?, ?> modeloColumn;
    @FXML
    private TableColumn<?, ?> anioColumn;
    @FXML
    private TableColumn<?, ?> kilometrosColumn;
    @FXML
    private TableColumn<?, ?> fechaColumn;
    @FXML
    private TableColumn<?, ?> vehiculoColumn;
    @FXML
    private TableColumn<?, ?> descripcionColumn;
    @FXML
    private TableColumn<?, ?> costoColumn;
    @FXML
    private TableColumn<?, ?> tallerColumn;

    @FXML
    private void handleNuevoVehiculo() {
        // TODO: Implementar lógica para nuevo vehículo
    }

    @FXML
    private void handleEditarVehiculo() {
        // TODO: Implementar lógica para editar vehículo
    }

    @FXML
    private void handleEliminarVehiculo() {
        // TODO: Implementar lógica para eliminar vehículo
    }

    @FXML
    private void handleNuevaReparacion() {
        // TODO: Implementar lógica para nueva reparación
    }

    @FXML
    private void handleEditarReparacion() {
        // TODO: Implementar lógica para editar reparación
    }

    @FXML
    private void handleEliminarReparacion() {
        // TODO: Implementar lógica para eliminar reparación
    }

    @FXML
    private void handleListarVehiculos() {
        // TODO: Implementar lógica para listar vehículos
    }

    @FXML
    private void handleBuscarVehiculo() {
        // TODO: Implementar lógica para buscar vehículo
    }

    @FXML
    private void handleHistorialReparaciones() {
        // TODO: Implementar lógica para historial de reparaciones
    }

    @FXML
    private void handleSalir() {
        Stage stage = (Stage) vehiculosTable.getScene().getWindow();
        stage.close();
    }
} 