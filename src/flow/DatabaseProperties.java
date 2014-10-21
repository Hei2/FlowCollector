package flow;

import classifier.UninitializedException;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseProperties {
    private static final int NO_FIELDS = 3;
    private static String user = "flow";
    private static String password = "Knuckles";
    private static String database = "flowcollectordb";
    
    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }
    
    public static String getDatabase(){
        return database;
    }
    
    /**
     * Initializes the database class from a config file. The config file must
     * have the following format: URL=<url>
     * USER=<user>
     * PASS=<pass>
     * DB=<db>
     *
     * @param config
     */
    public static void load(File config) throws UninitializedException, FileNotFoundException {
        boolean  userGiven = false, passGiven = false, dbGiven = false;
        int fields = 0;
        try (Scanner scanner = new Scanner(config);) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine(); 
                if (line.toUpperCase().startsWith("USER=")) {
                    user = line.substring(5);
                    userGiven = true;
                    fields++;
                } else if (line.toUpperCase().startsWith("PASS=")) {
                    password = line.substring(5);
                    passGiven = true;
                    fields++;
                } else if (line.toUpperCase().startsWith("DB=")) {
                    database = line.substring(3);
                    dbGiven = true;
                    fields++;
                }
            }

            if (fields < NO_FIELDS) {
                String error = "";
                if (!userGiven) {
                    error += "User definition not found in config file.";
                }
                if (!passGiven) {
                    error += "Password definition not found in config file.";
                }
                if (!dbGiven) {
                    error += "Database definition not found in config file.";
                }
                throw new UninitializedException("The configuration file was not complete." + error);
            }
        }
    }
}
