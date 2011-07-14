/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

import java.io.File;
import java.net.Socket;

import com.mindbright.ssh2.SSH2Preferences;
import com.mindbright.util.Util;

import com.mindbright.sshcommon.SSHSCP1;
import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2SCP1Client;

/**
 *
 * @author Souley
 */
public class FileTransferManager {
    static private FileTransferManager _instance = null;
    public final static String SCP_LOGFILE = "scp.log";

    private String host = "";
    private String user = "";
    private String pwd = "";

    protected FileTransferManager() {
    }

    public void init(final String aHost, final String aUser, final String aPwd) {
        host = aHost;
        user = aUser;
        pwd = aPwd;
    }

    static public FileTransferManager instance() {
        if (null == _instance) {
            _instance = new FileTransferManager();
        }
        return _instance;
    }

    boolean transferFile(String srcFile, String dstFile, final String dir) {
        boolean success = true;
        try {
            String server = Util.getHost(host);
            int    port = Util.getPort(server, RemoteRunner.DEFAULT_SSH_PORT);

            SSH2Preferences prefs = new SSH2Preferences();

            prefs.setPreference(SSH2Preferences.CIPHERS_C2S, RemoteRunner.CIPHER_BLOWFISH);
            prefs.setPreference(SSH2Preferences.CIPHERS_S2C, RemoteRunner.CIPHER_BLOWFISH);
            prefs.setPreference(SSH2Preferences.LOG_FILE, SCP_LOGFILE);

            Socket serverSocket   = new Socket(server, port);
            SSH2Transport transport = new SSH2Transport(serverSocket, prefs,
                                                        RemoteRunner.createSecureRandom());
            SSH2SimpleClient client = new SSH2SimpleClient(transport, user, pwd);
            SSH2SCP1Client scpClient =
                new SSH2SCP1Client(new File(System.getProperty("user.dir")),
                                   client.getConnection(), System.err, false);

            SSHSCP1 scp = scpClient.scp1();

            if (dir.equalsIgnoreCase("to")) {
                scp.copyToRemote(srcFile, dstFile, false);
            } else if (dir.equalsIgnoreCase("from")) {
                scp.copyToLocal(dstFile, srcFile, false);
            }

            transport.normalDisconnect("User disconnects");
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        return  success;
    }

    public boolean moveFileTo(String srcFile, String dstFile) {
       return transferFile(srcFile, dstFile, "to");
    }

    public boolean moveFrom(String srcFile, String dstFile) {
        return transferFile(srcFile, dstFile, "from");
    }

    public boolean moveFilesTo(String[] files, String dstDir) {
        for (String file : files) {
            String filename = file.substring(file.lastIndexOf(File.separator)+1);
            if (!moveFileTo(file, dstDir+"/"+filename)) {
                return false;
            }
        }
        return true;
    }

//    public boolean fetchResults(final String pathFrom) {
//        String outputName = ConfigManager.instance().getString("omnimatch.output.value");
//        String dstDir = ConfigManager.instance().getString("application.transferbuffer");
//        String[] files = new String[]{".ccf", ".ang"};
//        for (String file : files) {
//            String filename = outputName + file;
//            String remoteFile = pathFrom + "/" + filename;
//            String localFile = dstDir + File.separator + filename;
//            if (!moveFrom(remoteFile, localFile)) {
//                return false;
//            }
//        }
//        return true;
//    }
    public boolean fetchResults(final String pathFrom) {
        String dirName = pathFrom.substring(pathFrom.lastIndexOf("/")+1);
        String outputName = ConfigManager.instance().getString("omnimatch.output.value");
        String dstDir = ConfigManager.instance().getString("application.transferbuffer");
        dstDir += (File.separator+dirName);
        if (!(new File(dstDir)).mkdirs()) {
            System.out.println("Cannot create directory '" + dstDir + "'");
            return false;
        }
        String[] files = new String[]{".ccf", ".ang"};
        for (String file : files) {
            String filename = outputName + file;
            String remoteFile = pathFrom + "/" + filename;
            String localFile = dstDir + File.separator + filename;
            if (!moveFrom(remoteFile, localFile)) {
                return false;
            }
        }
        final String summary = "summary.xml";
        if (!moveFrom(pathFrom + "/" + summary, dstDir + File.separator + summary)) {
            return false;
        }
        return true;
    }

}
