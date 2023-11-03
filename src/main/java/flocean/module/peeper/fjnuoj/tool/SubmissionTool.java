package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.CounterData;
import flocean.module.peeper.fjnuoj.data.SimpleRankableRecord;
import flocean.module.peeper.fjnuoj.data.SubmissionData;
import flocean.module.peeper.fjnuoj.data.UserData;
import flocean.module.peeper.fjnuoj.enums.VerdictType;
import flocean.module.peeper.fjnuoj.utils.Pair;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.*;

public class SubmissionTool {

    /**
     * 爬取昨天的提交信息
     *
     * @return 提交信息
     * @throws Throwable 异常信息
     */
    public static List<SubmissionData> fetchData() throws Throwable {
        return fetchData(true);
    }

    /**
     * 爬取今天或昨天的提交信息
     *
     * @param isYesterday 是否选择爬取昨天的提交信息
     *                    true: 昨天的
     *                    false: 今天的
     * @return 提交信息
     * @throws Throwable 异常信息
     */
    public static List<SubmissionData> fetchData(boolean isYesterday) throws Throwable {
        List<SubmissionData> submissionDataList = new ArrayList<>();
        int i = 1;
        long[] yesterdayTime = new long[2];
        getYesterdayTime(yesterdayTime);
        if (!isYesterday) {
            yesterdayTime[0] += 86400;
            yesterdayTime[1] += 86400;
        }
        while (true) {
            final String url = Global.config.ojUrl() + "record?page=" + (i++);
            Document document = QuickUtils.wrapWithCookie(Jsoup.connect(url)).get();
            Element data = document.getElementsByClass("data-table").get(0)
                    .getElementsByTag("tbody").get(0);
            boolean eof = false; //是否读到了当天的第一条数据 (end of file)
            for (var each : data.getElementsByTag("tr")) {
                Element status = each.getElementsByClass("col--status__text").get(0);
                Element problem = each.getElementsByClass("col--problem").get(0);
                Element who = each.getElementsByClass("col--submit-by").get(0)
                        .getElementsByTag("a").get(0);
                Element time = each.getElementsByClass("time relative").get(0);
                int score = Integer.parseInt(status.getElementsByTag("span").text());
                String verdict = status.getElementsByTag("a").text();
                verdict = verdict.substring(verdict.split(" ")[0].length() + 1); //去掉分数
                String problemName = problem.getElementsByTag("a").text();
                String submitterName = who.text();
                String[] submitterIdData = who.attr("href").split("/");
                int submitterId = Integer.parseInt(submitterIdData[submitterIdData.length - 1]);
                long at = Long.parseLong(time.attr("data-timestamp"));
                if (at > yesterdayTime[1]) continue;
                if (at < yesterdayTime[0]) {
                    eof = true;
                    break;
                }
                submissionDataList.add(new SubmissionData(new UserData(submitterName, submitterId), score, VerdictType.searchVerdict(verdict), problemName, at));
            }
            if (eof) break;
        }
        return submissionDataList;
    }

    /**
     * 分类提交信息，得到分类型评测榜单
     *
     * @param submissionDataList 总提交信息
     * @return 分类型评测榜单 <<平均分, 通过率>, <类型, 数量>>
     */
    public static Pair<Pair<Double, Double>, Map<VerdictType, Integer>> classifyVerdict(List<SubmissionData> submissionDataList) {
        Map<VerdictType, Integer> verdicts = new HashMap<>();
        int scoreSum = 0, acSum = 0;
        for (var each : submissionDataList) {
            int pre = verdicts.getOrDefault(each.verdictType(), 0);
            verdicts.put(each.verdictType(), pre + 1);
            scoreSum += each.score();
            if(each.verdictType() == VerdictType.ACCEPTED) acSum ++;
        }
        return Pair.of(Pair.of((double) scoreSum / submissionDataList.size(), (double) acSum * 100 / submissionDataList.size()), verdicts);
    }


    /**
     * 获取指定类型的评测榜单
     *
     * @param submissionDataList 包装后的评测榜单
     * @param type               类型
     * @return 指定类型的评测榜单 <<指定类型的提交总数, 提交总数>, 用户>
     */
    public static Pair<Pair<Integer, Integer>, List<SimpleRankableRecord>> fetchVerdictRank(List<SubmissionData> submissionDataList, VerdictType type) {
        Map<UserData, Integer> topCount = new HashMap<>();
        int count = 0;
        for (var each : submissionDataList) {
            if (each.verdictType() != type) continue;
            count++;
            int pre = topCount.getOrDefault(each.user(), 0);
            topCount.put(each.user(), pre + 1);
        }
        List<SimpleRankableRecord> result = new ArrayList<>();
        for (var each : topCount.keySet()) {
            result.add(new SimpleRankableRecord(each, topCount.get(each)));
        }
        result.sort(Comparator.comparingInt(o -> -o.count()));
        return Pair.of(Pair.of(count, submissionDataList.size()), result);
    }

    /**
     * 查找最受欢迎的问题
     *
     * @param submissionDataList 包装后的评测榜单
     * @return 最受欢迎的问题 <问题名, 提交总数>
     */
    public static CounterData findMostPopularProblem(List<SubmissionData> submissionDataList) {
        Map<String, Set<UserData>> submissionMap = new HashMap<>();
        for (var each : submissionDataList) {
            submissionMap.computeIfAbsent(each.problemName(), k -> new HashSet<>());
            submissionMap.get(each.problemName()).add(each.user());
        }
        List<Pair<String, Set<UserData>>> problemList = new ArrayList<>();
        for (var each : submissionMap.keySet()) {
            problemList.add(Pair.of(each, submissionMap.get(each)));
        }
        problemList.sort(Comparator.comparingInt(o -> -o.B.size()));
        return new CounterData(problemList.get(0).A, problemList.get(0).B.size());
    }

    /**
     * 获取昨天的时间刻
     *
     * @param yesterdayTime 目标数组
     */
    private static void getYesterdayTime(long[] yesterdayTime) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.set(Calendar.HOUR_OF_DAY, 0);
        currentTime.set(Calendar.MINUTE, 0);
        currentTime.set(Calendar.SECOND, 0);
        yesterdayTime[0] = currentTime.getTimeInMillis() / 1000 - 86400;
        currentTime.set(Calendar.HOUR_OF_DAY, 23);
        currentTime.set(Calendar.MINUTE, 59);
        currentTime.set(Calendar.SECOND, 59);
        yesterdayTime[1] = currentTime.getTimeInMillis() / 1000 - 86400;
    }

    /**
     * 获取当天第一份ac提交
     *
     * @param submissionData 包装后的评测榜单
     * @return 第一份ac提交的信息 <提交时间, <用户名, 题目名>>
     */
    public static Pair<Long, Pair<UserData, String>> getFirstACAttempt(List<SubmissionData> submissionData) {
        for (int i = submissionData.size() - 1; i >= 0; i--) {
            SubmissionData nowData = submissionData.get(i);
            if (nowData.verdictType() != VerdictType.ACCEPTED) continue;
            return Pair.of(nowData.at(), Pair.of(nowData.user(), nowData.problemName()));
        }
        return null;
    }

}