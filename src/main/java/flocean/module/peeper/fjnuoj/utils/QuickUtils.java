package flocean.module.peeper.fjnuoj.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import flocean.module.peeper.fjnuoj.Main;
import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
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

    /**
     * 替换路径为正确的分隔符，防止在跨平台时出问题
     *
     * @return 校正后的路径
     */
    public static String decodePath(String path) {
        return path.replace('\\', File.separatorChar)
                .replace('/', File.separatorChar);
    }

    /**
     * 获取当前 jar 包工作路径，即 java -jar 工作路径/xxx.jar
     *
     * @return 路径
     */
    public static String getModulePath() {
        String path = System.getProperty("java.class.path");
        int pathBegin = path.lastIndexOf(System.getProperty("path.separator")) + 1;
        int pathEnd = path.lastIndexOf(File.separator) + 1;
        return path.substring(pathBegin, pathEnd);
    }

    /**
     * 根据名称读取 jar 中的图片
     *
     * @param name 名称
     * @return 对应的图片
     * @throws Throwable 异常信息
     */
    public static Image getImageByName(String name) throws Throwable {
        URL imgURL = Main.class.getResource("/img/" + name + ".png");
        if (imgURL == null) throw new RunModuleException("resource not found");
        return ImageIO.read(imgURL);
    }

    /**
     * 将 Jsoup 的连接附带上 Cookie
     *
     * @param connection 需要附带 Cookie 的连接
     * @return 操作之后的连接
     */
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
        File file = new File(decodePath(path));
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

    /**
     * 以 json 格式保存数据，出现源文件存在时默认不覆盖文件
     *
     * @param data   需要保存的数据
     * @param prefix 文件前缀
     * @throws Throwable 异常信息
     */
    public static void saveJsonData(Object data, String prefix) throws Throwable {
        saveJsonData(data, prefix, false);
    }

    /**
     * 以 json 格式保存数据
     *
     * @param data   需要保存的数据
     * @param prefix 文件前缀
     * @param force  源文件存在时是否覆盖
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

    /**
     * 读取 Json Array
     *
     * @param name   文件名
     * @param tClass 目标 Array 的 Class
     * @param <T>    目标 Array 的类型
     * @return 目标 Array
     * @throws Throwable 异常信息
     */
    public static <T> List<T> fetchJsonArrayData(String name, Class<T> tClass) throws Throwable {
        String path = Global.config.workPath() + "/data/" + name;
        File file = QuickUtils.fetchFile(path);
        if (file == null) throw new RunModuleException("file not found:" + path);
        String result = Files.readString(file.toPath());
        return JSON.parseArray(result, tClass);
    }

    /**
     * 读取 Json Object
     *
     * @param name   文件名
     * @param tClass 目标 Object 的 Class
     * @param <T>    目标 Object 的类型
     * @return 目标 Object
     * @throws Throwable 异常信息
     */
    public static <T> T fetchJsonData(String name, Class<T> tClass) throws Throwable {
        String path = Global.config.workPath() + "/data/" + name;
        File file = QuickUtils.fetchFile(path);
        if (file == null) throw new RunModuleException("file not found:" + path);
        String result = Files.readString(file.toPath());
        return JSON.parseObject(result, tClass);
    }

    /**
     * 调用 api 读取 qq名
     *
     * @param id qq号
     * @return qq名
     * @throws Throwable 异常信息
     */
    public static String getQQName(String id) throws Throwable {
        String url = "https://api.usuuu.com/qq/" + id;
        Document document = Jsoup.connect(url).ignoreContentType(true).get();
        JSONObject json = JSON.parseObject(document.body().text());
        if (json.getInteger("code") != 200)
            throw new RunModuleException("Http response code" + json.getInteger("code"));
        return json.getJSONObject("data").getString("name");
    }

    /**
     * 调用 api 检查指定 url 的连通性
     *
     * @param checkUrl 需要检查的 url
     * @return 连通状态. -1: api异常, 0: 活着, 1: 寄了
     * @throws Throwable 异常信息
     */
    public static int checkAlive(String checkUrl) throws Throwable {
        try {
            String url = "https://api.uptimerobot.com/v2/getMonitors";
            Document document = Jsoup.connect(url)
                    .requestBody("{\"api_key\":\"" + Global.config.uptime_apikey() + "\"}")
                    .header("Content-Type", "application/json")
                    .ignoreContentType(true)
                    .post();
            JSONObject json = JSON.parseObject(document.body().text());
            if (!json.getString("stat").equals("ok")) {
                return -1; //api寄了
            }
            JSONArray monitors = json.getJSONArray("monitors");
            JSONObject ojMonitor = null;
            for (int i = 0; i < monitors.size(); i++) {
                JSONObject currentMonitor = monitors.getJSONObject(i);
                if (currentMonitor.getString("url").equals(checkUrl)) {
                    ojMonitor = currentMonitor;
                    break;
                }
            }
            if (ojMonitor == null) return -1;
            return ojMonitor.getIntValue("status") == 2 ? 1 : 0;
        } catch (HttpStatusException exception) {
            return -1;
        }
    }

}
