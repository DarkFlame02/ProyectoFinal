package dam.dad.app.controller;

import dam.dad.app.MainApp;
import dam.dad.app.db.DatabaseManager;
import dam.dad.app.util.ValidationUtils;
import javafx.fxml.FXML;
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
                Stage stage = (Stage) usernameField.getScene().getWindow();
                MainApp.loadMainView(stage);
            } catch (Exception e) {
                showError("Error al cargar la aplicación: " + e.getMessage());
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