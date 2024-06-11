package com.mycompany.javafxapplication1;

import com.jcraft.jsch.*;
import java.io.IOException;
import javafx.event.ActionEvent;

import javax.swing.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

public class RemoteController {

    @FXML
    private Button cont1Button;

    @FXML
    private Button cont2Button;

    @FXML
    private Button cont3Button;

    @FXML
    private Button cont4Button;

    @FXML
    private TextArea outputArea;

    @FXML
    private Button backButton;

    private String username;

    private Map<Button, Integer> containerButtonMap = new HashMap<>();

    @FXML
    private void initialize() {
        // Initialize controller
        username = "root@comp20081-files-";

        containerButtonMap.put(cont1Button, 1);
        containerButtonMap.put(cont2Button, 2);
        containerButtonMap.put(cont3Button, 3);
        containerButtonMap.put(cont4Button, 4);

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
            String msg = "some data sent from remote Controller";
            secondaryStage.setUserData(msg);
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleContainerAction(ActionEvent event) throws IOException {
        Button clickedButton = (Button) event.getSource();
        Stage primaryStage = (Stage) clickedButton.getScene().getWindow();
        int containerNumber = containerButtonMap.get(clickedButton);
        setUsername("Container" + containerNumber);
        outputArea.appendText("Connecting with username: " + username + "\n");
        connectToRemoteTerminal(containerNumber);
    }

    private void setUsername(String container) {
        username = "root@comp20081-files-" + container;
    }

    private void connectToRemoteTerminal(int containerNumber) {
        User currentUser = AuthService.getCurrentUser();
        try {
            JSch jsch = new JSch();

            String contNum = "comp20081-files-container" + containerNumber;

            Session session = jsch.getSession("root", contNum, 22);

            String passwd = JOptionPane.showInputDialog("Enter password");
            session.setPassword(passwd);

            UserInfo ui = new MyUserInfo() {
                public void showMessage(String message) {
                    JOptionPane.showMessageDialog(null, message);
                }

                public boolean promptYesNo(String message) {
                    Object[] options = {"yes", "no"};
                    int foo = JOptionPane.showOptionDialog(null,
                            message,
                            "Warning",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return foo == 0;
                }

                // If password is not given before the invocation of Session#connect(),
                // implement also following methods,
                //   * UserInfo#getPassword(),
                //   * UserInfo#promptPassword(String message) and
                //   * UIKeyboardInteractive#promptKeyboardInteractive()
            };

            session.setUserInfo(ui);

            // It must not be recommended, but if you want to skip host-key check,
            // invoke following,
            // session.setConfig("StrictHostKeyChecking", "no");
            //session.connect();
            session.connect(30000);   // making a connection with timeout.

            Channel channel = session.openChannel("shell");

            // Enable agent-forwarding.
            //((ChannelShell)channel).setAgentForwarding(true);
            channel.setInputStream(System.in);
            /*
      // a hack for MS-DOS prompt on Windows.
      channel.setInputStream(new FilterInputStream(System.in){
          public int read(byte[] b, int off, int len)throws IOException{
            return in.read(b, off, (len>1024?1024:len));
          }
        });
             */

            channel.setOutputStream(System.out);
            AuditTrail audit = new AuditTrail();
            audit.log("User '" + currentUser.getUser() + "' successfully ran a remote terminal within '" + contNum + "'.");

            /*
      // Choose the pty-type "vt102".
      ((ChannelShell)channel).setPtyType("vt102");
             */

 /*
      // Set environment variable "LANG" as "ja_JP.eucJP".
      ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
             */
            //channel.connect();
            channel.connect(3 * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static abstract class MyUserInfo
            implements UserInfo, UIKeyboardInteractive {

        public String getPassword() {
            return null;
        }

        public boolean promptYesNo(String str) {
            return false;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination,
                String name,
                String instruction,
                String[] prompt,
                boolean[] echo) {
            return null;
        }
    }
}
