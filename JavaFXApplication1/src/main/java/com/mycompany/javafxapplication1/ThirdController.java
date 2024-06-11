/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;

//<dependency>
//<groupId>net.lingala.zzip4j</groupId>
//<arrtifactId>zip4j</artifactId>
//<version>2.9.0</version>
//</dependency>
//test push brandan 2
import com.jcraft.jsch.*;
import java.io.File;
import java.util.Scanner;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;

/**
 *
 * @author ntu-user
 */
public class ThirdController {

    private static final String REMOTE_HOST_1 = "comp20081-files-container1";
    private static final String REMOTE_HOST_2 = "comp20081-files-container2";
    private static final String REMOTE_HOST_3 = "comp20081-files-container3";
    private static final String REMOTE_HOST_4 = "comp20081-files-container4";
    private static final int chunkPartsLeng = 4;

    private static final String USERNAME = "root";
    private static final String PASSWORD = "ntu-user";
    private static final int REMOTE_PORT = 22;
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;

    private String[] userInfo;
    private String path = "/home/ntu-user/app";
    private String remoteFilePath = "/root/";

    @FXML
    private Button CreateBtn;

    @FXML
    private Button updatefileBtn;

    @FXML
    private Button readFileBtn;

    @FXML
    private Button givePermBtn;

    @FXML
    private Button deletefileBtn;

    @FXML
    private Button backButton;

    @FXML
    private Button revokePermBtn;

    @FXML
    private TextField filecontentTextField;

    @FXML
    private TextField filenameTextField;

    @FXML
    private TextField readContentTextField;

    @FXML
    private TextField userPermField;

    @FXML
    private CheckBox writeCheckBox;

    @FXML
    private CheckBox readCheckBox;
    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private Button setBtn;

