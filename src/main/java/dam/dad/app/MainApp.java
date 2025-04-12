package dam.dad.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import dam.dad.app.controller.RootController;
import dam.dad.app.db.DatabaseManager;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Cargar el CSS
            String cssPath = getClass().getResource("/css/css.css").toExternalForm();
            
            // Cargar la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            // Aplicar CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Método para cargar la vista principal después del login
     */
    public static void loadMainView(Stage stage) throws Exception {
        try {
            // Cargar el CSS
            String cssPath = MainApp.class.getResource("/css/css.css").toExternalForm();
            
            // Cargar la vista principal
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/rootView.fxml"));
            Parent root = loader.load();
            
            // Configurar el controlador
            RootController controller = loader.getController();
            controller.setPrimaryStage(stage);
            
            // Aplicar CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssPath);
            
            stage.setTitle("Gestión de Reparaciones de Vehículos");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        // Cerrar la conexión a la base de datos al cerrar la aplicación
        DatabaseManager.getInstance().closeConnection();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 