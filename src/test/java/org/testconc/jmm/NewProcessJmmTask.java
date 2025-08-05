package org.testconc.jmm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NewProcessJmmTask {

    public static void main(String[] args) throws IOException, InterruptedException {
        int ran = ThreadLocalRandom.current().nextInt(1000000000, 2100000000);
        List<ProcessBuilder> pbs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProcessBuilder pb = new ProcessBuilder("/usr/bin/bash", "-c", "java -XX:+StressLCM -XX:+StressGCM -XX:+StressIGVN -XX:+StressCCP -XX:StressSeed="+ran+" ~/Documents/job/Javas/JavaConcurrencyHighLevelAPI/src/test/java/org/testconc/jmm/WithMain.java >> ~/Documents/job/Javas/JavaConcurrencyHighLevelAPI/res.txt");
            pb.redirectErrorStream(true);
            pbs.add(pb);
        }
//        ProcessBuilder pb = new ProcessBuilder("/usr/bin/bash", "-c", "javac ~/Documents/job/Javas/JavaConcurrencyHighLevelAPI/src/test/java/org/testconc/jmm/WithMain.java");
//        ProcessBuilder pb = new ProcessBuilder("/usr/bin/bash", "-c", "echo $horse $dog $HOME > ~/Documents/job/Javas/JavaConcurrencyHighLevelAPI/res.txt");

//        List<Process> prs = new ArrayList<>();
        List<Process> prsRunning = new ArrayList<>();
        for (ProcessBuilder pb : pbs) {
            Process pr = pb.start();
            prsRunning.add(pr);
            if(prsRunning.size() >= 2) {
                System.out.println(pr.pid());
                prsRunning.removeFirst().waitFor();
            }
        }

        System.out.println("The END");
    }
}
