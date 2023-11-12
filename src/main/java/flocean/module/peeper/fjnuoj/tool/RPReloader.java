package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.enums.VerdictType;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPReloader {

    /**
     * 调用 post 请求，运行 oj 的 rp reload 脚本（需要管理员权限）
     *
     * @throws Throwable 异常信息
     */
    public static void reload() throws Throwable {
        final String rpUrl = Global.config.ojUrl() + "manage/script";
        Document rpDocument = QuickUtils.wrapWithCookie(Jsoup.connect(rpUrl))
                .requestBody("{\"args\":\"\",\"id\":\"rp\"}")
                .header("Content-Type", "application/json")
                .post();
        Elements scripts = rpDocument.getElementsByTag("script");
        String scriptDetail = scripts.get(scripts.size() - 1).html();

        Pattern pattern = Pattern.compile("rid=([^&]*)\"");
        Matcher matcher = pattern.matcher(scriptDetail);

        if (!matcher.find()) return;
        String ridValue = matcher.group(1);
        System.out.println("RPReload run id: " + ridValue);

        String status = "Started";
        long startTime = System.currentTimeMillis(), lastQueryTime = 0;
        while (!status.equals(VerdictType.ACCEPTED.getName())) {
            long nowTime = System.currentTimeMillis();
            if (nowTime - startTime > 10 * 60 * 1000) { //保证只等待10秒
                throw new RunModuleException("Refresh RP failed after 10s' wait.");
            }
            if (nowTime - lastQueryTime < 1000) continue; //1秒检查一次
            lastQueryTime = nowTime;

            final String runUrl = Global.config.ojUrl() + "record/" + ridValue;
            Document runDocument = QuickUtils.wrapWithCookie(Jsoup.connect(runUrl)).get();
            Element runStatus = runDocument.getElementsByClass("record-status--text").get(0);

            status = runStatus.text();
            System.out.println("Now status: " + status);
        }
    }
}
