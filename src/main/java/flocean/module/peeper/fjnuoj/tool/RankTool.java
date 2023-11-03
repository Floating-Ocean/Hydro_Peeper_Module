package flocean.module.peeper.fjnuoj.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import flocean.module.peeper.fjnuoj.config.Global;
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
     * 以 json 格式保存今日凌晨排行榜数据
     *
     * @param data 包装后的排行榜数据
     * @throws Throwable 异常信息
     */
    public static void updateTodayData(List<RankingData> data) throws Throwable {
        if (new File(Global.config.workPath() + "/data/" + generateFileName(new Date(), "json")).exists()) return; //不重复写入
        File file = QuickUtils.fetchFile(Global.config.workPath() + "/data/" + generateFileName(new Date(), "json"));
        if (file == null || !file.delete() || !file.createNewFile()) {
            throw new RuntimeException("File saved unsuccessfully.");
        }
        JSON.writeTo(new FileOutputStream(file), data, JSONWriter.Feature.PrettyFormat);
    }

    /**
     * 读取昨日排行榜数据 json
     *
     * @return 包装后的昨日排行榜数据
     * @throws Throwable 异常信息
     */
    public static List<RankingData> fetchYesterdayData() throws Throwable {
        String path = Global.config.workPath() + "/data/" + generateFileName(new Date(System.currentTimeMillis() - 86400 * 1000), "json");
        File file = QuickUtils.fetchFile(path);
        if (file == null) throw new RunModuleException("file not found:" + path);
        String result = Files.readString(file.toPath());
        return JSONArray.parseArray(result, RankingData.class);
    }

    /**
     * 读取今日凌晨的排行榜数据 json
     *
     * @return 包装后的今日凌晨的排行榜数据
     * @throws Throwable 异常信息
     */
    public static List<RankingData> fetchTodayData() throws Throwable {
        String path = Global.config.workPath() + "/data/" + generateFileName(new Date(System.currentTimeMillis()), "json");
        File file = QuickUtils.fetchFile(path);
        if (file == null) throw new RunModuleException("file not found:" + path);
        String result = Files.readString(file.toPath());
        return JSONArray.parseArray(result, RankingData.class);
    }
}
