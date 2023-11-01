package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.cookie.SignedInCookie;
import flocean.module.peeper.fjnuoj.data.UserInfoData;
import flocean.module.peeper.fjnuoj.utils.QQInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfoTool {

    public static UserInfoData fetchData(int uid) throws Throwable {
        final String url = "https://fjnuacm.top/d/junior/user/" + uid;
        Document document = SignedInCookie.wrapWithCookie(Jsoup.connect(url)).get();

        Element profileHolder = document.getElementsByClass("profile-header__main").get(0);
        Element profileIcon = document.getElementsByClass("profile-header__content").get(0)
                .getElementsByClass("media__left").get(0)
                .getElementsByTag("img").get(0);
        Element contactHolder = document.getElementsByClass("profile-header__contact-bar").get(0);
        Element personalDescription = document.getElementsByClass("section__body").get(0);
        String userName = profileHolder.getElementsByTag("h1").get(0).text(),
                userStatus = profileHolder.getElementsByTag("p").get(0).text(),
                userProgress = profileHolder.getElementsByTag("p").get(1).text();

        String qq = "unknown", mail = "unknown";
        for (var each : contactHolder.getElementsByTag("a")) {
            if (each.attr("data-tooltip").equals("复制电子邮件")) {
                mail = new String(Base64.getDecoder().decode(each.attr("data-copy")), StandardCharsets.UTF_8);
            } else if (each.attr("data-tooltip").equals("复制QQ号")) {
                qq = new String(Base64.getDecoder().decode(each.attr("data-copy")), StandardCharsets.UTF_8);
            }
        }
        if (profileIcon.attr("src").contains("q1.qlogo.cn")) {
            Pattern pattern = Pattern.compile("nk=(\\d+)");
            Matcher matcher = pattern.matcher(profileIcon.attr("src"));
            if (matcher.find()) qq = matcher.group(1);
        }
        StringBuilder description = new StringBuilder();
        for (var each : personalDescription.getElementsByTag("p")) {
            if (!description.isEmpty()) description.append("\n");
            description.append(each.text());
        }

        String qqName = null;
        if(!qq.equals("unknown")) qqName = QQInfo.getUserName(qq);

        return new UserInfoData(userName, userStatus, userProgress, mail, qq, qqName, description.toString());
    }
}
