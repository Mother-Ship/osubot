package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

    //后续在这个类里解析dom树获取网页内容
    //将异常抛出给调用者
    public BufferedImage getAvatar(int uid) throws IOException {
            URL avaurl = new URL(getAvaURL + uid + "?.png");
            return ImageIO.read(avaurl);
    }
    //1.1预计功能：获取reps watched，获取score rank，
    public int getRepWatched(String username) throws IOException {
        Document doc = Jsoup.connect("http://www.oschina.net/").get();
        //练习使用选择器
        return 0;
    }
}
