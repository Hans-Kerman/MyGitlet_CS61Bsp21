package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    /** git add和git rm变动的文件, 文件名 -> 哈希值 */
    TreeMap<String, String> addFiles = new TreeMap<>();
    TreeMap<String, String> rmFiles = new TreeMap<>();

    public TreeMap<String, String> getRmFiles() {
        return rmFiles;
    }

    public TreeMap<String, String> getAddFiles() {
        return addFiles;
    }

    /** 基础构造函数 */
    public Stage() {
        this.addFiles = new TreeMap<>();
        this.rmFiles = new TreeMap<>();
    }

    /** 从文件中读取Stage暂存区 */
    public static Stage getStage() {
        if (!Repository.Stage_path.exists()) {
            return new  Stage();
        }
        return readObject(Repository.Stage_path, Stage.class);
    }

    /** 写入暂存区 */
    public void writeStage() {
        writeObject(Repository.Stage_path, this);
    }
}
