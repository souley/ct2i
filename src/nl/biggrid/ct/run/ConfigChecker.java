/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
import java.util.HashMap;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.mindbright.util.Util;
import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.ssh2.SSH2Preferences;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2ConsoleRemote;

import java.io.File;
import nl.biggrid.ct.ui.ParamFileEntity;
import nl.biggrid.ct.ui.TomoEntity;
import nl.biggrid.ct.ui.ProcessEntity;

public class ConfigChecker {
    static int BUFFER_SIZE = 1024;
    static int SLEEP_DURATION = 500;

    private String host;
    private String login;
    private String pwd;
    private String toolPath;
    private String inputPath;
    private String outputPath;
    private String jobPath;
    private String transferBufferPath;

    public ConfigChecker(final String host,
                    final String login,
                    final String pwd,
                    final String toolPath,
                    final String inputPath,
                    final String outputPath,
                    final String jobPath,
                    final String transferBufferPath
                    ) {
        this.host = host;
        this.login = login;
        this.pwd = pwd;
        this.toolPath = toolPath;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.jobPath = jobPath;
        this.transferBufferPath = transferBufferPath;
        FileTransferManager.instance().init(host, login, pwd);
    }

    String runCommand(final String cmd) {
        StringBuffer outputBuffer = new StringBuffer();
        try{
            String server = Util.getHost(host);
            int    port   = Util.getPort(server, RemoteRunner.DEFAULT_SSH_PORT);

            SSH2Preferences prefs = new SSH2Preferences();
            prefs.setPreference(SSH2Preferences.CIPHERS_C2S, RemoteRunner.CIPHER_BLOWFISH);
            prefs.setPreference(SSH2Preferences.CIPHERS_S2C, RemoteRunner.CIPHER_BLOWFISH);
            prefs.setPreference(SSH2Preferences.LOG_FILE, RemoteRunner.SSH_LOGFILE);

            Socket serverSocket     = new Socket(server, port);
            SSH2Transport transport = new SSH2Transport(serverSocket, prefs,
                                      RemoteRunner.createSecureRandom());
            SSH2SimpleClient client = new SSH2SimpleClient(transport,
                                      login, pwd);

            SSH2ConsoleRemote console = new SSH2ConsoleRemote(client.getConnection(), null, null);
            if (console.command(cmd, true)) {
                console.waitForExitStatus();
                BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(console.getStdOut()));

                String line;
                while((line = stdout.readLine()) != null) {
                    outputBuffer = outputBuffer.append(line);
                }
                //console.waitForExitStatus();
            } else {
                System.err.println("failed to execute command: " + cmd);
            }
            client.getTransport().normalDisconnect("User disconnects");
        } catch(Exception e){
            outputBuffer = outputBuffer.append(e.getMessage());
        }
        return outputBuffer.toString();
    }

    String checkDirectory(final String what) {
        String failureReason = runCommand("stat --format=%F "+what);
        if (failureReason.equalsIgnoreCase("directory")) {
            failureReason = "";
        } else if (failureReason.indexOf("cannot stat") != -1) {
            failureReason = failureReason.substring(failureReason.lastIndexOf("stat")+4).trim();
        }
        return failureReason;
    }

    public String checkToolPath() {
        return checkDirectory(toolPath);
    }

    public String checkInputPath() {
        return checkDirectory(inputPath);
    }

    public String checkOutputPath() {
        return checkDirectory(outputPath);
    }

    public String checkJobScript(final String jobFile) {
        return checkFile(jobFile);
    }

    public String createDirectory(final String path) {
        return runCommand("mkdir -p "+path);
    }

    String checkFile(final String what) {
        String failureReason = runCommand("stat --format=%F "+what);
        if (failureReason.equalsIgnoreCase("regular file")) {
            failureReason = "";
        } else if (failureReason.indexOf("cannot stat") != -1) {
            failureReason = failureReason.substring(failureReason.lastIndexOf("stat")+4).trim();
        }
        return failureReason;
    }

    public String checkFileArgs(final HashMap<String, TomoEntity> entityMap) {
        String checkOutput = "";
        TomoEntity tomo = entityMap.get("tomogram");
        if (tomo != null) {
            checkOutput += moveFileToIf(((ParamFileEntity) tomo).getURL());
        }
        tomo = entityMap.get("template");
        if (tomo != null) {
            if (!checkOutput.isEmpty()) checkOutput += "<br>";
            checkOutput += moveFileToIf(((ParamFileEntity) tomo).getURL());
        }
        tomo = entityMap.get("psf");
        if (tomo != null) {
            if (!checkOutput.isEmpty()) checkOutput += "<br>";
            checkOutput += moveFileToIf(((ParamFileEntity) tomo).getURL());
        }
        tomo = entityMap.get("mask");
        if (tomo != null) {
            if (!checkOutput.isEmpty()) checkOutput += "<br>";
            checkOutput += moveFileToIf(((ParamFileEntity) tomo).getURL());
        }
        tomo = entityMap.get("omnimatch");
        if (tomo != null) {
            if (!checkOutput.isEmpty()) checkOutput += "<br>";
            checkOutput += moveBinToIf(((ProcessEntity) tomo).getCommand());
        }
        return checkOutput;
    }

    public boolean moveFileTo(final String from, final String to) {
        return FileTransferManager.instance().moveFileTo(from, to);
    }

    public boolean moveFilesTo(final String[] files, final String to) {
        return FileTransferManager.instance().moveFilesTo(files, to);
    }

    String moveFileToIf(final String inputFile) {
        String result = "";
        String remoteFile = inputPath + "/" + inputFile;
        String checkMsg = checkFile(remoteFile);
        if (!checkMsg.isEmpty()) {
            String localFile = transferBufferPath + File.separator + inputFile;
            if ((new File(localFile)).isFile()) {
                if (!moveFileTo(localFile, remoteFile)) {
                    result += (checkMsg + " and cannot transfer file '" + localFile +"'<br>");
                }
            } else {
                result += (checkMsg + " and cannot find file '" + localFile +"'<br>");
            }
        }
        return result;
    }

    String moveBinToIf(final String binFile) {
        String result = "";
        String remoteFile = toolPath + "/" + binFile;
        String checkMsg = checkFile(remoteFile);
        if (!checkMsg.isEmpty()) {
            String localFile = ConfigManager.CONFIG_DIR + File.separator + "bin" + File.separator + binFile;
            if ((new File(localFile)).isFile()) {
                if (!moveFileTo(localFile, remoteFile)) {
                    result += (checkMsg + " and cannot transfer file '" + localFile +"'<br>");
                }
            } else {
                result += (checkMsg + " and cannot find file '" + localFile +"'<br>");
            }
        }
        //Make executable
        runCommand("chmod +x " + remoteFile);
        return result;
    }

    public String checkFileArgs() {
        String checkOutput = "";
        ConfigManager configManager = ConfigManager.instance();
        checkOutput += moveFileToIf(configManager.getString("omnimatch.tomogram[@file]"));
        checkOutput += moveFileToIf(configManager.getString("omnimatch.template[@file]"));
        checkOutput += moveFileToIf(configManager.getString("omnimatch.mask[@file]"));
        checkOutput += moveFileToIf(configManager.getString("omnimatch.psf[@file]"));
        return checkOutput;
    }
}
