package net.benjaminurquhart.ksoftsi.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import org.json.JSONObject;

public class ImgGen extends Command {

	public static final String API = "https://services.is-going-to-rickroll.me/api/%s?text=%s&avatar1=%s&avatar2=%s&username1=%s&username2=%s";
	public static final String[] aliases = {"imggen"};
	private List<String> endpoints;
	
	@SuppressWarnings("unchecked")
	public ImgGen() throws Exception{
		super("imgen", "image name", "text");
		StringBuilder endpoints = new StringBuilder();
		InputStream stream = getStream("https://services.is-going-to-rickroll.me/endpoints.json", null);
		int c = 0;
		while((c = stream.read()) != -1) {
			endpoints.append((char)c);
		}
		stream.close();
		this.endpoints = new ArrayList<>(new JSONObject(endpoints.toString()).getJSONArray("endpoints").toList().stream().map((obj) -> ((java.util.HashMap<String, Object>)obj).get("name").toString()).collect(Collectors.toList()));
		this.endpoints.sort((s1,s2) -> s1.compareTo(s2));
	}
	private URLConnection getConn(String link, String auth) throws IOException{
		URL url = new URL(link);
		URLConnection conn = url.openConnection();
		if(auth != null){
			conn.setRequestProperty("Authorization", auth);
		}
		conn.setRequestProperty("User-Agent", "KSoft.Si Bot");
		return conn;
	}
	private InputStream getStream(String link, String auth) throws IOException{
		return getConn(link, auth).getInputStream();
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().split(" ", 4);
		for(User user : event.getMessage().getMentionedUsers()) {
			for(int i = 0; i < args.length; i++) {
				args[i] = args[i].replace(event.getGuild().getMember(user).getAsMention(), user.getAsTag());
				args[i] = args[i].replace(user.getAsMention(), user.getAsTag());
			}
		}
		try{
			String endpoint = args[2].toLowerCase();
			if(!endpoints.contains(endpoint)){
				channel.sendMessage("Invalid image name. Valid images:\n" + endpoints.stream().reduce("", (out,point) -> out += point + " ")).queue();
				return;
			}
			List<User> users = event.getMessage().getMentionedUsers();
			String text = "";
			if(args.length > 3){
				text = args[3];
			}
			User user1, user2;
			if(users.isEmpty()){
				user1 = event.getAuthor();
				user2 = event.getAuthor();
			}
			else if(users.size() == 1){
				user1 = users.get(0);
				user2 = event.getAuthor();
			}
			else{
				user1 = users.get(0);
				user2 = users.get(1);
			}
			channel.sendTyping().queue();
			text = text.replace(user1.getAsMention(), "").replace(user2.getAsMention(), "");
			URLConnection conn = getConn(String.format(API, endpoint, URLEncoder.encode(text, "UTF-8").replace("%2C", ","), user1.getAvatarUrl(), user2.getAvatarUrl(), URLEncoder.encode(user1.getName(), "UTF-8"), URLEncoder.encode(user2.getName(), "UTF-8")), self.getImgenToken());
			InputStream image = conn.getInputStream();
			String extension = conn.getHeaderField("Content-Type");
			extension = "." + extension.split(";")[0].split("/")[1];
			channel.sendFile(image, endpoint + extension).queue((m) -> {
				try {
					image.close();
				}
				catch (IOException e) {}
			});
		}
		catch(IndexOutOfBoundsException e){
			channel.sendMessage(this.getHelpMenu()).queue();
		}
		catch(IOException e) {
			channel.sendMessage(e.toString()).queue();
		}
	}
	@Override
	public String getDescription(){
		return "generates cool images for you!";
	}
	@Override
	public String[] getAliases() {
		return aliases;
	}
}
