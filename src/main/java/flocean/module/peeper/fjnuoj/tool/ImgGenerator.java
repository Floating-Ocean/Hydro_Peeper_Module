package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.SubmissionData;
import flocean.module.peeper.fjnuoj.data.UserInfoData;
import flocean.module.peeper.fjnuoj.data.rank.RankingItem;
import flocean.module.peeper.fjnuoj.data.rank.SimpleRankItem;
import flocean.module.peeper.fjnuoj.data.rank.SubmissionRankItem;
import flocean.module.peeper.fjnuoj.enums.VerdictType;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.ImgConvert;
import flocean.module.peeper.fjnuoj.utils.Pair;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static flocean.module.peeper.fjnuoj.utils.ImgConvert.GradientColors;
import static flocean.module.peeper.fjnuoj.utils.ImgConvert.StyledString;

public class ImgGenerator {
    /**
     * 生成每日榜单图片
     *
     * @param fullRankHolder 包装后的每日榜单数据
     * @param path           输出路径
     * @throws Throwable 异常信息
     */
    public static void generateFullRankImg(FullRankHolder fullRankHolder, String path) throws Throwable {
        StyledString title = packString("昨日卷王天梯榜", "H", 96);
        StyledString subtitle = packString(new SimpleDateFormat("yyyy.MM.dd").format(new Date(System.currentTimeMillis() - 86400 * 1000))
                + "  FJNUACM Online Judge Rank List", "H", 36);

        StyledString top1Title = packString("昨日卷王", "B", 36);
        StyledString top1Who = packString(fullRankHolder.top1, "H", 72);

        StyledString top5Subtitle = packString("过题数榜单", "B", 36);
        StyledString top5Title = packString("昨日过题数", "H", 72);
        StyledString top5Mark = packString("Top 5th", "H", 48);
        List<RankDrawHolder> top5Who = new ArrayList<>();
        for (var each : fullRankHolder.top5()) {
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, fullRankHolder.top5().get(0).val());
            top5Who.add(new RankDrawHolder(currentRankItem.getProgress(), !currentRankItem.fetchRank().equals("*"),
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho().name(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)));
        }

        StyledString submitCountTitle = packString("提交总数", "B", 36);
        StyledString submitCountHow = packString("" + fullRankHolder.submitCount, "H", 72);

        StyledString submitAveTitle = packString("提交平均分", "B", 36);
        //将小数分成 整数部分 和 小数部分，整数部分加粗
        String[] submitAve = String.format(Locale.ROOT, "%.2f", fullRankHolder.submitAve).split("\\.");
        StyledString submitAveHowMain = packString(submitAve[0], "H", 72);
        StyledString submitAveHowSub = packString("." + submitAve[1], "H", 72);

        StyledString submitAcTitle = packString("提交通过率", "B", 36);
        String[] submitAc = String.format(Locale.ROOT, "%.2f", fullRankHolder.acProportion).split("\\.");
        StyledString submitAcHowMain = packString(submitAc[0], "H", 72);
        StyledString submitAcHowSub = packString("." + submitAc[1], "H", 72);

        StyledString submitDetailWhat = packString("收到 " + fullRankHolder.submitUserAmount +
                " 个人的提交，其中包含 " + fullRankHolder.submitDetail, "M", 28);

        StyledString hourlyTitle = packString("提交时间分布", "B", 36);
        StyledString hourlyWhat = packString(fullRankHolder.hourlyInfoDetail, "M", 28);

        StyledString firstACTitle = packString("昨日最速通过", "B", 36);
        StyledString firstACWho = packString(fullRankHolder.firstACName, "H", 72);
        StyledString firstACWhat = packString(fullRankHolder.firstACInfo, "M", 28);

        StyledString mostPopularProblemTitle = packString("昨日最受欢迎的题目", "B", 36);
        StyledString mostPopularProblemWhat = packString(fullRankHolder.mostPopularProblem, "H", 72);
        StyledString mostPopularCountHow = packString("共有 " + fullRankHolder.mostPopularCount + " 个人提交本题", "M", 28);

