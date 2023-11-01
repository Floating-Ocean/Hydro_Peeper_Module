package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.Main;
import flocean.module.peeper.fjnuoj.data.rank.SimpleRankItem;
import flocean.module.peeper.fjnuoj.data.SubmissionData;
import flocean.module.peeper.fjnuoj.data.UserInfoData;
import flocean.module.peeper.fjnuoj.data.rank.SubmissionRankItem;
import flocean.module.peeper.fjnuoj.data.rank.TrainingRankItem;
import flocean.module.peeper.fjnuoj.enums.VerdictType;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;
import flocean.module.peeper.fjnuoj.utils.ImgConvert;
import flocean.module.peeper.fjnuoj.utils.Pair;
import flocean.module.peeper.fjnuoj.utils.ModuleFile;

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
     * @param path         输出路径
     * @throws Throwable 异常信息
     */
    public static void generateFullRankImg(FullRankHolder fullRankHolder, String path) throws Throwable {
        StyledString title = packString("昨日卷王天梯榜", "H", 96);
        StyledString subtitle = packString(new SimpleDateFormat("yyyy.MM.dd").format(new Date(System.currentTimeMillis() - 86400 * 1000))
                + "  FJNUACM Online Judge Rank List", "H", 36);

        StyledString top1Title = packString("昨日卷王", "B", 36);
        StyledString top1Who = packString(fullRankHolder.top1, "B", 72);

        StyledString top5Subtitle = packString("过题数榜单", "B", 36);
        StyledString top5Title = packString("昨日过题数", "B", 72);
        StyledString top5Mark = packString("Top 5th", "H", 48);
        List<Pair<Double, StyledString[]>> top5Who = new ArrayList<>();
        for(var each : fullRankHolder.top5()){
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, fullRankHolder.top5().get(0).val());
            StyledString[] current = {
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)
            };
            top5Who.add(Pair.of(currentRankItem.getProgress(), current));
        }

        StyledString submitCountTitle = packString("提交总数", "B", 36);
        StyledString submitCountHow = packString("" + fullRankHolder.submitCount, "B", 72);

        StyledString submitAveTitle = packString("提交平均分", "B", 36);
        StyledString submitAveHow = packString(String.format(Locale.ROOT, "%.2f", fullRankHolder.submitAve), "B", 72);

        StyledString submitAcTitle = packString("提交通过率", "B", 36);
        StyledString submitAcHow = packString(String.format(Locale.ROOT, "%.2f", fullRankHolder.acProportion), "B", 72);
        StyledString submitDetailWhat = packString("其中，共有 " + fullRankHolder.submitDetail, "M", 28);

        StyledString firstACTitle = packString("昨日最速通过", "B", 36);
        StyledString firstACWho = packString(fullRankHolder.firstACName, "B", 72);
        StyledString firstACWhat = packString(fullRankHolder.firstACInfo, "M", 28);

        StyledString mostPopularProblemTitle = packString("昨日最受欢迎的题目", "B", 36);
        StyledString mostPopularProblemWhat = packString(fullRankHolder.mostPopularProblem, "B", 72);
        StyledString mostPopularCountHow = packString("共有 " + fullRankHolder.mostPopularCount + " 个人提交本题", "M", 28);

        StyledString top10Subtitle = packString("训练榜单", "B", 36);
        StyledString top10Title = packString("新生训练题单完成比", "B", 72);
        StyledString top10Mark = packString("Top 10th", "H", 48);
        List<Pair<Double, StyledString[]>> top10Who = new ArrayList<>();
        for(var each : fullRankHolder.top10()){
            TrainingRankItem currentRankItem = new TrainingRankItem(each);
            StyledString[] current = {
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)
            };
            top10Who.add(Pair.of(currentRankItem.getProgress(), current));
        }

        StyledString fullRankSubtitle = packString("完整榜单", "B", 36);
        StyledString fullRankTitle = packString("昨日 OJ 总榜", "B", 72);
        List<Pair<Double, StyledString[]>> fullRankWho = new ArrayList<>();
        for(var each : fullRankHolder.fullRank()){
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, fullRankHolder.fullRank().get(0).val());
            StyledString[] current = {
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)
            };
            fullRankWho.add(Pair.of(currentRankItem.getProgress(), current));
        }

        StyledString copyright = packString(String.format(Locale.ROOT, """
                Generated by %s.
                ©2023 Floating Ocean.
                At""", VersionControl.fetchVersionInfoInline(Main.class)) + " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), "B", 24, 1.32);


        int totalHeight = calculateHeight(
                title, subtitle,
                top1Title, top1Who,
                top5Subtitle, top5Title,
                submitCountTitle, submitCountHow, submitDetailWhat,
                firstACTitle, firstACWho, firstACWhat,
                mostPopularProblemTitle, mostPopularProblemWhat, mostPopularCountHow,
                top10Subtitle, top10Title,
                fullRankSubtitle, fullRankTitle,
                copyright) +
                calculateHeight(top5Who, top10Who, fullRankWho) + 1176;

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

        AtomicInteger unchangedY = new AtomicInteger(currentY.get());
        drawText(outputCanvas, submitCountTitle, 8, currentY);
        drawText(outputCanvas, submitCountHow, 32, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        int submitCountWidth = Math.max(
                ImgConvert.calculateStringWidth(submitCountTitle.font, submitCountTitle.content),
                ImgConvert.calculateStringWidth(submitCountHow.font, submitCountHow.content));
        drawText(outputCanvas, submitAveTitle, 128 + submitCountWidth + 150, 8, currentY);
        drawText(outputCanvas, submitAveHow, 128 + submitCountWidth + 150, 32, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        submitCountWidth = submitCountWidth + Math.max(
                ImgConvert.calculateStringWidth(submitAveTitle.font, submitAveTitle.content),
                ImgConvert.calculateStringWidth(submitAveHow.font, submitAveHow.content));
        drawText(outputCanvas, submitAcTitle, 128 + 128 + submitCountWidth + 150, 8, currentY);
        drawText(outputCanvas, submitAcHow, 128 + 128 + submitCountWidth + 150, 32, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, submitDetailWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
        drawText(outputCanvas, firstACTitle, 8, currentY);
        drawText(outputCanvas, firstACWho, 32, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, firstACWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
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
     * @param path        输出路径
     * @throws Throwable 异常信息
     */
    public static void generateNowRankImg(NowRankHolder nowRankHolder, String path) throws Throwable {
        StyledString title = packString("今日当前题数榜单", "H", 96);
        StyledString subtitle = packString(new SimpleDateFormat("yyyy.MM.dd").format(new Date())
                + "  FJNUACM Online Judge Rank List", "H", 36);

        StyledString top5Subtitle = packString("过题数榜单", "B", 36);
        StyledString top5Title = packString("今日过题数", "B", 72);
        StyledString top5Mark = packString("Top 5th", "H", 48);
        List<Pair<Double, StyledString[]>> top5Who = new ArrayList<>();
        for(var each : nowRankHolder.top5()){
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, nowRankHolder.top5().get(0).val());
            StyledString[] current = {
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)
            };
            top5Who.add(Pair.of(currentRankItem.getProgress(), current));
        }

        StyledString submitCountTitle = packString("提交总数", "B", 36);
        StyledString submitCountHow = packString("" + nowRankHolder.submitCount, "B", 72);

        StyledString submitAveTitle = packString("提交平均分", "B", 36);
        StyledString submitAveHow = packString(String.format(Locale.ROOT, "%.2f", nowRankHolder.submitAve), "B", 72);

        StyledString submitAcTitle = packString("提交通过率", "B", 36);
        StyledString submitAcHow = packString(String.format(Locale.ROOT, "%.2f", nowRankHolder.acProportion), "B", 72);
        StyledString submitDetailWhat = packString("其中，共有 " + nowRankHolder.submitDetail, "M", 28);

        StyledString firstACTitle = packString("今日最速通过", "B", 36);
        StyledString firstACWho = packString(nowRankHolder.firstACName, "B", 72);
        StyledString firstACWhat = packString(nowRankHolder.firstACInfo, "M", 28);

        StyledString top52Subtitle = packString("训练榜单", "B", 36);
        StyledString top52Title = packString("新生训练题单完成比", "B", 72);
        StyledString top52Mark = packString("Top 5th", "H", 48);
        List<Pair<Double, StyledString[]>> top52Who = new ArrayList<>();
        for(var each : nowRankHolder.top52()){
            TrainingRankItem currentRankItem = new TrainingRankItem(each);
            StyledString[] current = {
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)
            };
            top52Who.add(Pair.of(currentRankItem.getProgress(), current));
        }

        StyledString copyright = packString(String.format(Locale.ROOT, """
                Generated by %s.
                ©2023 Floating Ocean.
                At""", VersionControl.fetchVersionInfoInline(Main.class)) + " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), "B", 24, 1.32);

        int totalHeight = calculateHeight(
                title, subtitle,
                top5Subtitle, top5Title,
                submitCountTitle, submitCountHow, submitDetailWhat,
                firstACTitle, firstACWho, firstACWhat,
                top52Subtitle, top52Title,
                copyright) +
                calculateHeight(top5Who, top52Who) + 736;

        BufferedImage outputImg = new BufferedImage(1280, totalHeight + 300, BufferedImage.TYPE_INT_BGR);
        AtomicInteger currentY = new AtomicInteger(134);
        Graphics2D outputCanvas = drawBasicContent(outputImg, totalHeight, title, subtitle, currentY);

        drawText(outputCanvas, top5Subtitle, 8, currentY);
        drawText(outputCanvas, top5Title, 32, currentY);
        currentY.addAndGet(-96);
        drawText(outputCanvas, top5Mark, 128 + ImgConvert.calculateStringWidth(top5Title.font, top5Title.content) + 28, 32, currentY);
        drawRankText(outputCanvas, top5Who, 108, currentY);

        AtomicInteger unchangedY = new AtomicInteger(currentY.get());
        drawText(outputCanvas, submitCountTitle, 8, currentY);
        drawText(outputCanvas, submitCountHow, 32, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        int submitCountWidth = Math.max(
                ImgConvert.calculateStringWidth(submitCountTitle.font, submitCountTitle.content),
                ImgConvert.calculateStringWidth(submitCountHow.font, submitCountHow.content));
        drawText(outputCanvas, submitAveTitle, 128 + submitCountWidth + 150, 8, currentY);
        drawText(outputCanvas, submitAveHow, 128 + submitCountWidth + 150, 32, currentY);

        currentY.set(unchangedY.get()); //保持同一行
        submitCountWidth = submitCountWidth + Math.max(
                ImgConvert.calculateStringWidth(submitAveTitle.font, submitAveTitle.content),
                ImgConvert.calculateStringWidth(submitAveHow.font, submitAveHow.content));
        drawText(outputCanvas, submitAcTitle, 128 + 128 + submitCountWidth + 150, 8, currentY);
        drawText(outputCanvas, submitAcHow, 128 + 128 + submitCountWidth + 150, 32, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, submitDetailWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
        drawText(outputCanvas, firstACTitle, 8, currentY);
        drawText(outputCanvas, firstACWho, 32, currentY);
        outputCanvas.setColor(new Color(0, 0, 0, 136));
        drawText(outputCanvas, firstACWhat, 108, currentY);

        outputCanvas.setColor(Color.black);
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
     * @param path            输出路径
     * @throws Throwable 异常信息
     */
    public static void generateVerdictRankImg(VerdictRankHolder verdictRankHolder, String path) throws Throwable {
        StyledString title = packString("今日当前提交榜单", "H", 96);
        StyledString subtitle = packString(new SimpleDateFormat("yyyy.MM.dd").format(new Date())
                + "  FJNUACM Online Judge Rank List", "H", 36);

        StyledString submitCountTitle = packString("提交总数", "B", 36);
        StyledString submitCountHow = packString("" + verdictRankHolder.submitCount, "B", 72);

        StyledString proportionTitle = packString(verdictRankHolder.verdict.getName() + " 占比", "B", 36);
        StyledString proportionHow = packString(String.format(Locale.ROOT, "%.2f", verdictRankHolder.proportion), "B", 72);

        StyledString top10Subtitle = packString("分类型提交榜单", "B", 36);
        StyledString top10Title = packString(verdictRankHolder.verdict.getName() + " 排行榜", "B", 72);
        StyledString top10Mark = packString("Top 10th", "H", 48);
        List<Pair<Double, StyledString[]>> top10Who = new ArrayList<>();
        for(var each : verdictRankHolder.top10()){
            SubmissionRankItem currentRankItem = new SubmissionRankItem(each, verdictRankHolder.top10().get(0).val());
            StyledString[] current = {
                    packString(currentRankItem.fetchRank(), "H", 64),
                    packString(currentRankItem.fetchWho(), "B", 36),
                    packString(currentRankItem.fetchVal(), "H", 36)
            };
            top10Who.add(Pair.of(currentRankItem.getProgress(), current));
        }

        StyledString copyright = packString(String.format(Locale.ROOT, """
                Generated by %s.
                ©2023 Floating Ocean.
                At""", VersionControl.fetchVersionInfoInline(Main.class)) + " " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), "B", 24, 1.32);

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


    private static void drawRankText(Graphics2D outputCanvas, List<Pair<Double, StyledString[]>> content, int paddingBottom, AtomicInteger currentY){
        for(var each : content){
            int progressLen = (int)(360 + 440 * each.A);

            AtomicInteger lineY = new AtomicInteger(currentY.get());
            int currentX = 128 + 32;
            drawText(outputCanvas, each.B[0], currentX, 12, currentY);

            currentX += ImgConvert.calculateStringWidth(each.B[0].font, each.B[0].content) + 28;
            currentY.set(lineY.get() + 40);
            drawText(outputCanvas, each.B[1], currentX, 12, currentY);

            currentX = Math.max(progressLen + 128, currentX + ImgConvert.calculateStringWidth(each.B[1].font, each.B[1].content)) + 36;
            currentY.set(lineY.get() + 40);
            drawText(outputCanvas, each.B[2], currentX, 32, currentY);

            LinearGradientPaint tilePaint = new LinearGradientPaint(0, 52, progressLen, 52, new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{new Color(0, 0, 0, 14), new Color(0, 0, 0, 28), new Color(0, 0, 0, 32)});
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
        Image rankImg = ImageIO.read(new File("/home/floatingcean/fjnuacm_rank/lib/img/ic_ranking.png"));

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
    public static int calculateHeight(StyledString... strings) {
        int height = 0;
        for (var each : strings) height += each.height;
        return height;
    }


    /**
     * 计算多个排行榜文本的总高度，以 32 像素为分隔
     * @param strings 排行榜文本
     * @return 总高
     */
    @SafeVarargs
    public static int calculateHeight(List<Pair<Double, StyledString[]>>... strings) {
        int height = 0;
        for(var string : strings) {
            for (var each : string) height += each.B[1].height + 40 + 32;
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
        File file = ModuleFile.fetchFile(path);
        if (file == null || !file.delete() || !file.createNewFile()) {
            throw new RunModuleException("File saved unsuccessfully.");
        }
        ImageIO.write(img, "png", file);
    }

    public record UserInfoHolder(UserInfoData userInfoData, int submitCount, double submitAve, double acProportion, String submitDetail,
                                 SubmissionData lastSubmit, int trainingProgress) {
    }

    public record FullRankHolder(String top1, List<SimpleRankItem> top5, int submitCount, double submitAve, double acProportion, String submitDetail,
                                 String firstACName, String firstACInfo, String mostPopularProblem, int mostPopularCount,
                                 List<SimpleRankItem> top10, List<SimpleRankItem> fullRank) {

        public FullRankHolder(String top1, SubmissionPackHolder submissionData, String mostPopularProblem, int mostPopularCount, List<SimpleRankItem> top10, List<SimpleRankItem> fullRank) {
            this(top1, submissionData.top5, submissionData.submitCount, submissionData.submitAve, submissionData.acProportion, submissionData.submitDetail, submissionData.firstACName, submissionData.firstACInfo, mostPopularProblem, mostPopularCount, top10, fullRank);
        }
    }

    public record NowRankHolder(List<SimpleRankItem> top5, List<SimpleRankItem> top52, int submitCount, double submitAve, double acProportion, String submitDetail,
                                String firstACName, String firstACInfo) {
        public NowRankHolder(List<SimpleRankItem> top52, SubmissionPackHolder submissionData) {
            this(submissionData.top5, top52, submissionData.submitCount, submissionData.submitAve, submissionData.acProportion, submissionData.submitDetail, submissionData.firstACName, submissionData.firstACInfo);
        }
    }

    public record SubmissionPackHolder(List<SimpleRankItem> top5, int submitCount, double submitAve, double acProportion, String submitDetail,
                                       String firstACName, String firstACInfo) {
    }

    public record VerdictRankHolder(VerdictType verdict, List<SimpleRankItem> top10, int submitCount, double proportion) {
    }
}
