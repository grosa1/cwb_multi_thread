import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        final Logger logger =  Logger.getLogger(Main.class.getName());

        if (Arrays.asList(args).contains("--help")) {
            System.out.println("USAGE: java -jar cwb-runner.jar <cwb path> <working dir> <max thread count> <data folder>");
            return;
        }

        final String cwbPath = args[0];
        final String workingDir = args[1];
        int poolSize = Integer.parseInt(args[2]);
        String dataFolder = args[3];

        List<String> users = loadFiles(new File(dataFolder), ".ccs");
        List<String> props = loadFiles(new File(dataFolder), ".mu");

        final List<String[]> runCommands = new ArrayList<>();
        for (String prop : props) {
            for (String user : users) {
                runCommands.add(getCommands(user, prop));
            }
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        for (String[] runCommand : runCommands) {
            threadPool.submit(new Runnable() {
                public void run() {
                    logger.info("RUNNING: " + runCommand[0] + " + " + runCommand[1] + " ...");
                    try {
                        boolean result = new CwbProcess(cwbPath).run(workingDir, runCommand, "TRUE");
                        logger.info("DONE: " + runCommand[0] + " + " + runCommand[1] + " = " + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static String[] getCommands(String userPath, String propPath) {
        return new String[]{
                "load " + userPath,
                "load " + propPath,
                "chk U X0",
                "quit"
        };
    }

    public static List<String> loadFiles(File folder, String ext) {
        List<String> files = new ArrayList<>();
        for (final File f : folder.listFiles()) {
            if (f.isFile()) {
                if (f.getName().endsWith(ext)) {
                    files.add(f.getAbsolutePath());
                }
            }
        }

        return files;
    }
}
