package flocean.module.peeper.fjnuoj.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.DailyRankData;
import flocean.module.peeper.fjnuoj.data.RankingData;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RankTool {

    /**
     * 爬取排行榜信息
     *
     * @return 排行榜信息
     * @throws Throwable 异常信息
     */
    public static List<RankingData> fetchData() throws Throwable {
        List<RankingData> rankingDataList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            final String url = Global.config.ojUrl() + "ranking?page=" + i;
            Document document = QuickUtils.wrapWithCookie(Jsoup.connect(url)).get();
            Element data = document.getElementsByClass("data-table").get(0)
                    .getElementsByTag("tbody").get(0);
            for (var each : data.getElementsByTag("tr")) {
                String userName = each.getElementsByClass("user-profile-name").get(0).text();
                String[] userIdData = each.getElementsByClass("user-profile-name").get(0).attr("href").split("/");
                int userId = Integer.parseInt(userIdData[userIdData.length - 1]);
                int acCount = Integer.parseInt(each.getElementsByClass("col--ac").get(0).text());
                String rank = each.getElementsByClass("col--rank").get(0).text();
                if (rank.equals("-")) continue;
                rankingDataList.add(new RankingData(userName, acCount, userId, Integer.parseInt(rank)));
            }
        }
        return rankingDataList;
    }

    /**
     * 读取昨日排行榜数据 json
     *
     * @return 包装后的昨日排行榜数据
     * @throws Throwable 异常信息
     */
    public static List<RankingData> fetchYesterdayData() throws Throwable {
        return QuickUtils.fetchJsonArrayData(
                QuickUtils.generateFileName(new Date(System.currentTimeMillis() - 86400 * 1000), "json"),
                RankingData.class);
    }

    /**
     * 读取今日凌晨的排行榜数据 json
     *
     * @return 包装后的今日凌晨的排行榜数据
     * @throws Throwable 异常信息
     */
    public static List<RankingData> fetchTodayData() throws Throwable {
        return QuickUtils.fetchJsonArrayData(
                QuickUtils.generateFileName(new Date(System.currentTimeMillis()), "json"),
                RankingData.class);
    }


    public static DailyRankData fetchDailyRankData() throws Throwable {
        try {
            return QuickUtils.fetchJsonData(
                    QuickUtils.generateFileName("daily", new Date(System.currentTimeMillis()), "json"),
                    DailyRankData.class);
        }catch(RunModuleException exception){
            return null;
        }
    }
}
