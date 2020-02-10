package net.benjaminurquhart.ksoftsi.commands;

import org.json.JSONException;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.benjaminurquhart.ksoftsi.util.EmbedUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.explodingbush.ksoftapi.KSoftAPI;
import net.explodingbush.ksoftapi.entities.Reddit;
import net.explodingbush.ksoftapi.enums.ImageType;
import net.explodingbush.ksoftapi.enums.Span;
import net.explodingbush.ksoftapi.exceptions.APIException;
import net.explodingbush.ksoftapi.exceptions.NotFoundException;

public class RandImage extends Command{

	public RandImage(){
		super("randimage", "subreddit");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		TextChannel channel = event.getChannel();
		KSoftAPI api = self.getAPI();
		String[] args = event.getMessage().getContentRaw().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		Reddit image = null;
		try{
			image = api.getRedditImage(ImageType.RANDOM_REDDIT)
					   .setSubreddit(args[2].toLowerCase())
					   .allowNSFW(channel.isNSFW())
					   .setSpan(Span.ALL)
					   .execute();
			if(!channel.isNSFW() && image.isNsfw()) {
				channel.sendMessage("Unable to find a non-nsfw image from that subreddit.").queue();
				return;
			}
			EmbedBuilder eb = EmbedUtils.getEmbed(event.getGuild(), image.getImageUrl(), image.getSubreddit(), event.getAuthor());
			eb.setTitle(image.getAuthor());
			eb.setAuthor(image.getTitle(), image.getSourceUrl());
			channel.sendMessage(eb.build()).queue();
		}
		catch(NotFoundException e){
			channel.sendMessage("Unknown subreddit: " + args[2].toLowerCase()).queue();
		}
		catch(APIException e) {
			if(e.getCause() instanceof NotFoundException) {
				channel.sendMessage("Unknown subreddit: " + args[2].toLowerCase()).queue();
			}
			else if(e.getCause() instanceof JSONException) {
				channel.sendMessage("API returned invalid data, try again later").queue();
			}
			else {
				throw e;
			}
		}
		catch(IllegalArgumentException e) {
			channel.sendMessage("Invalid input.\n" + this.getHelpMenu()).queue();
		}
		catch(JSONException e) {
			channel.sendMessage("API returned invalid data, try again later").queue();
			System.out.println(image);
			e.printStackTrace();
		}
	}
	@Override
	public String getDescription(){
		return "gets a random image from the given subreddit";
	}
}
