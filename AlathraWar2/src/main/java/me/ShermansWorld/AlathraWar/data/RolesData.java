package me.ShermansWorld.AlathraWar.data;

import org.bukkit.Bukkit;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RolesData {

    public String editData(UUID pID, String key, Object value) {
        Map <String, Object> userData = getPlayer(pID);
        userData.put(key, value);
        setPlayer(pID, userData);
        return "Banana";
    }

    public Object getData(UUID pID) {
        return getPlayer(pID);
    }

    private File getFile(String strID) {
        // Gets user file, or creates if not yet made.
        File userFile = new File("plugins" + File.separator + "AlathraWar" + File.separator + "userdata" + File.separator + strID + ".yml");
        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[AlathraWar] Encountered error when creating user file for " + strID);
            }
        }
        return userFile;
    }

    private Map getPlayer(UUID pID) {
        Map<String, Object> DefaultData = new HashMap<String,Object>();
        DefaultData.put("MercPermission", false);
        DefaultData.put("AssassinPermission", false);
        DefaultData.put("Contracts", new HashMap<String, Map>());


        String strID = pID.toString();
        File userFile = getFile(strID);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(userFile);
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().warning("[AlathraWar] Encountered error when creating FileInputStream for " + strID);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        //System.out.println(data);
        if (data == null) {data = DefaultData; setPlayer(pID, data);}
        return data;
    }

    private void setPlayer(UUID pID, Map<String, Object> data) {
        String strID = pID.toString();
        File userFile = getFile(strID);

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(userFile);
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().warning("[AlathraWar] Encountered error when creating PrintWriter for " + strID);
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        yaml.dump(data, writer);
    }
}