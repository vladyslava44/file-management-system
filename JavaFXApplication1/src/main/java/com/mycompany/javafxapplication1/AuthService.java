/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.security.spec.InvalidKeySpecException;
//ejdh

/**
 *
 * @author ntu-user
 */
public class AuthService {

    private static User currentUser;

    public static boolean authenticate(String username, String password) {
        DB myObj = new DB();
        try {
            if (myObj.validateUser(username, password)) {
                currentUser = new User(username, password);
                return true;
            }
        } catch (InvalidKeySpecException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkStatus() {
        boolean status = false;
        User currentUser = AuthService.getCurrentUser();
        if (AuthService.isLoggedIn()) {
            System.out.println(" IS LOGGED IN");
            status = true;

        } else if (AuthService.getCurrentUser() == null) {
            System.out.println(" IS LOGGED OUT");
            status = false;
        }
        return status;

    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

}
