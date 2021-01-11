package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

class Stage implements Serializable {

    /** Constructor for staging area. */
    Stage() {
        _stagingMap = new HashMap<String, String>();
        _stagingRemove = new ArrayList<String>();
        _commitList = new ArrayList<String>();
        _branches = new HashMap<String, String>();

    }
    /** current branch. */
    public String current_branch_name;

    /** Maps the name to the head commit SHA-1 ID of the branch. */
    public HashMap<String, String> _branches = new HashMap<String, String>();

    /** HashMap of all staged files. */
    public HashMap<String, String> _stagingMap;

    /** Arraylist of filenames that you want to remove. */
    public ArrayList<String> _stagingRemove;

    /** Arraylist of all committed SHA-1 IDs. */
    public ArrayList<String> _commitList;

}