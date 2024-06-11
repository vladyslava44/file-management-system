/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ntu-user
 */
public class UpdateController {

    @FXML
    private Button backToSecBtn;

    @FXML
    private Button updateButton;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField renewPasswordField;

    @FXML
    private Text usernameText;

    @FXML
    private Text errorMessageText;

    @FXML
    private void updateBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) updateButton.getScene().getWindow();

        try {
            FXMLLoader loader = new FXMLLoader();
            DB myObj = new DB();
            User currentUser = AuthService.getCurrentUser();

            if ((AuthService.authenticate(currentUser.getUser(), oldPasswordField.getText())) & (!newPasswordField.getText().equals(oldPasswordField.getText())) & (newPasswordField.getText().equals(renewPasswordField.getText())) & (!renewPasswordField.getText().equals(""))) {
                myObj.updateTable(currentUser.getUser(), newPasswordField.getText());
                dialogue("Updating password", "Successful!");
                AuditTrail audit = new AuditTrail();
                audit.log("User '" + currentUser.getUser() + "' successfully updated their password.");
                String[] credentials = {currentUser.getUser(), newPasswordField.getText()};
                loader.setLocation(getClass().getResource("secondary.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                SecondaryController controller = loader.getController();
                secondaryStage.setTitle("Show users");
                controller.initialise(credentials);
                secondaryStage.show();
                primaryStage.close();
            } else if ((!myObj.validateUser(currentUser.getUser(), oldPasswordField.getText()))) {
                errorMessageText.setText("Your old password is incorrect!");
            } else if (!newPasswordField.getText().equals(renewPasswordField.getText())) {
                errorMessageText.setText("New password must be repeated!");

            } else if ((newPasswordField.getText().equals(oldPasswordField.getText())) & (!newPasswordField.getText().equals(""))) {
                errorMessageText.setText("Old and new passwords are the same!");
            } else {
                errorMessageText.setText("Fill in all the fields");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }

    @FXML
    private void backToSecBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backToSecBtn.getScene().getWindow();
        try {
            User currentUser = AuthService.getCurrentUser();
            FXMLLoader loader = new FXMLLoader();
            String[] credentials = {currentUser.getUser(), newPasswordField.getText()};
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            SecondaryController controller = loader.getController();
            secondaryStage.setTitle("Show users");
            controller.initialise(credentials);
            String msg = "some data sent from uPDATE Controller";
            secondaryStage.setUserData(msg);
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialise(String[] credentials) {
        try {
            User currentUser = AuthService.getCurrentUser();
            usernameText.setText(currentUser.getUser());
            if (AuthService.getCurrentUser() == null) {
                System.out.println("IS LOGGED OUT");
            }
        } catch (Exception ex) {
            Logger.getLogger(UpdateController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
