package gitlet;

import java.io.File;

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
    /** 文件存储对象objects路径 */
    public static final File OBJ_DIR = join(GITLET_DIR, "objects");
    public static final File BLOBS_DIR = join(OBJ_DIR, "blobs");
    public static final File COMMITS_DIR = join(OBJ_DIR, "commits");
    /** branch指针的父目录, 是.gitlet/refs/, 内部每个文件名字是分支名, 明文保存Commit的git-SHA1 */
    public static final File BRANCH_DIR = join(GITLET_DIR, "refs");

    /* TODO: fill in the rest of this class. */
    /** HEAD指针文件, 在.gitlet/HEAD, 因为不会头指针分离, 直接保存指向的branch */
    public static final File HEAD_path = join(GITLET_DIR, "HEAD");
    public static String HEAD_branch = Utils.readContentsAsString(HEAD_path);



    /** 工具函数, 给传入的Commit或者Blob计算git-SHA1哈希值 */
    public static String gitSHA1(Commit c) {
        return sha1((Object) serialize(c));
    }
    public static String gitSHA1(byte[] b) {
        return sha1((Object) b);
    }

    /** Init仓库, 创建基本的文件结构, 创建第一个InitCommit并落盘 */
    public static void InitRepository () {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(1);
        }
        boolean success = GITLET_DIR.mkdirs() && OBJ_DIR.mkdirs() && BLOBS_DIR.mkdir() && COMMITS_DIR.mkdirs() && BRANCH_DIR.mkdir();
        if (!success) {
            throw new GitletException("Unable to create the Gitlet directory.");
        }
        Commit init = Commit.InitCommit();
        String initHash = gitSHA1(init);
        writeObject(join(COMMITS_DIR, initHash), init);
        writeContents(join(BRANCH_DIR, "master"), initHash);
        writeContents(HEAD_path, "master");
    }
}
