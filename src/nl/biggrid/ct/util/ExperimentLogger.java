/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.io.FileOutputStream;

import java.io.InputStream;
import nl.biggrid.ct.util.StringEncrypter.EncryptionException;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javax.swing.JFrame;
import nl.biggrid.ct.run.ConfigManager;
/**
 *
 * @author Souley
 */
public class ExperimentLogger {
    final String DB_SETTINGS_FILE = File.separator+"config"+File.separator+"settings"+File.separator+"db-settings.properties";
    final String CURRENT_DIR = System.getProperty("user.dir");
    final String USER_CT2I = CURRENT_DIR + File.separator+ ".ct2i";
    final String CONFIG_FILE = "defaultsettings.xml";
    //private Properties settings = new Properties();
    private StringEncrypter encrypter = null;
    private String dbHost = "", dbUser = "", dbPwd = "";
    private Connection dbConnection = null;
    private String sqlExceptionMsg = "";
    private boolean ct2iKnown = false;
    private ConfigManager configManager = ConfigManager.instance();

    public ExperimentLogger() {
        try {
            encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME, "123456789012345678901234567890");
        } catch (EncryptionException ex) {
            ex.printStackTrace();
        }
        ct2iKnown = (new File(USER_CT2I)).isDirectory();
        initSettings();
        saveEncryptedSettings();

        dbConnection = getConnection();
    }

    void initSettings() {
//        try {
//            InputStream in = null;
//            if (ct2iKnown) {
//                in = new FileInputStream(USER_CT2I + DB_SETTINGS_FILE);
//            } else {
//                in = new FileInputStream(CURRENT_DIR + DB_SETTINGS_FILE);
//            }
//            settings.load(in);
//            in.close();
//        } catch (FileNotFoundException fnfe) {
//            System.err.println("Cannot load settings file:\n\t"+fnfe.getMessage());
//            return;
//        } catch (IOException ioe) {
//            System.err.println("IO exception :\n\t"+ioe.getMessage());
//            return;
//        }
        
//        String encryption = settings.getProperty("db_encryption");
//        if (encryption.equalsIgnoreCase("on")) {
//            try {
//                dbHost = encrypter.decrypt(settings.getProperty("db_host"));
//                dbUser = encrypter.decrypt(settings.getProperty("db_user"));
//                dbPwd = encrypter.decrypt(settings.getProperty("db_pwd"));
//            } catch (EncryptionException ex) {
//                ex.printStackTrace();
//            }
//        } else {
//            dbHost = settings.getProperty("db_host");
//            dbUser = settings.getProperty("db_user");
//            dbPwd = settings.getProperty("db_pwd");
//        }

            try {
                dbHost = encrypter.decrypt(configManager.getString("database.url"));
                dbUser = encrypter.decrypt(configManager.getString("database.user"));
                dbPwd = encrypter.decrypt(configManager.getString("database.pwd"));
            } catch (EncryptionException ex) {
                ex.printStackTrace();
            }
    }

    void saveEncryptedSettings() {
//        settings.setProperty("db_encryption", "on");
//        try {
//            settings.setProperty("db_host", encrypter.encrypt(dbHost));
//            settings.setProperty("db_user", encrypter.encrypt(dbUser));
//            settings.setProperty("db_pwd", encrypter.encrypt(dbPwd));
//        } catch (EncryptionException ex) {
//            ex.printStackTrace();
//        }
        try {
            configManager.setProperty("database.url", encrypter.encrypt(dbHost));
            configManager.setProperty("database.user", encrypter.encrypt(dbUser));
            configManager.setProperty("database.pwd", encrypter.encrypt(dbPwd));
        } catch (EncryptionException ex) {
            ex.printStackTrace();
        }
        //configManager.save(USER_CT2I+CONFIG_FILE);
//        try {
//            final String pfp = USER_CT2I+DB_SETTINGS_FILE;
//            String path = pfp.substring(0, pfp.lastIndexOf(File.separator));
//            File ct2iConfigDir = new File(path);
//            if (!ct2iConfigDir.isDirectory() && !ct2iConfigDir.mkdirs()) {
//                System.err.println("Cannot create directory '"+path+"'");
//                return;
//            }
//            File propFile = new File(pfp);
//            if (!propFile.isFile() && !propFile.createNewFile()) {
//                System.err.println("Cannot create file '"+pfp+"'");
//                return;
//            }
//            FileOutputStream out = new FileOutputStream(propFile);
//            //settings.store(out, "");
//            out.close();
//        } catch (FileNotFoundException fnfe) {
//            System.err.println("Cannot save DB settings file:\n\t"+fnfe.getMessage());
//            return;
//        } catch (IOException ioe) {
//            System.err.println("IO exception :\n\t"+ioe.getMessage());
//            return;
//        }
    }

    Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(dbHost, dbUser, dbPwd);
        } catch (SQLException ex) {
            ex.printStackTrace();
            sqlExceptionMsg = ex.getMessage();
        }
        return con;
    }

    int insertData(final String insertStatement) {
        int addedRowCount = 0;
        if (dbConnection != null) {
            try {
                Statement stmt = dbConnection.createStatement();
                addedRowCount = stmt.executeUpdate(insertStatement);
            } catch (SQLException sqle) {
                System.err.println("SQL exception :\n\t"+sqle.getMessage());
                sqlExceptionMsg = sqle.getMessage();
            }
        }
        return addedRowCount;
    }

    public void addExperiment(final JFrame parent, final String host, final String user, final String tooldir,
            final String indir, final String outdir, final String jobdir, final String experimentName) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("insert into Experiment (HostName,UserLogin,ToolDir,InputDir,OutputDir,JobScript,RunningInstance) values (");
        sBuffer.append("\""+host+"\", ");
        sBuffer.append("\""+user+"\", ");
        sBuffer.append("\""+tooldir+"\", ");
        sBuffer.append("\""+indir+"\", ");
        sBuffer.append("\""+outdir+"\", ");
        sBuffer.append("\""+jobdir+"\", ");
        sBuffer.append("\""+experimentName+"\")");
        if (insertData(sBuffer.toString()) != 1) {
            nl.biggrid.ct.util.MessageBox.showErrorMsg(parent, sqlExceptionMsg);
        }

    }
}
