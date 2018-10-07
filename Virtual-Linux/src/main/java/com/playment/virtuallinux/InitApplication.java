package com.playment.virtuallinux;

import java.util.Scanner;

import org.apache.log4j.Logger;

import com.playment.virtuallinux.core.CommandImplementer;

/**
 * InitApplication initiates VirtualApplication program.
 * 
 * @author Vignesh Baskaran
 *
 */
public class InitApplication {

	/** The headerLogger - header logger with header format */
	private static final Logger headerLogger = Logger.getLogger("header");

	/** The outputmsgLogger - msg logger with msg format */
	private static final Logger outputmsgLogger = Logger.getLogger("outputmsg");

	/** The EMPTY string */
	private static final String EMPTY = "";

	/**
	 * main function from where application starts
	 * 
	 * @param args
	 *            start arguments
	 */
	public static void main(String[] args) {
		displayWelcomeMessage();

		Scanner scanner = new Scanner(System.in);
		CommandImplementer commandImplementer = new CommandImplementer();
		while (true) {
			headerLogger.info(commandImplementer.executePwd(commandImplementer.getCurrentDirectory()));
			String command = scanner.nextLine();

			if (command.equals("exit")) {
				break;
			}

			if (!EMPTY.equals(command.trim())) {
				commandImplementer.setCommand(command.trim());
				commandImplementer.implementCommand();
			}
		}
		scanner.close();

	}

	/**
	 * displayWelcomeMessage displays welcome message in console
	 */
	private static void displayWelcomeMessage() {
		outputmsgLogger.info("*********  Welcome to the virtual linux application  ********* \n");
		outputmsgLogger.info("Below commands are supported till now: \n");
		outputmsgLogger.info("1. mkdir");
		outputmsgLogger.info("2. ls");
		outputmsgLogger.info("3. pwd");
		outputmsgLogger.info("4. rm");
		outputmsgLogger.info("5. cd");
		outputmsgLogger.info("6. session clear");
		outputmsgLogger.info("7. exit");

	}
}
