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
        // Cargar la vista de login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Gestión de Reparaciones de Vehículos - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Método para cargar la vista principal después del login
     */
    public static void loadMainView(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/rootView.fxml"));
        Parent root = loader.load();
        
        RootController controller = loader.getController();
        controller.setPrimaryStage(stage);
        
        Scene scene = new Scene(root);
        stage.setTitle("Gestión de Reparaciones de Vehículos");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
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