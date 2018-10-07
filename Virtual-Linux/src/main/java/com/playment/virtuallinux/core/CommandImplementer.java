/**
 * 
 */
package com.playment.virtuallinux.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.playment.virtuallinux.type.Directory;
import com.playment.virtuallinux.type.SupportedCommands;

/**
 * CommandImplementer will handle logic for all the supported commands
 * 
 * @author Vignesh Baskaran
 *
 */
public class CommandImplementer {
	/** The outputmsgLogger - Logger for logging outputs */
	private static final Logger outputmsgLogger = Logger.getLogger("outputmsg");

	/** EMPTY - to check for empty */
	private static final String EMPTY = "";

	/** INVALID_ARGUMENTS - literal */
	private static final String INVALID_ARGUMENTS = "ERR: INVALID ARGUMENTS";

	/** INVALID_PATH - literal */
	private static final String INVALID_PATH = "ERR: INVALID PATH";

	/** command - command given by user */
	private String command;

	/** currentDirectory - current directory in which user is on */
	private Directory<String> currentDirectory;

	public CommandImplementer() {
		currentDirectory = new Directory<>("/");
	}

	public CommandImplementer(String command) {
		this.command = command;
		currentDirectory = new Directory<>("/");
	}

	/**
	 * @return the currentDirectory user is in
	 */
	public Directory<String> getCurrentDirectory() {
		return currentDirectory;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * implementCommand is a method used to implement all commands given by the
	 * user. It checks for valid command and implements it using utility methods
	 * 
	 */
	public void implementCommand() {
		List<String> commandPieces = new ArrayList<>();
		commandPieces.addAll(Arrays.asList(command.split("\\s+")));
		String commandKeyword = commandPieces.get(0).toLowerCase();

		commandPieces.remove(0);
		if (SupportedCommands.isSupported(commandKeyword)) {
			commandSelector(commandKeyword, commandPieces);
		} else {
			outputmsgLogger.error("ERR: CANNOT RECOGNIZE INPUT");
		}
	}

	/**
	 * commandSelector is a method which directs user command to appropriate
	 * function to handle corresponding logic.
	 * 
	 * @param commandKeyword
	 *            user command(typically first word of their command)
	 * @param commandArgs
	 *            words typed after the keyword command.
	 * 
	 */
	private void commandSelector(String commandKeyword, List<String> commandArgs) {

		switch (SupportedCommands.getSupportedCommand(commandKeyword)) {
		case PWD:
			outputmsgLogger.info("PATH: " + executePwd(currentDirectory));
			break;
		case LS:
			executeLs();
			break;
		case MKDIR:
			if (commandArgs.isEmpty()) {
				outputmsgLogger.error(INVALID_ARGUMENTS);
			} else {
				decideMkdirCommandMode(commandArgs);
			}
			break;
		case CD:
			if (commandArgs.isEmpty()) {
				outputmsgLogger.error(INVALID_ARGUMENTS);
			} else {
				decideCdCommandMode(commandArgs.get(0));
			}
			break;
		case RM:
			if (commandArgs.isEmpty()) {
				outputmsgLogger.error(INVALID_ARGUMENTS);
			} else {
				decideRmCommandMode(commandArgs);
			}

			break;
		case SESSION:
			if (commandArgs.isEmpty()) {
				outputmsgLogger.error(INVALID_ARGUMENTS);
			} else {
				executeSessionClear(commandArgs.get(0));
			}
			break;
		}

	}

	/**
	 * executePwd is a method which executes 'pwd' command logic
	 * 
	 * @param directory
	 *            refers to currentDirectory in which user is now in
	 * @return whole path of given directory
	 * 
	 */
	public String executePwd(Directory<String> directory) {
		if (Objects.isNull(directory.getParent())) {
			return directory.getData();
		}

		return executePwd(directory.getParent()) + "/" + directory.getData();
	}

	/**
	 * executeLs is a method which executes 'ls' command logic. prints list of
	 * directories in the current path.
	 * 
	 */
	private void executeLs() {
		String dirNames = currentDirectory.getChildren().stream().map(Directory::getData)
				.collect(Collectors.joining(" "));
		if (EMPTY.equals(dirNames.trim())) {
			outputmsgLogger.error("DIRS: NO DIRECTORY EXIST");
		} else {
			outputmsgLogger.info("DIRS: " + dirNames);
		}
	}

	/**
	 * decideMkdirCommandMode decides whether given command is executed from root or
	 * current directory
	 * 
	 * @param commands
	 *            list of command args
	 */
	private void decideMkdirCommandMode(List<String> commands) {
		for (String mkcommand : commands) {
			if (isExecuteOnRoot(mkcommand)) {
				executeMkdir(mkcommand, true);
			} else if (isDeepCommandArgs(mkcommand)) {
				executeMkdir(mkcommand, false);
			} else {
				executeMkdirCurrentSingle(mkcommand);
			}
		}
	}

	/**
	 * executeMkdir is a method which executes 'mkdir' command logic. It handles
	 * creation of multiple folders in hierarchy at a time
	 * 
	 * It handles
	 * 
	 * 1. mkdir from root
	 * 
	 * (Example: 'mkdir /<folder-name>/<folder-name>' which creates folder from root
	 * directory)
	 * 
	 * 2. mkdir from current directory
	 * 
	 * (Example: 'mkdir <folder-name>/<folder-name>' which creates folders from
	 * current directory)
	 * 
	 * It is fail safe. It supports 'Deep creation'. Deep creation means creating
	 * multiple folder in hierarchy.
	 * 
	 * Example: 'mkdir /playment/hiring' - creates folder 'playment' in root and
	 * creates 'hiring' folder inside the playment folder.
	 * 
	 * If 'playment' folder already exists but user inputs above command, then it
	 * creates hiring folder alone inside the playment folder. That's how it is fail
	 * safe.
	 * 
	 * It prints full path from root of every folder it creates, after successful
	 * creation. If not created but already existed, then it prints as already
	 * existed
	 * 
	 * @param command
	 *            command command arguments to execute
	 * @param isExecuteOnRoot
	 *            it is set to true if the folder has to be created from root folder
	 */
	private void executeMkdir(String command, boolean isExecuteOnRoot) {
		List<String> commandPieces = splitReturn(command, "/");
		Directory<String> root = isExecuteOnRoot ? getRoot(currentDirectory) : currentDirectory;
		for (String dirName : commandPieces) {
			if (!checkEmptyAndNull(dirName)) {
				Directory<String> newdirectory = getDirectoryByName(dirName, root);

				if (Objects.isNull(newdirectory)) {
					newdirectory = root.createDirectory(new Directory<String>(dirName));
					outputmsgLogger.info("SUCC: CREATED SUCCESSFULLY - FULL PATH: " + executePwd(newdirectory));
				} else {
					outputmsgLogger.info("ERR: ALREADY EXISTED - FULL PATH: " + executePwd(newdirectory));
				}
				root = newdirectory;
			}
		}
	}

	/**
	 * executeMkdir is a method which executes 'mkdir' command logic. It handles
	 * creation of multiple folders in the same directory.
	 * 
	 * It is fail safe because it creates other folders even if some folders in the
	 * command exists
	 * 
	 * Example - 'mkdir playment hiring success'
	 * 
	 * Above command creates three folders namely playment hiring success. If some
	 * folders already exists say, 'hiring' then it creates 'playment' and 'sucess'
	 * folders
	 * 
	 * It prints full path from root of every folder it creates after successful
	 * creation. If not created and already existed, then it prints as already
	 * existed
	 * 
	 * @param dirName
	 *            dirName to create in a current directory
	 */
	private void executeMkdirCurrentSingle(String dirName) {
		if (isDirectoryAlreadyExist(dirName)) {
			outputmsgLogger.info("ERR: " + dirName + " ALREADY EXISTED");
		} else {
			Directory<String> directory = currentDirectory.createDirectory(new Directory<String>(dirName));
			outputmsgLogger.info("SUCC: CREATED SUCCESSFULLY - FULL PATH: " + executePwd(directory));
		}
	}

	/**
	 * decideCdCommandMode decides whether given command is executed from root or
	 * current directory
	 * 
	 * @param command
	 *            command args
	 */
	private void decideCdCommandMode(String command) {
		if (isExecuteOnRoot(command)) {
			executeCd(command, true);
		} else if (isDeepCommandArgs(command)) {
			executeCd(command, false);
		} else {
			executeCd(command);
		}
	}

	/**
	 * executeCd is a method which executes 'cd' command logic. It handles
	 * navigation from current directory.
	 * 
	 * Example - cd playment Navigates to 'playment' directory if existed. otherwise
	 * print as invalid directory
	 * 
	 * @param dirName
	 *            directory name to navigate
	 */
	private void executeCd(String dirName) {
		Directory<String> directory = getDirectoryByName(dirName);
		if (Objects.nonNull(directory)) {
			currentDirectory = directory;
			outputmsgLogger.info("SUCC: REACHED: " + executePwd(directory));
		} else {
			outputmsgLogger.error("ERR: INVALID DIRECTORY");
		}
	}

	/**
	 * executeCd is a method which executes 'cd' command logic. It handles
	 * navigation to multiple directory.
	 * 
	 * 1. cd /<folder-name>/<folder-name> Moves to corresponding folder from root
	 * 
	 * 2. cd <folder-name>/<folder-name> Moves to corresponding folder from current
	 * directory.
	 * 
	 * 3. cd / Moves to root directory.
	 * 
	 * Example - cd playment/hiring/success Navigates to 'playment/hiring/success'
	 * directory if existed. otherwise print as invalid directory
	 * 
	 * @param commandArgs
	 *            user input to for navigation
	 * @param isExecuteFromRoot
	 *            set to true if needs to execute from root
	 */
	private void executeCd(String commandArgs, boolean isExecuteFromRoot) {
		List<String> directories = splitReturn(commandArgs, "/");
		if (directories.isEmpty()) {
			if (commandArgs.trim().matches("/+")) {
				currentDirectory = getRoot(currentDirectory);
				outputmsgLogger.info("SUCC: REACHED TO ROOT DIRECTORY ");
			}
			return;
		}

		if (isPathAvailable(isExecuteFromRoot, directories)) {
			currentDirectory = isExecuteFromRoot ? getRoot(currentDirectory) : currentDirectory;
			for (String dirName : directories) {
				if (!checkEmptyAndNull(dirName)) {
					Directory<String> directory = getDirectoryByName(dirName);
					if (Objects.nonNull(directory)) {
						currentDirectory = directory;
						outputmsgLogger.info("SUCC: REACHED: " + executePwd(directory));
					} else {
						outputmsgLogger.error(INVALID_PATH);
						break;
					}
				}
			}
		} else {
			outputmsgLogger.error(INVALID_PATH);
		}
	}

	/**
	 * decideRmCommandMode decides whether given command is executed from root or
	 * current directory
	 * 
	 * @param commands
	 *            list of command args
	 */
	private void decideRmCommandMode(List<String> commands) {
		for (String rmcommand : commands) {
			if (isExecuteOnRoot(rmcommand)) {
				executeRm(rmcommand, true);
			} else if (isDeepCommandArgs(rmcommand)) {
				executeRm(rmcommand, false);
			} else {
				executeRm(rmcommand);
			}
		}
	}

	/**
	 * executeRm is a method which executes 'rm' command logic. It handles
	 * navigation to removal of directory. It supports multiple file removal in
	 * hierarchy at a time.
	 * 
	 * It validates whether user removing current directory or its ancestor. If so,
	 * you cannot remove current directory or its parent
	 * 
	 * 
	 * 1. rm /<folder-name> /<folder-name> - Removes corresponding folders from root
	 * 
	 * 2. rm <folder-name>/<folder-name> - Removes corresponding folders in
	 * hierarchy from current directory .
	 * 
	 * 
	 * @param rmcommand
	 *            user input to for removal
	 * @param isExecuteFromRoot
	 *            set true to execute command on root or else from current
	 *            directory.
	 */
	private void executeRm(String rmcommand, boolean isExecuteFromRoot) {
		List<String> directories = removeEmptyAndNull(splitReturn(rmcommand, "/"));
		Directory<String> root = isExecuteFromRoot ? getRoot(currentDirectory) : currentDirectory;
		if (isPathAvailable(isExecuteFromRoot, directories)) {
			for (String dirName : directories) {

				Directory<String> lookupDirectory = getDirectoryByName(dirName, root);
				root = lookupDirectory;
			}
			Directory<String> parent = root.getParent();
			if (isRemovable(root, currentDirectory)) {
				if (parent.getChildren().remove(root)) {
					outputmsgLogger.info("SUCC: DELETED");
				}
			} else {
				outputmsgLogger.error("ERR: CANNOT REMOVE CURRENT DIRECTORY OR ITS PARENT");
			}
		} else {
			outputmsgLogger.error(INVALID_PATH);
		}

	}

	/**
	 * executeRm is a method which executes 'rm' command logic. It handles
	 * navigation to removal of directory. It supports multiple file removal at a
	 * time in a same directory.
	 * 
	 * 
	 * 2. rm <folder-name> <folder-name> - Removes to corresponding folders from
	 * current directory.
	 * 
	 * @param dirName
	 *            user input to for removal
	 */
	private void executeRm(String dirName) {
		Directory<String> directory = getDirectoryByName(dirName);
		if (Objects.isNull(directory)) {
			outputmsgLogger.error("ERR: DIRECTORY DOESN'T EXIST");
			return;
		}
		boolean isRemoved = currentDirectory.getChildren().remove(directory);
		if (isRemoved) {
			outputmsgLogger.info("SUCC: DELETED");
		} else {
			outputmsgLogger.error("ERR: DIRECTORY DOESN'T EXIST");
		}
	}

	/**
	 * executeSessionClear implements session clear command logic session clear
	 * method reset the application to start
	 * 
	 * @param command
	 *            session clear command
	 */
	private void executeSessionClear(String command) {
		if (command.equals("clear")) {
			currentDirectory = new Directory<>("/");
			outputmsgLogger.info("SUCC: RESET TO ROOT /");
		} else {
			outputmsgLogger.error("ERR: UNSUPPORTED ARGUMENTS.");
		}

	}

	/**
	 * isPathAvailable is a utility method which checks whether given path is valid
	 * or not.
	 * 
	 * @param isExecuteFromRoot
	 *            set to true if needs to execute from root
	 * @param directories
	 *            user input for navigation
	 * @return true if it is an valid path
	 */
	private boolean isPathAvailable(boolean isExecuteFromRoot, List<String> directories) {
		Directory<String> root = isExecuteFromRoot ? getRoot(currentDirectory) : currentDirectory;

		boolean isExists = false;
		for (String dirName : directories) {
			if (!checkEmptyAndNull(dirName)) {
				Directory<String> directory = getDirectoryByName(dirName, root);
				if (Objects.nonNull(directory)) {
					root = directory;
					isExists = true;
				} else {
					return false;
				}
			}
		}

		return isExists;
	}

	/**
	 * isDirectoryAlreadyExist is a utility method which checks whether the
	 * directory exists or not by given name.
	 * 
	 * @param dirName
	 *            dirName to check existence
	 * @return true if it exists
	 */
	private boolean isDirectoryAlreadyExist(String dirName) {
		return currentDirectory.getChildren().stream().anyMatch(directory -> directory.getData().equals(dirName));
	}

	/**
	 * getDirectoryByName is a utility method which checks whether the directory
	 * exists or not by given name in a currentDirectory.
	 * 
	 * @param dirName
	 *            dirName to check existence
	 * @return Directory if it exists
	 */
	private Directory<String> getDirectoryByName(String dirName) {
		Optional<Directory<String>> directory = currentDirectory.getChildren().stream()
				.filter(dir -> dir.getData().equals(dirName)).findFirst();
		return directory.isPresent() ? directory.get() : null;
	}

	/**
	 * getDirectoryByName is a utility method which checks whether the directory
	 * exists or not by given name in a given directory.
	 * 
	 * @param dirName
	 *            dirName to check existence
	 * @param lookupDirectory
	 *            directory to check
	 * @return Directory if it exists
	 */
	private Directory<String> getDirectoryByName(String dirName, Directory<String> lookupDirectory) {
		Optional<Directory<String>> directory = lookupDirectory.getChildren().stream()
				.filter(dir -> dir.getData().equals(dirName)).findFirst();
		return directory.isPresent() ? directory.get() : null;
	}

	/**
	 * getRoot is a utility method which gets root from the given directory.
	 * 
	 * @param directory
	 *            dirName to get root
	 * @return Directory root directory
	 */
	private Directory<String> getRoot(Directory<String> directory) {
		if (Objects.isNull(directory.getParent())) {
			return directory;
		}
		return getRoot(directory.getParent());
	}

	/**
	 * isExecuteOnRoot is a method which checks whether the command has to be
	 * executed from root or current directory.
	 * 
	 * @param commandArgs
	 *            command arguments
	 * @return true if it should be executed from root
	 */
	private boolean isExecuteOnRoot(String commandArgs) {
		return commandArgs.startsWith("/");
	}

	/**
	 * isDeepCommandArgs is a method which checks whether the command has to be
	 * executed in hierarchy.
	 * 
	 * @param commandArgs
	 *            command arguments
	 * @return true if it should be executed deeply
	 */
	private boolean isDeepCommandArgs(String commandArgs) {
		return !commandArgs.startsWith("/") && commandArgs.contains("/");
	}

	/**
	 * checkEmptyAndNull is a method which checks empty and null
	 * 
	 * @param command
	 *            command arguments
	 * @return true if it should be executed from root
	 */
	private boolean checkEmptyAndNull(String command) {
		return (Objects.isNull(command) || command.trim().equals(""));
	}

	/**
	 * splitReturn is a method which splits given string by given delimiter
	 * 
	 * @param command
	 *            string to split
	 * @param delimiter
	 *            split with this delimiter string
	 * @return list of split strings
	 */
	private List<String> splitReturn(String command, String delimiter) {
		return new ArrayList<>(Arrays.asList(command.trim().split(delimiter)));
	}

	/**
	 * isRemovable is a method which checks whether given directory is an current
	 * directory or its current directory parent
	 * 
	 * @param directory
	 *            directory needs to check whether it is removable
	 * @param currentDirectory
	 *            currentDirectory and its parent
	 * @return true if its removable
	 */
	private boolean isRemovable(Directory<String> directory, Directory<String> currentDirectory) {
		if (currentDirectory == null) {
			return true;
		}
		if (directory.equals(currentDirectory)) {
			return false;
		}
		return isRemovable(directory, currentDirectory.getParent());
	}

	private List<String> removeEmptyAndNull(List<String> directories) {
		return directories.stream().filter(dirName -> !checkEmptyAndNull(dirName)).collect(Collectors.toList());
	}
}
