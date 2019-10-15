import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class CwbProcess {
    final Logger logger =  Logger.getLogger(CwbProcess.class.getName());

    String cwbPath;
    String cwbOutput;

    public CwbProcess(String cwbPath) {
        this.cwbPath = cwbPath;
        this.cwbOutput = "";
    }

    public boolean run(String workingDir, String[] commandsToRun, String resultString) throws IOException {
        File logFile;
        try {
            String currentUser = commandsToRun[0].substring(commandsToRun[0].lastIndexOf("_") + 1, commandsToRun[0].lastIndexOf("."));
            String currentRule = commandsToRun[1].substring(commandsToRun[1].lastIndexOf("_") + 1, commandsToRun[1].lastIndexOf("."));
            logFile = Paths.get(workingDir, "log_u" + currentUser + "_r" + currentRule + ".txt").toFile();

            ProcessBuilder pb = new ProcessBuilder(this.cwbPath, "ccs");
            pb.directory(new File(workingDir));
            pb.redirectOutput(ProcessBuilder.Redirect.to(logFile));
//            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process process = pb.start();

            OutputStream outputStream = process.getOutputStream();
            PrintStream printOutputStream = new PrintStream(outputStream);

            for (String command : commandsToRun) {
                printOutputStream.println(command);
                printOutputStream.flush();
            }

            process.waitFor();

            printOutputStream.close();
        } catch (Exception e) {
            System.out.println("Error during evaluation routine: " + e.getMessage());
            return false;
        }

        return new String(Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8).contains(resultString);
    }
}
