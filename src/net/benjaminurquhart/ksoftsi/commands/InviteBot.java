package net.benjaminurquhart.ksoftsi.commands;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class InviteBot extends Command {
	
	public InviteBot() {
		super("inv","id");
	}

	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		if(!event.getGuild().getId().equals("458341246453415947")) return;
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().toLowerCase().split(" ");
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		if(!args[2].matches("\\d+")) {
			channel.sendMessage("Invalid snowflake");
			return;
		}
		channel.sendMessage("https://discordapp.com/oauth2/authorize?client_id="+args[2]+"&permissions=0&scope=bot&guild_id=404731402429530132").queue();
		
	}
	@Override
	public boolean hide() {return true;}
}
