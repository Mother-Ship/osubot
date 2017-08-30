package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by QHS on 2017/8/18.
 */
public class pageUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String getAvaURL = "https://a.ppy.sh/";
    private final String getUserURL = "https://osu.ppy.sh/u/";
    private final String getUserProfileURL = "https://osu.ppy.sh/pages/include/profile-general.php?u=";
    private final String getBGURL = "http://bloodcat.com/osu/i/";
    //后续在这个类里解析dom树获取网页内容
    //将异常抛出给调用者
    public BufferedImage getAvatar(int uid) throws IOException {
        URL avaurl = new URL(getAvaURL + uid + "?.png");
        return ImageIO.read(avaurl);
    }

    public BufferedImage getBG(int bid)throws IOException {
        HttpURLConnection httpConnection = null;
        int retry = 0;
        BufferedImage img = null;
        while (retry < 5) {
            try {

                httpConnection =
                        (HttpURLConnection) new URL(getBGURL + bid).openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setConnectTimeout(5000);
                httpConnection.setReadTimeout(5000);
                httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.40 Safari/537.36");
                if (httpConnection.getResponseCode() != 200) {
                    logger.error("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                Thread.sleep(2000);
                img = ImageIO.read(httpConnection.getInputStream());
                //手动关闭流
                httpConnection.disconnect();

                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        if (retry == 5) {
            logger.error("获取" + bid + "的背景图，失败五次");
            throw new IOException();
        }
        return img;

    }

    public int getRepWatched(int uid) {
        int retry = 0;
        Document doc = null;
        while (retry < 5) {
            try {
                logger.info("正在获取" + uid + "的Replays被观看次数");
                doc = Jsoup.connect(getUserProfileURL + uid).get();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1)  + "次");
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
        if (rScore == endValue) {
            return end;
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


    private long getScore(int rank){
        Document doc = null;
        int retry = 0;
        logger.info("正在抓取#"+rank+"的玩家的分数");
        //一定要把除出来的值强转
        //math.round好像不太对，应该是ceil
        int p = (int)Math.ceil((float) rank/50);
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
        String score = doc.select("td[class*=focused]").get(num).child(0).attr("title");
        return Long.valueOf(score.replace(",",""));

    }

    public Date getLastActive(int uid){
        int retry = 0;
        Document doc = null;
        while (retry < 5) {
            try {
                logger.info("正在获取" + uid + "的上次活跃时间");
                doc = Jsoup.connect(getUserURL + uid).get();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }
        }
        if (retry == 5) {
            logger.error("玩家" + uid + "请求API获取数据，失败五次");
            return null;
        }
        Elements link = doc.select("time[class*=timeago]");
        String a = link.get(1).text();
        a = a.substring(0,19);
        try {
            //转换为北京时间
            return new Date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(a).getTime()+8*3600*1000);
        } catch (ParseException e) {
            logger.error("将时间转换为Date对象出错");
        }
        return null;
    }

}