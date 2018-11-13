package net.benjaminurquhart.ksoftsi.commands;

import java.util.List;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.benjaminurquhart.ksoftsi.util.EmbedUtils;
import net.benjaminurquhart.ksoftsi.util.Usage;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.explodingbush.ksoftapi.KSoftAPI;
import net.explodingbush.ksoftapi.entities.Lyric;

public class Lyrics extends Command {

	public Lyrics() {
		super("lyrics");
	}

	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		TextChannel channel = event.getChannel();
		KSoftAPI api = self.getAPI();
		String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(Usage.getUsage(this, "query"));
			return;
		}
		List<Lyric> results;
		try{
			results = api.getLyrics(args[2]).setLimit(1).execute();
		}
		catch(Exception e){
			channel.sendMessage("No results for query `" + args[2] + "`").queue();
			return;
		}
		Lyric lyric = results.get(0);
		String text = lyric.getLyrics();
		if(text.length() > 1024){
			text = text.substring(0, 1020) + "...";
		}
		EmbedBuilder eb = EmbedUtils.getEmbed(event.getGuild(), null, "Track ID: " + lyric.getSongId(), event.getAuthor());
		eb.setDescription(text);
		eb.setTitle("\"" + lyric.getTitle() + "\" by " + lyric.getArtistName());
		channel.sendMessage(eb.build()).queue();
	}

}