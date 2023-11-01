package flocean.module.peeper.fjnuoj.utils;

import java.io.File;

public class ModuleFile {
    public static final String path = "/home/floatingcean/fjnuacm_rank/lib";
//    public static final String path = "D:\\root\\storage\\emulated\\999\\FJNU_ACM rank";


    /**
     * 保证文本存在的条件下，读取 file
     *
     * @param path 文件路径
     * @return file
     * @throws Throwable 异常信息
     */
    public static File fetchFile(String path) throws Throwable {
        File file = new File(path);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) return null;
        if (!file.exists() && !file.createNewFile()) return null;
        return file;
    }
}
