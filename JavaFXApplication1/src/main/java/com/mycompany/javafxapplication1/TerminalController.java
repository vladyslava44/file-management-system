/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.BufferedReader;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.Vector;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Pagination;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author ntu-user
 */
public class TerminalController {

    @FXML
    private Button submitTerminalButton;

    @FXML
    private TextField terminalTextField;

    @FXML
    private TextArea terminalTextArea;

    @FXML
    private Text warningText;

    @FXML
    private Label terminalLabel;

    @FXML
    private Button backButton;

    @FXML
    private void switchToSecondary(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backButton.getScene().getWindow();
        try {
            User currentUser = AuthService.getCurrentUser();
            FXMLLoader loader = new FXMLLoader();
            String[] credentials = {currentUser.getUser(), ""};
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            SecondaryController controller = loader.getController();
            secondaryStage.setTitle("Show users");
            controller.initialise(credentials);
            String msg = "some data sent from Register Controller";
            secondaryStage.setUserData(msg);
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private String submitTerminalButtonHandler(ActionEvent event) throws IOException {
        warningText.setText("");
        User currentUser = AuthService.getCurrentUser();

        Vector<String> allowedCommands = new Vector<>(Arrays.asList("mv", "cp", "ls", "mkdir", "ps", "whoami", "tree", "nano"));

        String inputCommand = terminalTextField.getText().trim();
        String result = "";
        String[] commandParts = inputCommand.split(" ");

        // checking if the command is allowed
        try {
            if (!allowedCommands.contains(commandParts[0])) {
                warningText.setText("This command doesn't exist. Try again.");
                result = "This command doesn't exist. Try again.";
                return result;

            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (allowedCommands.contains(commandParts[0])) {
            var processBuilder = new ProcessBuilder();

            if (commandParts[0].equals("nano")) {
                processBuilder.command("terminator", "-e", "nano");
            } else {
                processBuilder.command(commandParts);
            }

            terminalTextArea.appendText("$ " + inputCommand + "\n");
            var process = processBuilder.start();

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    terminalTextArea.appendText(line + "\n");
                    result = line;
                }
                AuditTrail audit = new AuditTrail();
                audit.log("User '" + currentUser.getUser() + "' successfully ran the '" + inputCommand + "' command.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        terminalTextField.setText("");
        terminalTextArea.appendText("\n");
        return result;
    }

    public void initialise(String[] credentials) {

        terminalTextField.setPromptText("mv, cp, ls, mkdir, ps, whoami, tree, nano");

        // Event listener to clear the prompt text when the user starts typing
        terminalTextField.setOnKeyTyped(event -> {
            terminalTextField.setPromptText("");
        });

        terminalTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && terminalTextField.getText().isEmpty()) {
                terminalTextField.setPromptText("mv, cp, ls, mkdir, ps, whoami, tree, nano");
            }
        });
        terminalTextField.setOnKeyTyped(event -> {
            warningText.setText("");
        });
        terminalTextArea.setEditable(false);
    }
}
