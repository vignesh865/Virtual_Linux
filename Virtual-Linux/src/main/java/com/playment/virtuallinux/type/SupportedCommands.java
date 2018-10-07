package com.playment.virtuallinux.type;

import java.util.Arrays;

/**
 * SupportedCommands will have all the commands supported by the VirtualLinux
 * application
 * 
 * @author Vignesh Baskaran
 *
 */
public enum SupportedCommands {
	PWD("pwd"), LS("ls"), MKDIR("mkdir"), CD("cd"), RM("rm"), SESSION("session");

	private String command;

	SupportedCommands(String command) {
		this.command = command;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * isSupported checks whether given command is supported by application or not
	 * 
	 * @param command
	 *            command given by user
	 * @return true if given command is supported by the VirtualLinux application
	 */
	public static boolean isSupported(String command) {
		return Arrays.asList(SupportedCommands.values()).stream()
				.anyMatch(supportedCommand -> supportedCommand.getCommand().equals(command));
	}

	/**
	 * SupportedCommands returns corresponding enum for given string
	 * 
	 * @param userCommand
	 *            command given by user
	 * @return SupportedCommands get string as enum
	 */
	public static SupportedCommands getSupportedCommand(String userCommand) {
		for (SupportedCommands command : SupportedCommands.values()) {
			if (command.getCommand().equals(userCommand)) {
				return command;
			}
		}
		return null;
	}
}
