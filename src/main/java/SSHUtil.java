import com.jcraft.jsch.*;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;


public class SSHUtil {

    private static final int SHORT_WAIT_MSEC = 100;

    public static Session createSession(String host, String user, String password) {
        Session session = null;
        try {
            session = new JSch().getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // It must not be recommended, but if you want to skip host-key check
            session.connect(10000);
            System.out.println("SSH session created");
        } catch (JSchException e) {
            System.out.println("SSH session not created " + e);
        }
        return session;
    }

    public static void cmdExec(Session session, List<String> commands) {
        Channel channel = null;
        InputStream in = null;
        BufferedReader bufferedReader = null;
        try {
            channel = session.openChannel("exec");
            String string = String.join("; ", commands);
            ((ChannelExec)channel).setCommand(string);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            in = channel.getInputStream();
            channel.connect();

            bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
        } catch(Exception e){
            e.printStackTrace();
        }  finally {
            try {
                assert bufferedReader != null;
                bufferedReader.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel.disconnect();
            session.disconnect();
            System.out.println("DONE");
        }
    }

    static void downloadFromFolder(Session session, String sftpWorkingDir, String localDirectory) {
        try {
            ChannelSftp channelSftp = (ChannelSftp)session.openChannel("sftp");
            channelSftp.connect();
            System.out.println("channel connected");

            Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(sftpWorkingDir);
            new File(localDirectory).mkdir();

            //download all files (except the ., .. and folders) from given folder
            for (ChannelSftp.LsEntry en : entries) {
                if (en.getFilename().equals(".") || en.getFilename().equals("..") || en.getAttrs().isDir()) {
                    continue;
                }

                System.out.println("Downloading " + (sftpWorkingDir + en.getFilename()) + " ----> " + "download" + File.separator + en.getFilename());
                channelSftp.get(sftpWorkingDir + "/" + en.getFilename(), localDirectory + File.separator + en.getFilename());
                System.out.println("End downloading " + (sftpWorkingDir + en.getFilename()));
            }
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }
    }
}
