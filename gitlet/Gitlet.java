package gitlet;

import java.io.*;
import java.lang.System;
import java.text.SimpleDateFormat;
import java.util.*;
import static gitlet.Utils.message;

/** @authors: Judy Moon, Jiaming Yuan, Maoqi Zhang **/

public class Gitlet {
    /**
     * Create a new gitlet version control system in current directory.
     * @throws IOException if there is already a gitlet version control system in currDirectory.
     * should NOT overwrite the existing system with a new one.
     */
    public void init() throws IOException {
        Stage stagingArea = new Stage();

        /**
         * Makes the initial commit and the master branch.
         * Sets the current branch to master.
         * */
        gitlet.Commit new_commit = new gitlet.Commit();
        stagingArea._branches.put("master", new_commit.getSha());
        stagingArea.current_branch_name = "master";

        /** Creates the .gitlet directory if it doesn't already exist. */
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            message("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
        gitlet.mkdir();

        /** Stage the newly made directory. */
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        stagedFile.createNewFile();

        File objects =  new File(System.getProperty("user.dir")
                + "/.gitlet/objects");
        objects.mkdir();

        new_commit.hashMapblobs.putAll(stagingArea._stagingMap);
        stagingArea._stagingMap.clear();
        stagingArea._commitList.add(new_commit.getSha());
        ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(stagedFile));
        output.writeObject(stagingArea);
        output.close();

        ObjectOutputStream output2 = new ObjectOutputStream(new FileOutputStream(objects
                        + "/" + new_commit.getSha()));
        output2.writeObject(new_commit);
        output2.close();

        /** Set the head commit of the current branch as the first commit. */
        File head = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Utils.writeObject(head, new_commit);
    }

    /**
     * Adds copy of file as it currently exists to the staging area.
     * Staging the file for addition.
     * */
    public void add(String filename) throws ClassNotFoundException, IOException {
        File fhead = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(fhead, Commit.class);

        File oneFile = new File(filename);
        if (!oneFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        /** Make a SHA-1 ID for the given oneFile. */
        byte[] shaContents = Utils.readContents(oneFile);
        String shaFile = Utils.sha1(shaContents);

        File f = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(f, Stage.class);

        if (staged._stagingRemove.contains(filename)) {
            staged._stagingRemove.remove(filename);
        }

        if (head.hashMapblobs.containsKey(filename)
                && head.hashMapblobs.get(filename).equals(shaFile)) {
            staged._stagingMap.remove(filename);
            Utils.writeObject(f, staged);
        } else {
            staged._stagingMap.put(filename, shaFile);

            File blob = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + shaFile);
            blob.createNewFile();
            byte[] blobContents = Utils.readContents(oneFile);

            Utils.writeContents(blob, (Object) blobContents);
            Utils.writeObject(f, staged);
        }
    }

    /**
     * Saves a snapshot of certain files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit.
     */
    public void commit(String message) {
        File f = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(f, Stage.class);
        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        if (staged._stagingMap.isEmpty() && staged._stagingRemove.isEmpty()) {
            message("No changes added to the commit.");
        }
        if (message.equals("")) {
            message("Please enter a commit message.");
        }
        Commit new_commit = new Commit(message);

        staged._stagingMap.clear();
        staged._commitList.add(new_commit.getSha());

        /** Set the head of branch to be this commit. */
        staged._branches.put(staged.current_branch_name, new_commit.getSha());
        staged._stagingRemove.removeAll(head.hashMapblobs.keySet());

        /** Update information to file. */
        File commit = new File(System.getProperty("user.dir") + "/.gitlet/objects/" + new_commit.getSha());
        Utils.writeObject(commit, new_commit);
        Utils.writeObject(f, staged);
        Utils.writeObject(headfile, new_commit);
    }

