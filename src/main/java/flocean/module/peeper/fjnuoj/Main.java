package flocean.module.peeper.fjnuoj;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import flocean.module.peeper.fjnuoj.data.*;
import flocean.module.peeper.fjnuoj.data.RankableRecord;
import flocean.module.peeper.fjnuoj.data.rank.SimpleRankItem;
import flocean.module.peeper.fjnuoj.data.SimpleRankableRecord;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.tool.*;
import flocean.module.peeper.fjnuoj.utils.CrashHandler;
import flocean.module.peeper.fjnuoj.utils.Pair;
import flocean.module.peeper.fjnuoj.utils.ModuleFile;
import flocean.module.peeper.fjnuoj.enums.VerdictType;
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

import static flocean.module.peeper.fjnuoj.tool.ImgGenerator.*;

public class Main {

    public static void main(String[] args) {
        try {
            saveTextLocally("ok", ModuleFile.path + "/bug/out.txt");
        } catch (Throwable e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            if (args.length > 0) {
                if (args.length == 1 && args[0].equals("/necessity")) {
                    System.out.println(checkNecessity() ? "needed" : "useless");
                } else if (args.length == 2 && args[0].equals("/version")) {
                    String version = VersionControl.fetchVersionInfo(Main.class);
                    System.out.println(version);
                    saveTextLocally(version, args[1]);
                } else {
                    if (args.length == 3 && args[0].equals("/full")) generateCompletely(args[1], args[2]);
                    else if (args.length == 3 && args[0].equals("/now")) generateTemporarily(args[1], args[2]);
                    else if (args.length == 4 && args[0].equals("/verdict")) generateVerdict(args[1], args[2], args[3]);
                    else if (args.length == 4 && args[0].equals("/user")){
                        if(args[1].equals("id")) generateUserInfo(Integer.parseInt(args[2]), args[3]);
                        if(args[1].equals("name")) fuzzyMatchUser(args[2], args[3]);
                        else throw new RunModuleException("Operation Not Supported.");
                    } else printErrorParameter();
                }
            } else printErrorParameter();
        } catch (Throwable e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            try {
                saveTextLocally(CrashHandler.handleError(e), ModuleFile.path + "/bug/out.txt");
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
     * 检查是否需要重新生成每日榜单
     *
     * @return 必要性
     */
    private static boolean checkNecessity() {
        return !new File(ModuleFile.path + "/data/" + RankTool.generateFileName(new Date(System.currentTimeMillis()), "json")).exists();
    }


    /**
     * 对用户名进行模糊匹配
     * @param userName 目标用户名
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
            double currentScore = fuzzyScore.fuzzyScore(userName, each.user());
            if (currentScore >= 0.6) { //0.6的阈值
                matches.add(Pair.of(new UserData(each.user(), each.id()), currentScore));
            }
        }
        matches.sort(Comparator.comparingDouble(o -> -o.B));
        System.out.println("， 完成");

        if(matches.isEmpty()){
            saveTextLocally("mismatch", plainPath);
        }else {
            JSON.writeTo(new FileOutputStream(plainPath), matches.get(0).A, JSONWriter.Feature.PrettyFormat);
        }
    }


    /**
     * 生成用户信息
     * @param uid 用户 uid
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
        List<SubmissionData> currentSubmissionData = new ArrayList<>(submissionData.stream().filter(o -> o.id() == uid).toList());
        currentSubmissionData.sort(Comparator.comparingLong(o -> -o.at()));
        System.out.println("， 完成");

        System.out.print("分类答案判定");
        Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData = SubmissionTool.classifyVerdict(submissionData);
        System.out.println("， 完成");

        //Step3. 爬取训练数据
        System.out.println("\n正在爬取该用户的训练数据");
        int trainingProgress = TrainingTool.fetchSingleData(uid);
        System.out.println(". 完成");

        //Step4. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<UserInfoHolder, String> result = packUserInfo(userInfoHolder, currentSubmissionData, verdictData, trainingProgress);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 生成分类型评测榜单
     *
     * @param type     类型，如 ac, wa
     * @param imgPath  以图片格式输出的路径
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

        //Step4. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<VerdictRankHolder, String> result = generateVerdictResult(verdictType, verdictRank);

        ImgGenerator.generateVerdictRankImg(result.A, imgPath);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 生成今日当前榜单
     *
     * @param imgPath  以图片格式输出的路径
     * @param plainPath 以文本格式输出的路径
     * @throws Throwable 异常信息
     */
    private static void generateTemporarily(String imgPath, String plainPath) throws Throwable {
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

        //Step3. 爬取昨天所有提交记录
        System.out.println("\n正在爬取今天的所有提交记录");
        List<SubmissionData> submissionData = SubmissionTool.fetchData(false);
        System.out.println("爬取完成，正在处理提交数据");

        System.out.print("分类答案判定");
        Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData = SubmissionTool.classifyVerdict(submissionData);
        System.out.println("， 完成");

        System.out.print("开始数据分析");
        Pair<Long, Pair<String, String>> firstACData = SubmissionTool.getFirstACAttempt(submissionData);
        System.out.println("， 完成");

        //Step3. 爬取训练数据
        System.out.println("\n正在爬取新生前10名的训练数据");
        List<TrainingData> newbieTrainingData = dealTrainingData(rankingDataList, 10);
        System.out.println("完成");

        //Step4. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<NowRankHolder, String> result = packNowResult(deltaRankingData, submissionData, verdictData, firstACData, newbieTrainingData);
        ImgGenerator.generateNowRankImg(result.A, imgPath);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 生成每日榜单
     *
     * @param imgPath  以图片格式输出的路径
     * @param plainPath 以文本格式输出的路径
     * @throws Throwable 异常信息
     */
    private static void generateCompletely(String imgPath, String plainPath) throws Throwable {
        if (new File(ModuleFile.path + "/data/" + RankTool.generateFileName(new Date(System.currentTimeMillis()), "json")).exists()) {
            System.out.println("重复生成榜单，将不再爬取");
            return;
        }

        //Step1. 刷新RP
        callReloadRP();

        //Step2. 爬取排行榜数据
        System.out.println("\n正在爬取排行榜数据");
        List<RankingData> rankingDataList = RankTool.fetchData();
        System.out.println("爬取完成，正在处理排行榜数据");

        System.out.print("更新今日数据");
        RankTool.updateTodayData(rankingDataList);
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
        System.out.println("， 完成");

        System.out.print("开始数据分析");
        CounterData mostPopularProblem = SubmissionTool.findMostPopularProblem(submissionData);
        Pair<Long, Pair<String, String>> firstACData = SubmissionTool.getFirstACAttempt(submissionData);
        System.out.println("， 完成");


        //Step4. 爬取训练数据
        System.out.println("\n正在爬取新生前20名的训练数据");
        List<TrainingData> newbieTrainingData = dealTrainingData(rankingDataList);
        System.out.println("完成");


        //Step5. 生成结果
        System.out.println("\n正在生成结果\n\n");
        Pair<FullRankHolder, String> result = packFullResult(deltaRankingData, submissionData, verdictData, firstACData, mostPopularProblem, newbieTrainingData);
        ImgGenerator.generateFullRankImg(result.A, imgPath);
        saveTextLocally(result.B, plainPath);

        System.out.println(result.B);
    }


    /**
     * 调用工具刷新 RP，并等待刷新结束
     * @throws Throwable 异常信息
     */
    private static void callReloadRP() throws Throwable {
        System.out.println("正在刷新RP");
        RPReloader.reload();
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
        File file = ModuleFile.fetchFile(path);
        if (file == null || !file.delete() || !file.createNewFile()) {
            throw new RunModuleException("File saved unsuccessfully.");
        }
        Files.writeString(file.toPath(), info);
    }


    /**
     * 处理并包装训练数据
     *
     * @param rankingDataList 训练数据
     * @return 处理完成后的训练数据
     * @throws Throwable 异常信息
     */
    private static List<TrainingData> dealTrainingData(List<RankingData> rankingDataList) throws Throwable {
        return dealTrainingData(rankingDataList, 20);
    }


    /**
     * 处理并包装训练数据
     *
     * @param rankingDataList 训练数据
     * @param limit           限制爬取多少个新生的训练页面
     * @return 处理完成后的训练数据
     * @throws Throwable 异常信息
     */
    private static List<TrainingData> dealTrainingData(
            List<RankingData> rankingDataList,
            int limit) throws Throwable {

        List<RankingData> newbieRankingData = new ArrayList<>(rankingDataList.stream().filter(x -> x.id() >= 1014).toList());
        newbieRankingData.sort((o1, o2) ->
                o1.ac() == o2.ac() ? Integer.compare(o1.rank(), o2.rank()) : -Integer.compare(o1.ac(), o2.ac()));

        List<TrainingData> newbieTrainingData =
                TrainingTool.fetchData(newbieRankingData.stream().limit(limit).toList(),
                        () -> System.out.print("."));

        newbieTrainingData.sort((o1, o2) ->
                o1.progress() == o2.progress() ? Integer.compare(o1.generalRank(), o2.generalRank()) : -Integer.compare(o1.progress(), o2.progress()));
        return newbieTrainingData;
    }


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


    private static Pair<UserInfoHolder, String> packUserInfo(
            UserInfoData userInfoData,
            List<SubmissionData> currentSubmissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            int trainingProgress
    ) throws Throwable {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("用户信息查询：\n\n");

        plainResult.append("基本信息：\n");
        plainResult.append(userInfoData.userName())
                .append("\n").append(userInfoData.userStatus())
                .append("\n").append(userInfoData.userProgress());

        if(trainingProgress == -1){
            plainResult.append(", 未参加 2023级新手村训练");
        }else{
            plainResult.append(", 训练题单完成度: ").append(trainingProgress).append("%");
        }

        plainResult.append("\n\n个人简介：\n").append(userInfoData.description());

        plainResult.append("\n\n社交信息：")
                .append("\nQQ: ").append(userInfoData.qq());
        if(!userInfoData.qq().equals("unknown")) plainResult.append("  ").append(userInfoData.qqName());
        plainResult.append("\n邮箱: ").append(userInfoData.mail().replace(".", " ."));

        int submitCount = currentSubmissionData.size();
        double submitAve = -1, acProportion = -1;
        SubmissionData lastSubmit = null;
        StringBuilder submitDetail = new StringBuilder();

        if(submitCount == 0) {
            plainResult.append("\n\n今日暂无提交数据");
        } else {
            lastSubmit = currentSubmissionData.get(0);
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

        plainResult.append("\n--------\n")
                .append(String.format(Locale.ROOT, """
                        Generated by %s.
                        ©2023 Floating Ocean.
                        At""", VersionControl.fetchVersionInfoInline(Main.class)))
                .append(" ")
                .append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        return Pair.of(new UserInfoHolder(userInfoData, submitCount, submitAve, acProportion, submitDetail.toString(),
                lastSubmit, trainingProgress), plainResult.toString());
    }



    /**
     * 处理并包装今日当前题数数据
     *
     * @param deltaRankingData   今日当前题数数据
     * @param newbieTrainingData 新生训练数据
     * @return 处理完成后的今日当前题数数据 <包装数据，文本结果>
     */
    private static Pair<NowRankHolder, String> packNowResult(
            List<RankingData> deltaRankingData,
            List<SubmissionData> submissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            Pair<Long, Pair<String, String>> firstACdata,
            List<TrainingData> newbieTrainingData
    ) throws Throwable {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("今日当前题数榜单：\n\n");

        plainResult.append("今日过题数 Top 5：\n");
        SubmissionPackHolder submissionPackHolder = packSubmissionPackData(submissionData, verdictData, firstACdata, deltaRankingData, plainResult);

        plainResult.append("\n新生训练题单完成比 Top 5：\n");
        Pair<StringBuilder, List<SimpleRankItem>> top52 = generateRank(newbieTrainingData, 5, null, "%");
        plainResult.append(top52.A);

        plainResult.append("\n--------\n")
                .append(String.format(Locale.ROOT, """
                        Generated by %s.
                        ©2023 Floating Ocean.
                        At""", VersionControl.fetchVersionInfoInline(Main.class)))
                .append(" ")
                .append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

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
     * @param newbieTrainingData 新生训练数据
     * @return 处理完成后的每日总榜数据 <包装数据，文本结果>
     */
    private static Pair<FullRankHolder, String> packFullResult(
            List<RankingData> deltaRankingData,
            List<SubmissionData> submissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            Pair<Long, Pair<String, String>> firstACdata,
            CounterData mostPopular,
            List<TrainingData> newbieTrainingData
    ) throws Throwable {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("昨日卷王天梯榜：\n\n");

        String top1 = deltaRankingData.get(0).user();
        plainResult.append("昨日卷王：\n")
                .append(top1);
        List<RankingData> deltaNewbie = deltaRankingData.stream().filter(x -> x.id() >= 1014).toList();

        plainResult.append("\n\n昨日过题数 Top 5：\n");
        SubmissionPackHolder submissionPackHolder = packSubmissionPackData(submissionData, verdictData, firstACdata, deltaNewbie, plainResult);

        String mostPopularProblem = mostPopular.name();
        int mostPopularCount = mostPopular.count();
        plainResult.append("\n\n昨日最受欢迎的题目：\n").append(mostPopularProblem)
                .append("\n共有 ").append(mostPopularCount).append(" 个人提交本题\n");

        plainResult.append("\n新生训练题单完成比 Top 10：\n");
        Pair<StringBuilder, List<SimpleRankItem>> top10 = generateRank(newbieTrainingData, 10, null, "%");
        plainResult.append(top10.A).append("\n");

        plainResult.append("昨日 OJ 总榜：")
                .append("\n");

        Pair<StringBuilder, List<SimpleRankItem>> fullRank = generateRank(deltaRankingData, Integer.MAX_VALUE, each -> each.id() >= 1014, "");
        plainResult.append(fullRank.A);

        plainResult.append("\n--------\n")
                .append(String.format(Locale.ROOT, """
                        Generated by %s.
                        ©2023 Floating Ocean.
                        At""", VersionControl.fetchVersionInfoInline(Main.class)))
                .append(" ")
                .append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        return Pair.of(new FullRankHolder(top1, submissionPackHolder, mostPopularProblem, mostPopularCount, top10.B, fullRank.B), plainResult.toString());
    }


    /**
     * 包装提交数据
     * @param submissionData 提交数据
     * @param verdictData 评测数据
     * @param firstACdata 第一个通过的数据
     * @param deltaNewbie 新生的榜单数据
     * @param plainResult 目标文本输出
     * @return 包装后的数据
     */
    private static SubmissionPackHolder packSubmissionPackData(
            List<SubmissionData> submissionData,
            Pair<Pair<Double, Double>, Map<VerdictType, Integer>> verdictData,
            Pair<Long, Pair<String, String>> firstACdata,
            List<RankingData> deltaNewbie,
            StringBuilder plainResult
    ) {

        Pair<StringBuilder, List<SimpleRankItem>> top5 = generateRank(deltaNewbie, 5, null, "");
        plainResult.append(top5.A).append("\n");

        long submitUserAmount = submissionData.stream().map(SubmissionData::id).distinct().count();
        int submitCount = submissionData.size();
        double submitAve = verdictData.A.A, acProportion = verdictData.A.B;

        String firstACWho = firstACdata == null ? "无人过题" : firstACdata.B.A;
        StringBuilder firstACInfo = new StringBuilder();
        if (firstACdata == null) firstACInfo.append("虽然但是，就是没有人过题啊！");
        else firstACInfo.append("在 ").append(new SimpleDateFormat("HH:mm:ss").format(new Date(firstACdata.A * 1000)))
                .append(" 提交了 ").append(firstACdata.B.B).append(" 并通过");

        plainResult.append("提交总数：\n").append(submitCount)
                .append("\n提交平均分：\n").append(String.format(Locale.ROOT, "%.2f", submitAve))
                .append("\n提交通过率：\n").append(String.format(Locale.ROOT, "%.2f", acProportion))
                .append("\n\n第一位AC提交者：\n").append(firstACWho)
                .append("\n").append(firstACInfo).append("\n");

        StringBuilder submitDetail = generateSubmitDetail(verdictData);
        plainResult.append("共收到 ").append(submitUserAmount).append(" 个人的提交，其中包含 ")
                .append(submitDetail).append("\n");
        return new SubmissionPackHolder(top5.B, submitUserAmount, submitCount, submitAve, acProportion, submitDetail.toString(), firstACWho, firstACInfo.toString());
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
    ) throws Throwable {

        StringBuilder plainResult = new StringBuilder();
        plainResult.append("提交总数：\n").append(verdictRank.A.B).append("\n")
                .append(type.getName()).append(" 占比：\n")
                .append(String.format(Locale.ROOT, "%.2f", (double) verdictRank.A.A * 100 / verdictRank.A.B)).append("\n\n\n")
                .append(type.getName()).append(" 排行榜 Top 10：\n");

        Pair<StringBuilder, List<SimpleRankItem>> top10 = generateRank(verdictRank.B, 10, null, "");
        plainResult.append(top10.A).append("\n");

        plainResult.append("\n--------\n")
                .append(String.format(Locale.ROOT, """
                        Generated by %s.
                        ©2023 Floating Ocean.
                        At""", VersionControl.fetchVersionInfoInline(Main.class)))
                .append(" ")
                .append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        return Pair.of(new VerdictRankHolder(type, top10.B, verdictRank.A.B, (double) verdictRank.A.A * 100 / verdictRank.A.B),
                plainResult.toString());
    }


    /**
     * 生成提交总信息
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
     * @param rankData 排行数据
     * @param limit 限制显示前几名
     * @param rated 算入正式榜单的条件
     *              传入 null 即算入所有人
     * @param suffix 每行数据的后缀，如百分号
     * @return 排行信息
     */
    private static <T extends RankableRecord> Pair<StringBuilder, List<SimpleRankItem>> generateRank(
            List<T> rankData,
            int limit,
            Predicate<T> rated,
            String suffix
    ){

        Map<Integer, Integer> rank = new HashMap<>();
        StringBuilder plainResult = new StringBuilder();
        List<SimpleRankItem> visualResult = new ArrayList<>();

        int currentRank = 1;
        for (int i = 0; i < rankData.size(); i++) {
            T each = rankData.get(i);
            if (!rank.containsKey(each.fetchCount())) rank.put(each.fetchCount(), currentRank);
            if (rank.get(each.fetchCount()) > limit) break;
            if (rated == null || rated.test(each)) currentRank ++; //判断是否不计入榜中
            plainResult.append(rated == null || rated.test(each) ? rank.get(each.fetchCount()) : "*")
                    .append(". ").append(each.fetchName()).append(": ").append(each.fetchCount()).append(suffix);
            visualResult.add(new SimpleRankItem(
                    rated == null || rated.test(each) ? String.valueOf(rank.get(each.fetchCount())) : "*",
                    each.fetchName(), each.fetchCount()));
            if (i < rankData.size() - 1) plainResult.append("\n");
        }
        return Pair.of(plainResult, visualResult);
    }
}