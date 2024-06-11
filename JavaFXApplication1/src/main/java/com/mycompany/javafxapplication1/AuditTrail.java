/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ntu-user
 */
public class AuditTrail {

    private static final String LOG_DIRECTORY = "/home/ntu-user/app";
    private static final String LOG_FILE = LOG_DIRECTORY + File.separator + "audit.log";

    public void createDirectory() {
        File directory = new File(LOG_DIRECTORY);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory);
            } else {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }
    }

    public static void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
            String timestamp = dateFormat.format(new Date());
            writer.println(timestamp + "" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
