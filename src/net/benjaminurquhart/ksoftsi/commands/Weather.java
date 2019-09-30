package net.benjaminurquhart.ksoftsi.commands;

import java.util.List;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.benjaminurquhart.ksoftsi.util.EmbedUtils;
import net.benjaminurquhart.ksoftsi.util.Usage;
import net.benjaminurquhart.ksoftsi.util.WeatherUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.explodingbush.ksoftapi.KSoftAPI;
import net.explodingbush.ksoftapi.entities.Alert;
import net.explodingbush.ksoftapi.enums.ReportType;
import net.explodingbush.ksoftapi.enums.Units;
import net.explodingbush.ksoftapi.exceptions.NotFoundException;

public class Weather extends Command{
	
	@Override
	public void handle(GuildMessageReceivedEvent event, KSoftSi self) {
		TextChannel channel = event.getChannel();
		KSoftAPI api = self.getAPI();
		String[] args = event.getMessage().getContentRaw().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(Usage.getUsage(this, "location")).queue();
			return;
		}
		try{
			channel.sendTyping().queue();
			net.explodingbush.ksoftapi.entities.Weather data = api.getKumo().getWeatherAction().setLocationQuery(args[2]).setReportType(ReportType.CURRENTLY).setUnits(Units.AUTO).execute();
			//System.out.println(data.getUnits());
			EmbedBuilder eb = EmbedUtils.getEmbed(event.getGuild(), data.getIconUrl(), data.getLocation().getAddress(), event.getAuthor());
			eb.setTimestamp(data.getTime().minusHours(5));
			eb.setTitle(data.getSummary());
			eb.setDescription(WeatherUtil.format(data));
			List<Alert> alerts = data.getAlerts();
			if(!alerts.isEmpty()){
				for(Alert alert : alerts){
					eb.addField(String.format("%s (Severity: %s)", alert.getTitle(), alert.getSeverity()), (alert.getDescription().length() > 1024 ? alert.getDescription().substring(0, 1020) + "..." : alert.getDescription()), true);
				}
			}
			channel.sendMessage(eb.build()).queue();
		}
		catch(NotFoundException e){
			channel.sendMessage(e.getMessage() + "\nTry checking your spelling.").queue();
		}
		catch(Exception e){
			channel.sendMessage(e.toString()).queue();
		}
	}
	@Override
	public String getDescription(){
		return "Gets the current weather for the given location";
	}
	
}
