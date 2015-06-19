package bot;

import java.io.*;
import java.util.*;

import org.jibble.pircbot.*;

@SuppressWarnings({"rawtypes","unchecked"})
public class TwitchBot extends PircBot{
	//default if cannot connect to CSV File
	String name		= "Samaquabot";
	String url		= "irc.twitch.tv";
	int port		= 6667;
	String oAuth	= "oauth:0vgjetqvkmx33ohu7e8o9kgcd5jhu8";
	String channel	= "#samqua";
	String rewards	= "lootBag.aqua";
	String commands	= "commands.aqua";
	String chatUser	= "Samqua";
	String cmdP		= "?";

	List lootBag	= null;
	Map claimedLoot	= new HashMap();
	
	public TwitchBot(){}
	
	public void run() throws Exception{
		this.setEncoding("utf-8");
		String configFile	= "/config.aqua";
		BufferedReader br	= null;
		String line			= null;
		String lineData[]	= null;
		
		try {
			br = new BufferedReader(new FileReader(configFile));
			while ((line = br.readLine()) != null) {
				lineData = line.split(":");
				
				if (lineData[0].equals("username")){
					name = lineData[1];
				} else if (lineData[0].equals("port")){
					port = Integer.valueOf(lineData[1]);
				} else if (lineData[0].equals("password")){
					oAuth = lineData[1];
				} else if (lineData[0].equals("channel")){
					channel = lineData[1];
				} else if (lineData[0].equals("lootbag")){
					rewards = lineData[1];
				} else if (lineData[0].equals("trigger")){
					cmdP = lineData[1];
				} else if (lineData[0].equals("irc")){
					url = lineData[1];
					if (lineData.length > 2){
						port = Integer.valueOf(lineData[2]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		this.setName(name);
		this.setVerbose(true);
		this.connect(url, port, oAuth);
		this.joinChannel(channel);
		this.openRewards(rewards);
	}
	
	public void openRewards(String filename){
		BufferedReader br	= null;
		String line			= null;
		String lineData[]	= null;
		lootBag				= new ArrayList();
		rewards				= filename;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				lineData = line.split(",");
				lootBag.add(lineData);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addReward(String prize){
		lootBag.add(prize.split(" "));
	}
	
	public void addReward(String[] prize){
		lootBag.add(prize);
	}
	
	public int randNum(){
		return randNum(0, 100);
	}
	
	public int randNum(int max){
		return randNum(0, max);
	}
	
	public int randNum(int min, int max){
		return (int)(Math.random()*(max-min)+min);
	}
	
	public String pickReward(){
		return pickReward(lootBag.size());
	}
	
	public String pickReward(int itemNum){
		if (itemNum > lootBag.size()){
			return "The selected number exceeds the number of rewards in the rewards list.";
		}
		return claimLoot(itemNum);
	}
	
	public String pickReward(String item){
		for (int i = 0; i < lootBag.size(); i++){
			String[] prize = (String[]) lootBag.get(i);
			if (prize[0].equals(item)){
				return claimLoot(i);
			}
		}
		return "The selected reward does not exist in the rewards list.";
	}
	
	public void setChatUser(String chatUser){
		this.chatUser = chatUser;
	}
	
	public String claimLoot(int lootIndex){
		String[] prize = (String[]) lootBag.get(lootIndex);
		claimedLoot.put(chatUser, prize);
		lootBag.remove(lootIndex);
		saveRewards();
		return prize[0] + ": " + prize[1];
	}
	
	public void saveRewards(){
		BufferedWriter bw	= null;
		String line			= null;
		String lineData[]	= null;
		
		try {
			bw = new BufferedWriter(new FileWriter(rewards));
			for (int i = 0; i < lootBag.size(); i++){
				lineData = (String[]) lootBag.get(i);
				line = "";
				for (int j = 0; j < lineData.length; j++){
					line += lineData[i] + ",";
				}
				line.substring(0, line.length()-1);
				line += "\n";
				bw.write(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message){
		if (sender.equals(this.name)){
			return;
		}
		
		if (message.toLowerCase().contains("samaquabot")){
			this.sendMessage(channel, "Hey I heard you say my name! I'm SamaquaBot v.0.1 Alpha. For a list of commands type in '"+cmdP+"help'.");
		} else if (message.startsWith(cmdP) && message.length() > 1){
			message = message.substring(1);
			
			String[] mArray = message.split(" ");
			
			String command = mArray[0].toLowerCase();
			List params = new ArrayList();
			
			if (mArray.length > 1){
				for (int i = 1; i < mArray.length; i++){
					params.add(mArray[i]);
				}
			}
			
			if (command.equals("njh")){
				this.sendMessage(channel, "AQUA <3 NJH");
			} else if (command.equals("help")){
				this.sendMessage(channel, "This is the help menu v.0.1, the only current command is ?njh. Give it a try!");
			} else if (command.equals("steve")){
				this.sendMessage(channel, "Ah, god damn it.");
			} else if (command.equals("kill")){
				if (mArray.length > 1){
					this.sendMessage(channel, "/me stabs " + mArray[1] + " with a rusty spoon.");
				} else {
					this.sendMessage(channel, "/me pushes " + sender + " off a rusty spoon.");
				}
			}
		}
	}
}
