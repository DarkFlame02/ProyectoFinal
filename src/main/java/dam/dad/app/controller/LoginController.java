package dam.dad.app.controller;

import java.io.IOException;
import dam.dad.app.db.DatabaseManager;
import dam.dad.app.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validar campos
        String usernameError = ValidationUtils.validateUsername(username);
        String passwordError = ValidationUtils.validatePassword(password);

        if (usernameError != null) {
            showError(usernameError);
            return;
        }

        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        if (dbManager.validateUser(username, password)) {
            try {
                // Cargar la vista principal
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RootView.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Gestión de Reparaciones de Vehículos");
            } catch (IOException e) {
                showError("Error al cargar la aplicación");
                e.printStackTrace();
            }
        } else {
            showError("Usuario o contraseña incorrectos");
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validar campos
        String usernameError = ValidationUtils.validateUsername(username);
        String passwordError = ValidationUtils.validatePassword(password);

        if (usernameError != null) {
            showError(usernameError);
            return;
        }

        if (passwordError != null) {
            showError(passwordError);
            return;
        }

        if (dbManager.registerUser(username, password)) {
            showSuccess("Usuario registrado correctamente");
            // Limpiar campos
            usernameField.clear();
            passwordField.clear();
        } else {
            showError("El usuario ya existe");
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        successLabel.setText("");
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        errorLabel.setText("");
    }
} 