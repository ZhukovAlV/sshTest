import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;


public class SSHUtil {

    private static final int SHORT_WAIT_MSEC = 10000;

    public static Session createSession(String host, String user, String password) {
        Session session = null;
        try {
            session = new JSch().getSession(user, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // It must not be recommended, but if you want to skip host-key check
            session.connect(SHORT_WAIT_MSEC);
            System.out.println("SSH session created");
        } catch (JSchException e) {
            System.out.println("SSH session not created " + e);
        }
        return session;
    }

    public static void cmdExec(Channel channel, List<String> commands) {
        try (InputStream in = channel.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {
            String stringList = String.join("; ", commands);
            ((ChannelExec)channel).setCommand(stringList);
            //channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            channel.connect();

            String line;
            while((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
            System.out.println("DONE");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    static void downloadDirectoryFromRemoteServer(Channel channelSftp, String remoteSourceDirectory, String localDestinationDirectory) {
        try {
            channelSftp.connect();

            Vector<ChannelSftp.LsEntry> entries = ((ChannelSftp)channelSftp).ls(remoteSourceDirectory);
            new File(localDestinationDirectory).mkdir();

            // Загружаются файлы только из каталога, из подкаталогов не загружаются
            for (ChannelSftp.LsEntry en : entries) {
                if (en.getFilename().equals(".") || en.getFilename().equals("..") || en.getAttrs().isDir()) {
                    continue;
                }
                System.out.println("Downloading " + (remoteSourceDirectory + en.getFilename()) + " ----> " + "download" + File.separator + en.getFilename());
                ((ChannelSftp)channelSftp).get(remoteSourceDirectory + "/" + en.getFilename(), localDestinationDirectory + File.separator + en.getFilename());
                System.out.println("End downloading " + (remoteSourceDirectory + en.getFilename()));
            }
        } catch (SftpException | JSchException e) {
            e.printStackTrace();
        }
    }

    static void uploadFolderToRemoteServer(Channel channelSftp, String localSourceDirectory, String remoteDestinationDirectory) {
        try {
            channelSftp.connect();

            File sourceDir = new File(localSourceDirectory);
            if (!sourceDir.getName().startsWith(".")) {
                File[] files = sourceDir.listFiles();

                for (File file : files) {
                    if (file.getName().equals(".") || file.getName().equals("..")) {
                        continue;
                    } else if (file.isDirectory()) {
                        SftpATTRS attrs = null;
                        // check if the directory is already existing
                        try {
                            attrs = ((ChannelSftp)channelSftp).stat(remoteDestinationDirectory + File.separator + file.getName());
                        } catch (Exception e) {
                            System.out.println(remoteDestinationDirectory + File.separator + file.getName() + " not found");
                        }
                        if (attrs != null) {
                            System.out.println("Directory exists IsDir=" + attrs.isDir());
                        } else {
                            System.out.println("Creating dir " + remoteDestinationDirectory + File.separator + file.getName());
                            ((ChannelSftp)channelSftp).mkdir(remoteDestinationDirectory + File.separator + file.getName());
                        }
                        uploadFolderToRemoteServer(channelSftp,
                                localSourceDirectory + File.separator + file.getName(),
                                remoteDestinationDirectory + File.separator + file.getName());
                    } else {
                        System.out.println("Uploading " + (localSourceDirectory + File.separator + file.getName()) + " ----> " + remoteDestinationDirectory + File.separator + file.getName());
                        ((ChannelSftp)channelSftp).put(localSourceDirectory + File.separator + file.getName(), remoteDestinationDirectory + File.separator + file.getName());
                        System.out.println("End uploading " + (localSourceDirectory + file.getName()));
                    }
                }
            }
        } catch (SftpException | JSchException e) {
            e.printStackTrace();
        }
    }

    public static Channel getChannel(Session session, TypeChannel typeChannel) {
        Channel channel = null;
        try {
            if (typeChannel.equals(TypeChannel.SFTP)) channel = session.openChannel("sftp");
            else if (typeChannel.equals(TypeChannel.EXEC)) channel = session.openChannel("exec");
            System.out.println("channel connected");
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return channel;
    }
    
}
