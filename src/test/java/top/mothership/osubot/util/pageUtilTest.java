package top.mothership.osubot.util;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;



public class pageUtilTest extends TestCase {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String getUserURL = "https://osu.ppy.sh/pages/include/profile-general.php?u=";
    private final int uid = 7679162;
    private apiUtil apiUtil = new apiUtil();
    public void testGetRepWatched() throws Exception {
        int scoreRank = 0;
        int retry = 0;
        long rScore = apiUtil.getUser(null,uid).getRanked_score();
        Document doc = null;
        for(int i=0;i<40;i++) {
            long score = 0;
            while (retry < 5) {
                try {
                    logger.info("正在抓取第"+(i+1)+"页的排名");
                    doc = Jsoup.connect("https://osu.ppy.sh/rankings/osu/score?page="+(i+1)).timeout(5000).get();
                    score = Long.valueOf(doc.select("td[class*=focused] > span").get(49).attr("title").replace(",", ""));
                    break;
                } catch (IOException e) {
                    logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                }

            }
            if (retry == 5) {
                logger.error("请求全球排名失败五次");
                return;
            }

            if(rScore>score){
                Elements ele = doc.getElementsByAttribute("data-user-id");
                break;
            }

        }
    if(scoreRank==0){
            //看情况处理，反正是没在前2k
    }


    }
}