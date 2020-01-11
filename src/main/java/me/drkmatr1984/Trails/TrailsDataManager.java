package me.drkmatr1984.Trails;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.util.internal.ConcurrentSet;
import me.drkmatr1984.Trails.TrailsConfigManager.LanguageConfig;
import me.drkmatr1984.Trails.objects.Link;
import me.drkmatr1984.Trails.objects.Trail;
import me.drkmatr1984.Trails.objects.TrailBlock;
import me.drkmatr1984.Trails.objects.WrappedLocation;;

public class TrailsDataManager
{
	
	private BlockDataManager blockData;
	private PlayerDataManager playerData;
	private Trails plugin;
	private final File dataFolder;
	private LanguageConfig langConfig;
	
	public TrailsDataManager(Trails plugin){
		this.plugin = plugin;
		this.dataFolder = new File(this.plugin.getDataFolder().toString()+"/data");
		this.langConfig = this.plugin.getConfigManager().getLanguageConfig();
		this.setBlockData(new BlockDataManager());
		this.setPlayerData(new PlayerDataManager());
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
		private FileConfiguration data;
		public ConcurrentSet<TrailBlock> walkedOver;
		
		
		public BlockDataManager(){
			initLists();
			if(plugin.getConfigManager().getConfig().isSaveData()) {
		    	new BukkitRunnable() {
					@Override
					public void run() {
						saveBlockList();
						//send save message to players with reload perm
						for(Player p : Bukkit.getServer().getOnlinePlayers()) {
							if(p.hasPermission("trails.savemessage")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', langConfig.prefix + langConfig.saveData));
							}
						}
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', langConfig.prefix + langConfig.saveData));
					}
		    	}.runTaskTimerAsynchronously(plugin, plugin.getConfigManager().getConfig().getSaveInterval() * 60 * 20, plugin.getConfigManager().getConfig().getSaveInterval() * 60 * 20);
		    }
		}
			
		private void initLists(){
			saveDefaultBlockList();
			loadBlockList();
		}
		
	    ////////////////////////////////////////////////////////////
		public void saveDefaultBlockList() {
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
			walkedOver = new ConcurrentSet<TrailBlock>();
			data = YamlConfiguration.loadConfiguration(dataFile);
			for(String key : data.getKeys(false)){
				ConfigurationSection section = data.getConfigurationSection(key);
				if(section.getString("location")!=null && section.getString("location")!=""){
					if(section.getInt("walks")!=0){
						if(section.getString("location")!=null && section.getString("location")!=""){
							try {
								walkedOver.add(new TrailBlock(WrappedLocation.fromBase64(section.getString("location")), section.getInt("walks"), Link.fromBase64(section.getString("link"))));
							} catch (IOException e) {
								Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.cantDecodeBlockData));
							}
						}
					}
					
				}
			}
		}
		  
		public void saveBlockList(){
			if(walkedOver!=null && !walkedOver.isEmpty())
			{
				int i = 0;
				for(TrailBlock b : walkedOver){
					data.set(i + ".location", b.getWrappedLocation().toBase64());
					data.set(i + ".walks", b.getWalks());
					data.set(i + ".link", b.getLink().toBase64());
					i++;
				}
			}
			if(dataFile.exists())
				dataFile.delete();
			try {
				data.save(dataFile);
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.cantSaveBlockData));
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.errorWriteAccess));
			}
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.cantCreateBlockData));
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.errorWriteAccess));
			}	
		}
		
		public ConcurrentSet<TrailBlock> getTrailBlocks(){
			return walkedOver;
		}
		
		public boolean isTrailBlock(Block b) {
			for(TrailBlock trail : walkedOver) {
				if(trail.getWrappedLocation().isLocation(b.getLocation())) {
					return true;
				}
			}
			return false;
		}
		
		public boolean isTrailBlock(Location loc) {
			for(TrailBlock trail : walkedOver) {
				if(trail.getWrappedLocation().isLocation(loc)) {
					return true;
				}
			}
			return false;
		}
		
		public TrailBlock getTrailBlock(Location loc) {
			for(TrailBlock trail : walkedOver) {
				if(trail.getWrappedLocation().isLocation(loc)) {
					return trail;
				}
			}
			return null;
		}
		
		public TrailBlock getTrailBlock(Block b) {
			for(TrailBlock trail : walkedOver) {
				if(trail.getWrappedLocation().isLocation(b.getLocation())) {
					return trail;
				}
			}
			return null;
		}
		
		public boolean containsTrailBlock(TrailBlock b) {
			if(walkedOver.contains(b)) {
				return true;
			}
			for(TrailBlock trail : walkedOver) {
				if(trail.getWrappedLocation().isLocation(b.getLocation()) && trail.getWalks() == b.getWalks() &&
					trail.getTrailName().equalsIgnoreCase(b.getTrailName())	) {
					return true;
				}
			}
			return false;
		}
		
		public void removeTrailBlock(TrailBlock b) {
			for(TrailBlock trail : walkedOver) {
				if(trail.getWrappedLocation().isLocation(b.getLocation()) && 
						trail.getTrailName().equalsIgnoreCase(b.getTrailName())	) {
					walkedOver.remove(trail);
				}
			}
		}
		
		public void addTrailBlock(TrailBlock b) {
			removeTrailBlock(b);
			walkedOver.add(b);
		}
		
	}
	
	public class PlayerDataManager
	{
		private File dataFile;
		private FileConfiguration data;
		private ConcurrentMap<UUID, Boolean> players = new ConcurrentHashMap<UUID, Boolean>();
		
		public PlayerDataManager(){
			initLists();
		}
			
		private void initLists(){
			saveDefaultPlayerList();
			loadPlayerList();
			if(plugin.getConfigManager().getConfig().isSaveData()) {
		    	new BukkitRunnable() {
					@Override
					public void run() {
						savePlayerList();
					}
		    	}.runTaskTimerAsynchronously(plugin, plugin.getConfigManager().getConfig().getSaveInterval() * 60 * 20, plugin.getConfigManager().getConfig().getSaveInterval() * 60 * 20);
		    }
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
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.cantSavePlayerData));
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.errorWriteAccess));
			}
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.cantCreatePlayerData));
				Bukkit.getLogger().log(Level.SEVERE, ChatColor.stripColor(langConfig.prefix + langConfig.errorWriteAccess));
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
		
		public boolean isToggled(UUID uuid) {
			if(players.keySet().contains(uuid)) {
				return players.get(uuid);
			}else {
				players.put(uuid, Boolean.TRUE);
				return Boolean.TRUE;
			}
		}
		
		public void setToggled(Player p, boolean toggled) {
			players.put(p.getUniqueId(), toggled);			
		}
		
		public void setToggled(UUID p, boolean toggled) {
			players.put(p, toggled);			
		}
	}
	
}