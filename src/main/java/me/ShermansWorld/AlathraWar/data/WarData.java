package me.ShermansWorld.AlathraWar.data;

import java.io.IOException;
import java.io.InputStream;

import com.sun.tools.javac.util.Pair;
import org.bukkit.configuration.Configuration;
import java.io.Reader;
import java.io.InputStreamReader;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ShermansWorld.AlathraWar.Main;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nullable;

public class WarData
{
    private Main plugin;
    private File configFile;
    private FileConfiguration configConfig;


    //Stored as a pair so theres no need to sync two lists
    private final List<Pair<FileConfiguration, File>> warsData;
    
    public WarData(final Main plugin) {
        this.configFile = null;
        this.configConfig = null;
        this.warsData = new ArrayList<Pair<FileConfiguration, File>>();
        this.plugin = plugin;
        this.saveDefaultConfig();
    }
    
    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "wars.yml");
        }

        //Gather all war files in this
        warsData.clear();
        Stream<Path> stream = null;
        List<File> warsFiles = new ArrayList<File>();
        try {
            stream = Files.list(Paths.get(new URI(this.plugin.getDataFolder().toString() + "/wars/")));
            warsFiles.addAll(stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::toFile)
                    .filter(file -> {
                        if (file.getName().split(".").length > 1) {
                            String[] parse = file.getName().split(".");
                            int extensionCheck = Arrays.binarySearch(parse, "yml");
                            // -1 if search fail
                            if (extensionCheck >= 0) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        int i = 0;
        for(File file : warsFiles) {
            this.warsData.add(i, new Pair(YamlConfiguration.loadConfiguration(file), file));
            //template
            final InputStream defaultStream = this.plugin.getResource("warstemplate.yml");
            if (defaultStream != null) {
                final YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(defaultStream));
                this.warsData.get(i).fst.setDefaults((Configuration)defaultConfig);
            }
            i++;
        }
    }

    @Nullable
    public FileConfiguration getConfig(int id) {
        if (this.warsData.isEmpty() || this.warsData == null) {
            this.reloadConfig();
        }


        for (Pair<FileConfiguration,File> file : this.warsData) {
            if(file.fst.getInt("id") == id) {
                return file.fst;
            }
        }
        return null;
    }
    
    public void saveConfig(int id) {
        if (this.warsData.isEmpty() || this.warsData == null) {
            return;
        }

        FileConfiguration toSave = null;

        for (int i = 0; i < warsData.size(); i++) {
            if (warsData.get(i).fst.getInt("id") == id) {
                try {
                    warsData.get(i).fst.save(warsData.get(i).snd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Archive a wars file for later reference (deleted basically)
     * Also removes the war from the data list
     * @param id
     */
    public void archiveWar(int id) {
        if (this.warsData.isEmpty() || this.warsData == null) {
            return;
        }

        for (int i = 0; i < warsData.size(); i++) {
            if (warsData.get(i).fst.getInt("id") == id) {
                try {
                    Path archive = Paths.get(this.plugin.getDataFolder().toString() + "/wars/archive/" + warsData.get(i).snd.getName() + ".old");
                    Files.copy(warsData.get(i).snd.toPath(), archive, StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(warsData.get(i).snd.toPath());
                    warsData.remove(i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public FileConfiguration getConfigConfig() {
        if (this.warsData.isEmpty() || this.warsData == null) {
            this.reloadConfig();
        }

        return this.configConfig;
    }

    public void saveConfigConfig() {
        if (this.configConfig == null || this.configFile == null) {
            return;
        }
        try {
            this.getConfigConfig().save(this.configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "wars.yml");
        }
        if (!this.configFile.exists()) {
            this.plugin.saveResource("wars.yml", false);
        }
    }
}
