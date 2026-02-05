package gitlet;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.InitRepository();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String fileName = args[1];
                Repository.addFileToStage(fileName);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                // 基础错误处理：没message
                if (args.length < 2 || args[1].isBlank()) {
                    throw error("Please enter a commit message.");
                } else {
                    Repository.commitToGitlet(args[1]);
                }
                break;
            case "rm":
                String filename = args[1];
                Repository.removeFile(filename);
                break;
            case "log":
                Repository.printLog();
                break;
            case "global-log":
                Repository.printGlobalLog();
                break;
            case "find":
                String matchMessage = args[1];
                Repository.findMatchMessage(matchMessage);
                break;
            case "status":
                Repository.printStatus();
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutOneHeadFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutOneCommitFile(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutWholeBranch(args[1]);
                }
        }
    }
}
