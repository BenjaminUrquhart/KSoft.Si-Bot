package net.benjaminurquhart.ksoftsi.commands;

import java.util.List;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.benjaminurquhart.ksoftsi.util.EmbedUtils;
import net.benjaminurquhart.ksoftsi.util.Usage;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.explodingbush.ksoftapi.KSoftAPI;
import net.explodingbush.ksoftapi.entities.lyrics.Track;
import net.explodingbush.ksoftapi.exceptions.NotFoundException;

public class Lyrics extends Command {

	public Lyrics() {
		super("lyrics", "query");
	}

	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		TextChannel channel = event.getChannel();
		KSoftAPI api = self.getAPI();
		String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(Usage.getUsage(this, "query")).queue();
			return;
		}
		List<Track> results;
		try{
			results = api.getLyrics().search(args[2]).setLimit(1).execute();
			if(results.isEmpty()) {
				throw new NotFoundException("");
			}
		}
		catch(NotFoundException e){
			channel.sendMessage("No results for query `" + args[2].replace("`", "") + "`").queue();
			return;
		}
		catch(NullPointerException e) {
			channel.sendMessage("Failed to retrieve metadata for the given track. Blame NANI.").queue();
			e.printStackTrace();
			return;
		}
		Track lyric = results.get(0);
		String text = lyric.getLyrics();
		if(text.length() > 2000){
			text = text.substring(0, 1994) + "...";
		}
		EmbedBuilder eb = EmbedUtils.getEmbed(event.getGuild(), null, "Track ID: " + lyric.getId(), event.getAuthor());
		eb.setDescription(text);
		eb.setThumbnail(lyric.getAlbumArt());
		eb.setTitle("\"" + lyric.getName() + "\" by " + lyric.getArtist().getName());
		channel.sendMessage(eb.build()).queue();
	}

	@Override
	public String getDescription() {
		return "gets lyrics to the given song";
	}

}
