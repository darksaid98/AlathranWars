package com.github.alathra.AlathranWars;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlathranWarsLogger {

    final @NotNull File logsFolder;
    final @NotNull File log;
    @Nullable PrintWriter output = null;

    public AlathranWarsLogger() {
        logsFolder = new File("plugins" + File.separator + "AlathranWars" + File.separator + "logs");
        log = new File(
            "plugins" + File.separator + "AlathranWars" + File.separator + "logs" + File.separator + "log.txt");
    }

    public boolean checkFiles() {
        if (!logsFolder.exists()) {
            Bukkit.getLogger().warning("[AlathranWars] Error in AlathranWarsLogger - could not find logs directory");
            return false;
        }

        if (!log.exists()) {
            Bukkit.getLogger().warning("[AlathranWars] Error in AlathranWarsLogger - could not find log.txt");
            return false;
        }

        return true;
    }

    private void initFileWriter() {
        // init file writer
        try {
            @NotNull FileWriter fw = new FileWriter(
                "plugins" + File.separator + "AlathranWars" + File.separator + "logs" + File.separator + "log.txt",
                true);
            @NotNull BufferedWriter bw = new BufferedWriter(fw);
            output = new PrintWriter(bw);
        } catch (IOException e1) {
            Bukkit.getLogger().warning("[AlathranWars] Error in AlathranWarsLogger - could not initialize file writer");
        }
    }

    public void log(String msg) {
        // check for null files
        if (!checkFiles()) {
            return;
        }

        initFileWriter();
        @NotNull Date date = new Date();
        @NotNull SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        @NotNull String logMsg = "[" + format.format(date) + "] " + msg;
        output.println(logMsg);
        output.close();
    }
}
