package com.chat;

import com.chat.common.DatabaseUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.chat.ui.MainView;
import com.chat.ui.LoginController;

public class Main extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(WhatsAppApplication.class)
                .headless(false)
                .run();
        DatabaseUtil.initialize(); // Initialize the database tables
    }

    @Override
    public void start(Stage primaryStage) {
        LoginController loginController = springContext.getBean(LoginController.class);
        loginController.showLoginView(primaryStage); // Show login view first
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
