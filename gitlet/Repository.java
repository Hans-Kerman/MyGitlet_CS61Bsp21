package gitlet;

import java.io.File;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  是一个管理类,包含了很多static的File常量指向具体的文件
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    /** HEAD指针文件, 在.gitlet/HEAD, 因为不会头指针分离, 直接保存指向的branch */
    public static final File HEAD_path = join(GITLET_DIR, "HEAD");
    public static String HEAD_ptr = Utils.readContentsAsString(HEAD_path);

    /** branch指针的父目录, 是.gitlet/refs/heads/, 内部每个文件名字是分支名, 明文保存Commit的git-SHA1 */
    public static final File Branches_path = join(join(GITLET_DIR, "refs"), "heads");


    /** 工具函数, 给传入的Commit或者Blob计算git-SHA1哈希值 */
    public static String gitSHA1(Commit c) {
        return sha1((Object) serialize(c));
    }
    public static String gitSHA1(byte[] b) {
        return sha1((Object) b);
    }

}