        StyledString top10Subtitle = packString("训练榜单", "B", 36);
        StyledString top10Title = packString("新生排名", "H", 72);
        StyledString top10Mark = packString("Top 10th", "H", 48);
        List<RankDrawHolder> top10Who = new ArrayList<>();
        int maxCount = 0;
        for (var each : fullRankHolder.top10()) {
            maxCount = Math.max(maxCount, each.val());
        }
        for (var each : fullRankHolder.top10()) {
            RankingItem currentRankItem = new RankingItem(each, maxCount);
            top10Who.add(new RankDrawHolder(currentRankItem.getProgress(), !currentRankItem.fetchRank().equals("*"),
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho().name(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)));
        }

        StyledString fullRankSubtitle = packString("完整榜单", "B", 36);
        StyledString fullRankTitle = packString("昨日 OJ 总榜", "H", 72);
        List<RankDrawHolder> fullRankWho = new ArrayList<>();
        for (var each : fullRankHolder.fullRank()) {
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, fullRankHolder.fullRank().get(0).val());
            fullRankWho.add(new RankDrawHolder(currentRankItem.getProgress(), !currentRankItem.fetchRank().equals("*"),
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho().name(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)));
        }

        StyledString copyright = packString(String.format(Locale.ROOT, """
                Generated by %s.
                ©2023-2024 Floating Ocean.
                At""", Global.buildInfoInline) + " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), "B", 24, 1.32);


        int totalHeight = calculateHeight(
                title, subtitle,
                top1Title, top1Who,
                top5Subtitle, top5Title,
                submitCountTitle, submitCountHow, submitDetailWhat,
                firstACTitle, firstACWho, firstACWhat,
                mostPopularProblemTitle, mostPopularProblemWhat, mostPopularCountHow,
                hourlyTitle, hourlyWhat,
                top10Subtitle, top10Title,
                fullRankSubtitle, fullRankTitle,
                copyright) +
                calculateHeight(top5Who, top10Who, fullRankWho) + 1380 + 200;

        BufferedImage outputImg = new BufferedImage(1280, totalHeight + 300, BufferedImage.TYPE_INT_BGR);
        AtomicInteger currentY = new AtomicInteger(134);
        Graphics2D outputCanvas = drawBasicContent(outputImg, totalHeight, title, subtitle, currentY);

        drawText(outputCanvas, top1Title, 8, currentY);
        drawText(outputCanvas, top1Who, 108, currentY);

        drawText(outputCanvas, top5Subtitle, 8, currentY);
        drawText(outputCanvas, top5Title, 32, currentY);
        currentY.addAndGet(-96);
        drawText(outputCanvas, top5Mark, 128 + ImgConvert.calculateStringWidth(top5Title.font, top5Title.content) + 28, 32, currentY);
        drawRankText(outputCanvas, top5Who, 108, currentY);

        drawSubmitDetail(currentY, outputCanvas,
                submitCountTitle, submitCountHow, submitAveTitle, submitAveHowMain, submitAveHowSub,
                submitAcTitle, submitAcHowMain, submitAcHowSub, submitDetailWhat,
                hourlyTitle, fullRankHolder.hourlyInfoData, hourlyWhat,
                firstACTitle, firstACWho, firstACWhat);

        drawText(outputCanvas, mostPopularProblemTitle, 8, currentY);
        drawText(outputCanvas, mostPopularProblemWhat, 32, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, mostPopularCountHow, 108, currentY);

        outputCanvas.setColor(Color.black);
        drawText(outputCanvas, top10Subtitle, 8, currentY);
        drawText(outputCanvas, top10Title, 32, currentY);
        currentY.addAndGet(-96);
        drawText(outputCanvas, top10Mark, 128 + ImgConvert.calculateStringWidth(top10Title.font, top10Title.content) + 28, 32, currentY);
        drawRankText(outputCanvas, top10Who, 108, currentY);

        drawText(outputCanvas, fullRankSubtitle, 8, currentY);
        drawText(outputCanvas, fullRankTitle, 32, currentY);
        drawRankText(outputCanvas, fullRankWho, 108, currentY);

        drawText(outputCanvas, copyright, 108, currentY);

        outputCanvas.dispose();

        //输出png图片到指定目录
        saveImgToFile(outputImg, path);
    }

    /**
     * 生成今日当前榜单图片
     *
     * @param nowRankHolder 包装后的今日当前榜单数据
     * @param path          输出路径
     * @throws Throwable 异常信息
     */
    public static void generateNowRankImg(NowRankHolder nowRankHolder, String path) throws Throwable {
        StyledString title = packString("今日当前题数榜单", "H", 96);
        StyledString subtitle = packString(new SimpleDateFormat("yyyy.MM.dd").format(new Date())
                + "  FJNUACM Online Judge Rank List", "H", 36);

        StyledString topsSubtitle = packString("过题数榜单", "B", 36);
        StyledString topsTitle = packString("今日过题数", "H", 72);
        StyledString topsMark = packString(
                nowRankHolder.topCount < Integer.MAX_VALUE ? "Top " + nowRankHolder.topCount + "th" : "Full",
                "H", 48);
        List<RankDrawHolder> topsWho = new ArrayList<>();
        for (var each : nowRankHolder.tops()) {
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, nowRankHolder.tops().get(0).val());
            topsWho.add(new RankDrawHolder(currentRankItem.getProgress(), !currentRankItem.fetchRank().equals("*"),
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho().name(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)));
        }

        StyledString submitCountTitle = packString("提交总数", "B", 36);
        StyledString submitCountHow = packString("" + nowRankHolder.submitCount, "H", 72);

        StyledString submitAveTitle = packString("提交平均分", "B", 36);
        //将小数分成 整数部分 和 小数部分，整数部分加粗
        String[] submitAve = String.format(Locale.ROOT, "%.2f", nowRankHolder.submitAve).split("\\.");
        StyledString submitAveHowMain = packString(submitAve[0], "H", 72);
        StyledString submitAveHowSub = packString("." + submitAve[1], "H", 72);

        StyledString submitAcTitle = packString("提交通过率", "B", 36);
        String[] submitAc = String.format(Locale.ROOT, "%.2f", nowRankHolder.acProportion).split("\\.");
        StyledString submitAcHowMain = packString(submitAc[0], "H", 72);
        StyledString submitAcHowSub = packString("." + submitAc[1], "H", 72);
        StyledString submitDetailWhat = packString("收到 " + nowRankHolder.submitUserAmount +
                " 个人的提交，其中包含 " + nowRankHolder.submitDetail, "M", 28);

        StyledString hourlyTitle = packString("提交时间分布", "B", 36);
        StyledString hourlyWhat = packString(nowRankHolder.hourlyInfoDetail, "M", 28);

        StyledString firstACTitle = packString("今日最速通过", "B", 36);
        StyledString firstACWho = packString(nowRankHolder.firstACName, "H", 72);
        StyledString firstACWhat = packString(nowRankHolder.firstACInfo, "M", 28);

        StyledString top52Subtitle = packString("训练榜单", "B", 36);
        StyledString top52Title = packString("新生排名", "H", 72);
        StyledString top52Mark = packString("Top 5th", "H", 48);
        List<RankDrawHolder> top52Who = new ArrayList<>();
        int maxCount = 0;
        for (var each : nowRankHolder.top52()) {
            maxCount = Math.max(maxCount, each.val());
        }
        for (var each : nowRankHolder.top52()) {
            RankingItem currentRankItem = new RankingItem(each, maxCount);
            top52Who.add(new RankDrawHolder(currentRankItem.getProgress(), !currentRankItem.fetchRank().equals("*"),
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho().name(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)));
        }

        StyledString copyright = packString(String.format(Locale.ROOT, """
                Generated by %s.
                ©2023-2024 Floating Ocean.
                At""", Global.buildInfoInline) + " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), "B", 24, 1.32);

        int totalHeight = calculateHeight(
                title, subtitle,
                topsSubtitle, topsTitle,
                submitCountTitle, submitCountHow, submitDetailWhat,
                firstACTitle, firstACWho, firstACWhat,
                hourlyTitle, hourlyWhat,
                top52Subtitle, top52Title,
                copyright) +
                calculateHeight(topsWho, top52Who) + 940 + 200;

        BufferedImage outputImg = new BufferedImage(1280, totalHeight + 300, BufferedImage.TYPE_INT_BGR);
        AtomicInteger currentY = new AtomicInteger(134);
        Graphics2D outputCanvas = drawBasicContent(outputImg, totalHeight, title, subtitle, currentY);

        drawText(outputCanvas, topsSubtitle, 8, currentY);
        drawText(outputCanvas, topsTitle, 32, currentY);
        currentY.addAndGet(-96);
        drawText(outputCanvas, topsMark, 128 + ImgConvert.calculateStringWidth(topsTitle.font, topsTitle.content) + 28, 32, currentY);
        drawRankText(outputCanvas, topsWho, 108, currentY);

        drawSubmitDetail(currentY, outputCanvas,
                submitCountTitle, submitCountHow, submitAveTitle, submitAveHowMain, submitAveHowSub,
                submitAcTitle, submitAcHowMain, submitAcHowSub, submitDetailWhat,
                hourlyTitle, nowRankHolder.hourlyInfoData, hourlyWhat,
                firstACTitle, firstACWho, firstACWhat);

        drawText(outputCanvas, top52Subtitle, 8, currentY);
        drawText(outputCanvas, top52Title, 32, currentY);
        currentY.addAndGet(-96);
        drawText(outputCanvas, top52Mark, 128 + ImgConvert.calculateStringWidth(top52Title.font, top52Title.content) + 28, 32, currentY);
        drawRankText(outputCanvas, top52Who, 108, currentY);

        drawText(outputCanvas, copyright, 108, currentY);

        outputCanvas.dispose();

        //输出png图片到指定目录
        saveImgToFile(outputImg, path);
    }

    /**
     * 生成分类型评测榜单图片
     *
     * @param verdictRankHolder 包装后的分类型评测榜单数据
     * @param path              输出路径
     * @throws Throwable 异常信息
     */
    public static void generateVerdictRankImg(VerdictRankHolder verdictRankHolder, String path) throws Throwable {
        StyledString title = packString("今日当前提交榜单", "H", 96);
        StyledString subtitle = packString(new SimpleDateFormat("yyyy.MM.dd").format(new Date())
                + "  FJNUACM Online Judge Rank List", "H", 36);

        StyledString submitCountTitle = packString("提交总数", "B", 36);
        StyledString submitCountHow = packString("" + verdictRankHolder.submitCount, "H", 72);

        StyledString proportionTitle = packString(verdictRankHolder.verdict.getName() + " 占比", "B", 36);
        StyledString proportionHow = packString(String.format(Locale.ROOT, "%.2f", verdictRankHolder.proportion), "H", 72);

        StyledString top10Subtitle = packString("分类型提交榜单", "B", 36);
        StyledString top10Title = packString(verdictRankHolder.verdict.getAlias() + " 排行榜", "H", 72);
        StyledString top10Mark = packString("Top 10th", "H", 48);
        List<RankDrawHolder> top10Who = new ArrayList<>();
        for (var each : verdictRankHolder.top10()) {
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, verdictRankHolder.top10().get(0).val());
            top10Who.add(new RankDrawHolder(currentRankItem.getProgress(), !currentRankItem.fetchRank().equals("*"),
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho().name(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)));
        }

        StyledString copyright = packString(String.format(Locale.ROOT, """
                Generated by %s.
                ©2023-2024 Floating Ocean.
                At""", Global.buildInfoInline) + " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), "B", 24, 1.32);

        int totalHeight = calculateHeight(
                title, subtitle,
                top10Subtitle, top10Title,
                copyright) +
                calculateHeight(top10Who) + 576;

        BufferedImage outputImg = new BufferedImage(1280, totalHeight + 300, BufferedImage.TYPE_INT_BGR);
        AtomicInteger currentY = new AtomicInteger(134);
        Graphics2D outputCanvas = drawBasicContent(outputImg, totalHeight, title, subtitle, currentY);

        AtomicInteger unchangedY = new AtomicInteger(currentY.get());
        drawText(outputCanvas, submitCountTitle, 8, currentY);
        drawText(outputCanvas, submitCountHow, 108, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        int submitCountWidth = Math.max(
                ImgConvert.calculateStringWidth(submitCountTitle.font, submitCountTitle.content),
                ImgConvert.calculateStringWidth(submitCountHow.font, submitCountHow.content));
        drawText(outputCanvas, proportionTitle, 128 + submitCountWidth + 150, 8, currentY);
        drawText(outputCanvas, proportionHow, 128 + submitCountWidth + 150, 108, currentY);

        drawText(outputCanvas, top10Subtitle, 8, currentY);
        drawText(outputCanvas, top10Title, 32, currentY);
        currentY.addAndGet(-96);
        drawText(outputCanvas, top10Mark, 128 + ImgConvert.calculateStringWidth(top10Title.font, top10Title.content) + 28, 32, currentY);
        drawRankText(outputCanvas, top10Who, 108, currentY);

        drawText(outputCanvas, copyright, 108, currentY);

        outputCanvas.dispose();

        //输出png图片到指定目录
        saveImgToFile(outputImg, path);
    }

    /**
     * 简短的 StyledString 包装函数
     *
     * @param content 文本内容
     * @param weight  字重
     * @param size    字体大小
     * @return 对应的 StyledString
     */
    private static StyledString packString(String content, String weight, int size) {
        return new StyledString(content, new Font("OPPOSans " + weight, Font.PLAIN, size));
    }


    /**
     * 简短的 StyledString 包装函数
     *
     * @param content        文本内容
     * @param weight         字重
     * @param size           字体大小
     * @param lineMultiplier 行距
     * @return 对应的 StyledString
     */
    private static StyledString packString(String content, String weight, int size, double lineMultiplier) {
        return new StyledString(content, new Font("OPPOSans " + weight, Font.PLAIN, size), lineMultiplier);
    }


    /**
     * 绘制提交信息
     *
     * @param currentY     开始绘制文本的高度
     * @param outputCanvas 目标图层
     */
    private static void drawSubmitDetail(
            AtomicInteger currentY, Graphics2D outputCanvas,
            StyledString submitCountTitle, StyledString submitCountHow,
            StyledString submitAveTitle, StyledString submitAveHowMain, StyledString submitAveHowSub,
            StyledString submitAcTitle, StyledString submitAcHowMain, StyledString submitAcHowSub,
            StyledString submitDetailWhat,
            StyledString hourlyTitle, List<Pair<Double, Double>> hourlyInfoData, StyledString hourlyWhat,
            StyledString firstACTitle, StyledString firstACWho,
            StyledString firstACWhat
    ) {

        AtomicInteger unchangedY = new AtomicInteger(currentY.get());
        drawText(outputCanvas, submitCountTitle, 8, currentY);
        drawText(outputCanvas, submitCountHow, 32, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        int submitCountWidth = Math.max(
                ImgConvert.calculateStringWidth(submitCountTitle.font, submitCountTitle.content),
                ImgConvert.calculateStringWidth(submitCountHow.font, submitCountHow.content));
        drawText(outputCanvas, submitAveTitle, 128 + submitCountWidth + 150, 8, currentY);
        AtomicInteger unchangedY2 = new AtomicInteger(currentY.get());
        drawText(outputCanvas, submitAveHowMain, 128 + submitCountWidth + 150, 32, currentY);
        currentY.set(unchangedY2.get()); //保持同一行
        outputCanvas.setColor(new Color(0, 0, 0, 64));
        drawText(outputCanvas, submitAveHowSub,
                128 + submitCountWidth + 150 + ImgConvert.calculateStringWidth(submitAveHowMain.font, submitAveHowMain.content),
                32, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        outputCanvas.setColor(Color.black);
        submitCountWidth = submitCountWidth + Math.max(
                ImgConvert.calculateStringWidth(submitAveTitle.font, submitAveTitle.content),
                ImgConvert.calculateStringWidth(submitAveHowMain.font, submitAveHowMain.content)
                        + ImgConvert.calculateStringWidth(submitAveHowSub.font, submitAveHowSub.content));
        drawText(outputCanvas, submitAcTitle, 128 + 128 + submitCountWidth + 150, 8, currentY);
        drawText(outputCanvas, submitAcHowMain, 128 + 128 + submitCountWidth + 150, 32, currentY);
        currentY.set(unchangedY2.get()); //保持同一行
        outputCanvas.setColor(new Color(0, 0, 0, 64));
        drawText(outputCanvas, submitAcHowSub,
                128 + 128 + submitCountWidth + 150 + ImgConvert.calculateStringWidth(submitAcHowMain.font, submitAcHowMain.content),
                32, currentY);

        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, submitDetailWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
        drawText(outputCanvas, hourlyTitle, 24, currentY);
        drawVerticalGraph(outputCanvas, hourlyInfoData, 40, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, hourlyWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
        drawText(outputCanvas, firstACTitle, 8, currentY);
        drawText(outputCanvas, firstACWho, 32, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, firstACWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
    }

    /**
     * 绘制竖条形图
     *
     * @param outputCanvas  目标图层
     * @param content       条形图数据
     * @param paddingBottom 下边距
     * @param currentY      开始绘制的高度
     */
    private static void drawVerticalGraph(Graphics2D outputCanvas, List<Pair<Double, Double>> content, int paddingBottom, AtomicInteger currentY) {
        int currentX = 152;
        currentY.addAndGet(16);

        //绘制边框
        outputCanvas.setColor(new Color(0, 0, 0, 32));
        outputCanvas.fillRect(128, currentY.get(), 24, 4);
        outputCanvas.fillRect(128, currentY.get() + 4, 4, 20);
        outputCanvas.fillRect(128, currentY.get() + 240, 24, 4);
        outputCanvas.fillRect(128, currentY.get() + 220, 4, 20);

        for (var each : content) {
            int progressLen = (int) (24 + 176 * each.A),
                    subProgressLen = each.A > 0 ? (int) (24 + 176 * each.A * each.B) : 24;
            AtomicInteger lineY = new AtomicInteger(currentY.get() + 24);

            outputCanvas.setColor(new Color(0, 0, 0, 32));
            outputCanvas.fillRoundRect(currentX, lineY.get() + 200 - progressLen, 24, progressLen, 24, 24);
            outputCanvas.setColor(new Color(0, 0, 0, 16));
            outputCanvas.fillRoundRect(currentX, lineY.get() + 200 - subProgressLen, 24, subProgressLen, 24, 24);
            currentX += 24 + 16;
        }

        currentX -= 24 + 16;
        outputCanvas.setColor(new Color(0, 0, 0, 32));
        outputCanvas.fillRect(currentX + 24, currentY.get(), 24, 4);
        outputCanvas.fillRect(currentX + 44, currentY.get() + 4, 4, 20);
        outputCanvas.fillRect(currentX + 24, currentY.get() + 240, 24, 4);
        outputCanvas.fillRect(currentX + 44, currentY.get() + 220, 4, 20);

        outputCanvas.setColor(Color.BLACK);
        currentY.addAndGet(paddingBottom + 224);
    }


    /**
     * 绘制排行榜
     *
     * @param outputCanvas  目标图层
     * @param content       排行榜内容
     * @param paddingBottom 下边距
     * @param currentY      开始绘制文本的高度
     */
    private static void drawRankText(Graphics2D outputCanvas, List<RankDrawHolder> content, int paddingBottom, AtomicInteger currentY) {
        String preRank = ""; //上一个 rank
        for (var each : content) {
            int progressLen = (int) (360 + 440 * each.percent);

            AtomicInteger lineY = new AtomicInteger(currentY.get());
            int currentX = 128 + 32;
            boolean sameRank = false;
            if (each.rated) {
                sameRank = each.strings[0].content.equals(preRank);
                //相同 rank 不重复显示，显示为透明
                outputCanvas.setColor(new Color(0, 0, 0, sameRank ? 0 : 255));
                preRank = each.strings[0].content;
            } else {
                //打星显示为半透明
                outputCanvas.setColor(new Color(0, 0, 0, 100));
            }
            drawText(outputCanvas, each.strings[0], currentX, 12, currentY);

            currentX += ImgConvert.calculateStringWidth(each.strings[0].font, each.strings[0].content) + 28;
            currentY.set(lineY.get() + 40);
            outputCanvas.setColor(new Color(0, 0, 0, each.rated ? 255 : 100));
            drawText(outputCanvas, each.strings[1], currentX, 12, currentY);

            currentX = Math.max(progressLen + 128, currentX + ImgConvert.calculateStringWidth(each.strings[1].font, each.strings[1].content)) + 36;
            currentY.set(lineY.get() + 40);
            drawText(outputCanvas, each.strings[2], currentX, 32, currentY);

            LinearGradientPaint tilePaint = new LinearGradientPaint(0, 52, progressLen, 52, new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{
                            new Color(0, 0, 0, each.rated ? (sameRank ? 2 : 14) : 10),
                            new Color(0, 0, 0, each.rated ? (sameRank ? 18 : 28) : 15),
                            new Color(0, 0, 0, each.rated ? 32 : 18)});
            outputCanvas.setPaint(tilePaint);
            outputCanvas.fillRoundRect(128, lineY.get() + 50, progressLen, 52, 52, 52);

            outputCanvas.setPaint(null);
            outputCanvas.setColor(Color.BLACK);
        }
        currentY.addAndGet(paddingBottom - 32);
    }

    /**
     * 绘制基础信息，如背景，标题
     *
     * @param outputImg   目标图片
     * @param totalHeight 计算得到的图片总高
     * @param title       标题
     * @param subtitle    副标题
     * @param currentY    开始绘制文本的高度
     * @return 目标图层
     * @throws Throwable 异常信息
     */
    private static Graphics2D drawBasicContent(BufferedImage outputImg, int totalHeight, StyledString title, StyledString subtitle, AtomicInteger currentY) throws Throwable {
        Image rankImg = QuickUtils.getImageByName("ic_ranking");

        Graphics2D outputCanvas = outputImg.createGraphics();
        outputCanvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        outputCanvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        totalHeight += 300;

        Pair<float[], Color[]> currentGradient = GradientColors.generateGradient();

        LinearGradientPaint bgPaint = new LinearGradientPaint(0, 0, 1216, totalHeight - 64, currentGradient.A, currentGradient.B);
        outputCanvas.setPaint(bgPaint);
        outputCanvas.fillRoundRect(32, 32, 1216, totalHeight - 64, 192, 192);

        outputCanvas.setPaint(null);
        outputCanvas.setColor(new Color(255, 255, 255, 180));
        outputCanvas.fillRoundRect(32, 32, 1216, totalHeight - 64, 192, 192);

        Color accentColor = currentGradient.B[0],
                accentDarkColor = accentColor.darker().darker().darker();

        outputCanvas.setColor(accentDarkColor);
        rankImg = ImgConvert.applyTint(rankImg, accentDarkColor);
        outputCanvas.drawImage(rankImg, 108, 160, 140, 140, null);
        drawText(outputCanvas, title, 260, 32, currentY);
        outputCanvas.setColor(new Color(accentDarkColor.getRed(), accentDarkColor.getGreen(), accentDarkColor.getBlue(), 136));
        drawText(outputCanvas, subtitle, 108, currentY);

        outputCanvas.setColor(Color.black);
        return outputCanvas;
    }

    /**
     * 绘制一个文本
     *
     * @param outputCanvas  目标绘制图层
     * @param content       文本内容
     * @param paddingBottom 下边距
     * @param currentY      文本左上角的纵坐标
     */
    private static void drawText(Graphics2D outputCanvas, StyledString content, int paddingBottom, AtomicInteger currentY) {
        drawText(outputCanvas, content, 128, paddingBottom, currentY);
    }

    /**
     * 绘制一个文本
     *
     * @param outputCanvas  目标绘制图层
     * @param content       文本内容
     * @param x             文本左上角的横坐标
     * @param paddingBottom 下边距
     * @param currentY      文本左上角的纵坐标
     */
    private static void drawText(Graphics2D outputCanvas, StyledString content, int x, int paddingBottom, AtomicInteger currentY) {
        ImgConvert.drawString(outputCanvas, content, x, currentY.get(), 1024, content.lineMultiplier);
        currentY.addAndGet(calculateHeight(content) + paddingBottom);
    }

    /**
     * 计算多个文本的总高度
     *
     * @param strings 多个文本
     * @return 总高度
     */
    private static int calculateHeight(StyledString... strings) {
        int height = 0;
        for (var each : strings) height += each.height;
        return height;
    }


    /**
     * 计算多个排行榜文本的总高度，以 32 像素为分隔
     *
     * @param strings 排行榜文本
     * @return 总高
     */
    @SafeVarargs
    private static int calculateHeight(List<RankDrawHolder>... strings) {
        int height = 0;
        for (var string : strings) {
            for (var each : string) height += each.strings[1].height + 40 + 32;
            height -= 32;
        }
        return height;
    }


    /**
     * 输出图片到文件
     *
     * @param img  图片
     * @param path 输出路径
     * @throws Throwable 异常信息
     */
    private static void saveImgToFile(BufferedImage img, String path) throws Throwable {
        File file = QuickUtils.fetchFile(path);
        if (file == null || !file.delete() || !file.createNewFile()) {
            throw new RunModuleException("File saved unsuccessfully.");
        }
        ImageIO.write(img, "png", file);
    }

    private record RankDrawHolder(double percent, boolean rated, StyledString... strings) {
    }

    public record UserInfoHolder(UserInfoData userInfoData, int submitCount, double submitAve, double acProportion,
                                 String submitDetail,
                                 SubmissionData lastSubmit, int trainingProgress) {
    }

    public record FullRankHolder(String top1, List<SimpleRankItem> top5, long submitUserAmount, int submitCount,
                                 double submitAve, double acProportion, String submitDetail,
                                 List<Pair<Double, Double>> hourlyInfoData, String hourlyInfoDetail,
                                 String firstACName, String firstACInfo, String mostPopularProblem,
                                 int mostPopularCount,
                                 List<SimpleRankItem> top10, List<SimpleRankItem> fullRank) {

        public FullRankHolder(String top1, SubmissionPackHolder submissionData, String mostPopularProblem, int mostPopularCount, List<SimpleRankItem> top10, List<SimpleRankItem> fullRank) {
            this(top1, submissionData.tops, submissionData.submitUserAmount, submissionData.submitCount, submissionData.submitAve, submissionData.acProportion, submissionData.submitDetail, submissionData.hourlyInfoData, submissionData.hourlyInfoDetail, submissionData.firstACName, submissionData.firstACInfo, mostPopularProblem, mostPopularCount, top10, fullRank);
        }
    }

    public record NowRankHolder(List<SimpleRankItem> tops, List<SimpleRankItem> top52, long submitUserAmount,
                                int submitCount, double submitAve, double acProportion, String submitDetail,
                                List<Pair<Double, Double>> hourlyInfoData, String hourlyInfoDetail,
                                String firstACName, String firstACInfo, int topCount) {
        public NowRankHolder(List<SimpleRankItem> top52, SubmissionPackHolder submissionData) {
            this(submissionData.tops, top52, submissionData.submitUserAmount, submissionData.submitCount, submissionData.submitAve, submissionData.acProportion, submissionData.submitDetail, submissionData.hourlyInfoData, submissionData.hourlyInfoDetail, submissionData.firstACName, submissionData.firstACInfo, submissionData.topCount);
        }
    }

    public record SubmissionPackHolder(List<SimpleRankItem> tops, long submitUserAmount, int submitCount,
                                       double submitAve, double acProportion, String submitDetail,
                                       List<Pair<Double, Double>> hourlyInfoData, String hourlyInfoDetail,
                                       String firstACName, String firstACInfo, int topCount) {
    }

    public record VerdictRankHolder(VerdictType verdict, List<SimpleRankItem> top10, int submitCount,
                                    double proportion) {
    }

}
