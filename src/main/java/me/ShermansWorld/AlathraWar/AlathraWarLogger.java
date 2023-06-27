package me.ShermansWorld.AlathraWar;

import org.bukkit.Bukkit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlathraWarLogger {

    final File logsFolder;
    final File log;
    PrintWriter output = null;

    public AlathraWarLogger() {
        logsFolder = new File("plugins" + File.separator + "AlathraWar" + File.separator + "logs");
        log = new File(
            "plugins" + File.separator + "AlathraWar" + File.separator + "logs" + File.separator + "log.txt");
    }

    public boolean checkFiles() {
        if (!logsFolder.exists()) {
            Bukkit.getLogger().warning("[AlathraWar] Error in AlathraWarLogger - could not find logs directory");
            return false;
        }

        if (!log.exists()) {
            Bukkit.getLogger().warning("[AlathraWar] Error in AlathraWarLogger - could not find log.txt");
            return false;
        }

        return true;
    }

    private void initFileWriter() {
        // init file writer
        try {
            FileWriter fw = new FileWriter(
                "plugins" + File.separator + "AlathraWar" + File.separator + "logs" + File.separator + "log.txt",
                true);
            BufferedWriter bw = new BufferedWriter(fw);
            output = new PrintWriter(bw);
        } catch (IOException e1) {
            Bukkit.getLogger().warning("[AlathraWar] Error in AlathraWarLogger - could not initialize file writer");
        }
    }

    public void log(String msg) {
        // check for null files
        if (!checkFiles()) {
            return;
        }

        initFileWriter();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String logMsg = "[" + format.format(date) + "] " + msg;
        output.println(logMsg);
        output.close();
    }
}
