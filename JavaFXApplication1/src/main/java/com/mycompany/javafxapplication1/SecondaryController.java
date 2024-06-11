package com.mycompany.javafxapplication1;

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
import java.util.Optional;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

public class SecondaryController {

    private String[] userInfo;

    @FXML
    private TableView dataTableView;

    @FXML
    private Button secondaryButton;

    @FXML
    private Button refreshBtn;

    @FXML
    private TextField customTextField;

    @FXML
    private Button updateButton;

    @FXML
    private Text currentUserText;

    @FXML
    private Button deleteAccButton;

    @FXML
    private Button terminalButton;

    @FXML
    private Button fileButton;

    @FXML
    private Button remoteButton;

    @FXML
    private void switchToRemote(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) remoteButton.getScene().getWindow();
        try {
            if (AuthService.isLoggedIn() == true) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("remote.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                secondaryStage.setTitle("Remote Terminal Emulation");
                secondaryStage.show();
                primaryStage.close();
            } else {
                System.out.println("Error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToTerminal(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) terminalButton.getScene().getWindow();
        try {
            if (AuthService.isLoggedIn() == true) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("terminal.fxml"));
                Parent root = loader.load();
                User currentUser = AuthService.getCurrentUser();
                String[] credentials = {currentUser.getUser()};
                TerminalController controller = loader.getController();
                controller.initialise(credentials);
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                secondaryStage.setTitle("Terminal");
                secondaryStage.show();
                primaryStage.close();
            } else {
                System.out.println("Error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToFile(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) fileButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("third.fxml"));
            Parent root = loader.load();
            User currentUser = AuthService.getCurrentUser();
            String[] credentials = {currentUser.getUser()};
            ThirdController controller = loader.getController();
            controller.initialise(userInfo);
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("File");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void RefreshBtnHandler(ActionEvent event) {
        Stage primaryStage = (Stage) customTextField.getScene().getWindow();
        customTextField.setText((String) primaryStage.getUserData());
    }

    @FXML
    private void switchToPrimary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) secondaryButton.getScene().getWindow();
        try {
            User currentUser = AuthService.getCurrentUser();
            System.out.print(currentUser.getUser());
            AuditTrail audit = new AuditTrail();
            audit.log("User '" + currentUser.getUser() + "' logged out.");
            AuthService.logout();
            AuthService.checkStatus();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Login");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToUpdate(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) updateButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("update.fxml"));
            Parent root = loader.load();
            User currentUser = AuthService.getCurrentUser();
            String[] credentials = {currentUser.getUser()};
            UpdateController controller = loader.getController();
            controller.initialise(credentials);
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Update");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteAcc(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) deleteAccButton.getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader();
            DB myObj = new DB();
            User currentUser = AuthService.getCurrentUser();

            if (AuthService.isLoggedIn()) {
                myObj.deleteAccount(currentUser.getUser(), currentUser.getPass());
                System.out.print(currentUser.getUser());
                dialogue("Confirmation", "Deleting account", "Successful!", Alert.AlertType.CONFIRMATION);
                AuditTrail audit = new AuditTrail();
                audit.log("User '" + currentUser.getUser() + "' deleted their account.");
                AuthService.logout();
                AuthService.checkStatus();
                loader.setLocation(getClass().getResource("primary.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                secondaryStage.setTitle("Primary");
                secondaryStage.show();
                primaryStage.close();
            } else {
                dialogue("Warning", "Deleting account", "Failed", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dialogue(String titleMsg, String headerMsg, String contentMsg, Alert.AlertType anAlertType) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(anAlertType);
        alert.setTitle(titleMsg);
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }

    public void initialise(String[] credentials) {

        if (AuthService.isLoggedIn()) {
            User currentUser = AuthService.getCurrentUser();
            currentUserText.setText("Welcome, " + currentUser.getUser() + "!");
        }

        DB myObj = new DB();
        ObservableList<User> data;
        try {
            data = myObj.getDataFromTable();
            TableColumn user = new TableColumn("User");
            user.setCellValueFactory(
                    new PropertyValueFactory<>("user"));
            TableColumn pass = new TableColumn("Pass");
            pass.setCellValueFactory(
                    new PropertyValueFactory<>("pass"));
            dataTableView.setItems(data);
            dataTableView.getColumns().addAll(user, pass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
        userInfo = credentials;
    }
}
