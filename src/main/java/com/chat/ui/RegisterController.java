package com.chat.ui;

import com.chat.common.DatabaseUtil;
import com.chat.common.User;
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

import java.time.LocalDateTime;

@Component
public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorMessageLabel;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LoginController loginController;

    @FXML
    private void handleRegisterButton(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorMessageLabel.setText("Please fill in all fields");
            errorMessageLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessageLabel.setText("Passwords do not match");
            errorMessageLabel.setVisible(true);
            return;
        }

        User newUser = new User(username, password);
        newUser.setOnline(false);
        newUser.setLastSeen(LocalDateTime.now().toString()); // Set initial last seen

        if (DatabaseUtil.registerUser(newUser)) {
            System.out.println("Registration successful for user: " + username);
            // Go back to login view
            Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
            stage.close();
            loginController.showLoginView(new Stage());
        } else {
            errorMessageLabel.setText("Registration failed. Username might already exist.");
            errorMessageLabel.setVisible(true);
        }
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        // Go back to login view
        Stage stage = (Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow();
        stage.close();
        loginController.showLoginView(new Stage());
    }

    public void showRegisterView() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            // You might want to log this error or show it in a dialog
            System.err.println("Error loading register view: " + e.getMessage());
        }
    }
} 