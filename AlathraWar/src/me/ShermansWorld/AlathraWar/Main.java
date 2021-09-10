package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	public static WarData data;
	public static SiegeData data2;
	public static Main instance = null;
	
	@SuppressWarnings("unchecked")
	private static void initData() {
		try {
			Set<String> warsSet = data.getConfig().getConfigurationSection("Wars").getKeys(false);
			Iterator<String> it = warsSet.iterator();
			ArrayList<String> warsTempList = new ArrayList<String>();
			// converts set to arraylist
			while (it.hasNext()) {
				warsTempList.add(it.next());
			}
			for (int i = 0; i < warsTempList.size(); i++) {
				War war = new War(
						warsTempList.get(i),
						data.getConfig().getString("Wars." + warsTempList.get(i) + ".side1"),
						data.getConfig().getString("Wars." + warsTempList.get(i) + ".side2")
						);
				
				ArrayList<String> side1Players = new ArrayList<String>();
				ArrayList<String> side2Players = new ArrayList<String>();
				side1Players = (ArrayList<String>) data.getConfig().getList("Wars." + war.getName() + ".side1players");
				side2Players = (ArrayList<String>) data.getConfig().getList("Wars." + war.getName() + ".side2players");
				war.setSide1Players(side1Players);
				war.setSide2Players(side2Players);
				WarCommands.wars.add(war);
				
			}
		} catch (NullPointerException e) {
			return;
		}
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	
	@Override
	public void onEnable() { //What runs when you start server
		instance = this;
		data = new WarData(this); // Initialize war data file
		
		//initialize commands
		new WarCommands(this);
		new SiegeCommands(this);
		getServer().getPluginManager().registerEvents(new KillsListener(), this);
		
		initData();
	}
	
	@Override
	public void onDisable() {
		
	}
	
}
