package flocean.module.peeper.fjnuoj.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class QQInfo {
    public static String getUserName(String id) throws Throwable{
        String url = "https://api.usuuu.com/qq/" + id;
        Document document = Jsoup.connect(url).ignoreContentType(true).get();
        JSONObject json = JSON.parseObject(document.body().text());
        if(json.getInteger("code") != 200) throw new RunModuleException("Http response code" + json.getInteger("code"));
        return json.getJSONObject("data").getString("name");
    }
}
