/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
import java.io.File;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.mindbright.util.Util;
import com.mindbright.util.SecureRandomAndPad;
import com.mindbright.util.RandomSeed;

import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.ssh2.SSH2Preferences;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2ConsoleRemote;
import com.mindbright.jca.security.SecureRandom;

public abstract class RemoteRunner implements IRunner {
    public final static int    DEFAULT_SSH_PORT = 22;
    public final static String CIPHER_BLOWFISH = "blowfish-cbc";
    public final static String SSH_LOGFILE = "ssh.log";

    final static int BUFFER_SIZE      = 1024;
    final static String UNIX_RANDOM_SEED = "/dev/urandom";

    private String host;
    private String login;
    private String pwd;

    class RemoteRunInfo extends RunInfo {
        private String cmdOutput;
        RemoteRunInfo(final String c) {
            cmdOutput = c;
        }
        public String getName() {
            return cmdOutput;
        }
    }

    public RemoteRunner(final String host,
                      final String login,
                      final String pwd) {
        this.host  = host;
        this.login = login;
        this.pwd   = pwd;
    }

    public String runCmd(final String cmd, final boolean quiet, final boolean mergeOutputs) {
        StringBuffer outputBuffer = new StringBuffer();
        try{
            String server = Util.getHost(host);
            int    port   = Util.getPort(server, RemoteRunner.DEFAULT_SSH_PORT);

            SSH2Preferences prefs = new SSH2Preferences();
            prefs.setPreference(SSH2Preferences.CIPHERS_C2S, CIPHER_BLOWFISH);
            prefs.setPreference(SSH2Preferences.CIPHERS_S2C, CIPHER_BLOWFISH);
            prefs.setPreference(SSH2Preferences.LOG_FILE, SSH_LOGFILE);

            Socket serverSocket     = new Socket(server, port);
            SSH2Transport transport = new SSH2Transport(serverSocket, prefs,
                                      RemoteRunner.createSecureRandom());
            SSH2SimpleClient client = new SSH2SimpleClient(transport, login, pwd);
            SSH2ConsoleRemote console = new SSH2ConsoleRemote(client.getConnection(), null, System.err);

            String wrappedCmd;
            if (quiet) {
                wrappedCmd = "nohup "+cmd+" > ssh.out 2> ssh.err < /dev/null &";
            } else if (mergeOutputs) {
                wrappedCmd = cmd+" 2>&1";
            } else {
                wrappedCmd = cmd;
            }

            if (console.command(wrappedCmd)) {
                console.waitForExitStatus();
                BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(console.getStdOut()));
                String line;
                while((line = stdout.readLine()) != null) {
                    outputBuffer = outputBuffer.append(line+"\n");
                }
            } else {
                System.err.println("failed to execute command: " + cmd);
            }
            client.getTransport().normalDisconnect("User disconnects");
        }
        catch(Exception e){
            outputBuffer = outputBuffer.append(e.getMessage());
            //e.printStackTrace();
        }
        return outputBuffer.toString();
    }

    public String run(final String cmd) {
        return runCmd(cmd, false, false);
    }

    public RunInfo monitor(final String cmd) {
        return new RemoteRunInfo(runCmd(cmd, true, false));
    }

    public String stop(final String cmd) {
        return runCmd(cmd, true, false);
    }

    public void createSummaryFile(final String where, final String what) {
        String summaryFilename = where+"/summary.xml";
        String wrappedCmd = "echo '"+what+"' > "+summaryFilename;
        runCmd(wrappedCmd, false, false);
    }

    public static SecureRandomAndPad createSecureRandom() {
        byte[] seed;
        File devRandom = new File(UNIX_RANDOM_SEED);
        if (devRandom.exists()) {
            RandomSeed rs = new RandomSeed(UNIX_RANDOM_SEED, UNIX_RANDOM_SEED);
            seed = rs.getBytesBlocking(20);
        } else {
            seed = RandomSeed.getSystemStateHash();
        }
        return new SecureRandomAndPad(new SecureRandom(seed));
    }

}
