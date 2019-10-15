/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
        final List<String[]> executed = new ArrayList<>();

        List<String> users = loadFiles(new File(dataFolder), ".ccs");
        List<String> props = loadFiles(new File(dataFolder), ".mu");

        final List<String[]> runCommands = new ArrayList<>();
        for (String prop : props) {
            for (String user : users) {
                runCommands.add(getCommands(user, prop));
            }
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        for (int i = 0; i < runCommands.size(); i++) {
            final String[] commands = runCommands.get(i);

            threadPool.submit(new Runnable() {
                public void run() {
                    logger.info("RUNNING: " + commands[0] + " + " + commands[1] + " ...");
                    CwbProcess cwb = new CwbProcess(cwbPath);
                    try {
                        boolean result = cwb.run(workingDir, commands, "TRUE");
                        logger.info("DONE: " + commands[0] + " + " + commands[1] + " = " + result);
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
