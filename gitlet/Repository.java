package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

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
    public static final File HEAD_path = join(GITLET_DIR, "HEAD");  //HEAD文件路径
    public static String HEAD_branch = readContentsAsString(HEAD_path);     //取出来的HEAD信息(branch名字的String)

    /** gitlet的暂存区信息, 保存在.gitlet/index文件中 */
    public static File Stage_path = join(GITLET_DIR, "index");


    /** 工具函数, 给传入的Commit或者Blob计算git-SHA1哈希值 */
    public static String gitSHA1(Commit c) {
        return sha1((Object) serialize(c));
    }
    public static String gitSHA1(File file) {
        return sha1((Object) readContents(file));
    }

    /** 工具函数, 用于把Blob或者Commit数据落盘 */
    public static void storeBlob(File f) {
        writeContents(join(BLOBS_DIR, gitSHA1(f)), (Object) readContents(f));
    }
    public static void storeCommit(Commit commit) {
        writeObject(join(COMMITS_DIR, gitSHA1(commit)), commit);
    }

    /** Init仓库, 创建基本的文件结构, 创建第一个InitCommit并落盘 */
    public static void InitRepository () {
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
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

    /** 工具函数, 输入哈希返回Commit对象 */
    public static Commit getCommit(String hash) {
        List<String> commitsList = plainFilenamesIn(COMMITS_DIR);
        if (commitsList == null || !commitsList.contains(hash)) {
            throw error("Internal error searching commit.");
        } else {
            return readObject(join(COMMITS_DIR, hash), Commit.class);
        }
    }
    /** 工具函数, 返回指定branch的最后一个Commit对象 */
    public static Commit getCommitByBranch(String branch) {
        String hash = readContentsAsString(join(BRANCH_DIR, branch));
        return getCommit(hash);
    }
    /** 工具函数, 返回最后一个(HEAD所在的)Commit对象 */
    public static Commit getLastCommit() {
        return getCommitByBranch(HEAD_branch);
    }

    /**  add
     * 1. 覆盖已经暂存的条目
     * 2. 如果版本与已提交的相同则检查并且移除暂存
     * 3. 离开准备移除区域
     */
    public static void addFileToStage(String file) {
        File filePath = join(CWD, file);
        if (!filePath.exists()) {
            throw error("File does not exist.");
        }
        Commit lastCommit = getLastCommit();
        Stage stage = Stage.getStage();

        String diskUid = gitSHA1(filePath); //准备暂存的版本
        if (lastCommit.getBlobs().containsKey(file)
            && lastCommit.getBlobs().get(file).equals(diskUid)
        ) { //任务2
                stage.addFiles.remove(file);
        } else {
            storeBlob(filePath);
            stage.addFiles.put(file, diskUid);
        }
        stage.rmFiles.remove(file);
        stage.writeStage();
    }
}