    /**
     * Unstage or remove the file if it is currently staged for addition.
     */
    public void rm(String filename) {
        boolean stagedArea;
        boolean headIsTracking = false;

        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        stagedArea = staged._stagingMap.containsKey(filename);

        if (stagedArea) {
            staged._stagingMap.remove(filename);
        }

        if (head.hashMapblobs.containsKey(filename)) {
            Utils.restrictedDelete(filename);
            headIsTracking = true;
            if (!staged._stagingRemove.contains(filename)) {
                staged._stagingRemove.add(filename);
            }
        }
        if (!headIsTracking && !stagedArea) {
            message("No reason to remove the file.");
        }

        Utils.writeObject(stagedFile, staged);
    }

    /** Display information about each commit backwards.
     * display commit id, time, commit message (look at log helper function).
     */
    public void log() {
        File headFile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit _head = Utils.readObject(headFile, Commit.class);

        Commit pointer = _head;
        Formatter output = new Formatter();

        output.format("===\n");
        output.format("commit %s\n", pointer.getSha());

        SimpleDateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        output.format("Date: %s\n", dateformat.format(pointer.getTime()));

        output.format("%s\n", pointer.getMessage());

        while (true) {
            File parentcommitFILE = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/"
                    + pointer.getParent());
            pointer = Utils.readObject(parentcommitFILE, Commit.class);

            output.format("\n===\n");
            output.format("commit %s\n", pointer.getSha());
            output.format("Date: %s\n", dateformat.format
                    (pointer.getTime()));
            output.format("%s\n", pointer.getMessage());

            if (pointer.getParent().equals("")) {
                break;
            }
        }
        System.out.println(output.toString());
    }

    /**
     * Like log, except displays information about all commits ever made.
     * The order of the commits does not matter.
     */
    public void globalLog() {
        Formatter output = new Formatter();
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);
        Commit commit;
        File commitFile;
        SimpleDateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        for (String sha1 : staged._commitList) {
            commitFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + sha1);
            commit = Utils.readObject(commitFile, Commit.class);

            output.format("===\n");
            output.format("commit %s\n", commit.getSha());
            output.format("Date: %s\n", dateformat.format(commit.getTime()));
            output.format("%s\n\n", commit.getMessage());
        }
        String text = output.toString();
        System.out.println(text.substring(0, text.lastIndexOf('\n')));
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * @param logMessage
     */
    public void find(String logMessage) {
        Formatter output = new Formatter();
        File stagedFile = new File(System.getProperty("user.dir") + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);
        int count = 0;
        String message;
        for (String sha1 : staged._commitList) {
            File commitfile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + sha1);
            Commit c = Utils.readObject(commitfile, Commit.class);
            message = c.getMessage();

            if (message.equals(logMessage)) {
                output.format("%s\n", c.getSha());
                count ++;
            }
        }
        if (count == 0) {
            message("Found no commit with that message.");
        }
        System.out.println(output.toString());
    }

    private class SArrayList extends ArrayList<String> {
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     */
    public void status() {
        Formatter output = new Formatter();
        File headfile = new File(System.getProperty("user.dir") + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        File stagedFile = new File(System.getProperty("user.dir") + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        SArrayList branchNames = new SArrayList();
        branchNames.addAll(staged._branches.keySet());
        Collections.sort(branchNames);

        output.format("=== Branches ===\n");
        for (String branchName : branchNames) {
            if (staged._branches.get(branchName).equals(head.getSha())) {
                branchName = '*' + branchName;
            }
            output.format(branchName + "\n");
        }

        output.format("\n=== Staged Files ===\n");
        SArrayList stagedNames = new SArrayList();
        stagedNames.addAll(staged._stagingMap.keySet());

        Collections.sort(stagedNames);
        for (String stagedName : stagedNames) {
            output.format(stagedName + "\n");
        }

        output.format("\n=== Removed Files ===\n");
        ArrayList<String> deletedNames = staged._stagingRemove;
        Collections.sort(deletedNames);
        for (String delete : deletedNames) {
            output.format(delete + "\n");
        }

        output.format("\n=== Modifications Not Staged For Commit ===\n");

        output.format("\n=== Untracked Files ===\n");

        System.out.println(output.toString());
    }

    /**
     * Three possible usages of checkout.
     * @param filename Name of file that user wants to checkout
     */
    public void checkout(String filename) throws IOException {
        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);
        checkout(head.getSha(), filename);
    }

    /** Helper functions for checkout. */
    public void checkout(String commitID, String filename) throws IOException {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        commitID = commitSearch(commitID);

        /** Error messages. */
        if (commitID.equals("null")) {
            System.out.println("No commit with that id exists.");
            return;
        }

        File commitFile = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + commitID);
        Commit c = Utils.readObject(commitFile, Commit.class);
        if (!c.hashMapblobs.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String shaFile = c.hashMapblobs.get(filename);
        File direct = new File(filename);
        File blob = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + shaFile);

        if (!direct.exists()) {
            direct.createNewFile();
        }

        byte[] fileContent = Utils.readContents(blob);
        Utils.writeContents(direct, (Object) fileContent);

    }

    public void branchCheckout(String branchName) throws IOException {
        File stagedFile = new File(System.getProperty("user.dir") + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        if (!staged._branches.containsKey(branchName)) {
            message("No such branch exists.");
        } else if (staged.current_branch_name.equals(branchName)) {
            message("No need to checkout the current branch.");
        } else {
            File headFile_2 = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/"
                    + staged._branches.get(branchName));
            Commit head_2 = Utils.readObject(headFile_2, Commit.class);

            Set<String> headFiles = head.hashMapblobs.keySet();
            Set<String> headFiles_2 = head_2.hashMapblobs.keySet();

            List<String> listFiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));
            for (String file : listFiles) {
                if ((!headFiles.contains(file))
                        && headFiles_2.contains(file)) {
                    message("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                }
            }
            for (String file : headFiles) {
                if (!headFiles_2.contains(file)) {
                    Utils.restrictedDelete(file);
                }
            }
            for (String bfile : headFiles_2) {
                checkout(head_2.getSha(), bfile);
            }

            if (!staged.current_branch_name.equals(branchName)) {
                staged._stagingMap.clear();
            }
            staged.current_branch_name = branchName;
            Utils.writeObject(headfile, head_2);
            Utils.writeObject(stagedFile, staged);
        }
    }

    /**
     * Creates a new branch with the given name, and points it at the current head node.
     */
    public void branch(String branchName) {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        if (staged._branches.containsKey(branchName)) {
            message("A branch with that name already exists.");
        } else {
            File headfile = new File(System.getProperty("user.dir") + "/.gitlet/HEAD");
            Commit head = Utils.readObject(headfile, Commit.class);
            staged._branches.put(branchName, head.getSha());
            Utils.writeObject(stagedFile, staged);
        }
    }

    /**
     * Deletes the branch with the given name.
     */
    public void rmbranch(String branchName) {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        if (!staged._branches.containsKey(branchName)) {
            message("A branch with that name does not exist.");
        } else if (staged.current_branch_name.equals(branchName)) {
            message("Cannot remove the current branch.");
        } else {
            staged._branches.remove(branchName);
            Utils.writeObject(stagedFile, staged);
        }
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     */
    public void reset(String commitID) throws IOException {
        commitID = commitSearch(commitID);
        if (commitID.equals("null")) {
            System.out.println("No commit with that id exists.");
            return;
        }

        List<String> listFiles = Utils.plainFilenamesIn(
                System.getProperty("user.dir"));

        File commitedFiles = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + commitID);
        Commit c = Utils.readObject(commitedFiles, Commit.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headfile, Commit.class);

        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        Set<String> trackedFiles = c.hashMapblobs.keySet();
        Set<String> trackedFiles_head = head.hashMapblobs.keySet();

        for (String file : listFiles) {
            if ((!trackedFiles_head.contains(file))
                    && trackedFiles.contains(file)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (String f : trackedFiles_head) {
            if (!trackedFiles.contains(f)) {
                Utils.restrictedDelete(f);
            }
        }
        for (String file : trackedFiles) {
            checkout(commitID, file);
        }
        staged._stagingMap.clear();
        staged._branches.put(staged.current_branch_name, commitID);
        Utils.writeObject(headfile, c);
        Utils.writeObject(stagedFile, staged);
    }


    /**
     * Merges files from the given branch into the current branch.
     * @param branchName name of the given branch
     */
    public void merge(String branchName) throws IOException, ClassNotFoundException {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);

        File headfile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit current_commit = Utils.readObject(headfile, Commit.class);

        Set<String> currFiles = current_commit.hashMapblobs.keySet();

        if (!staged._stagingMap.isEmpty()
                || !Collections.disjoint(currFiles, staged._stagingRemove)) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!staged._branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (branchName.equals(staged.current_branch_name)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        File fileBranch = new File(System.getProperty("user.dir")
                + "/.gitlet/objects/" + staged._branches.get(branchName));
        Commit input = Utils.readObject(fileBranch, Commit.class);

        Set<String> inputFiles = input.hashMapblobs.keySet();

        List<String> dirFiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String f : dirFiles) {
            if ((!currFiles.contains(f)) && inputFiles.contains(f)) {
                System.out.println("There is an untracked "
                        + "file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        String inputHead = staged._branches.get(branchName);
        String currHead = current_commit.getSha();
        String splitPointID = findSplitPoint(inputHead, currHead) ;

        if (splitPointID.equals(inputHead)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        } else if (splitPointID.equals(currHead)) {
            String currName = staged.current_branch_name;
            branchCheckout(branchName);
            staged.current_branch_name = currName;
            staged._branches.put(currName, inputHead);
            System.out.println("Current branch fast-forwarded.");
            Utils.writeObject(stagedFile, staged);
        } else {
            File splitedFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + splitPointID);
            Commit split = Utils.readObject(splitedFile, Commit.class);
            Set<String> splitedFiles = split.hashMapblobs.keySet();

            Set<String> allFiles = new HashSet<String>();
            allFiles.addAll(splitedFiles);
            allFiles.addAll(currFiles);
            allFiles.addAll(inputFiles);
            boolean conflict = false;

            for (String f : allFiles) {
                String sID = split.hashMapblobs.get(f);
                String cID = current_commit.hashMapblobs.get(f);
                String gID = input.hashMapblobs.get(f);

                if (sID != null) {
                    if (cID != null && gID != null) {
                        if ((!gID.equals(sID)) && cID.equals(sID)) {
                            checkout(inputHead, f);
                            add(f);
                        }
                    } else if (gID == null && cID != null) {
                        if (cID.equals(sID)) {
                            rm(f);
                        }
                    }
                } else if (cID == null && gID != null) {
                    checkout(inputHead, f);
                    add(f);
                }
                if ((sID != null && cID != null && gID != null
                        && !gID.equals(sID) && !gID.equals(sID)
                        && !cID.equals(sID))
                        || (sID == null && cID != null && gID != null
                        && !cID.equals(gID))
                        || (sID != null && gID == null && !sID.equals(cID))
                        || (sID != null && cID == null && !sID.equals(gID))) {
                    Formatter out = new Formatter();
                    File currFile = new File(System.getProperty("user.dir")
                            + "/.gitlet/objects/" + cID);
                    File inputFile = new File(System.getProperty("user.dir")
                            + "/.gitlet/objects/" + gID);
                    String curr;
                    String given;
                    if (!currFile.exists()) {
                        given = Utils.readContentsAsString(inputFile);
                        curr = "";
                    } else if (!inputFile.exists()) {
                        given = "";
                        curr = Utils.readContentsAsString(currFile);
                    } else {
                        given = Utils.readContentsAsString(inputFile);
                        curr = Utils.readContentsAsString(currFile);
                    }
                    out.format("<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n",
                            curr, given);
                    String newC = out.toString();
                    File conflicted = new File(f);
                    Utils.writeContents(conflicted, newC);
                    add(f);
                    conflict = true;
                }
            }
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
            String message = "Merged " + branchName + " into "
                    + staged.current_branch_name + ".";
            File f = new File(System.getProperty("user.dir")
                    + "/.gitlet/staged");
            File headfile1 = new File(System.getProperty("user.dir")
                    + "/.gitlet/HEAD");
            Commit head = Utils.readObject(headfile1, Commit.class);
            Commit new_commit = new Commit(message);
            new_commit.setMergeParent(input.getSha());
            staged._stagingMap.clear();
            staged._commitList.add(new_commit.getSha());
            staged._branches.put(staged.current_branch_name, new_commit.getSha());
            staged._stagingRemove.removeAll(head.hashMapblobs.keySet());
            File commit = new File(System.getProperty("user.dir") + "/.gitlet/objects/" + new_commit.getSha());
            Utils.writeObject(commit, new_commit);
            Utils.writeObject(f, staged);
            Utils.writeObject(headfile, new_commit);
        }
    }

    /**Locates the split point of two branches. */
    public String findSplitPoint(String input, String curr) {
        SArrayList input_hist = new SArrayList();
        while (!input.equals("")) {
            File inputFile = new File(System.getProperty("user.dir")
                    + "/.gitlet/objects/" + input);
            Commit iFile = Utils.readObject(inputFile, Commit.class);
            input_hist.add(input);
            input = iFile.getParent();
        }
        if (input_hist.contains(curr)) {
            return curr;
        } else {
            boolean merge = false;
            while (curr != null) {
                File currFile = new File(System.getProperty("user.dir")
                        + "/.gitlet/objects/" + curr);
                Commit cFile = Utils.readObject(currFile, Commit.class);
                if (cFile.getMergeParent() != null) {
                    merge = true; break;
                }
                if (input_hist.contains(curr)) {
                    return curr;
                }
                curr = cFile.getParent();
            }
            if (merge) {
                File currFile = new File(System.getProperty("user.dir")
                        + "/.gitlet/objects/" + curr);
                Commit File = Utils.readObject(currFile, Commit.class);
                String temp1 = File.getMergeParent();
                String temp2 = File.getParent();
                int index1 = 0;
                int index2 = 0;
                while (temp1 != null) {
                    File tempFile = new File(System.getProperty("user.dir")
                            + "/.gitlet/objects/" + temp1);
                    Commit cFile = Utils.readObject(tempFile, Commit.class);
                    if (cFile.getMergeParent() != null) {
                        merge = true; break;
                    }
                    if (input_hist.contains(temp1)) {
                        break;
                    }
                    temp1 = cFile.getParent();
                    index1 ++;
                }
                while (temp2 != null) {
                    File tempFile = new File(System.getProperty("user.dir")
                            + "/.gitlet/objects/" + temp2);
                    Commit cFile = Utils.readObject(tempFile, Commit.class);
                    if (cFile.getMergeParent() != null) {
                        merge = true; break;
                    }
                    if (input_hist.contains(temp2)) {
                        break;
                    }
                    temp2 = cFile.getParent();
                    index2 ++;
                }
                if (index1 > index2) {
                    return temp2;
                } else {
                    return temp1;
                }
            }
            return "";
        }
    }






    /** Provides short unique sha-1 ID when calling/using methods. Abbreviated from the 40 character SHA-1 ID.
     * @param sha abbreviated SHA-1 ID
     * @return long version with 40 char SHA-1 ID */
    public String commitSearch(String sha) {
        File stagedFile = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(stagedFile, Stage.class);
        for (String commitID : staged._commitList) {
            if (commitID.startsWith(sha)) {
                return commitID;
            }
        }
        return "null";
    }
}