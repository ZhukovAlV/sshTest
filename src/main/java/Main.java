import com.jcraft.jsch.Session;

import java.util.LinkedList;
import java.util.List;

public class Main {
    private static final String HOST = "10.255.253.146";
    private static final String USER = "user";
    private static final String PASSWORD = "20122012";
    private static final List<String> COMMANDS = new LinkedList<>(
            List.of("ls -a",
                    "cd VoiceApp/forJava14",
                    "ls -a",
                    "export DISPLAY=:0.0",
                    "nohup java -jar voicetest.jar"
            ));

    private static final String SFTPWORKINGDIR = "VoiceApp"; // Source Directory on SFTP server
    private static final String LOCALDIRECTORY = "myTestFolder"; // Local Target Directory

    public static void main(String[] args) {

        Session session = SSHUtil.createSession(HOST,USER,PASSWORD);

        // Просто выполнить команды
        // SSHUtil.cmdExec(session, COMMANDS);

        // Скопировать папку с файлами
        SSHUtil.downloadFromFolder(session, SFTPWORKINGDIR, LOCALDIRECTORY);
    }
}
