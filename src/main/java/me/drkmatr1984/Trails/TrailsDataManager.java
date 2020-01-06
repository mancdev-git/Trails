package me.drkmatr1984.Trails;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.drkmatr1984.Trails.objects.TrailBlock;
import me.drkmatr1984.Trails.objects.WrappedLocation;;

public class TrailsDataManager
{
	
	private BlockDataManager blockData;
	private PlayerDataManager playerData;
	
	public TrailsDataManager(Trails plugin){
		this.setBlockData(new BlockDataManager(plugin));
		this.setPlayerData(new PlayerDataManager(plugin));
	}
	
	public BlockDataManager getBlockData() {
		return blockData;
	}

	private void setBlockData(BlockDataManager blockData) {
		this.blockData = blockData;
	}
	
	public PlayerDataManager getPlayerData() {
		return playerData;
	}

	public void setPlayerData(PlayerDataManager playerData) {
		this.playerData = playerData;
	}

	public class BlockDataManager{
		
		private File dataFile;
		private File dataFolder;
		private FileConfiguration data;
		private Trails plugin;
		public List<TrailBlock> walkedOver;
		
		public BlockDataManager(Trails plugin){		
			this.plugin = plugin;
			dataFolder = new File(this.plugin.getDataFolder().toString()+"/data");
			initLists();
		}
			
		private void initLists(){
			saveDefaultBlockList();
			loadBlockList();
		}
		
	    ////////////////////////////////////////////////////////////
		public void saveDefaultBlockList() {
			//pickup toggle data
			if(!(dataFolder.exists())){
				dataFolder.mkdir();
			}
		    if (dataFile == null) {
		        dataFile = new File(dataFolder, "blocks.yml");
		    }
		    if (!dataFile.exists()) {           
		        plugin.saveResource("data/blocks.yml", false);
		    }
	    }
		  
		public void loadBlockList(){
			//pickup toggle data
			walkedOver = new ArrayList<TrailBlock>();
			data = YamlConfiguration.loadConfiguration(dataFile);
			for(String key : data.getKeys(false)){
				ConfigurationSection section = data.getConfigurationSection(key);
				if(section.getString("location")!=null && section.getString("location")!=""){
					if(section.getInt("walks")!=0){
						if(section.getString("location")!=null && section.getString("location")!=""){
							try {
								walkedOver.add(new TrailBlock(WrappedLocation.fromBase64(section.getString("location")), section.getInt("walks"), section.getString("trail")));
							} catch (IOException e) {
								Bukkit.getLogger().log(Level.SEVERE, "Trails is unable to decode the saved block data.");
							}
						}
					}
					
				}
			}
		}
		  
		public void saveBlockList(){
			//pickup toggle data
			if(walkedOver!=null && !walkedOver.isEmpty())
			{
				int i = 0;
				for(TrailBlock b : walkedOver){
					data.set(i + ".location", b.getWrappedLocation().toBase64());
					data.set(i + ".walks", b.getWalks());
					data.set(i + ".trail", b.getTrailName());
					i++;
				}
			}
			if(dataFile.exists())
				dataFile.delete();
			try {
				data.save(dataFile);
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Trails is unable to save block data");
				Bukkit.getLogger().log(Level.SEVERE, "Are you sure you have write-access?");
			}
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Trails is unable to create block data file");
				Bukkit.getLogger().log(Level.SEVERE, "Are you sure you have write-access?");
			}	
		}
		
		public List<TrailBlock> getTrailBlocks(){
			return walkedOver;
		}
		
		public void addTrailBlock(TrailBlock b) {
			walkedOver.add(b);
		}
		
		public void removeTrailBlock(TrailBlock b) {
			walkedOver.remove(b);
		}
	}
	
	public class PlayerDataManager
	{
		private File dataFile;
		private File dataFolder;
		private FileConfiguration data;
		private Trails plugin;
		private Map<UUID, Boolean> players = new HashMap<UUID, Boolean>();
		
		public PlayerDataManager(Trails plugin){		
			this.plugin = plugin;
			dataFolder = new File(this.plugin.getDataFolder().toString()+"/data");
			initLists();
		}
			
		private void initLists(){
			saveDefaultPlayerList();
			loadPlayerList();
		}
		
	    ////////////////////////////////////////////////////////////
		public void saveDefaultPlayerList() {
			if(!(dataFolder.exists())){
				dataFolder.mkdir();
			}
		    if (dataFile == null) {
		        dataFile = new File(dataFolder, "players.yml");
		    }
		    if (!dataFile.exists()) {           
		        plugin.saveResource("data/players.yml", false);
		    }
	    }
		  
		public void loadPlayerList(){
			data = YamlConfiguration.loadConfiguration(dataFile);
			for(String key : data.getKeys(false)){
				players.put(UUID.fromString(key), data.getConfigurationSection(key).getBoolean("toggled"));
			}
		}
		  
		public void savePlayerList(){
			//toggle data
			if(players!=null && !players.isEmpty())
			{
				for(UUID key : players.keySet()){
					data.set(key + ".toggled", players.get(key));
				}
			}
			if(dataFile.exists())
				dataFile.delete();
			try {
				data.save(dataFile);
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Trails is unable to save player data");
				Bukkit.getLogger().log(Level.SEVERE, "Are you sure you have write-access?");
			}
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Trails is unable to create player data file");
				Bukkit.getLogger().log(Level.SEVERE, "Are you sure you have write-access?");
			}	
		}
		
		public boolean isToggled(Player p) {
			if(players.keySet().contains(p.getUniqueId())) {
				return players.get(p.getUniqueId());
			}else {
				players.put(p.getUniqueId(), Boolean.TRUE);
				return Boolean.TRUE;
			}
		}
	}
}