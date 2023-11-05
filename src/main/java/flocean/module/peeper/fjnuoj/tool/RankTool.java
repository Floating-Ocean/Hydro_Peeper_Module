package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.DailyRankData;
import flocean.module.peeper.fjnuoj.data.RankingData;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class RankTool {

    /**
     * 爬取排行榜信息
     *
     * @return 排行榜信息
     * @throws Throwable 异常信息
     */
    public static List<RankingData> fetchData() throws Throwable {
        List<RankingData> rankingDataList = new ArrayList<>();
        Map<Integer, Boolean> uid = new HashMap<>(); //做一个uid重复的排除 (cookie 拥有者会一直出现在每页的顶部)
        int i = 1;
        while(true) {
            final String url = Global.config.ojUrl() + "ranking?page=" + (i ++);
            Document document = QuickUtils.wrapWithCookie(Jsoup.connect(url)).get();
            Elements dataContainer = document.getElementsByClass("data-table");
            if(dataContainer.isEmpty()) break;
            Element data = dataContainer.get(0).getElementsByTag("tbody").get(0);
            for (var each : data.getElementsByTag("tr")) {
                String userName = each.getElementsByClass("user-profile-name").get(0).text();
                String[] userIdData = each.getElementsByClass("user-profile-name").get(0).attr("href").split("/");
                int userId = Integer.parseInt(userIdData[userIdData.length - 1]);
                int acCount = Integer.parseInt(each.getElementsByClass("col--ac").get(0).text());
                String rank = each.getElementsByClass("col--rank").get(0).text();
                if (rank.equals("-") || uid.containsKey(userId)) continue;
                uid.put(userId, true);
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
