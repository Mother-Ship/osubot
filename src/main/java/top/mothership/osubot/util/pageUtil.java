package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by QHS on 2017/8/18.
 */
public class pageUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String getAvaURL = "https://a.ppy.sh/";

    //TODO 后续解析dom树获取网页内容
    //将异常抛出给调用者
    public BufferedImage getAvatar(int uid) throws IOException {
            URL avaurl = new URL(getAvaURL + uid + "?.png");
            return ImageIO.read(avaurl);
    }


}
