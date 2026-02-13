package gitlet;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author buttercat
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        if (!firstArg.equals("init")) {
            repoExistCheck();
        }
        try {
            switch (firstArg) {
                case "init":
                    if (args.length != 1) {
                        badCommandExit();
                    }
                    Repository.initRepository();
                    break;
                case "add":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    String fileName = args[1];
                    Repository.addFileToStage(fileName);
                    break;
                case "commit":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    // 基础错误处理：没message
                    if (args[1].isBlank()) {
                        throw error("Please enter a commit message.");
                    } else {
                        Repository.commitToGitlet(args[1]);
                    }
                    break;
                case "rm":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    String filename = args[1];
                    Repository.removeFile(filename);
                    break;
                case "log":
                    if (args.length != 1) {
                        badCommandExit();
                    }
                    Repository.printLog();
                    break;
                case "global-log":
                    if (args.length != 1) {
                        badCommandExit();
                    }
                    Repository.printGlobalLog();
                    break;
                case "find":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    String matchMessage = args[1];
                    Repository.findMatchMessage(matchMessage);
                    break;
                case "status":
                    if (args.length != 1) {
                        badCommandExit();
                    }
                    Repository.printStatus();
                    break;
                case "checkout":
                    if (args.length == 3 && args[1].equals("--")) {
                        Repository.checkoutOneHeadFile(args[2]);
                    } else if (args.length == 4 && args[2].equals("--")) {
                        Repository.checkoutOneCommitFile(args[1], args[3]);
                    } else if (args.length == 2) {
                        Repository.checkoutWholeBranch(args[1]);
                    } else {
                        badCommandExit();
                    }
                    break;
                case "branch":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    Repository.makeBranch(args[1]);
                    break;
                case "rm-branch":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    Repository.removeBranch(args[1]);
                    break;
                case "reset":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    Repository.reset(args[1]);
                    break;
                case "merge":
                    if (args.length != 2) {
                        badCommandExit();
                    }
                    Repository.merge(args[1]);
                    break;
                default:
                    message("No command with that name exists.");
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private static void badCommandExit() {
        message("Incorrect operands.");
        System.exit(0);
    }

    private static void repoExistCheck() {
        if (!join(Repository.CWD, ".gitlet").exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
