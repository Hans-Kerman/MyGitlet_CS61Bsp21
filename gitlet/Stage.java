package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    /** git add和git rm变动的文件, 文件名 -> 哈希值 */
    private TreeMap<String, String> addFiles = new TreeMap<>();
    private TreeMap<String, String> rmFiles = new TreeMap<>();

    public TreeMap<String, String> getRmFiles() {
        return rmFiles;
    }

    public void setRmFiles(TreeMap<String, String> rmFiles) {
        this.rmFiles = rmFiles;
    }

    public TreeMap<String, String> getAddFiles() {
        return addFiles;
    }

    public void setAddFiles(TreeMap<String, String> addFiles) {
        this.addFiles = addFiles;
    }

    public static Stage stage() {
        return readObject(Repository.Stage_path, Stage.class);
    }

    public void writeStage() {
        writeObject(Repository.Stage_path, this);
    }
}
