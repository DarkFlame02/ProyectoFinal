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
            String cssPath = getClass().getResource("/css/css.css").toExternalForm();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
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
    
    public static void loadMainView(Stage stage) throws Exception {
        try {
            String cssPath = MainApp.class.getResource("/css/css.css").toExternalForm();
            
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/rootView.fxml"));
            Parent root = loader.load();
            
            RootController controller = loader.getController();
            controller.setPrimaryStage(stage);
            
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
        DatabaseManager.getInstance().closeConnection();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 