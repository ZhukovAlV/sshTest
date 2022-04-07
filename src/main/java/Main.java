import com.jcraft.jsch.Session;

import java.util.LinkedList;
import java.util.List;

public class Main {
    private static final String HOST = "127.0.0.1";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final List<String> COMMANDS = new LinkedList<>(
            List.of("ls -a",
                    "cd VoiceApp/forJava14",
                    "ls -a",
                    "export DISPLAY=:0.0",
                    "nohup java -jar voicetest.jar"
            ));

    private static final String SFTPWORKINGDIR = "Test"; // Source Directory on SFTP server
    private static final String LOCALDIRECTORY = "myTestFolder"; // Local Target Directory

    public static void main(String[] args) {

        Session session = SSHUtil.createSession(HOST,USER,PASSWORD);

        // Просто выполнить команды
/*        var channelExec= SSHUtil.getChannel(session, TypeChannel.EXEC);
        SSHUtil.cmdExec(channelExec, COMMANDS);
        channelExec.disconnect();*/


        var channelSftp = SSHUtil.getChannel(session, TypeChannel.SFTP);

        // Скопировать папку с файлами с удаленного сервера
        SSHUtil.downloadDirectoryFromRemoteServer(channelSftp, SFTPWORKINGDIR, LOCALDIRECTORY);

        // Загрузить на удаленный сервер папку с файлами
        // SSHUtil.uploadFolderToRemoteServer(channelSftp, LOCALDIRECTORY, SFTPWORKINGDIR);

       channelSftp.disconnect();

    }
}
