package flocean.module.peeper.fjnuoj.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import flocean.module.peeper.fjnuoj.Main;
import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.RankingData;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class QuickUtils {

    public static String getModulePath() {
        String path = System.getProperty("java.class.path");
        int pathBegin = path.lastIndexOf(System.getProperty("path.separator")) + 1;
        int pathEnd = path.lastIndexOf(File.separator) + 1;
        return path.substring(pathBegin, pathEnd);
    }

    public static Image getImageByName(String name) throws Throwable {
        URL imgURL = Main.class.getResource("/img/" + name + ".png");
        if(imgURL == null) throw new RunModuleException("resource not found");
        return ImageIO.read(imgURL);
    }

    public static Connection wrapWithCookie(Connection connection) {
        return connection
                .cookie("sid", Global.config.cookie().sid())
                .cookie("sid.sig", Global.config.cookie().sid_sig());
    }

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

    /**
     * 按照 pre + date(yyy_MM_dd) + . + suffix 的格式输出文件名
     *
     * @param pre    前缀
     * @param date   日期
     * @param suffix 后缀（拓展名）
     * @return 文件名
     */
    public static String generateFileName(String pre, Date date, String suffix) {
        return pre + "_" + new SimpleDateFormat("yyyy_MM_dd").format(date) + "." + suffix;
    }

    /**
     * 按照 pre + date(yyy_MM_dd) + _ + System.currentTimeMillis() + . + suffix 的格式输出文件名
     * 即附加一个当前时间戳，防止文件重名
     *
     * @param pre    前缀
     * @param date   日期
     * @param suffix 后缀（拓展名）
     * @return 文件名
     */
    public static String generateFileNameWithMills(String pre, Date date, String suffix) {
        return pre + "_" + new SimpleDateFormat("yyyy_MM_dd").format(date) + "_" + System.currentTimeMillis() + "." + suffix;
    }

    /**
     * 按照 "rank" + date(yyy_MM_dd) + . + suffix 的格式输出文件名
     *
     * @param date   日期
     * @param suffix 后缀（拓展名）
     * @return 文件名
     */
    public static String generateFileName(Date date, String suffix) {
        return generateFileName("rank", date, suffix);
    }

    public static void saveJsonData(Object data, String prefix) throws Throwable {
        saveJsonData(data, prefix, false);
    }

    /**
     * 以 json 格式保存今日凌晨排行榜数据
     *
     * @param data 包装后的排行榜数据
     * @throws Throwable 异常信息
     */
    public static void saveJsonData(Object data, String prefix, boolean force) throws Throwable {
        String path = Global.config.workPath() + "/data/" + QuickUtils.generateFileName(prefix, new Date(), "json");
        if (!force && new File(path).exists()) return; //不重复写入
        File file = QuickUtils.fetchFile(path);
        if (file == null || !file.delete() || !file.createNewFile()) {
            throw new RuntimeException("File saved unsuccessfully.");
        }
        JSON.writeTo(new FileOutputStream(file), data, JSONWriter.Feature.PrettyFormat);
    }

    public static <T> List<T> fetchJsonArrayData(String name, Class<T> tClass) throws Throwable {
        String path = Global.config.workPath() + "/data/" + name;
        File file = QuickUtils.fetchFile(path);
        if (file == null) throw new RunModuleException("file not found:" + path);
        String result = Files.readString(file.toPath());
        return JSON.parseArray(result, tClass);
    }

    public static <T> T fetchJsonData(String name, Class<T> tClass) throws Throwable {
        String path = Global.config.workPath() + "/data/" + name;
        File file = QuickUtils.fetchFile(path);
        if (file == null) throw new RunModuleException("file not found:" + path);
        String result = Files.readString(file.toPath());
        return JSON.parseObject(result, tClass);
    }

    public static String getQQName(String id) throws Throwable{
        String url = "https://api.usuuu.com/qq/" + id;
        Document document = Jsoup.connect(url).ignoreContentType(true).get();
        JSONObject json = JSON.parseObject(document.body().text());
        if(json.getInteger("code") != 200) throw new RunModuleException("Http response code" + json.getInteger("code"));
        return json.getJSONObject("data").getString("name");
    }

}
