package com.chat.ui;

import com.chat.common.DatabaseUtil;
import com.chat.ui.MainView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessageLabel;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MainView mainView;

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (DatabaseUtil.authenticateUser(username, password)) {
            System.out.println("Login successful for user: " + username);
            // Close login stage and open main chat view
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            stage.close();
            mainView.start(new Stage()); // Assuming MainView takes a new Stage
        } else {
            errorMessageLabel.setText("Invalid username or password");
            errorMessageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleRegisterButton(ActionEvent event) {
        // Open registration view
        System.out.println("Opening registration view");
        Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
        stage.close();
        // TODO: Load and display the RegisterView.fxml
        // For now, let's just create a new stage for the register view
        RegisterController registerController = applicationContext.getBean(RegisterController.class);
        registerController.showRegisterView();
    }

    public void showLoginView(Stage stage) {
        try {
            // Load the FXML manually as SpringBootFXMLLoader does not handle this directly for the initial view
            // You might need to adjust this if you are using a custom FXMLLoader setup
            // For simplicity, we are assuming Main.java handles the initial FXML loading for now.
            System.out.println("Displaying login view.");
            // If this method is called, it means we are trying to re-show the login view, so we load it.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            loader.setControllerFactory(applicationContext::getBean); // Integrate with Spring
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            errorMessageLabel.setText("Error loading login view");
            errorMessageLabel.setVisible(true);
        }
    }
} 