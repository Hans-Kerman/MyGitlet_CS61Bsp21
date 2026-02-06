package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.Locale;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  Commit对象包含一个提交的所有信息, 包括:
 *      父提交(们)
 *      时间戳
 *      提示信息
 *      文件列表
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    /* TODO: fill in the rest of this class. */
    /** 父提交列表, 一个提交可以有多个父提交, 用提交的git-SHA1结果唯一标识 */
    private final ArrayList<String> parentCommits;

    /** 时间戳,使用当地时间(东八区)记录,init使用Unix纪元0点 */
    private final Date timestamp;

    /** 文件列表,存储文件名到git-SHA1的映射 */
    private final TreeMap<String, String> blobs;

    public String getMessage() {
        return message;
    }

    public ArrayList<String> getParentCommits() {
        return parentCommits;
    }

    public TreeMap<String, String> getBlobs() {
        if (blobs == null) {
            return new TreeMap<>();
        }
        return blobs;
    }

    public Commit(String message, ArrayList<String> parentCommits, Date timestamp, TreeMap<String, String> blobs) {
        this.message = message;
        this.parentCommits = parentCommits;
        this.timestamp = timestamp;
        this.blobs = blobs;
    }

    public Commit(String message, ArrayList<String> parentCommits, TreeMap<String, String> blobs) {
        this.message = message;
        this.parentCommits = parentCommits;
        this.blobs = blobs;
        this.timestamp = new Date();
    }

    public static Commit InitCommit () {
        return new Commit(
                "initial commit",
                new ArrayList<>(),
                new Date(0),
                new TreeMap<String, String>()
        );
    }

    public String getTimestampStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return sdf.format(timestamp);
    }


}
