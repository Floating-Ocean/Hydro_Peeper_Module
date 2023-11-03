package flocean.module.peeper.fjnuoj.tool;

import flocean.module.peeper.fjnuoj.config.Global;
import flocean.module.peeper.fjnuoj.data.RankingData;
import flocean.module.peeper.fjnuoj.data.TrainingData;
import flocean.module.peeper.fjnuoj.utils.QuickUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class TrainingTool {

    /**
     * 爬取训练榜单
     *
     * @param newbieRankData        新生排行榜数据
     * @param onCompleteOneListener 完成一个用户的爬取将会调用的监听器
     * @return 训练数据
     * @throws Throwable 异常信息
     */
    public static List<TrainingData> fetchData(List<RankingData> newbieRankData, Runnable onCompleteOneListener) throws Throwable {
        List<TrainingData> trainingDataList = new ArrayList<>();
        for (var each : newbieRankData) {
            trainingDataList.add(new TrainingData(each, fetchSingleData(each.id())));
            onCompleteOneListener.run();
        }
        return trainingDataList;
    }


    public static int fetchSingleData(int uid) throws Throwable{
        final String url = Global.config.ojUrl() + "training/64bcda4defe7b59ef9a36453?uid=" + uid;
        Document document = QuickUtils.wrapWithCookie(Jsoup.connect(url)).get();

        Element status = document.getElementsByClass("large horizontal").get(0)
                .getElementsByTag("dd").get(0);
        if (status.text().equals("未参加")) return -1;

        Element data = document.getElementsByClass("large horizontal").get(0)
                .getElementsByTag("dd").get(1);

        return Integer.parseInt(data.text().strip().split(" ")[1].split("%")[0]);
    }

}
