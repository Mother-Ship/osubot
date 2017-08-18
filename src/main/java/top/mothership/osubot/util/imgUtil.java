package top.mothership.osubot.util;

import com.google.common.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class imgUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    //TODO 最后生成之后压缩图片减少发送时间
    //TODO recentBP的绘制
    /*设计这个方法
    它应该输入username，应该输出一张完整的stat图的路径
    */
    private ResourceBundle rb;
    private pageUtil pageUtil;
    private dbUtil dbUtil;
    private apiUtil apiUtil;

    public imgUtil() {
        //构造器内初始化配置文件以及工具类
        pageUtil = new pageUtil();
        dbUtil = new dbUtil();
        apiUtil = new apiUtil();
        rb = ResourceBundle.getBundle("cabbage");
    }

    public String drawUserInfo(String userName, int day) {
        //将传入的天数转换为Date对象
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -day);
        //试图查询数据库中指定日期的user
        User userInDB = dbUtil.getUserInfo(userName, new Date(c.getTimeInMillis()));
        User userFromAPI = null;
        try {
            userFromAPI = apiUtil.getUser(userName);
        } catch (IOException e) {
            logger.error("从api获取用户信息失败");
            logger.error(e.getMessage());
            return "error";
        }
        //准备资源：背景图和用户头像，以及重画之后的用户头像
        BufferedImage bg = null;
        BufferedImage ava = null;
        BufferedImage resizedAva = null;
        try {
            //使用guava的类直接调用图片
            bg = ImageIO.read(new File(Resources.getResource(rb.getString("bg")).toURI()));
        } catch (IOException | URISyntaxException e) {
            logger.error("读取背景图片失败");
            logger.error(e.getMessage());
            return "error";
        }
        try {
            //此处传入的应该是用户的数字id
            ava = pageUtil.getAvatar(userFromAPI.getUser_id());
        } catch (IOException e) {
            logger.error("从官网获取头像失败");
            logger.error(e.getMessage());
            return "error";
        }

        //进行缩放
        if (ava.getHeight() > 128 || ava.getWidth() > 128) {
            //获取原图比例，将较大的值除以128，然后把较小的值去除以这个f
            int resizedHeight = 0;
            int resizedWidth = 0;
            if (ava.getHeight() > ava.getWidth()) {
                float f = (float) ava.getHeight() / 128;
                resizedHeight = 128;
                resizedWidth = (int) (ava.getWidth() / f);
            } else {
                float f = (float) ava.getWidth() / 128;
                resizedHeight = (int) (ava.getHeight() / f);
                resizedWidth = 128;
            }
            resizedAva = new BufferedImage(resizedWidth, resizedHeight, ava.getType());
            Graphics2D g2 = (Graphics2D) resizedAva.getGraphics();
            //这么重画有点锯齿严重，试试其他办法
//          g2.drawImage(ava, 0, 0, resizedWidth, resizedHeight, null);
            g2.drawImage(ava.getScaledInstance(resizedWidth,resizedHeight,Image.SCALE_SMOOTH), 0, 0, resizedWidth, resizedHeight, null);
            g2.dispose();
        } else {
            //如果不需要缩小，直接把引用转过来
            resizedAva = ava;
        }
        //绘制图片
        Graphics2D g2 = (Graphics2D) bg.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //先把头像画上去
        g2.drawImage(resizedAva, Integer.decode(rb.getString("avax")), Integer.decode(rb.getString("avay")), null);
        //开启平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //绘制用户名
        {
            //准备字体
            Font uNameFont = new Font(rb.getString("unameFont"), Font.PLAIN, Integer.decode(rb.getString("unameSize"))); // 创建字体对象
            //指定字体颜色
            g2.setPaint(Color.decode("000000"));
            //指定字体
            g2.setFont(uNameFont);
            //参数2
            g2.drawString(userName, Integer.decode(rb.getString("namex")), Integer.decode(rb.getString("namey")));
        }

        g2.dispose();
        try {
            ImageIO.write(bg, "png", new File("e:\\" + userName + ".png"));
        } catch (IOException e) {
            logger.error("绘制图片成品失败");
            logger.error(e.getMessage());
        }
        return "e:\\" + userName + ".png";

    }


}
