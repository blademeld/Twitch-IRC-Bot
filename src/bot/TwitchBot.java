package bot;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

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
	String prompt	= "?";

	List lootBag	= null;
	Map functions	= new HashMap();
	Map claimedLoot	= new HashMap();

	public static void main(String[] args) throws Exception{
		TwitchBot bot = new TwitchBot();
		bot.run();
	}
	
	public TwitchBot(){}
	
	public void run() throws Exception{
		this.setEncoding("utf-8");
		this.setMessageDelay(1000);
		String configFile	= "config.aqua";
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
				} else if (lineData[0].equals("prompt")){
					prompt = lineData[1];
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
		
		configFile	= commands;
		br			= null;
		line		= null;
		lineData	= null;
		
		try {
			functions = new HashMap();
			br = new BufferedReader(new FileReader(configFile));
			while ((line = br.readLine()) != null) {
				String regex = "(?<!\\\\)" + Pattern.quote("|");
				lineData = line.split(regex);
				
				functions.put(lineData[0], lineData[1]);
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
		return randNum(1, 100);
	}
	
	public int randNum(int max){
		return randNum(1, max);
	}
	
	public int randNum(int min, int max){
		return (int)(Math.random()*(max-min+1)+min);
	}
	
	public int randNum(String max){
		int val = -1;
		try {
			if (max.equals("")){
				return randNum();
			} else if (max.contains(",")){
				String[] minMax = max.replaceAll(" ", "").split(",");
				
				return randNum(Integer.parseInt(minMax[0]), Integer.parseInt(minMax[1]));
			} else {
				return randNum(Integer.parseInt(max));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return val;
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
		
		if (sender.equals("Edich9118") && message.toLowerCase().contains("kawai")){
			this.sendMessage(channel, "SO KAWAIIIII!!!!!!!~~~");
		}
		
		if (message.toLowerCase().contains("samaquabot")){
			this.sendMessage(channel, "Hey I heard you say my name! I'm SamaquaBot v.0.1 Alpha. For a list of commands type in '"+prompt+"help'.");
		} else if (message.startsWith(prompt) && message.length() > 1){
			message = message.substring(1);
			
			String[] mArray = message.split(" ");
			
			String command = mArray[0].toLowerCase();
			List params = new ArrayList();
			
			String target = sender;
			
			if (mArray.length > 1){
				for (int i = 1; i < mArray.length; i++){
					params.add(mArray[i]);
				}
				target = mArray[1];
			}
			
			if (command.equals("help")){
				this.sendMessage(channel, "This is the help menu v.0.1.1, try ?commandlist for more options.");
			} else if (command.equals("commandlist")){
				try {
					//this.sendRawLine("NOTICE "+sender+" :The current commands list is as follows:");
					//this.sendMessage(channel, "The current commands list is as follows:");
//					this.wait(100);
					//this.sendMessage("NOTICE " + sender, "The current commands list is as follows:");
	
					String messageOut = "The current commands list is as follows:				";
					
				    Iterator it = functions.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry pair = (Map.Entry)it.next();
				        //this.sendMessage(channel, "?" + pair.getKey() + " " + pair.getValue());
						messageOut += "?" + pair.getKey() + " " + pair.getValue() + "					";
				        //System.out.println(pair.getKey() + ": " + pair.getValue());
				        //this.sendRawLine("/msg "+sender+" ?" + pair.getKey() + "  " + pair.getValue());
//				        this.wait(100);
				        it.remove();
					}
					
					this.sendMessage(channel, messageOut);
				} catch(Exception e){
					this.sendMessage(channel, "Error while parsing commandlist.");
				}
			} else {
				try {
					String messageOut = functions.get(command).toString();

					messageOut = messageOut.replaceAll("@sender", sender);
					messageOut = messageOut.replaceAll("@target", target);
										
					if (messageOut.contains("!random")){
						String[] messageList = messageOut.split("!random");
						
						messageOut = messageList[0];
						for (int i = 1; i < messageList.length; i++){
							String randVal = messageList[i].substring(1,messageList[i].indexOf(")"));
							messageOut += this.randNum(randVal) + messageList[i].substring(messageList[i].indexOf(")")+1, messageList[i].length());
						}
					}
					
					this.sendMessage(channel, messageOut);
				} catch(Exception e){
					e.printStackTrace();
					this.sendMessage(channel, "That's not a vaild command. For a list of commands try ?commandlist and I'll PM you the entire thing!");
				}
			}
		}
	}
}
