package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Created by QHS on 2017/8/18.
 */
public class pageUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String getAvaURL = "https://a.ppy.sh/";
    private final String getUserURL = "https://osu.ppy.sh/pages/include/profile-general.php?u=";

    //后续在这个类里解析dom树获取网页内容
    //将异常抛出给调用者
    public BufferedImage getAvatar(int uid) throws IOException {
        URL avaurl = new URL(getAvaURL + uid + "?.png");
        return ImageIO.read(avaurl);
    }

    //1.1预计功能：获取reps watched，获取score rank,欢迎新人
    //
    public int getRepWatched(int uid) {
        int retry = 0;
        Document doc = null;
        while (retry < 5) {
            try {
                logger.info("正在获取" + uid + "的Replays被观看次数");
                doc = Jsoup.connect(getUserURL + uid).get();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + retry + 1 + "次");
                retry++;
            }
        }
        if (retry == 5) {
            logger.error("玩家" + uid + "请求API获取数据，失败五次");
            return 0;
        }
        Elements link = doc.select("div[title*=replays]");
        String a = link.text();
        a = a.substring(27).replace(" times", "").replace(",", "");
        return Integer.valueOf(a);
    }

    public int getRank(long rScore, int start, int end){
        long endValue = getScore(end);
        if (rScore < endValue||endValue==0) {
            return 0;
        }
        //第一次写二分法……不过大部分时间都花在算准确页数，和拿页面元素上了
        while (start <= end) {
            int middle = (start + end) / 2;


            long middleValue = getScore(middle);

            if (middleValue == 0) {
                return 0;
            }
            if (rScore == middleValue) {
                // 等于中值直接返回
                return middle;
            } else if (rScore > middleValue) {
                //rank和分数成反比，所以大于反而rank要在前半部分找
                end = middle - 1;
            } else {
                start = middle + 1;
            }
        }
        return 0;
    }


    public long getScore(int rank){
        Document doc = null;
        int retry = 0;
        logger.info("正在抓取#"+rank+"的玩家的分数");
        //一定要把除出来的值强转
        int p = Math.round((float) rank/50);
        //获取当前rank在当前页的第几个
        int num = (rank-1)%50;
        while (retry < 5) {
            try {
                doc = Jsoup.connect("https://osu.ppy.sh/rankings/osu/score?page="+p).timeout(5000).get();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }

        }
        if (retry == 5) {
            logger.error("查询分数失败五次");
            return 0;
        }
        return Long.valueOf(doc.select("td[class*=focused]").get(num).child(0).attr("title").replace(",",""));

    }
}