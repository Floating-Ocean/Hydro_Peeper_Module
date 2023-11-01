package flocean.module.peeper.fjnuoj.utils;

import com.alibaba.fastjson2.JSON;
import flocean.module.peeper.fjnuoj.cookie.SignedInCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class QQInfo {
    public static String getUserName(String id) throws Throwable{
        String url = "https://users.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + id;
        Document document = SignedInCookie.wrapWithCookie(Jsoup.connect(url)).get();
        String jsonString = document.body().text();

        // 去除 "portraitCallBack(" 和 ")" 部分，只保留 JSON 字符串
        String json = jsonString.substring("portraitCallBack(".length(), jsonString.length() - 1);

        return JSON.parseObject(json).getJSONArray(id).getString(6);
    }
}
