package dam.dad;

import dam.dad.controllers.RootController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application{
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("App");
        stage.setScene(new Scene(new RootController().getRoot()));
        stage.show();
    }
}
