package gitlet;


import java.io.File;
import java.util.*;

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
    //public static String HEAD_branch = readContentsAsString(HEAD_path);
    public static String getHeadBranchName() {      //取出来的HEAD信息(branch名字的String)
        return readContentsAsString(HEAD_path);
    }

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
    public static String storeBlob(File f) {
        String hash = gitSHA1(f);
        writeContents(join(BLOBS_DIR, hash), (Object) readContents(f));
        return hash;
    }
    public static String storeCommit(Commit commit) {
        String hash = gitSHA1(commit);
        writeObject(join(COMMITS_DIR, hash), commit);
        return hash;
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
        File filePath = join(COMMITS_DIR, hash);
        if (!filePath.exists()) {
            throw error("No commit with that id exists.");
        } else {
            return readObject(filePath, Commit.class);
        }
    }
    /** 工具函数, 返回指定branch的最后一个Commit对象 */
    public static Commit getCommitByBranch(String branch) {
        File branchPath = join(BRANCH_DIR, branch);
        if (!branchPath.exists()) {
            throw error("No such branch exists.");
        }
        String hash = readContentsAsString(branchPath);
        return getCommit(hash);
    }
    /** 工具函数, 返回最后一个(HEAD所在的)Commit对象 */
    public static Commit getLastCommit() {
        return getCommitByBranch(getHeadBranchName());
    }
    /** 工具函数, 返回最后一个(HEAD指向的)CommitHash */
    public static String getHEADCommitHash() {
        return readContentsAsString(join(BRANCH_DIR, getHeadBranchName()));
    }
    public static Commit getCommitByFuzzyHash(String hash) {
        if (hash.length() == 40) {
            return getCommit(hash);
        } else {
            List<String> commitsList = plainFilenamesIn(COMMITS_DIR);
            if (commitsList == null) {
                throw error("No commit with that id exists.");
            }
            String result = commitsList.stream()
                    .filter(s -> s.startsWith(hash))  // 严格前缀匹配
                    .findFirst()                         // 找到第一个
                    .orElse(null);                       // 没有则返回 null（或默认值）
            if (result == null) {
                throw error("No commit with that id exists.");
            }
            return getCommit(result);
        }
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
            stage.addFiles.put(file, diskUid);  //任务1
        }
        stage.rmFiles.remove(file);     //任务3
        stage.writeStage();
    }

    /** commit
     * 0. 基础错误处理：stage为空
     * 1. 完全根据stage“更新”blob映射(深拷贝)
     * 2. 使用新blob映射、时间、message、原HEAD构建新Commit
     * 3. Commit、stage文件落盘
     * 4. 更新HEAD信息
     */
    public static void commitToGitlet(String message) {
        Stage stage = Stage.getStage();
        if (stage.rmFiles.isEmpty() && stage.addFiles.isEmpty()) {
            throw error("No changes added to the commit.");
        }

        Commit lastCommit = getLastCommit();
        ArrayList<String> parents = new ArrayList<>();
        TreeMap<String, String> blobs = new TreeMap<>(lastCommit.getBlobs());
        parents.add(readContentsAsString(join(BRANCH_DIR, getHeadBranchName())));
        for (Map.Entry<String, String> addBlob : stage.addFiles.entrySet()) {
            String fileName = addBlob.getKey();
            String hash = addBlob.getValue();
            blobs.put(fileName, hash);
        }
        for (String rmFile : stage.rmFiles) {
            blobs.remove(rmFile);
        }

        Commit thisCommit = new Commit(
                message,
                parents,
                blobs
        );

        String commitHash = storeCommit(thisCommit);
        stage.clearStage();

        writeContents(join(BRANCH_DIR, getHeadBranchName()), commitHash);
    }

    /** rm
     * 1. 如果文件被暂存准备添加(git add)，则取消暂存
     * 2. 如果文件在**当前**commit中，则将它移除，并且从磁盘上删除
     * 3. 未被暂存或提交则报错
     */
    public static void removeFile(String file) {
        Stage stage = Stage.getStage();
        Commit lastCommit = getLastCommit();

        if (!stage.addFiles.containsKey(file) && !lastCommit.getBlobs().containsKey(file)) {
            throw error("No reason to remove the file.");   //任务3
        }

        stage.addFiles.remove(file);    //任务1
        if (lastCommit.getBlobs().containsKey(file)) {
            stage.rmFiles.add(file);    //任务2
            restrictedDelete(file);
        }
        stage.writeStage();
    }

    /** log
     *  从新到旧打印Commit历史，沿着第一个parent回溯
     */
    private static void printCommitLog(Commit commit, String hash) {
        List<String> parents = commit.getParentCommits();
        System.out.println("===");
        System.out.println("commit " + hash);
        if (commit.getParentCommits().size() == 2) {
            System.out.printf("Merge: %s %s\n", parents.get(0).substring(0, 7), parents.get(1).substring(0, 7));
        }
        System.out.println("Date: " + commit.getTimestampStr());
        System.out.println(commit.getMessage());
        System.out.println();
    }
    public static void printLog() {
        String curHash = getHEADCommitHash();
        Commit curCommit = getLastCommit();
        while (true) {
            printCommitLog(curCommit, curHash);
            if (curCommit.getParentCommits().isEmpty()) {
                break;
            }
            curHash = curCommit.getParentCommits().get(0);
            curCommit = getCommit(curHash);
        }
    }

    /** global-log
     *  无序打印Commit历史信息
     */
    public static void printGlobalLog() {
        List<String> commitLists = plainFilenamesIn(COMMITS_DIR);
        if (commitLists != null) {
            for (String hash : commitLists) {
                printCommitLog(getCommit(hash), hash);
            }
        }
    }

    /** find
     *  查找message并打印完全一致的commit
     */
    public static void findMatchMessage(String inputMessage) {
        List<String> commitLists = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        if (commitLists != null) {
            for (String hash : commitLists) {
                String message = getCommit(hash).getMessage();
                if (message.equals(inputMessage)) {
                    found = true;
                    System.out.println(hash);
                }
            }
        }
        if (!found) {
            throw error("Found no commit with that message.");
        }
    }

    /** status
     * 完成gitlet status，输出各种状态信息
     * 已修改但未暂存(下列任选)：
     *      1.在当前提交中追踪、在工作区更改、没被暂存添加
     *      2.已暂存添加，工作区与暂存添加区版本不同
     *      3.已暂存添加，但是工作区中删除
     *      4.没被暂存删除，但是被跟踪，并且在工作区删除
     * 未追踪(下列任选)：
     *      1.在工作区，不被跟踪 且 不被暂存添加
     *      2.在工作区，(不管是否被跟踪，)但是在暂存删除
     */
    private static String isModificationsNotStagedForCommit(
            String fileName,
            Commit commit,
            Stage stage
    ) {
        Set<String> commitTrackedFiles = commit.getBlobs().keySet();
        File filePath = join(CWD, fileName);
        if (filePath.exists()) {
            if (commitTrackedFiles.contains(fileName) &&
                    !gitSHA1(filePath).equals(commit.getBlobs().get(fileName)) &&
                    !stage.addFiles.containsKey(fileName)
            ) {
                return " (modified)";
            }
            if (!gitSHA1(filePath).equals(stage.addFiles.get(fileName)) &&
                    stage.addFiles.containsKey(fileName)) {
                return " (modified)";
            }
        } else {
            if (stage.addFiles.containsKey(fileName)) {
                return " (deleted)";
            }
            if (!stage.rmFiles.contains(fileName) && commitTrackedFiles.contains(fileName)) {
                return " (deleted)";
            }
        }
        return "";
    }
    public static void printStatus() {
        Stage stage = Stage.getStage();
        Commit commit = getLastCommit();
        Set<String> commitTrackedFiles = commit.getBlobs().keySet();
        Set<String> CWDFiles;
        if (plainFilenamesIn(CWD) == null) {
            CWDFiles = new TreeSet<>();
        } else {
            CWDFiles = new TreeSet<>(Objects.requireNonNull(plainFilenamesIn(CWD)));
        }
        Set<String> allFiles = new TreeSet<>(stage.addFiles.keySet());
        allFiles.addAll(commit.getBlobs().keySet());
        allFiles.addAll(CWDFiles);

        /* branches */
        System.out.println("=== Branches ===");
        String headBranchName = getHeadBranchName();
        TreeSet<String> branches = new TreeSet<>(Objects.requireNonNull(plainFilenamesIn(BRANCH_DIR)));
        for (String branchName : branches) {
            if (branchName.equals(headBranchName)) {
                System.out.println("*"+branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();

        /* Staged Files */
        System.out.println("=== Staged Files ===");
        for (String fileName : new TreeSet<>(stage.addFiles.keySet())) {
            System.out.println(fileName);
        }
        System.out.println();

        /* Removed Files */
        System.out.println("=== Removed Files ===");
        for (String fileName : stage.rmFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        /* 已修改未暂存 */
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : allFiles) {
            String op = isModificationsNotStagedForCommit(fileName, commit, stage);
            if (!op.isEmpty()) {
                System.out.println(fileName+op);
            }
        }
        System.out.println();

        /* 未跟踪 */
        System.out.println("=== Untracked Files ===");
        for (String fileName : CWDFiles) {
            if (!commitTrackedFiles.contains(fileName) &&
                !stage.addFiles.containsKey(fileName)
            ) {
                System.out.println(fileName);
            }
            if (commitTrackedFiles.contains(fileName) &&
                stage.rmFiles.contains(fileName))
            {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    // chekout共有三种用法

    /** 将文件在HEAD中的存档恢复到工作区
     */
    public static void checkoutOneHeadFile(String fileName) {
        checkoutOneCommitFile(getHEADCommitHash(), fileName);
    }

    /** 将文件在指定Commit的存档恢复到工作区
     */
    public static void checkoutOneCommitFile(String commitHash, String fileName) {
        Commit commit = getCommitByFuzzyHash(commitHash);
        Set<String> commitTrackedFiles = commit.getBlobs().keySet();
        if (!commitTrackedFiles.contains(fileName)) {
            throw error("File does not exist in that commit.");
        }
        writeContents(join(CWD, fileName), (Object) readContents(join(BLOBS_DIR, commit.getBlobs().get(fileName))));
    }

    /** 将给定分支的所有文件恢复，并且切换HEAD为它
     *  当前分支跟踪但是没在给定分支的文件将被删除，当前分支未跟踪的不能删除
     *  暂存区清空，除非checkout的就是当前分支(HEAD)
     */
    public static void checkoutWholeBranch(String branchName) {
        Commit checkoutCommit = getCommitByBranch(branchName);      //分支不存在会在get函数中正确抛出异常
        Set<String> checkoutCommitTrackedFiles = checkoutCommit.getBlobs().keySet();
        Commit HEADCommit = getLastCommit();
        Set<String> HEADCommitTrackedFiles = HEADCommit.getBlobs().keySet();
        Stage stage = Stage.getStage();

        if (getHeadBranchName().equals(branchName)) {
            throw error("No need to checkout the current branch.");
        }
        if (plainFilenamesIn(CWD) != null) {
            for (String fileName : Objects.requireNonNull(plainFilenamesIn(CWD))) {
                if (    //untracked条件与status统一
                        !HEADCommitTrackedFiles.contains(fileName) && !stage.addFiles.containsKey(fileName) ||  //untracked1
                        HEADCommitTrackedFiles.contains(fileName) && stage.rmFiles.contains(fileName)           //untracked2
                ) {
                    if (checkoutCommitTrackedFiles.contains(fileName)){
                        throw error("There is an untracked file in the way; delete it, or add and commit it first.");
                    }
                }
            }
        }

        for (String fileName : checkoutCommitTrackedFiles) {    //恢复文件
            writeContents(
                    join(CWD, fileName),
                    (Object) readContents(join(BLOBS_DIR, checkoutCommit.getBlobs().get(fileName)))
            );
        }

        for (String fileName : HEADCommit.getBlobs().keySet()) {    //删除文件
            if (!checkoutCommit.getBlobs().containsKey(fileName) && join(CWD, fileName).exists()) {
                restrictedDelete(fileName);
            }
        }

        stage.clearStage();
        stage.writeStage();
        writeContents(HEAD_path, branchName);
    }

    /** branch
     *  创建给定名字的新分支
     *  不要更改HEAD指针
     */
    public static void makeBranch(String branchName) {
        String HEADCommitHash = getHEADCommitHash();
        File newBranch = join(BRANCH_DIR, branchName);
        if (newBranch.exists()) {
            throw error("A branch with that name already exists.");
        }
        writeContents(newBranch, HEADCommitHash);
    }

    /** rm-branch
     *  使用名字删除已经存在的分支(删除分支指针)
     *  不会修改任何历史Commit
     */
    public static void removeBranch(String branchName) {
        File branchPath = join(BRANCH_DIR, branchName);
        if (!branchPath.exists()) {
            throw error("A branch with that name does not exist.");
        }
        String HEADBranchName = getHeadBranchName();
        if (branchName.equals(HEADBranchName)) {
            throw error("Cannot remove the current branch.");
        }
        restrictedDelete(branchPath);
    }
}
