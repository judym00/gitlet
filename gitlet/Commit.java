package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.lang.System;

/** @authors: Judy Moon, Jiaming Yuan, Maoqi Zhang **/

public class Commit implements Serializable{
    /** The committed files in this commit. */
    HashMap<String, String> hashMapblobs = new HashMap<String, String>();

    /** SHA-ID code of the commit. */
    private final String sha;

    /** Head of commit. */
    static Commit _head = null;

    /** Return the time of this commit. */
    private final Date _time;

    /** Return the message of this commit. */
    private final String commitMessage;

    /** Parent of the commit. */
    private final String parent;

    private String mergeParent;

    Commit(String commitMessage) {
        File headFile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headFile, Commit.class);

        String parentSHAID = head.sha;

        this.commitMessage = commitMessage;
        this.hashMapblobs = getHashMap();

        this.parent = parentSHAID;
        this._time = new Date();
        this.sha = Utils.sha1(commitMessage, _time.toString(),
                parent, hashMapblobs.toString());
    }

    /** Initializes the first commit in a gitlet directory once the init command is called. */
    Commit() {
        this.commitMessage = "initial commit";
        this.parent = "";
        this._time = new Date(0);
        this.sha = gitlet.Utils.sha1(commitMessage,
                _time.toString(),
                parent,
                hashMapblobs.toString());
    }

    /**
     * Return the message of this commit.
     * @return _message
     */
    String getMessage() {
        return this.commitMessage;
    }

    /**
     * Return the first parent sha1code of this commit.
     * @return _parent
     */
    String getParent() {
        return this.parent;
    }

    /**
     * Return the time of this commit.
     * @return _time
     */
    Date getTime() {
        return this._time;
    }

    /**
     * Return the sha1code of this commit.
     * @return _sha1code
     */
    String getSha() {
        return this.sha;
    }

    Commit getHead() {
        return this._head;
    }

    HashMap<String, String> getHashMap() {
        File headFile = new File(System.getProperty("user.dir")
                + "/.gitlet/HEAD");
        Commit head = Utils.readObject(headFile, Commit.class);

        HashMap<String, String> blobMap = head.hashMapblobs;
        File file = new File(System.getProperty("user.dir")
                + "/.gitlet/staged");
        Stage staged = Utils.readObject(file, Stage.class);
        blobMap.putAll(staged._stagingMap);

        for (String filename : staged._stagingRemove) {
            blobMap.remove(filename);
        }
        return blobMap;
    }

    String getMergeParent(){
        return this.mergeParent;
    }

    void setMergeParent(String parent){
        mergeParent = parent;
    }

}