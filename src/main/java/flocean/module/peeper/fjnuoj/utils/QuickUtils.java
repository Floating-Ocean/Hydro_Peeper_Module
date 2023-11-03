package flocean.module.peeper.fjnuoj.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import flocean.module.peeper.fjnuoj.Main;
import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.net.URL;

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

    public static String getQQName(String id) throws Throwable{
        String url = "https://api.usuuu.com/qq/" + id;
        Document document = Jsoup.connect(url).ignoreContentType(true).get();
        JSONObject json = JSON.parseObject(document.body().text());
        if(json.getInteger("code") != 200) throw new RunModuleException("Http response code" + json.getInteger("code"));
        return json.getJSONObject("data").getString("name");
    }

}
