package net.benjaminurquhart.ksoftsi;

import java.io.File;
import java.nio.file.Files;

import org.json.JSONObject;

import net.benjaminurquhart.ksoftsi.util.CommandHandler;
import net.benjaminurquhart.ksoftsi.util.Timestamps;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.explodingbush.ksoftapi.KSoftAPI;

public class KSoftSi {

	private static String token, imgenToken, ksoftToken;
	private static KSoftAPI api;
	public static String prefix;
	
	public KSoftSi() throws Exception{
		String data = new String(Files.readAllBytes(new File("ksoft-config.json").toPath()));
		JSONObject json = new JSONObject(data);
		ksoftToken = json.getString("ksoft");
		token = json.getString("token");
		prefix = json.getString("prefix");
		api = new KSoftAPI(ksoftToken);
		if(json.has("imgen")){
			imgenToken = json.getString("imgen");
		}
		else{
			imgenToken = null;
		}
		Timestamps.enable();
	}
	public KSoftAPI getAPI(){
		return api;
	}
	public String getKSoftToken(){
		return ksoftToken;
	}
	public String getImgenToken(){
		return imgenToken;
	}
	public static void main(String[] args) throws Exception{
		KSoftSi self = new KSoftSi();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> Timestamps.disable()));
		new JDABuilder(token).addEventListener(new CommandHandler(self)).setGame(Game.watching("people get the prefix wrong (it's 'ksoft')")).build();
	}
}
