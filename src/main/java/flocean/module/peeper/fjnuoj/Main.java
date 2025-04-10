package flocean.module.peeper.fjnuoj;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.*;
import flocean.module.peeper.fjnuoj.data.rank.SimpleRankItem;
import flocean.module.peeper.fjnuoj.enums.VerdictType;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.tool.*;
import flocean.module.peeper.fjnuoj.utils.CrashHandler;
import flocean.module.peeper.fjnuoj.utils.Pair;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.apache.commons.text.similarity.FuzzyScore;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static flocean.module.peeper.fjnuoj.tool.ImgGenerator.*;

public class Main {

    public static void main(String[] args) {
        try {
            saveTextLocally("ok", Global.config.workPath() + "/bug/out.txt");
        } catch (Throwable e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            if (args.length > 0) {
                if (args.length == 1 && args[0].equals("/necessity")) {
                    System.out.println(checkNecessity() ? "needed" : "useless");
                } else if (args.length == 2 && args[0].equals("/version")) {
                    System.out.println(Global.buildInfo);
                    saveTextLocally(Global.buildInfo, args[1]);
                } else if (args.length == 2 && args[0].equals("/alive")) {
                    checkAlive(args[1]);
                } else if (args.length == 1 && args[0].equals("/update")) generateFullRank(null, null);
                else if (args.length == 3 && args[0].equals("/full")) generateFullRank(args[1], args[2]);
                else if (args.length == 3 && args[0].equals("/now")) generateNowRank(false, args[1], args[2]);
                else if (args.length == 4 && args[0].equals("/now"))
                    generateNowRank(args[1].equals("full"), args[2], args[3]);
                else if (args.length == 4 && args[0].equals("/verdict")) generateVerdict(args[1], args[2], args[3]);
                else if (args.length == 4 && args[0].equals("/user")) {
                    if (args[1].equals("id")) generateUserInfo(Integer.parseInt(args[2]), args[3]);
                    else if (args[1].equals("name")) fuzzyMatchUser(args[2], args[3]);
                    else throw new RunModuleException("Operation Not Supported.");
                } else printErrorParameter();
            } else printErrorParameter();
        } catch (Throwable e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            try {
                saveTextLocally(CrashHandler.handleError(e), Global.config.workPath() + "/bug/out.txt");
            } catch (Throwable ee) {
                Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }


    /**
     * 当输入的参数有问题时，输出提示信息
     */
    private static void printErrorParameter() {
        System.out.println("""
                Input a valid parameter, such as: /full, /now, /verdict wa. And input a file path below.
                If you want to get a plain text, put /plain at the beginning.
                For debug, input /version to get version info, input /necessity to check if we need to regenerate daily rank list.
                """);
        throw new RunModuleException("input parameter invalid.");
    }

    /**
     * 检查 Online Judge 是否存活
     *
     * @param plainPath 输出路径
     * @throws Throwable 异常信息
     */
    private static void checkAlive(String plainPath) throws Throwable {
        int[] status = {
                QuickUtils.checkAlive("https://fjnuacm.top"),
                QuickUtils.checkAlive("https://codeforces.com"),
                QuickUtils.checkAlive("https://atcoder.jp")
        };
        StringBuilder plainResult = new StringBuilder();
        if (status[0] == -1 || status[1] == -1 || status[2] == -1) plainResult.append("Api 调用异常");
        else if (status[0] * status[1] * status[2] == 1) plainResult.append("所有服务均正常");
        else plainResult.append("部分服务存在异常");
        plainResult.append("\n");
        String[] tags = {"Fjnuacm OJ", "Codeforces", "AtCoder"};
        for (int i = 0; i < 3; i++) {
            plainResult.append("\n");
            plainResult.append("[").append(tags[i]).append("] ");
            if (status[i] == -1) plainResult.append("Api 异常");
            else if (status[i] == 1) plainResult.append("正常");
            else plainResult.append("异常");
        }
        saveTextLocally(plainResult.toString(), plainPath);

        System.out.println(plainResult);
    }


    /**
     * 检查是否需要重新生成每日榜单
     *
     * @return 必要性
     */
    private static boolean checkNecessity() throws Throwable {
        return RankTool.fetchDailyRankData() == null;
    }


    /**
     * 对用户名进行模糊匹配
     *
     * @param userName  目标用户名
     * @param plainPath 输出路径
     * @throws Throwable 异常信息
     */
    private static void fuzzyMatchUser(String userName, String plainPath) throws Throwable {

        //Step1. 爬取用户列表
        System.out.println("\n正在爬取排行榜数据");
        List<RankingData> rankingDataList = RankTool.fetchData();

        System.out.print("\n处理数据");
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.getDefault());
        List<Pair<UserData, Double>> matches = new ArrayList<>();
        for (var each : rankingDataList) {
            //解析包含别名的用户名，分开匹配提高准度
            Pattern pattern = Pattern.compile("(.*?) \\((.*?)\\)");
            Matcher matcher = pattern.matcher(each.user());
            if (matcher.matches()) {
                for (int i = 1; i <= 2; i++) {
                    double currentScore = fuzzyScore.fuzzyScore(userName, matcher.group(i));
                    if (currentScore >= 0.6) { //0.6的阈值
                        matches.add(Pair.of(each.packUser(), currentScore));
                    }
                }
            } else {
                double currentScore = fuzzyScore.fuzzyScore(userName, each.user());
                if (currentScore >= 0.6) { //0.6的阈值
                    matches.add(Pair.of(each.packUser(), currentScore));
                }
            }
        }
        matches.sort(Comparator.comparingDouble(o -> -o.B));
        System.out.println("， 完成");

        if (matches.isEmpty()) {
            saveTextLocally("mismatch", plainPath);
        } else {
            JSON.writeTo(new FileOutputStream(plainPath), matches.get(0).A, JSONWriter.Feature.PrettyFormat);
        }
    }


    /**
     * 生成用户信息
     *
     * @param uid       用户 uid
     * @param plainPath 输出路径
     * @throws Throwable 异常信息
     */
    private static void generateUserInfo(int uid, String plainPath) throws Throwable {

        //Step1. 爬取用户信息
        System.out.println("\n正在爬取用户信息");
        UserInfoData userInfoHolder = UserInfoTool.fetchData(uid);

        //Step2. 爬取今天所有提交记录
        System.out.println("\n正在爬取今天的所有提交记录");
        List<SubmissionData> submissionData = SubmissionTool.fetchData(false);
        System.out.println("爬取完成，正在处理提交数据");

        System.out.print("筛选该用户提交");
        List<SubmissionData> currentSubmissionData = new ArrayList<>(submissionData.stream().filter(o -> o.user().id() == uid).toList());
        System.out.println("， 完成");

        System.out.print("分类答案判定");
        Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData = SubmissionTool.classifyVerdict(currentSubmissionData);
        System.out.println("， 完成");

//        //Step3. 爬取训练数据
//        System.out.println("\n正在爬取该用户的训练数据");
//        int trainingProgress = TrainingTool.fetchSingleData(uid);
//        System.out.println(". 完成");

        //Step4. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<UserInfoHolder, String> result = packUserInfo(userInfoHolder, currentSubmissionData, verdictData);//, trainingProgress);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 生成分类型评测榜单
     *
     * @param type      类型，如 ac, wa
     * @param imgPath   以图片格式输出的路径
     * @param plainPath 以文本格式输出的路径
     * @throws Throwable 异常信息
     */
    private static void generateVerdict(String type, String imgPath, String plainPath) throws Throwable {
        VerdictType verdictType = VerdictType.searchVerdict(type);
        if (verdictType == null) {
            System.out.println("input a valid verdictType, such as: ac, wa, ce, tle, mle, re.");
            throw new RunModuleException("verdict type invalid.");
        }

        //Step1. 刷新RP
        callReloadRP();

        //Step2. 爬取所有提交记录
        System.out.println("\n正在爬取今日所有提交记录");
        List<SubmissionData> submissionData = SubmissionTool.fetchData(false);
        System.out.println("爬取完成，正在处理提交数据");

        System.out.print("开始数据分析");
        Pair<Pair<Integer, Integer>, List<SimpleRankableRecord>> verdictRank = SubmissionTool.fetchVerdictRank(submissionData, verdictType);
        System.out.println("， 完成");

        //Step3. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<VerdictRankHolder, String> result = generateVerdictResult(verdictType, verdictRank);

        ImgGenerator.generateVerdictRankImg(result.A, imgPath);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 生成今日当前榜单
     *
     * @param imgPath   以图片格式输出的路径
     * @param plainPath 以文本格式输出的路径
     * @throws Throwable 异常信息
     */
    private static void generateNowRank(boolean needFull, String imgPath, String plainPath) throws Throwable {
        //Step1. 刷新RP
        callReloadRP();

        //Step2. 爬取排行榜数据
        System.out.println("\n正在爬取排行榜数据");
        List<RankingData> rankingDataList = RankTool.fetchData();
        System.out.println("爬取完成，正在处理排行榜数据");

        System.out.print("读取今日初始数据");
        List<RankingData> yesterdayData = RankTool.fetchTodayData();
        System.out.println("， 完成");

        System.out.print("开始数据分析");
        List<RankingData> deltaRankingData = dealRankingData(rankingDataList, yesterdayData);
        System.out.println("， 完成");

        //Step3. 爬取今天所有提交记录
        System.out.println("\n正在爬取今天的所有提交记录");
        List<SubmissionData> submissionData = SubmissionTool.fetchData(false);
        System.out.println("爬取完成，正在处理提交数据");

        System.out.print("分类答案判定");
        Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData = SubmissionTool.classifyVerdict(submissionData);
        List<Pair<Integer, Integer>> hourlyData = SubmissionTool.classifyHourly(submissionData, false);
        System.out.println("， 完成");

        System.out.print("开始数据分析");
        Pair<Long, Pair<UserData, String>> firstACData = SubmissionTool.getFirstACAttempt(submissionData);
        System.out.println("， 完成");

//        //Step4. 爬取训练数据
//        System.out.println("\n正在爬取新生前10名的训练数据");
//        List<TrainingData> newbieTrainingData = dealTrainingData(rankingDataList, 10);
//        System.out.println("完成");

        //Step5. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<NowRankHolder, String> result = packNowResult(rankingDataList, deltaRankingData, submissionData, verdictData, hourlyData, firstACData/*, newbieTrainingData*/, needFull);
        ImgGenerator.generateNowRankImg(result.A, imgPath);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 生成每日榜单
     *
     * @param imgPath   以图片格式输出的路径
     * @param plainPath 以文本格式输出的路径
     * @throws Throwable 异常信息
     */
    private static void generateFullRank(String imgPath, String plainPath) throws Throwable {
        DailyRankData result = RankTool.fetchDailyRankData();

        if (result != null) {
            System.out.println("重复生成榜单，将不再爬取");
        } else {
            //Step1. 刷新RP
            callReloadRP();

            //Step2. 爬取排行榜数据
            System.out.println("\n正在爬取排行榜数据");
            List<RankingData> rankingDataList = RankTool.fetchData();
            System.out.println("爬取完成，正在处理排行榜数据");

            System.out.print("更新今日数据");
            QuickUtils.saveJsonData(rankingDataList, "rank");
            System.out.println("， 完成");

            System.out.print("读取昨日数据");
            List<RankingData> yesterdayData = RankTool.fetchYesterdayData();
            System.out.println("， 完成");

            System.out.print("开始数据分析");
            List<RankingData> deltaRankingData = dealRankingData(rankingDataList, yesterdayData);
            System.out.println("， 完成");

            //Step3. 爬取昨天所有提交记录
            System.out.println("\n正在爬取昨天的所有提交记录");
            List<SubmissionData> submissionData = SubmissionTool.fetchData();
            System.out.println("爬取完成，正在处理提交数据");

            System.out.print("分类答案判定");
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData = SubmissionTool.classifyVerdict(submissionData);
            List<Pair<Integer, Integer>> hourlyData = SubmissionTool.classifyHourly(submissionData, true);
            System.out.println("， 完成");

            System.out.print("开始数据分析");
            CounterData mostPopularProblem = SubmissionTool.findMostPopularProblem(submissionData);
            Pair<Long, Pair<UserData, String>> firstACData = SubmissionTool.getFirstACAttempt(submissionData);
            System.out.println("， 完成");

//            //Step4. 爬取训练数据
//            System.out.println("\n正在爬取新生前20名的训练数据");
//            List<TrainingData> newbieTrainingData = dealTrainingData(rankingDataList);
//            System.out.println("完成");

            //Step5. 生成结果
            System.out.println("\n正在生成结果\n\n");
            result = packFullResult(rankingDataList, deltaRankingData, submissionData, verdictData, hourlyData, firstACData, mostPopularProblem);//, newbieTrainingData);

            QuickUtils.saveJsonData(result, "daily", true);
        }

        if (imgPath != null) ImgGenerator.generateFullRankImg(result.fullRankHolder(), imgPath);
        if (plainPath != null) saveTextLocally(result.plain(), plainPath);

        System.out.println(result.plain());
    }


    /**
     * 调用工具刷新 RP，并等待刷新结束
     *
     * @throws Throwable 异常信息
     */
    private static void callReloadRP() throws Throwable {
        System.out.println("正在刷新RP和problemStat");
        RPReloader.reload("problemStat");
        RPReloader.reload("rp");
        System.out.println("刷新完成");
    }


    /**
     * 将文本信息输出到文件
     *
     * @param info 输出的内容
     * @param path 输出路径
     * @throws Throwable 异常信息
     */
    private static void saveTextLocally(String info, String path) throws Throwable {
        File file = QuickUtils.fetchFile(path);
        if (file == null || !file.delete() || !file.createNewFile()) {
            throw new RunModuleException("File saved unsuccessfully.");
        }
        Files.writeString(file.toPath(), info);
    }


//    /**
//     * 处理并包装训练数据
//     *
//     * @param rankingDataList 训练数据
//     * @return 处理完成后的训练数据
//     * @throws Throwable 异常信息
//     */
//    private static List<TrainingData> dealTrainingData(List<RankingData> rankingDataList) throws Throwable {
//        return dealTrainingData(rankingDataList, 20);
//    }


//    /**
//     * 处理并包装训练数据
//     *
//     * @param rankingDataList 训练数据
//     * @param limit           限制爬取多少个新生的训练页面
//     * @return 处理完成后的训练数据
//     * @throws Throwable 异常信息
//     */
//    private static List<TrainingData> dealTrainingData(
//            List<RankingData> rankingDataList,
//            int limit) throws Throwable {
//
//        List<RankingData> newbieRankingData = new ArrayList<>(rankingDataList.stream()
//                .filter(x -> x.id() >= 1014).toList());
//        newbieRankingData.sort((o1, o2) ->
//                o1.ac() == o2.ac() ? Integer.compare(o1.rank(), o2.rank()) : -Integer.compare(o1.ac(), o2.ac()));
//
//        List<TrainingData> newbieTrainingData =
//                TrainingTool.fetchData(newbieRankingData.stream().limit(limit).toList(),
//                        () -> System.out.print("."));
//
//        newbieTrainingData.sort((o1, o2) ->
//                o1.progress() == o2.progress() ? Integer.compare(o1.generalRank(), o2.generalRank()) : -Integer.compare(o1.progress(), o2.progress()));
//        return newbieTrainingData;
//    }


    /**
     * 处理排行榜差值数据
     *
     * @param rankingDataList 训练数据
     * @param yesterdayData   昨天的排行榜数据
     * @return 处理完成后的排行榜数据
     */
    private static List<RankingData> dealRankingData(
            List<RankingData> rankingDataList,
            List<RankingData> yesterdayData) {

        Function<List<RankingData>, Map<Integer, RankingData>> toMap = list -> {
            Map<Integer, RankingData> mp = new HashMap<>();
            for (RankingData each : list) mp.put(each.id(), each);
            return mp;
        };

        List<RankingData> deltaRankingData = new ArrayList<>();
        Map<Integer, RankingData> todayMap = toMap.apply(rankingDataList),
                yesterdayMap = toMap.apply(yesterdayData);

        //计算每个人的过题数差值
        for (int id : todayMap.keySet()) {
            RankingData today = todayMap.get(id), yesterday = yesterdayMap.get(id);

            int delta = today.ac();
            if (yesterday != null) delta -= yesterday.ac();
            if (delta <= 0) continue;

            deltaRankingData.add(new RankingData(today.user(), delta, today.id(), today.rank()));
        }

        deltaRankingData.sort((o1, o2) ->
                o1.ac() == o2.ac() ? Integer.compare(o1.rank(), o2.rank()) : -Integer.compare(o1.ac(), o2.ac()));
        return deltaRankingData;
    }


    /**
     * 处理并包装用户数据
     *
     * @param userInfoData          用户数据
     * @param currentSubmissionData 当前提交数据
     * @param verdictData           当前评测数据
//     * @param trainingProgress      当前用户的训练题单完成度
     * @return 处理完成后的用户信息
     */
    private static Pair<UserInfoHolder, String> packUserInfo(
            UserInfoData userInfoData,
            List<SubmissionData> currentSubmissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData//,
           //int trainingProgress
    ) {
        int trainingProgress = -1;
        StringBuilder plainResult = new StringBuilder();
        plainResult.append("用户信息查询：\n\n");

        plainResult.append("基本信息：\n");
        plainResult.append(userInfoData.userName())
                .append("\n").append(userInfoData.userStatus())
                .append("\n").append(userInfoData.userProgress());

        if (trainingProgress == -1) {  //todo:这里要搞成Ranking里面的题数
            plainResult.append(", 未参加 2023级新手村训练");
        } else {
            plainResult.append(", 训练题单完成度: ").append(trainingProgress).append("%");
        }

        plainResult.append("\n\n个人简介：\n").append(userInfoData.description());

        plainResult.append("\n\n社交信息：")
                .append("\nQQ: ").append(userInfoData.qq());
        if (!userInfoData.qq().equals("unknown")) plainResult.append(" @").append(userInfoData.qqName());
        //为了防止被解析成url，给域名包一个 []
        plainResult.append("\n邮箱: ").append(userInfoData.mail().replaceAll("(\\.\\w+)$", " [$1]"));

        int submitCount = currentSubmissionData.size();
        double submitAve = -1, acProportion = -1;
        SubmissionData lastSubmit = null;
        StringBuilder submitDetail = new StringBuilder();

        if (submitCount == 0) {
            plainResult.append("\n\n今日暂无提交数据");
        } else {
            lastSubmit = currentSubmissionData.stream().max(Comparator.comparingLong(o -> -o.at())).get();
            plainResult.append("\n\n最后一次提交：")
                    .append("\n").append(lastSubmit.problemName())
                    .append("\n于 ")
                    .append(new SimpleDateFormat("HH:mm:ss").format(new Date(lastSubmit.at() * 1000)))
                    .append(" 得到了 ")
                    .append(lastSubmit.verdictType().getName())
                    .append(" 判定");

            submitAve = verdictData.A.A;
            acProportion = verdictData.A.B;

            plainResult.append("\n\n今日提交信息：\n");
            submitDetail.append("提交总数：\n").append(submitCount)
                    .append("\n提交平均分：\n").append(String.format(Locale.ROOT, "%.2f", submitAve))
                    .append("\n提交通过率：\n").append(String.format(Locale.ROOT, "%.2f", acProportion)).append("%");
            plainResult.append(submitDetail).append("\n");
        }

        appendGenerationInfo(plainResult);

        return Pair.of(new UserInfoHolder(userInfoData, submitCount, submitAve, acProportion, submitDetail.toString(),
                lastSubmit, trainingProgress), plainResult.toString());
    }


    /**
     * 处理并包装今日当前题数数据
     *
     * @param deltaRankingData   今日当前题数数据
//     * @param newbieTrainingData 新生训练数据
     * @return 处理完成后的今日当前题数数据 <包装数据，文本结果>
     */
    private static Pair<NowRankHolder, String> packNowResult(
            List<RankingData> rankingDataList,
            List<RankingData> deltaRankingData,
            List<SubmissionData> submissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            List<Pair<Integer, Integer>> hourlyData,
            Pair<Long, Pair<UserData, String>> firstACdata,
            //List<TrainingData> newbieTrainingData,
            boolean needFull
    ) {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("今日当前题数榜单：\n\n");

        plainResult.append("今日过题数 ").append(needFull ? "Full" : "Top 5").append("：\n");
        SubmissionPackHolder submissionPackHolder = packSubmissionPackData(submissionData, verdictData, hourlyData, firstACdata, deltaRankingData, plainResult, needFull ? Integer.MAX_VALUE : 5);

        plainResult.append("\n新生排名 Top 5：\n");
        Pair<StringBuilder, List<SimpleRankItem>> top52 = generateRank(rankingDataList, 5, x -> !Global.config.excludeID().contains(x.id()) && x.id() >= 1533, "题");
        plainResult.append(top52.A);

        appendGenerationInfo(plainResult);

        return Pair.of(new NowRankHolder(top52.B, submissionPackHolder), plainResult.toString());
    }


    /**
     * 处理并包装每日总榜数据
     *
     * @param deltaRankingData   两天的排行榜差值数据
     * @param submissionData     提交数据
     * @param verdictData        评测数据
     * @param firstACdata        第一份AC提交数据
     * @param mostPopular        最受欢迎的题目
//     * @param newbieTrainingData 新生训练数据
     * @return 处理完成后的每日总榜数据 <包装数据，文本结果>
     */
    private static DailyRankData packFullResult(
            List<RankingData> rankingDataList,
            List<RankingData> deltaRankingData,
            List<SubmissionData> submissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            List<Pair<Integer, Integer>> hourlyData,
            Pair<Long, Pair<UserData, String>> firstACdata,
            CounterData mostPopular//,
//            List<TrainingData> newbieTrainingData
    ) {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("昨日卷王天梯榜：\n\n");

        String top1 = deltaRankingData.stream()
                .filter(x -> !Global.config.excludeID().contains(x.id())).toList().get(0).user();
        plainResult.append("昨日卷王：\n")
                .append(top1);
        List<RankingData> deltaNewbie = deltaRankingData.stream().filter(x -> x.id() >= 1533).toList();

        plainResult.append("\n\n昨日过题数 Top 5：\n");
        SubmissionPackHolder submissionPackHolder = packSubmissionPackData(submissionData, verdictData, hourlyData, firstACdata, deltaNewbie, plainResult, 5);

        String mostPopularProblem = mostPopular.name();
        int mostPopularCount = mostPopular.count();
        plainResult.append("\n\n昨日最受欢迎的题目：\n").append(mostPopularProblem)
                .append("\n共有 ").append(mostPopularCount).append(" 个人提交本题\n");

        plainResult.append("\n新生排名 Top 10：\n");
        Pair<StringBuilder, List<SimpleRankItem>> top10 = generateRank(rankingDataList, 10, x -> !Global.config.excludeID().contains(x.id()), "题");
        plainResult.append(top10.A).append("\n");

        plainResult.append("昨日 OJ 总榜：")
                .append("\n");

        Pair<StringBuilder, List<SimpleRankItem>> fullRank = generateRank(deltaRankingData, Integer.MAX_VALUE, each -> each.id() >= 1533 && !Global.config.excludeID().contains(each.id()), "");
        plainResult.append(fullRank.A);

        appendGenerationInfo(plainResult);

        return new DailyRankData(new FullRankHolder(top1, submissionPackHolder, mostPopularProblem, mostPopularCount, top10.B, fullRank.B), plainResult.toString());
    }


    /**
     * 包装提交数据
     *
     * @param submissionData 提交数据
     * @param verdictData    评测数据
     * @param firstACdata    第一个通过的数据
     * @param deltaNewbie    新生的榜单数据
     * @param plainResult    目标文本输出
     * @return 包装后的数据
     */
    private static SubmissionPackHolder packSubmissionPackData(
            List<SubmissionData> submissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            List<Pair<Integer, Integer>> hourlyData,
            Pair<Long, Pair<UserData, String>> firstACdata,
            List<RankingData> deltaNewbie,
            StringBuilder plainResult,
            int topCount
    ) {

        Pair<StringBuilder, List<SimpleRankItem>> tops = generateRank(deltaNewbie, topCount, x -> !Global.config.excludeID().contains(x.id()), "");
        plainResult.append(tops.A).append("\n");

        long submitUserAmount = submissionData.stream().map(x -> x.user().id()).distinct().count();
        int submitCount = submissionData.size();
        double submitAve = verdictData.A.A, acProportion = verdictData.A.B;

        String firstACWho = firstACdata == null ? "无人过题" : firstACdata.B.A.name();
        StringBuilder firstACInfo = new StringBuilder();
        if (firstACdata == null) firstACInfo.append("虽然但是，就是没有人过题啊！");
        else firstACInfo.append("在 ").append(new SimpleDateFormat("HH:mm:ss").format(new Date(firstACdata.A * 1000)))
                .append(" 提交了 ").append(firstACdata.B.B).append(" 并通过");

        plainResult.append("提交总数：\n").append(submitCount)
                .append("\n提交平均分：\n").append(String.format(Locale.ROOT, "%.2f", submitAve))
                .append("\n提交通过率：\n").append(String.format(Locale.ROOT, "%.2f", acProportion))
                .append("\n");

        StringBuilder submitDetail = generateSubmitDetail(verdictData);
        plainResult.append("收到 ").append(submitUserAmount).append(" 个人的提交，其中包含 ")
                .append(submitDetail).append("\n");

        plainResult.append("\n每小时提交信息：\n");
        Pair<List<Pair<Double, Double>>, String> hourlyInfoData = packHourlyData(hourlyData, plainResult);

        plainResult.append("\n第一位AC提交者：\n").append(firstACWho)
                .append("\n").append(firstACInfo).append("\n");
        return new SubmissionPackHolder(tops.B, submitUserAmount, submitCount, submitAve, acProportion, submitDetail.toString(), hourlyInfoData.A, hourlyInfoData.B, firstACWho, firstACInfo.toString(), topCount);
    }


    /**
     * 处理并包装每小时的数据
     *
     * @param hourlyData  每小时的源数据 <每小时总提交量, 每小时总Ac量>
     * @param plainResult 目标文本输出
     * @return 处理结束后的每小时数据 <<每小时提交量对最大提交量的占比, 每小时的Ac占比>, 文本结果>
     */
    private static Pair<List<Pair<Double, Double>>, String> packHourlyData(List<Pair<Integer, Integer>> hourlyData, StringBuilder plainResult) {
        Pair<List<Pair<Double, Double>>, Integer> hourlyInfoData = generateHourlyInfoData(hourlyData);
        StringBuilder hourlyInfoDetail = new StringBuilder();
        int maxTime = 0;
        double maxTimeAc = 0;
        for (int i = 0; i < 24; i++) {
            Pair<Double, Double> each = hourlyInfoData.A.get(i);
            if (each.A == 0) continue;
            plainResult.append(String.format(Locale.ROOT, "%02d", i)).append(": ")
                    .append(String.format(Locale.ROOT, "%.2f", each.A * 100))
                    .append("% Top, ")
                    .append(String.format(Locale.ROOT, "%.2f", each.B * 100))
                    .append("% Ac.\n");
            if (each.A == 1) {
                maxTime = i;
                maxTimeAc = Math.max(maxTimeAc, each.B);
            }
        }

        hourlyInfoDetail.append("提交高峰时段为 ")
                .append(String.format(Locale.ROOT, "%02d:00", maxTime))
                .append(" - ")
                .append(String.format(Locale.ROOT, "%02d:59", maxTime))
                .append(". 在 ").append(hourlyInfoData.B).append(" 份提交中, 通过率为 ")
                .append(String.format(Locale.ROOT, "%.2f", maxTimeAc * 100))
                .append("%.");

        plainResult.append(hourlyInfoDetail).append("\n");

        return Pair.of(hourlyInfoData.A, hourlyInfoDetail.toString());
    }


    /**
     * 处理并包装分类型评测数据
     *
     * @param type        评测类型
     * @param verdictRank 评测数据
     * @return 处理完成后的分类型评测数据 <包装数据，文本结果>
     */
    private static Pair<VerdictRankHolder, String> generateVerdictResult(
            VerdictType type,
            Pair<Pair<Integer, Integer>, List<SimpleRankableRecord>> verdictRank
    ) {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("提交总数：\n").append(verdictRank.A.B).append("\n")
                .append(type.getName()).append(" 占比：\n")
                .append(String.format(Locale.ROOT, "%.2f", (double) verdictRank.A.A * 100 / verdictRank.A.B)).append("\n\n\n")
                .append(type.getName()).append(" 排行榜 Top 10：\n");

        Pair<StringBuilder, List<SimpleRankItem>> top10 = generateRank(verdictRank.B, 10, x -> !Global.config.excludeID().contains(x.user().id()), "");
        plainResult.append(top10.A).append("\n");

        appendGenerationInfo(plainResult);

        return Pair.of(new VerdictRankHolder(type, top10.B, verdictRank.A.B, (double) verdictRank.A.A * 100 / verdictRank.A.B),
                plainResult.toString());
    }


    /**
     * 生成提交总信息
     *
     * @param verdictData 分类型评测信息
     * @return 提交总信息
     */
    private static StringBuilder generateSubmitDetail(
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData
    ) {

        StringBuilder submitDetail = new StringBuilder();
        for (VerdictType type : VerdictType.values()) {
            if (verdictData.B.getOrDefault(type, 0) > 0) {
                if (!submitDetail.isEmpty()) submitDetail.append(", ");
                submitDetail.append(verdictData.B.get(type)).append(" ").append(type.getAlias());
            }
        }
        return submitDetail;
    }


    /**
     * 生成排行信息
     *
     * @param rankData 排行数据
     * @param limit    限制显示前几名
     * @param rated    算入正式榜单的条件
     *                 传入 null 即算入所有人
     * @param suffix   每行数据的后缀，如百分号
     * @return 排行信息
     */
    private static <T extends RankableRecord> Pair<StringBuilder, List<SimpleRankItem>> generateRank(
            List<T> rankData,
            int limit,
            Predicate<T> rated,
            String suffix
    ) {

        Map<Integer, Integer> rank = new HashMap<>();
        StringBuilder plainResult = new StringBuilder();
        List<SimpleRankItem> visualResult = new ArrayList<>();

        int currentRank = 1;
        for (int i = 0; i < rankData.size(); i++) {
            T each = rankData.get(i);
            if (!rank.containsKey(each.fetchCount())) rank.put(each.fetchCount(), currentRank);
            if (rank.get(each.fetchCount()) > limit) break;
            if (rated == null || rated.test(each)) currentRank++; //判断是否不计入榜中
            plainResult.append(rated == null || rated.test(each) ? rank.get(each.fetchCount()) : "*")
                    .append(". ").append(each.fetchWho().name()).append(": ").append(each.fetchCount()).append(suffix);
            visualResult.add(new SimpleRankItem(
                    rated == null || rated.test(each) ? String.valueOf(rank.get(each.fetchCount())) : "*",
                    each.fetchWho(), each.fetchCount()));
            if (i < rankData.size() - 1) plainResult.append("\n");
        }
        return Pair.of(plainResult, visualResult);
    }

    /**
     * 将每小时详细数据转化为比例数据
     *
     * @param hourlyData 每小时详细数据 <每小时总提交量, 每小时总Ac量>
     * @return <<每小时提交量对最大提交量的占比, 每小时的Ac占比>, 最多提交数量>
     */
    private static Pair<List<Pair<Double, Double>>, Integer> generateHourlyInfoData(List<Pair<Integer, Integer>> hourlyData) {
        int maxCount = hourlyData.stream().max(Comparator.comparingInt(o -> o.A))
                .orElse(Pair.of(1, 0)).A;
        List<Pair<Double, Double>> hourlyInfoData = new ArrayList<>();
        hourlyData.forEach(each ->
                hourlyInfoData.add(Pair.of((double) each.A / maxCount, (double) each.B / each.A)));
        return Pair.of(hourlyInfoData, maxCount);
    }

    /**
     * 将输出信息拼到结尾
     *
     * @param plainResult 目标文本输出
     */
    private static void appendGenerationInfo(StringBuilder plainResult) {
        plainResult.append("\n--------\n")
                .append(String.format(Locale.ROOT, """
                        Generated by %s.
                        ©2023-2024 Floating Ocean.
                        At""", Global.buildInfoInline))
                .append(" ")
                .append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    }
}