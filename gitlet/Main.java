package gitlet;

import java.io.File;
import java.io.IOException;
import java.lang.System;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Judy Moon, Jiaming Yuan, Maoqi Zhang
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args)
            throws IOException, ClassNotFoundException {

        File gitlet = new File(".gitlet");
        Gitlet commands = new Gitlet();

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (!args[0].equals("init") && !gitlet.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (args[0]) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.init();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.add(args[1]);
                break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Please enter a commit message.");
                    break;
                }
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.commit(args[1]);
                break;
            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.globalLog();
                break;
            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.find(args[1]);
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.rm(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.status();
                break;
            case "checkout":
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        break;
                    }
                    commands.checkout(args[2]);
                    break;
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        break;
                    }
                    commands.checkout(args[1], args[3]);
                    break;
                } else if (args.length == 2) {
                    commands.branchCheckout(args[1]);
                    break;
                } else {
                    System.out.println("Incorrect operands");
                    break;
                }
            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.branch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.rmbranch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                commands.merge(args[1]);
                break;
            default :
                System.out.println("No command with that name exists.");
                break;
        }
    }
}