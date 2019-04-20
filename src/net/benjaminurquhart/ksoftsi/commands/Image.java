package net.benjaminurquhart.ksoftsi.commands;

import java.util.List;
import java.util.stream.Collectors;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.benjaminurquhart.ksoftsi.util.EmbedUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.explodingbush.ksoftapi.KSoftAPI;
import net.explodingbush.ksoftapi.entities.TaggedImage;
import net.explodingbush.ksoftapi.image.ImageTag;

public class Image extends Command{
	
	public Image(){
		super("image", "type");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		TextChannel channel = event.getChannel();
		KSoftAPI api = self.getAPI();
		String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ");
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		try{
			ImageTag tag = ImageTag.valueOf(args[2].toUpperCase());
			if(!channel.isNSFW() && tag.isNSFW()){
				channel.sendMessage("That tag cannot be used outside of an NSFW channel!").queue();
				return;
			}
			TaggedImage image = api.getTaggedImage(tag).allowNsfw(channel.isNSFW()).execute();
			EmbedBuilder eb = EmbedUtils.getEmbed(event.getGuild(), image.getUrl(), tag.toString().toLowerCase(), event.getAuthor());
			eb.setTitle("Here's your random " + tag.toString().toLowerCase());
			channel.sendMessage(eb.build()).queue();
		}
		catch(IllegalArgumentException e){
			List<ImageTag> tags = ImageTag.getTags();
			channel.sendMessage("Unknown tag `" + args[2] + "`.\nValid tags: " + tags.stream().map(tag -> tag.toString()).collect(Collectors.joining(", "))).queue();
		}
		catch(Exception e){
			channel.sendMessage(e.toString()).queue();
		}
	}
	@Override
	public String getDescription() {
		return "gets a random image";
	}

}