    public void dialogue(String titleMsg, String headerMsg, String contentMsg, Alert.AlertType anAlertType) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(anAlertType);
        alert.setTitle(titleMsg);
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }

    @FXML
    private void setButtonAction(ActionEvent event) {
        String selectedOption = choiceBox.getValue();
        filenameTextField.setText(selectedOption);
    }

    public static long calculateCRC32(String chunkFilePath) throws IOException {
        CRC32 crc32 = new CRC32();

        try (FileInputStream fis = new FileInputStream(chunkFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }
        }

        return crc32.getValue();
    }

    @FXML
    private void createFile(ActionEvent event) throws ClassNotFoundException {

        AuditTrail audit = new AuditTrail();
        if (!filenameTextField.getText().isEmpty()) {

            DB myObj = new DB();
            User currentUser = AuthService.getCurrentUser();
            String user = currentUser.getUser();
            boolean isOwner = myObj.isFileExists(user, filenameTextField.getText());
            String fileOwner = myObj.isOwner(filenameTextField.getText(), user, isOwner);
            boolean hasPermissions = false;

            if (!isOwner) {
                boolean permission = myObj.isPermissionGiven(fileOwner, filenameTextField.getText(), user, true, true, false, true);
                if (permission) {
                    hasPermissions = true;
                }

            }

            if (!myObj.isFileExists(user, filenameTextField.getText()) && !hasPermissions) {

                File directory = new File(path + File.separator + userInfo[0]);
                if (!directory.exists()) {
                    if (directory.mkdirs()) {
                        System.out.println("Directory created: " + directory);
                    } else {
                        throw new RuntimeException("Failed to create directory: " + directory);
                    }
                }

                String fileName = filenameTextField.getText();
                String fileContent = filecontentTextField.getText();

                SecretKey secretKey = generateSecretKey();
                System.out.println("THis is the key from dtaabase" + secretKey);

                byte[] keyBytes = secretKey.getEncoded();
                String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
                byte[] encryptedContent = encrypt(fileContent, secretKey);
                String encodedcontent = Base64.getEncoder().encodeToString(encryptedContent);

                int chunkSize = (encodedcontent.length() / chunkPartsLeng);
                String extension = ".txt";
                System.out.println(encryptedContent.length);
                System.out.println(fileContent.length());
                int filesize = 0;
                String chunkFileName;
                String chunkFilePath = null;
                try {

                    for (int i = 0; i < encodedcontent.length(); i += chunkSize) {
                        int endIndex = Math.min(i + chunkSize, encodedcontent.length());
                        //byte[] chunk = Arrays.copyOfRange(encryptedContent, i, endIndex);
                        String chunkString = encodedcontent.substring(i, endIndex);

                        System.out.println(chunkString);

                        chunkFileName = fileName + "_part_" + (i / chunkSize + 1) + ".txt";
                        System.out.println("file:" + i / chunkSize + 1);
                        chunkFilePath = path + File.separator + userInfo[0] + File.separator + chunkFileName;

                        try (FileOutputStream fos = new FileOutputStream(chunkFilePath)) {

                            System.out.println(chunkFileName + ":");
                            System.out.println(chunkString);
                            filesize = filesize + chunkString.length();
                            //String encodedchunk = Base64.getEncoder().encodeToString(chunk);
                            //fos.write(encodedchunk.getBytes(StandardCharsets.UTF_8));
                            fos.write(chunkString.getBytes(StandardCharsets.UTF_8));
                            fos.close();
                            long crc32Value = calculateCRC32(chunkFilePath);
                            System.out.println("The crc value for this file is: " + crc32Value);
                            ScpTo(chunkFilePath, chunkFileName);
                            File chunkFile = new File(chunkFilePath);
                            if (chunkFile.exists()) {
                                if (chunkFile.delete()) {
                                    System.out.println("Chunk file deleted: " + chunkFilePath);
                                } else {
                                    System.err.println("Failed to delete chunk file: " + chunkFilePath);
                                }
                            } else {
                                System.err.println("Chunk file does not exist: " + chunkFilePath);
                            }
                            //scpFrom(chunkFilePath, chunkFileName);
                        }

                        //byte[] fileData = readFromFile("test1_part_" + (i / chunkSize + 1) + ".txt");
                        //String fileData2 = new String(fileData, StandardCharsets.UTF_8);
                        //System.out.println("this i whats in the file" + fileData2);
                    }

                    System.out.println("File Created: " + fileName);
                    myObj.addDataToFileDB(fileName, userInfo[0], filesize, encodedKey);
                    audit.log("User '" + currentUser.getUser() + "' created a new file named '" + fileName + ".txt'.");
                    dialogue("Confirmation", "File '" + fileName + "' created", "Successful!", Alert.AlertType.CONFIRMATION);

                } catch (IOException e) {
                    System.out.println("An error occured.");
                    e.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ThirdController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                System.out.println("File has already been created!");
                audit.log("User '" + currentUser.getUser() + "' attempted to create a file, but the file already exists.");
                dialogue("Warning", "File has already been created!", "Failed", Alert.AlertType.ERROR);

            }
        } else {
            System.out.println("File name text field is empty");
            dialogue("Warning", "File name text field is empty!", "Failed", Alert.AlertType.ERROR);
        }

    }

    @FXML
    private void recoverFile(ActionEvent event) throws ClassNotFoundException {
        AuditTrail audit = new AuditTrail();
        if (!filenameTextField.getText().isEmpty()) {
            DB myObj = new DB();
            String fileName = filenameTextField.getText();
            String currentUser = AuthService.getCurrentUser().getUser();
            boolean isOwner = myObj.isFileExists(currentUser, fileName);
            String fileOwner = myObj.isOwner(fileName, currentUser, isOwner);
            boolean hasPermissions = false;
            if (!isOwner) {
                boolean permission1 = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, false, true, false);
                boolean permission2 = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, true, true, false);
                if (permission1 || permission2) {
                    hasPermissions = true;
                }
            }
            if (isOwner) {

                if (myObj.isFileDeleted(fileOwner, fileName)) {

                    String binFolderPath = path + File.separator + userInfo[0] + "_bin";
                    boolean recovering = true;
                    int chunkIndex = 1;
                    try {
                        while (recovering) {
                            File chunkFile = new File(binFolderPath + File.separator + fileName + "_part_" + chunkIndex + ".txt");

                            if (!chunkFile.exists()) {

                                recovering = false;
                                if (chunkIndex < 5) {
                                    System.out.println("File has not been deleted or doesnt exist");
                                }
                            } else {
                                System.out.println("recovering: " + chunkFile);
                                String newFilePath = path + File.separator + userInfo[0] + File.separator + fileName + "_part_" + chunkIndex + ".txt";
                                File destFile = new File(newFilePath);

                                myObj.deleteFile(fileOwner, fileName, false);
                                boolean success = chunkFile.renameTo((destFile));
                                ScpTo(newFilePath, fileName + "_part_" + chunkIndex + ".txt");

                                if (destFile.exists()) {
                                    if (destFile.delete()) {
                                        System.out.println("Chunk file deleted: " + newFilePath);
                                    } else {
                                        System.err.println("Failed to delete chunk file: " + newFilePath);
                                    }
                                } else {
                                    System.err.println("Chunk file does not exist: " + newFilePath);
                                }
                                chunkIndex = chunkIndex + 1;

                                if (!success) {
                                    System.out.println("Failed to move file: " + chunkFile.getAbsolutePath());
                                }

                            }

                        }

                        audit.log("User '" + currentUser + "' successfully recovered the file '" + fileName + ".txt'.");

                    } catch (SecurityException e) {
                        System.out.print("File doesn't exist or You do not have permission to delete this file");
                    }
                    dialogue("Confirmation", "File '" + fileName + "' recovered", "Successful!", Alert.AlertType.CONFIRMATION);
                } else {
                    System.out.println("Can't recover. File is not deleted");
                    dialogue("Warning", "Can't recover. File is not deleted!", "Failed", Alert.AlertType.ERROR);

                }
            } else {
                System.out.println("You do not have permission to recover this file");
                audit.log("User '" + currentUser + "' attempted to recover the file '" + fileName + ".txt' but did not have sufficient permissions.");
                dialogue("Warning", "You do not have permission to recover this file!", "Failed", Alert.AlertType.ERROR);

            }
        } else {
            System.out.println("File name text field is empty");
            dialogue("Warning", "File name text field is empty!", "Failed", Alert.AlertType.ERROR);
        }
    }

    //deletes Files in App
    @FXML
    public void deleteFile(ActionEvent event) throws ClassNotFoundException {
        AuditTrail audit = new AuditTrail();

        if (!filenameTextField.getText().isEmpty()) {
            DB myObj = new DB();
            String fileName = filenameTextField.getText();
            String currentUser = AuthService.getCurrentUser().getUser();
            boolean isOwner = myObj.isFileExists(currentUser, fileName);
            String fileOwner = myObj.isOwner(fileName, currentUser, isOwner);
            boolean hasPermissions = false;
            if (!isOwner) {
                boolean permission1 = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, false, true, false);
                boolean permission2 = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, true, true, false);
                if (permission1 || permission2) {
                    hasPermissions = true;
                }
            }

            //String fileName = filenameTextField.getText();
            if (isOwner) {
                if (!myObj.isFileDeleted(fileOwner, fileName)) {
                    File deletefile = new File(path + File.separator + currentUser, fileName);

                    String binFolderPath = path + File.separator + currentUser + "_bin";

                    boolean deleting = true;
                    int chunkIndex = 1;
                    File binFolder = new File(binFolderPath);
                    if (!binFolder.exists()) {
                        binFolder.mkdirs();
                    }
                    String encodedKey = myObj.getEncodedKey(fileOwner, fileName);
                    byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                    SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
                    String chunkFilePath = "";

                    StringBuilder contentBuilder = new StringBuilder();
                    for (int i = 1; i <= chunkPartsLeng; i++) {
                        String chunkFileName = fileName + "_part_" + i + ".txt";
                        chunkFilePath = path + File.separator + fileOwner + File.separator + chunkFileName;
                        System.out.println(chunkFilePath + chunkFileName);
                        scpFrom(chunkFilePath, chunkFileName, true);

                    }
                    myObj.deleteFile(fileOwner, fileName, true);
                    try {
                        while (deleting) {
                            File chunkFile = new File(path + File.separator + currentUser + File.separator + fileName + "_part_" + chunkIndex + ".txt");

                            if (!chunkFile.exists()) {

                                deleting = false;
                                if (chunkIndex < 5) {
                                    System.out.println("File doesnt exist");
                                }

                            } else {
                                System.out.println("deleting: " + chunkFile);
                                String newFilePath = binFolderPath + File.separator + fileName + "_part_" + chunkIndex + ".txt";
                                File destFile = new File(newFilePath);
                                boolean success = chunkFile.renameTo((destFile));
                                chunkIndex = chunkIndex + 1;
                                if (!success) {
                                    System.out.println("Failed to move file: " + chunkFile.getAbsolutePath());
                                }
                            }
                        }
                        audit.log("User '" + currentUser + "' successfully deleted the file '" + fileName + ".txt'.");

                    } catch (SecurityException e) {
                        System.out.print("File doesn't exist or You do not have permission to delete this file");
                    }
                    dialogue("Confirmation", "File '" + fileName + "' deleted", "Successful!", Alert.AlertType.CONFIRMATION);

                } else {
                    System.out.println("File is deleted");
                    audit.log("User '" + currentUser + "' attempted to delete the file '" + fileName + ".txt', but the file does not exist.");
                    dialogue("Warning", "File doesn't exist!", "Failed", Alert.AlertType.ERROR);
                }
            } else {

                System.out.println("You do not have permission to delete this file");
                audit.log("User '" + currentUser + "' attempted to delete the file '" + fileName + ".txt', but did not have sufficient permissions.");
                dialogue("Warning", "You do not have permission to delete this file!", "Failed", Alert.AlertType.ERROR);

            }
        } else {
            System.out.println("File name text field is empty");
            dialogue("Warning", "File name text field is empty!", "Failed", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void updateFile(ActionEvent event) throws ClassNotFoundException {
        AuditTrail audit = new AuditTrail();

        if (!filenameTextField.getText().isEmpty() && !filecontentTextField.getText().isEmpty()) {
            DB myObj = new DB();
            String fileName = filenameTextField.getText();
            String newContent = filecontentTextField.getText();

            String currentUser = AuthService.getCurrentUser().getUser();
            boolean isOwner = myObj.isFileExists(currentUser, fileName);
            String fileOwner = myObj.isOwner(fileName, currentUser, isOwner);
            boolean hasPermissions = false;
            if (!isOwner) {
                boolean perms = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, true, true, false);
                if (perms) {
                    hasPermissions = true;
                }
            }

            if (isOwner || hasPermissions) {
                if (!myObj.isFileDeleted(fileOwner, fileName)) {
                    int filesize = 0;
                    String encodedKey = myObj.getEncodedKey(fileOwner, fileName);
                    byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                    SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");

                    byte[] newencryptedContent = encrypt(newContent, secretKey);
                    String encodedContent = Base64.getEncoder().encodeToString(newencryptedContent);

                    StringBuilder totalContent = new StringBuilder();

                    int chunkIndex = 1;
                    File chunkFile;

                    String chunkFilePath = "";
                    File deleteFileCheck = null;
                    for (int i = 1; i <= chunkPartsLeng; i++) {
                        String binFolderPath = path + File.separator + currentUser + "_bin";

                        deleteFileCheck = new File(binFolderPath + File.separator + fileName + "_part_" + i + ".txt");

                        String chunkFileName = fileName + "_part_" + i + ".txt";
                        chunkFilePath = path + File.separator + fileOwner + File.separator + chunkFileName;
                        if (!deleteFileCheck.exists()) {
                            scpFrom(chunkFilePath, chunkFileName, false);
                        }
                    }

                    while ((chunkFile = new File(path + File.separator + fileOwner, fileName + "_part_" + chunkIndex + ".txt")).exists()) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(chunkFile))) {
                            StringBuilder chunkContent = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {

                                chunkContent.append(line);
                            }
                            totalContent.append(chunkContent);
                        } catch (IOException e) {
                            System.err.println("Error Reading File: " + e.getMessage());
                        }
                        chunkIndex++;
                    }

                    //totalContent.append(newContent);
                    byte[] decodedBytes = Base64.getDecoder().decode(totalContent.toString());
                    String decryptedBytes = decrypt(decodedBytes, secretKey);
                    String totaldecryptedData = decryptedBytes + newContent;
                    System.out.println("encoed content: " + totaldecryptedData);

                    byte[] totalencryptedData = encrypt(totaldecryptedData, secretKey);
                    String totalencodedData = Base64.getEncoder().encodeToString(totalencryptedData);
                    //String chunkFilePath ="";
                    //adding chunkParts to app folder
                    for (int i = 1; i <= chunkPartsLeng; i++) {
                        String chunkFileName = fileName + "_part_" + i + ".txt";
                        chunkFilePath = path + File.separator + fileOwner + File.separator + chunkFileName;
                        System.out.println(chunkFilePath + chunkFileName);
                    }

                    try {
                        int chunkSize = (totalencodedData.length() / chunkPartsLeng);

                        for (int i = 0; i < totalencodedData.length(); i += chunkSize) {
                            int endIndex = Math.min(i + chunkSize, totalencodedData.length());
                            //byte[] chunk = Arrays.copyOfRange(totalencryptedData, i, endIndex);
                            String chunkString = totalencodedData.substring(i, endIndex);
                            System.out.println(chunkString);

                            String chunkFileName = fileName + "_part_" + (i / chunkSize + 1) + ".txt";
                            System.out.println("file:" + i / chunkSize + 1);
                            if (isOwner) {
                                chunkFilePath = path + File.separator + currentUser + File.separator + chunkFileName;
                            } else if (hasPermissions) {
                                chunkFilePath = path + File.separator + fileOwner + File.separator + chunkFileName;
                            }

                            try (FileOutputStream fos = new FileOutputStream(chunkFilePath)) {

                                System.out.println(chunkFileName + ":");
                                System.out.println(chunkString);
                                int chunkFileSize = chunkString.getBytes(StandardCharsets.UTF_8).length;

                                filesize = filesize + chunkFileSize;
                                //String encodedchunk = Base64.getEncoder().encodeToString(chunk);
                                //fos.write(encodedchunk.getBytes(StandardCharsets.UTF_8));
                                fos.write(chunkString.getBytes(StandardCharsets.UTF_8));

                                fos.close();
                                long crc32Value = calculateCRC32(chunkFilePath);
                                System.out.println("The crc value for this file is: " + crc32Value);
                                ScpTo(chunkFilePath, chunkFileName);
                                chunkFile = new File(chunkFilePath);
                                if (chunkFile.exists()) {
                                    if (chunkFile.delete()) {
                                        System.out.println("Chunk file deleted: " + chunkFilePath);
                                    } else {
                                        System.err.println("Failed to delete chunk file: " + chunkFilePath);
                                    }
                                } else {
                                    System.err.println("Chunk file does not exist: " + chunkFilePath);
                                }
                                //scpFrom(chunkFilePath, chunkFileName);

                            }

                            //byte[] fileData = readFromFile("test1_part_" + (i / chunkSize + 1) + ".txt");
                            //String fileData2 = new String(fileData, StandardCharsets.UTF_8);
                            //System.out.println("this i whats in the file" + fileData2);
                        }
                        System.out.println("File Updated: " + fileName);

                        if (isOwner) {
                            myObj.UpdateDataToFileDB(fileName, fileOwner, fileOwner, filesize, true);
                            audit.log("User '" + currentUser + "' successfully updated the file '" + fileName + ".txt'.");
                            dialogue("Confirmation", "File '" + fileName + "' updated", "Successful!", Alert.AlertType.CONFIRMATION);
                            // Syst
                        } else if (hasPermissions) {
                            myObj.UpdateDataToFileDB(fileName, fileOwner, currentUser, filesize, false);
                            System.out.println(currentUser);
                            audit.log("User '" + currentUser + "' updated the file '" + fileName + ".txt' with write/read permissions granted by '" + fileOwner + "'.");
                            dialogue("Confirmation", "File '" + fileName + "' updated", "Successful!", Alert.AlertType.CONFIRMATION);
                        } else {
                            System.out.println("Unsuccesfull update");
                        }
                    } catch (IOException e) {
                        System.out.println("An error occured.");
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("Can't update. File is deleted");
                    audit.log("User '" + currentUser + "' attempted to update the file '" + fileName + ".txt', but the file does not exist.");
                    dialogue("Warning", "File doesn't exist!", "Failed", Alert.AlertType.ERROR);
                }
            } else {

                System.out.println("No permission to update");
                audit.log("User '" + currentUser + "' attempted to update the file '" + fileName + ".txt', but did not have sufficient permissions.");
                dialogue("Warning", "You do not have permission to update this file!", "Failed", Alert.AlertType.ERROR);
            }
        } else {
            System.out.println("File name text field or new content is empty");
            dialogue("Warning", "File name text field or new content is empty!", "Failed", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void readFile(ActionEvent event) throws ClassNotFoundException, IOException {
        AuditTrail audit = new AuditTrail();

        if (!filenameTextField.getText().isEmpty()) {

            DB myObj = new DB();
            String fileName = filenameTextField.getText();
            String currentUser = AuthService.getCurrentUser().getUser();
            boolean isOwner = myObj.isFileExists(currentUser, fileName);
            String fileOwner = myObj.isOwner(fileName, currentUser, isOwner);
            boolean hasPermissions = false;
            if (!isOwner) {
                boolean permission1 = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, false, true, false);
                boolean permission2 = myObj.isPermissionGiven(fileOwner, fileName, currentUser, true, true, true, false);
                if (permission1 || permission2) {
                    hasPermissions = true;
                }
            }

            if (isOwner || hasPermissions) {
                if (!myObj.isFileDeleted(fileOwner, fileName)) {

                    String encodedKey = myObj.getEncodedKey(fileOwner, fileName);
                    byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                    SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
                    String chunkFilePath = "";

                    StringBuilder contentBuilder = new StringBuilder();
                    //adding chunkParts to app folder
                    for (int i = 1; i <= chunkPartsLeng; i++) {
                        String chunkFileName = fileName + "_part_" + i + ".txt";
                        chunkFilePath = path + File.separator + fileOwner + File.separator + chunkFileName;
                        System.out.println(chunkFilePath + chunkFileName);
                        scpFrom(chunkFilePath, chunkFileName, false);
                        long crc32Value = calculateCRC32(chunkFilePath);
                        System.out.println("The crc value for this file is: " + crc32Value);
                    }

                    try {
                        int chunkIndex = 1;
                        boolean reading = true;
                        while (reading) {
                            File chunkFile;
                            if (isOwner) {
                                chunkFile = new File(path + File.separator + currentUser + File.separator + fileName + "_part_" + chunkIndex + ".txt");
                            } else if (hasPermissions) {
                                chunkFile = new File(path + File.separator + fileOwner + File.separator + fileName + "_part_" + chunkIndex + ".txt");
                            } else {
                                break; // Exit loop if neither owner nor has permissions
                            }

                            System.out.println("Reading From: " + chunkFile);

                            if (!chunkFile.exists()) {
                                System.out.println("Chunk file doesn't exist");
                                reading = false;
                                // reading data from app folder
                            } else if (chunkFile.exists()) {
                                try (FileReader fileReader = new FileReader(chunkFile)) {
                                    char[] buffer = new char[1024];
                                    int charsRead;

                                    while ((charsRead = fileReader.read(buffer)) != -1) {
                                        contentBuilder.append(buffer, 0, charsRead);
                                    }
                                    //deleting chunkParts from app folder
                                    if (chunkFile.delete()) {
                                        System.out.println("Chunk file deleted: " + chunkFilePath);
                                    } else {
                                        System.err.println("Failed to delete chunk file: " + chunkFilePath);
                                    }
                                }
                            } else {
                                System.err.println("Chunk file does not exist: " + chunkFilePath);
                            }
                            if (chunkIndex < chunkPartsLeng) {
                                chunkIndex++;
                            }

                        }
                        byte[] decodedContent = Base64.getDecoder().decode(contentBuilder.toString());
                        String decryptedContent = decrypt(decodedContent, secretKey);
                        System.out.println("Decrypted Content: " + decryptedContent + decryptedContent.length());
                        readContentTextField.setText(decryptedContent);
                        if (hasPermissions) {
                            audit.log("User '" + currentUser + "' read the file '" + fileName + ".txt' with read permissions granted by '" + fileOwner + "'.");
                        } else if (isOwner) {
                            audit.log("User '" + currentUser + "' read the file '" + fileName + ".txt'.");

                        }
                        //filecontentTextField.setText(decryptedContent);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Can't read. File is deleted");

                    audit.log("User '" + currentUser + "' attempted to read the file '" + fileName + ".txt', but the file does not exist.");
                    dialogue("Warning", "File doesn't exist!", "Failed", Alert.AlertType.ERROR);
                }
            } else {
                audit.log("User '" + currentUser + "' attempted to update the file '" + fileName + ".txt', but did not have sufficient permissions.");
                dialogue("Warning", "You do not have permission to read this file!", "Failed", Alert.AlertType.ERROR);
            }

        } else {
            System.out.println("File name text field is empty");
            dialogue("Warning", "File name text field is empty!", "Failed", Alert.AlertType.ERROR);
        }
    }

    private SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, new SecureRandom());
            return keyGenerator.generateKey();
        } catch (Exception e) {
            System.out.println("Error generating AES key.");
            e.printStackTrace();
            return null;
        }
    }

    private byte[] encrypt(String data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return encryptedBytes;
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println("Error encrypting data.");
            e.printStackTrace();
            return null;
        }
    }

    private String decrypt(byte[] encryptedData, Key key) {
        try {

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            System.out.println("Error decrypting data.");
            e.printStackTrace();
            return null;
        }
    }

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
            String msg = "some data sent from file Controller";
            secondaryStage.setUserData(msg);
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void givePermBtnHandler(ActionEvent event) throws ClassNotFoundException {
        AuditTrail audit = new AuditTrail();

        DB myObj = new DB();
        String currentUser = AuthService.getCurrentUser().getUser();
        boolean isPermissionExists = myObj.isPermissionGiven(currentUser, filenameTextField.getText(), userPermField.getText(), readCheckBox.isSelected(), writeCheckBox.isSelected(), false, false);
        boolean isOwner = myObj.isFileExists(currentUser, filenameTextField.getText());
        String fileOwner = myObj.isOwner(filenameTextField.getText(), currentUser, isOwner);
        boolean hasPermissions = false;
        if (!isOwner) {
            boolean permission1 = myObj.isPermissionGiven(fileOwner, filenameTextField.getText(), currentUser, true, false, true, false);
            boolean permission2 = myObj.isPermissionGiven(fileOwner, filenameTextField.getText(), currentUser, true, true, true, false);
            if (permission1 || permission2) {
                hasPermissions = true;
            }
        }

        try {
            if (myObj.isFileExists(currentUser, filenameTextField.getText()) && myObj.isUsernameExists(userPermField.getText())) {
                if (isOwner) {
                    if (!myObj.isFileDeleted(fileOwner, filenameTextField.getText())) {
                        if (!isPermissionExists) {

                            if (writeCheckBox.isSelected() && (readCheckBox.isSelected() || (!readCheckBox.isSelected()))) {
                                myObj.grantFilePermissions(filenameTextField.getText(), currentUser, userPermField.getText(), true, true, false);
                                System.out.println("Permission to write/read for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' given!");
                                audit.log("User '" + currentUser + "' granted write-read permission for the file '" + filenameTextField.getText() + ".txt' to user '" + userPermField.getText() + "'.");
                                dialogue("Confirmation", "Permission to write/read for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' given!", "Successful!", Alert.AlertType.CONFIRMATION);
                            } else if (!writeCheckBox.isSelected() && readCheckBox.isSelected()) {
                                myObj.grantFilePermissions(filenameTextField.getText(), currentUser, userPermField.getText(), true, false, false);
                                System.out.println("Permission to read for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' given!");
                                audit.log("User '" + currentUser + "' granted read-only permission for the file '" + filenameTextField.getText() + ".txt' to user '" + userPermField.getText() + "'.");
                                dialogue("Confirmation", "Permission to read for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' given!", "Successful!", Alert.AlertType.CONFIRMATION);

                            } else {
                                System.out.println("Check the box for type of permission");
                            }

                        } else if (isPermissionExists) {
                            if ((!writeCheckBox.isSelected()) && (!readCheckBox.isSelected())) {
                                System.out.println("Check the box for type of permission");
                            } else if (writeCheckBox.isSelected()) {
                                myObj.grantFilePermissions(filenameTextField.getText(), currentUser, userPermField.getText(), true, true, true);
                                System.out.println("Permission for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + ".txt' has been updated!");
                                audit.log("User '" + currentUser + "' updated permission to write/read for the file '" + filenameTextField.getText() + ".txt' to user '" + userPermField.getText() + "'.");
                                dialogue("Confirmation", "Permission to write/read for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' has been updated!", "Successful!", Alert.AlertType.CONFIRMATION);

                            } else if ((!writeCheckBox.isSelected()) && (readCheckBox.isSelected())) {
                                myObj.grantFilePermissions(filenameTextField.getText(), currentUser, userPermField.getText(), true, false, true);
                                System.out.println("Permission for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + ".txt' has been updated!");
                                audit.log("User '" + currentUser + "' updated permission to read-only for the file '" + filenameTextField.getText() + ".txt' to user '" + userPermField.getText() + "'.");
                                dialogue("Confirmation", "Permission to read-only for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' has been updated!", "Successful!", Alert.AlertType.CONFIRMATION);

                            }

                        } else {
                            System.out.println("Permission already exists");

                        }
                    } else {
                        System.out.print("Can't give permission. File is deleted");
                        audit.log("User '" + currentUser + "' attempted to grant access to the file '" + filenameTextField.getText() + ".txt', but the file does not exist.");
                        dialogue("Warning", "File doesn't exist!", "Failed", Alert.AlertType.ERROR);

                    }

                } else {
                    System.out.println("You do not have permission to grant for this file");
                    audit.log("User '" + currentUser + "' attempted to grant access to the file '" + filenameTextField.getText() + ".txt', but did not have sufficient permissions.");
                    dialogue("Warning", "You do not have permission to grant for this file!", "Failed", Alert.AlertType.ERROR);
                }
            } else {
                System.out.println("File or user you want to give permission to doesn't exist");
                audit.log("User '" + currentUser + "' attempted to grant access to the file '" + filenameTextField.getText() + ".txt', but the file does not exist.");
                dialogue("Warning", "No permission. File or user you want to give permission to doesn't exist!", "Failed", Alert.AlertType.ERROR);

            }
        } catch (SecurityException e) {
            System.out.print("File doesn't exist or You do not have permission to grant for this file");

        }

    }

    @FXML
    private void revokePermBtnHandler(ActionEvent event) throws ClassNotFoundException {
        AuditTrail audit = new AuditTrail();

        DB myObj = new DB();
        String currentUser = AuthService.getCurrentUser().getUser();
        boolean isPermissionExists = myObj.isPermissionGiven(currentUser, filenameTextField.getText(), userPermField.getText(), readCheckBox.isSelected(), writeCheckBox.isSelected(), false, false);
        boolean isOwner = myObj.isFileExists(currentUser, filenameTextField.getText());
        String fileOwner = myObj.isOwner(filenameTextField.getText(), currentUser, isOwner);
        boolean hasPermissions = false;
        if (!isOwner) {
            boolean permission1 = myObj.isPermissionGiven(fileOwner, filenameTextField.getText(), currentUser, true, false, true, false);
            boolean permission2 = myObj.isPermissionGiven(fileOwner, filenameTextField.getText(), currentUser, true, true, true, false);
            if (permission1 || permission2) {
                hasPermissions = true;
            }
        }
        try {
            if (myObj.isFileExists(currentUser, filenameTextField.getText()) && myObj.isUsernameExists(userPermField.getText())) {
                if (isOwner) {
                    if (!myObj.isFileDeleted(fileOwner, filenameTextField.getText())) {

                        if (isPermissionExists && (writeCheckBox.isSelected() && !readCheckBox.isSelected())) {
                            myObj.grantFilePermissions(filenameTextField.getText(), currentUser, userPermField.getText(), false, false, true);
                            System.out.println("Permission for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' has been revoked!");
                            audit.log("User '" + currentUser + "' revoked write permission for the file '" + filenameTextField.getText() + ".txt' to user '" + userPermField.getText() + "'.");
                            dialogue("Confirmation", "Permission to write for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' has been revoked!", "Successful!", Alert.AlertType.CONFIRMATION);

                        } else if ((isPermissionExists && readCheckBox.isSelected()) || (!readCheckBox.isSelected() && !writeCheckBox.isSelected())) {
                            myObj.grantFilePermissions(filenameTextField.getText(), currentUser, userPermField.getText(), false, false, true);
                            System.out.println("Permission for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' has been revoked!");
                            audit.log("User '" + currentUser + "' revoked write/read permission for the file '" + filenameTextField.getText() + ".txt' to user '" + userPermField.getText() + "'.");
                            dialogue("Confirmation", "Permission to write/read for file '" + filenameTextField.getText() + "' to user '" + userPermField.getText() + "' has been revoked!", "Successful!", Alert.AlertType.CONFIRMATION);

                        } else {
                            System.out.println("Permission wasn't given");
                        }
                    } else {
                        System.out.println("Can't revoke. File is deleted");
                        audit.log("User '" + currentUser + "' attempted to revoke permission to the file '" + filenameTextField.getText() + ".txt', but the file or user does not exist.");

                        dialogue("Warning", "File or user you want to revoke permission from doesn't exist!", "Failed", Alert.AlertType.ERROR);

                    }
                } else {
                    System.out.println("You do not have permission to revoke for this file");
                    audit.log("User '" + currentUser + "' attempted to revoke permission to the file '" + filenameTextField.getText() + ".txt', but did not have sufficient permissions.");
                    dialogue("Warning", "You do not have permission to revoke for this file!", "Failed", Alert.AlertType.ERROR);
                }
            } else {
                System.out.println("File or user you want to revoke permission from doesn't exist");
                audit.log("User '" + currentUser + "' attempted to revoke permission to the file '" + filenameTextField.getText() + ".txt', but the file or user does not exist.");
                dialogue("Warning", "File or user you want to revoke permission from doesn't exist!", "Failed", Alert.AlertType.ERROR);
            }
        } catch (SecurityException e) {
            System.out.print("File doesn't exist or You do not have permission to revoke for this file");

        }

    }

    public void initialise(String[] credentials) throws ClassNotFoundException {

        userInfo = credentials;
        String binFolderPath = path + File.separator + userInfo[0] + "_bin";

        deleteFilesOlderThan(new File(binFolderPath), 1);
        DB myObj = new DB();
        String currentUser = AuthService.getCurrentUser().getUser();
        try {
            String[] userFiles = myObj.allFilesForUser(currentUser);
            ObservableList<String> options = FXCollections.observableArrayList(userFiles);
            choiceBox.setItems(options);
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException occurred: " + e.getMessage());
        }
        //String a = "test.txt";
        //ScpTo();
    }

    private void deleteFilesOlderThan(File directory, int days) throws ClassNotFoundException {

        DB myObj = new DB();

        long now = System.currentTimeMillis();
        long threshold = now - (days * 24 * 60 * 60 * 1000L); // Convert days to milliseconds
        //long threshold = now - (minutes * 60 * 1000L);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < threshold) {
                    String fileName = file.getName();
                    String[] parts = fileName.split("_part_");
                    System.out.println(parts[0]);
                    if (!file.delete()) {
                        System.out.println("Failed to delete file: " + file.getAbsolutePath());
                    } else {
                        myObj.DelDataToFileDB(parts[0], userInfo[0]);
                        System.out.println("Fully deletd file: " + file.getAbsolutePath());

                    }
                }
            }
        }
    }

    private void ScpTo(String filepath, String filename) {

        String[] parts = filename.split("_");
        String lastPart = parts[parts.length - 1];
        String[] fileAndExtension = lastPart.split("\\.");
        int partNumber = Integer.parseInt(fileAndExtension[0]);
        /*File file = new File("test.txt");
        try{
            file.createNewFile();
        }catch(IOException e){
        
        }*/
        String localFile = filepath;
        String remoteFile = "/root/" + filename;
        Session jschSession = null;

        try {

            JSch jsch = new JSch();
            jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts");

            // Set the StrictHostKeyChecking option to "no" to automatically answer "yes" to the prompt
            switch (partNumber) {
                case 1:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_1, REMOTE_PORT);
                    break;
                case 2:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_2, REMOTE_PORT);
                    break;
                case 3:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_3, REMOTE_PORT);
                    break;
                default:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_4, REMOTE_PORT);
                    break;
            }

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);

            // authenticate using password
            jschSession.setPassword(PASSWORD);

            // 10 seconds session timeout
            jschSession.connect(SESSION_TIMEOUT);

            Channel sftp = jschSession.openChannel("sftp");

            // 5 seconds timeout
            sftp.connect(CHANNEL_TIMEOUT);

            ChannelSftp channelSftp = (ChannelSftp) sftp;

            // transfer file from local to remote server
            channelSftp.put(localFile, remoteFile);

            // Ask the user if they want to delete the remote file
            /*System.out.print("Do you want to delete the remote file? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(userInput)) {
                // Delete the remote file
                channelSftp.rm(remoteFile);
                System.out.println("Remote file deleted.");
            } else {
                System.out.println("Remote file not deleted.");
            }*/
            channelSftp.exit();

        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }

        System.out.println("Done");

    }

    public void scpFrom(String filepath, String filename, boolean forDelete) {

        String[] parts = filename.split("_");
        String lastPart = parts[parts.length - 1];
        String[] fileAndExtension = lastPart.split("\\.");
        int partNumber = Integer.parseInt(fileAndExtension[0]);

        String remoteFile = "/root/" + filename;
        String localDestinationPath = filepath;

        Session jschSession = null;
        try {
            JSch jsch = new JSch();
            jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts");

            switch (partNumber) {
                case 1:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_1, REMOTE_PORT);
                    break;
                case 2:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_2, REMOTE_PORT);
                    break;
                case 3:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_3, REMOTE_PORT);
                    break;
                default:
                    jschSession = jsch.getSession(USERNAME, REMOTE_HOST_4, REMOTE_PORT);
                    break;
            }
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);
            jschSession.setPassword(PASSWORD);
            jschSession.connect(SESSION_TIMEOUT);

            Channel channel = jschSession.openChannel("sftp");
            channel.connect(CHANNEL_TIMEOUT);

            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get(remoteFile, localDestinationPath);

            // Ask the user if they want to delete the remote file
            /*System.out.print("Do you want to delete the remote file? (y/n): ");
            Scanner scanner = new Scanner(System.in);
            String userInput = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(userInput)) {
                // Delete the remote file
                sftpChannel.rm(remoteFile);
                System.out.println("Remote file deleted.");
            } else {
                System.out.println("Remote file not deleted.");
            }*/
            //!!!!!!!!!!!!!!!!!!!!!!!
            if (forDelete) {
                sftpChannel.rm(remoteFile);
            }
            sftpChannel.exit();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
    }

}
