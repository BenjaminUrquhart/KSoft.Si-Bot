package net.benjaminurquhart.ksoftsi.util;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import net.benjaminurquhart.ksoftsi.KSoftSi;
import net.benjaminurquhart.ksoftsi.commands.Command;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CommandHandler extends ListenerAdapter{

	private KSoftSi self;
	private HashMap<String, Command> commands;
	
	private ExecutorService executor;
	
	public CommandHandler(KSoftSi self){
		this.self = self;
		this.commands = new HashMap<>();
		Reflections reflections = new Reflections("net.benjaminurquhart.ksoftsi.commands");
		reflections.getSubTypesOf(Command.class).forEach((cls) -> {
			try{
				this.registerCommand(cls.getDeclaredConstructor().newInstance());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		});
		executor = Executors.newFixedThreadPool(10);
	}
	public void registerCommand(Command command){
		commands.put(command.getName(), command);
		for(String alias : command.getAliases()) {
			commands.put(alias, command);
		}
		System.out.println("Registered " + (command.hide() ? "private " : "") + "command " + command.getName());
	}
	public List<Command> getRegisteredCommands(){
		return commands.values().stream().distinct().collect(Collectors.toList());
	}
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event){
		if(!event.getChannel().canTalk()){
			return;
		}
		String msg = event.getMessage().getContentRaw().toLowerCase();
		if(event.getAuthor().isBot() || !msg.startsWith(KSoftSi.prefix)){
			return;
		}
		String cmd = msg.substring(KSoftSi.prefix.length()).trim();
		if(cmd.contains(" ")){
			cmd = cmd.split(" ")[0];
		}
		Command command = commands.get(cmd);
		if(command != null){
			executor.execute(() -> {
				try{
					command.handle(event, self);
				}
				catch(Exception e){
					e.printStackTrace();
					event.getChannel().sendMessage("Oh no! Something went wrong while executing that command!\nThis incident has been reported.\n" + e).queue();
					User owner = event.getJDA().getUserById("273216249021071360");
					if(!(owner == null)){
						owner.openPrivateChannel().queue(
						(channel) ->{
							String out = "```" + e.toString();
							for(StackTraceElement trace : e.getStackTrace()){
								out += "\n\tat " + trace.toString();
							}
							if(out.length() > 1990){
								out = out.substring(0, 1990) + "...";
							}
							channel.sendMessage(out + "```").queue((m) -> {},
							(error) ->{
								event.getChannel().sendMessage("Failed to report the incident!\n" + error).queue();
							});
							channel.sendMessage("Command: `" + event.getMessage().getContentRaw() + "`\nGuild: `" + event.getGuild() + "`\nUser: `" + event.getAuthor() + "`").queue((m) -> {}, (error) -> {});
						});
					}
				}
			});
		}
	}
}
