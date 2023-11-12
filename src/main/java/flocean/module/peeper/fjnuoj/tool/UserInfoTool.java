package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.UserInfoData;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfoTool {

    /**
     * 爬取用户数据
     *
     * @param uid 指定用户的 uid
     * @return 用户数据
     * @throws Throwable 异常信息
     */
    public static UserInfoData fetchData(int uid) throws Throwable {
        final String url = Global.config.ojUrl() + "user/" + uid;
        Document document = QuickUtils.wrapWithCookie(Jsoup.connect(url)).get();

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
        //通过头像读取qq
        if (profileIcon.attr("src").contains("q1.qlogo.cn")) {
            Pattern pattern = Pattern.compile("nk=(\\d+)");
            Matcher matcher = pattern.matcher(profileIcon.attr("src"));
            if (matcher.find()) qq = matcher.group(1);
        }
        //通过个人信息读取邮箱和qq
        for (var each : contactHolder.getElementsByTag("a")) {
            if (each.attr("data-tooltip").equals("复制电子邮件")) {
                mail = new String(Base64.getDecoder().decode(each.attr("data-copy")), StandardCharsets.UTF_8);
            } else if (qq.equals("unknown") && each.attr("data-tooltip").equals("复制QQ号")) {
                qq = new String(Base64.getDecoder().decode(each.attr("data-copy")), StandardCharsets.UTF_8);
            }
        }
        //通过邮箱解析qq
        if (qq.equals("unknown") && mail.contains("@qq.com")){
            String possiblyQQ = mail.replace("@qq.com", "");
            if(possiblyQQ.chars().allMatch(Character::isDigit)) qq = possiblyQQ;
        }
        StringBuilder description = new StringBuilder();
        for (var each : personalDescription.getElementsByTag("p")) {
            if (!description.isEmpty()) description.append("\n");
            description.append(each.text());
        }

        String qqName = null;
        if(!qq.equals("unknown")) qqName = QuickUtils.getQQName(qq);

        return new UserInfoData(userName, userStatus, userProgress, mail, qq, qqName, description.toString());
    }
}
