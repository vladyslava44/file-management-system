/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import java.util.Date;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author ntu-user
 */
public class DB {

    private String fileName = "jdbc:sqlite:comp20081.db";
    private String directoryPath = "/home/ntu-user/app";
    private String fullFilePath = directoryPath + File.separator + fileName;
    private int timeout = 30;
    private String dataBaseName = "COMP20081";
    private String dataBaseTableName = "Users";
    private String dataBaseTableNameFileServer = "UserFiles";
    private String dataBasePermissionsTableName = "FilePermissions";
    private List<String> fileId = new ArrayList<>();

    Connection connection = null;
    private Random random = new SecureRandom();
    private String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private int iterations = 10000;
    private int keylength = 256;
    private String saltValue;

    /**
     * @brief constructor - generates the salt if it doesn't exists or load it
     * from the file .salt
     */
    DB() {
        try {
            File fp = new File(".salt");
            if (!fp.exists()) {
                saltValue = this.getSaltvalue(30);
                FileWriter myWriter = new FileWriter(fp);
                myWriter.write(saltValue);
                myWriter.close();
            } else {
                Scanner myReader = new Scanner(fp);
                while (myReader.hasNextLine()) {
                    saltValue = myReader.nextLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief create a new table
     * @param tableName name of type String
     */
    public void createTable(String tableName) throws ClassNotFoundException {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory);
            } else {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("create table if not exists " + tableName + "(id integer primary key autoincrement, name string, password string)");

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    public void createFileTable(String tableName) throws ClassNotFoundException {

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory);
            } else {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }

        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("create table if not exists " + tableName + "(id integer primary key autoincrement, creator_name string, file_name string, file_size int,creation_date date, last_update_date date, last_modified_by string, encodedkey string, isDeleted int default 0)");

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    public void createPermissionTable(String tableName) throws ClassNotFoundException {

        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + directory);
            } else {
                throw new RuntimeException("Failed to create directory: " + directory);
            }
        }

        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT, file_id INTEGER, file_name TEXT, creator_name TEXT, permission_user TEXT, write_permission INTEGER DEFAULT 0, read_permission INTEGER DEFAULT 0)");

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @brief delete table
     * @param tableName of type String
     */
    public void delTable(String tableName) throws ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("drop table if exists " + tableName);
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    public void updateTable(String username, String newPassword) throws InvalidKeySpecException, ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("update " + dataBaseTableName + " set password='" + generateSecurePassword(newPassword) + "' where name='" + username + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    public void
            deleteFile(String ownerName, String fileName, boolean delete) throws ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            if (delete) {
                statement.executeUpdate("update " + dataBaseTableNameFileServer + " set isDeleted= 1 where creator_name = '" + ownerName + "' and file_name = '" + fileName + "'");
            } else {
                statement.executeUpdate("update " + dataBaseTableNameFileServer + " set isDeleted= 0 where creator_name = '" + ownerName + "' and file_name = '" + fileName + "'");

            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    public boolean isFileDeleted(String ownerName, String fileName) throws ClassNotFoundException {
        boolean exists = false;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + dataBaseTableNameFileServer + " WHERE creator_name = '" + ownerName + "' AND file_name = '" + fileName + "' AND isDeleted = 1");
            if (rs.next()) {
                int count = rs.getInt(1);
                exists = (count > 0);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return exists;
    }

    public void deleteAccount(String username, String password) throws InvalidKeySpecException, ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("update " + dataBaseTableName + " set password= '' where name='" + username + "'" + "AND password ='" + generateSecurePassword(password) + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.lang.ClassNotFoundException
     * @brief add data to the database method
     * @param user name of type String
     * @param password of type String
     */
    public void addDataToDB(String user, String password) throws InvalidKeySpecException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
//            System.out.println("Adding User: " + user + ", Password: " + password);
            statement.executeUpdate("insert into " + dataBaseTableName + " (name, password) values('" + user + "','" + generateSecurePassword(password) + "')");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * @brief get data from the Database method
     * @retunr results as ResultSet
     */
    public ObservableList<User> getDataFromTable() throws ClassNotFoundException {
        ObservableList<User> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + this.dataBaseTableName);
            while (rs.next()) {
                // read the result set
                result.add(new User(rs.getString("name"), rs.getString("password")));
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    public boolean isUsernameExists(String username) throws ClassNotFoundException {
        boolean exists = false;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + this.dataBaseTableName + " WHERE name = '" + username + "'");
            if (rs.next()) {
                int count = rs.getInt(1);
                exists = (count > 0);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return exists;
    }

    public void grantFilePermissions(String fileName, String creatorName, String permissionName, boolean readPermission, boolean writePermission, boolean permGiven) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);

            int readPerm = 0;
            int writePerm = 0;
            if (readPermission) {
                readPerm = 1;
            } else {
                readPerm = 0;
            }
            if (writePermission) {
                writePerm = 1;
            } else {
                writePerm = 0;
            }
            String fileId = "";

            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select id from " + dataBaseTableNameFileServer + " where file_name = '" + fileName + "' and creator_name = '" + creatorName + "'");
            if (rs.next()) {
                fileId = rs.getString("id");
            }

            if (!permGiven) {

                statement.executeUpdate("INSERT INTO " + dataBasePermissionsTableName + " (file_id, file_name, creator_name, permission_user, read_permission, write_permission) values(" + fileId + ", '" + fileName + "', '" + creatorName + "', '" + permissionName + "', " + readPerm + ", " + writePerm + ")");
            } else if (permGiven) {
                //statement.executeUpdate("INSERT INTO " + dataBasePermissionsTableName + " (file_id, file_name, creator_name, permission_user, read_permission, write_permission) values("+ fileId + ", '" + fileName + "', '" + creatorName + "', '" + permissionName + "', " +readPerm + ", " + writePerm + " where file_id ="+fileId+" and file_name= "+ fileName+" and creator_name = "+creatorName+" and permission_user = "+permissionName));
                statement.executeUpdate("UPDATE " + dataBasePermissionsTableName + " SET read_permission = " + readPerm + ", write_permission = " + writePerm + " WHERE file_id = " + fileId + " AND file_name = '" + fileName + "' AND creator_name = '" + creatorName + "' AND permission_user = '" + permissionName + "'");

            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public String isOwner(String fileName, String permUser, boolean isCreator) throws ClassNotFoundException {
        String creatorName = ""; // Declare creatorName outside the try block

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            ResultSet rs = null;
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            if (isCreator) {
                rs = statement.executeQuery("SELECT creator_name FROM " + dataBaseTableNameFileServer + " WHERE file_name = '" + fileName + "' and creator_name = '" + permUser + "'");
            } else if (!isCreator) {
                rs = statement.executeQuery("SELECT creator_name FROM " + dataBasePermissionsTableName + " WHERE file_name = '" + fileName + "' and permission_user = '" + permUser + "'");

            }
            if (rs.next()) {
                creatorName = rs.getString("creator_name");
            } else {
                System.out.println("Not owner");
                creatorName = null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return creatorName;
    }

    public String[] allFilesForUser(String user) throws ClassNotFoundException {
        List<String> fileId = new ArrayList<>();
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(timeout);

            ResultSet rs = statement.executeQuery("SELECT file_name FROM " + dataBaseTableNameFileServer + " WHERE creator_name = '" + user + "'");
            while (rs.next()) {
                fileId.add(rs.getString("file_name"));
            }
            ResultSet rs1 = statement.executeQuery("SELECT file_name FROM " + dataBasePermissionsTableName + " WHERE permission_user = '" + user + "'");
            while (rs1.next()) {
                fileId.add(rs.getString("file_name"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return fileId.toArray(new String[0]);
    }

    public boolean isPermissionGiven(String creatorName, String fileName, String permUser, boolean readPerm, boolean writePerm, boolean accurate, boolean permission) throws ClassNotFoundException {
        boolean exists = false;
        try {

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);

            String fileId = "";

            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs1 = null;

            ResultSet rs = statement.executeQuery("SELECT id FROM " + dataBaseTableNameFileServer + " WHERE file_name = '" + fileName + "' AND creator_name = '" + creatorName + "'");
            if (rs.next()) {
                fileId = rs.getString("id");
            }
            if (accurate) {
                rs1 = statement.executeQuery("SELECT COUNT(*) FROM " + dataBasePermissionsTableName + " WHERE creator_name = '" + creatorName + "' AND permission_user = '" + permUser + "' AND file_id = '" + fileId + "' AND write_permission = " + writePerm + " AND read_permission = " + readPerm);
            } else if (!accurate) {
                rs1 = statement.executeQuery("SELECT COUNT(*) FROM " + dataBasePermissionsTableName + " WHERE creator_name = '" + creatorName + "' AND permission_user = '" + permUser + "' AND file_id = '" + fileId + "'");

            } else if (permission) {

                rs1 = statement.executeQuery("SELECT COUNT(*) FROM " + dataBasePermissionsTableName + " WHERE permission_user = '" + permUser + "' and file_id ='" + fileId + "'");

            }
            if (rs1.next()) {
                int count = rs1.getInt(1);
                exists = (count > 0);
            } else {
                System.out.println("Doesn't exist");
                exists = false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        if (!exists) {
            System.out.println("No permissions found");
        }
        return exists;
    }

    public boolean isFileExists(String username, String fileName) throws ClassNotFoundException {
        boolean exists = false;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + dataBaseTableNameFileServer + " WHERE creator_name = '" + username + "' and file_name = '" + fileName + "'");
            if (rs.next()) {
                int count = rs.getInt(1);
                exists = (count > 0);
            } else {
                exists = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return exists;
    }

    public void addDataToFileDB(String file_name, String creator_name, int file_size, String encodedKey) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
//            System.out.println("Adding User: " + user + ", Password: " + password);
            Date currentDate = new Date();
            statement.executeUpdate("insert into " + dataBaseTableNameFileServer + " (creator_name, file_name, file_size, encodedKey, creation_date, last_update_date, last_modified_by ) values('" + creator_name + "','" + file_name + "','" + file_size + "','" + encodedKey + "','" + currentDate + "','" + currentDate + "', '" + creator_name + "')");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void DelDataToFileDB(String file_name, String creator_name) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
//            System.out.println("Adding User: " + user + ", Password: " + password);
            Date currentDate = new Date();
            statement.executeUpdate("DELETE FROM " + dataBaseTableNameFileServer + " WHERE creator_name = '" + creator_name + "' AND file_name = '" + file_name + "'");
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void UpdateDataToFileDB(String file_name, String creator_name, String modifier_name, int new_file_size, boolean isOwner) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            // System.out.println("Adding User: " + user + ", Password: " + password);
            Date currentDate = new Date();
            if (isOwner) {
                System.out.println("IM WONER");
                statement.executeUpdate("UPDATE " + dataBaseTableNameFileServer + " SET creator_name = '" + creator_name + "', file_size = '" + new_file_size + "', last_modified_by= '" + modifier_name + "'," + " last_update_date = '" + currentDate + "'" + " WHERE file_name = '" + file_name + "'" + "AND creator_name = '" + creator_name + "'");
            } else if (!isOwner) {
                statement.executeUpdate("UPDATE " + dataBaseTableNameFileServer + " SET creator_name = '" + creator_name + "'," + " last_modified_by= '" + modifier_name + "', file_size = '" + new_file_size + "'," + " last_update_date = '" + currentDate + "'" + " WHERE file_name = '" + file_name + "'" + "AND creator_name = '" + creator_name + "'");

            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed.
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public String getEncodedKey(String creator_name, String file_name) throws ClassNotFoundException {

        String encodedKey = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            String sql = "SELECT encodedkey FROM " + dataBaseTableNameFileServer + " WHERE file_name = '" + file_name + "'";

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                // Process the result set
                if (resultSet.next()) {
                    // Retrieve the encoded key from the result set
                    encodedKey = resultSet.getString("encodedkey");
                    System.out.println("Encoded Key for " + file_name + ": " + encodedKey);

                } else {
                    System.out.println("No encoded key found for file: " + file_name);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return encodedKey;

    }

    /**
     * @brief decode password method
     * @param user name as type String
     * @param pass plain password of type String
     * @return true if the credentials are valid, otherwise false
     */
    public boolean validateUser(String user, String pass) throws InvalidKeySpecException, ClassNotFoundException {
        Boolean flag = false;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + fullFilePath);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select name, password from " + this.dataBaseTableName);
            String inPass = generateSecurePassword(pass);
            // Let's iterate through the java ResultSet
            while (rs.next()) {
                if (user.equals(rs.getString("name")) && rs.getString("password").equals(inPass)) {
                    flag = true;
                    break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return flag;
    }

    private String getSaltvalue(int length) {
        StringBuilder finalval = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            finalval.append(characters.charAt(random.nextInt(characters.length())));
        }

        return new String(finalval);
    }

    /* Method to generate the hash value */
    private byte[] hash(char[] password, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keylength);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public String generateSecurePassword(String password) throws InvalidKeySpecException {
        String finalval = null;

        byte[] securePassword = hash(password.toCharArray(), saltValue.getBytes());

        finalval = Base64.getEncoder().encodeToString(securePassword);

        return finalval;
    }

    /**
     * @brief get table name
     * @return table name as String
     */
    public String getTableName() {
        return this.dataBaseTableName;
    }

    public String getFileTableName() {
        return this.dataBaseTableNameFileServer;
    }

    /**
     * @brief print a message on screen method
     * @param message of type String
     */
    public void log(String message) {
        System.out.println(message);

    }

//    public static void main(String[] args) throws InvalidKeySpecException {
//        DB myObj = new DB();
//        myObj.log("-------- Simple Tutorial on how to make JDBC connection to SQLite DB ------------");
//        myObj.log("\n---------- Drop table ----------");
//        myObj.delTable(myObj.getTableName());
//        myObj.log("\n---------- Create table ----------");
//        myObj.createTable(myObj.getTableName());
//        myObj.log("\n---------- Adding Users ----------");
//        myObj.addDataToDB("ntu-user", "12z34");
//        myObj.addDataToDB("ntu-user2", "12yx4");
//        myObj.addDataToDB("ntu-user3", "a1234");
//        myObj.log("\n---------- get Data from the Table ----------");
//        myObj.getDataFromTable(myObj.getTableName());
//        myObj.log("\n---------- Validate users ----------");
//        String[] users = new String[]{"ntu-user", "ntu-user", "ntu-user1"};
//        String[] passwords = new String[]{"12z34", "1235", "1234"};
//        String[] messages = new String[]{"VALID user and password",
//            "VALID user and INVALID password", "INVALID user and VALID password"};
//
//        for (int i = 0; i < 3; i++) {
//            System.out.println("Testing " + messages[i]);
//            if (myObj.validateUser(users[i], passwords[i], myObj.getTableName())) {
//                myObj.log("++++++++++VALID credentials!++++++++++++");
//            } else {
//                myObj.log("----------INVALID credentials!----------");
//            }
//        }
//    }
}
