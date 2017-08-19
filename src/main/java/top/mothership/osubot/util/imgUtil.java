package top.mothership.osubot.util;

import cc.plural.jsonij.JSON;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class imgUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    //TODO recentBP的绘制
    /*设计这个方法
    它应该输入username，应该输出一张完整的stat图的路径
    */
    private ResourceBundle rb;
    private pageUtil pageUtil;

    public imgUtil() {
        //构造器内初始化配置文件以及工具类
        pageUtil = new pageUtil();
        rb = ResourceBundle.getBundle("cabbage");
    }

    public String drawUserInfo(User userFromAPI, User userInDB, int day, boolean near) {
        //准备资源：背景图和用户头像，以及重画之后的用户头像
        BufferedImage bg = null;
        BufferedImage layout = null;
        BufferedImage ava = null;
        BufferedImage resizedAva = null;
        try {
            //使用guava的类直接调用图片
            bg = ImageIO.read(new File(Resources.getResource(rb.getString("userbg")).toURI()));
            //layout = ImageIO.read(new File(Resources.getResource(rb.getString("userlayout")).toURI()));
        } catch (IOException | URISyntaxException e) {
            logger.error("读取背景图片失败");
            logger.error(e.getMessage());
            return "error";
        }
        Graphics2D g2 = (Graphics2D) bg.getGraphics();


        try {
            //此处传入的应该是用户的数字id
            ava = pageUtil.getAvatar(userFromAPI.getUser_id());
        } catch (IOException | NullPointerException e) {
            logger.error("从官网获取头像失败");
            logger.error(e.getMessage());
        }
        if (ava != null) {
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
                Graphics2D g = (Graphics2D) resizedAva.getGraphics();
                //这么重画有点锯齿严重，试试其他办法
//          g.drawImage(ava, 0, 0, resizedWidth, resizedHeight, null);
                g.drawImage(ava.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH), 0, 0, resizedWidth, resizedHeight, null);
                g.dispose();
            } else {
                //如果不需要缩小，直接把引用转过来
                resizedAva = ava;
            }
            //先把头像画上去
            g2.drawImage(resizedAva, Integer.decode(rb.getString("avax")), Integer.decode(rb.getString("avay")), null);
        }
        //绘制文字

        //预留的把布局画到背景图上的代码
        //g2.drawImage(layout,0,0,null);


        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //开启平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //绘制用户名

        //指定字体颜色
        g2.setPaint(Color.decode(rb.getString("unameColor")));
        //指定字体
        g2.setFont(new Font(rb.getString("unameFont"), 0, Integer.decode(rb.getString("unameSize"))));
        //指定坐标
        g2.drawString(userFromAPI.getUsername(), Integer.decode(rb.getString("namex")), Integer.decode(rb.getString("namey")));

        //绘制Rank
        g2.setPaint(Color.decode(rb.getString("defaultColor")));
        g2.setFont(new Font(rb.getString("numberFont"), 0, Integer.decode(rb.getString("rankSize"))));
        g2.drawString("#" + userFromAPI.getPp_rank(), Integer.decode(rb.getString("rankx")), Integer.decode(rb.getString("ranky")));

        //绘制PP
        g2.setPaint(Color.decode(rb.getString("ppColor")));
        g2.setFont(new Font(rb.getString("numberFont"), 0, Integer.decode(rb.getString("ppSize"))));
        g2.drawString(userFromAPI.getPp_raw().toString(),
                Integer.decode(rb.getString("ppx")), Integer.decode(rb.getString("ppy")));

        //绘制RankedScore
        g2.setPaint(Color.decode(rb.getString("defaultColor")));
        g2.setFont(new Font(rb.getString("numberFont"), 0, Integer.decode(rb.getString("numberSize"))));
        g2.drawString(new DecimalFormat("###,###").format(userFromAPI.getRanked_score()),
                Integer.decode(rb.getString("rScorex")), Integer.decode(rb.getString("rScorey")));

        //绘制acc
        g2.drawString(new DecimalFormat("###.00").format(userFromAPI.getAccuracy()) + "%",
                Integer.decode(rb.getString("accx")), Integer.decode(rb.getString("accy")));

        //绘制pc
        g2.drawString(new DecimalFormat("###,###").format(userFromAPI.getPlaycount()),
                Integer.decode(rb.getString("pcx")), Integer.decode(rb.getString("pcy")));

        //绘制tth
        g2.drawString(new DecimalFormat("###,###").format(userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300()),
                Integer.decode(rb.getString("tthx")), Integer.decode(rb.getString("tthy")));

        //绘制Level
        g2.drawString(Integer.toString((int) Math.floor(userFromAPI.getLevel())) + " (" + (int) ((userFromAPI.getLevel() - Math.floor(userFromAPI.getLevel())) * 100) + "%)",
                Integer.decode(rb.getString("levelx")), Integer.decode(rb.getString("levely")));

        //绘制SS计数
        g2.setPaint(Color.decode(rb.getString("defaultColor")));
        g2.setFont(new Font(rb.getString("numberFont"), 0, Integer.decode(rb.getString("countSize"))));
        g2.drawString(Integer.toString(userFromAPI.getCount_rank_ss()),
                Integer.decode(rb.getString("ssCountx")), Integer.decode(rb.getString("ssCounty")));

        //绘制S计数
        g2.drawString(Integer.toString(userFromAPI.getCount_rank_s())
                , Integer.decode(rb.getString("sCountx")), Integer.decode(rb.getString("sCounty")));

        //绘制A计数
        g2.drawString(Integer.toString(userFromAPI.getCount_rank_a())
                , Integer.decode(rb.getString("aCountx")), Integer.decode(rb.getString("aCounty")));

        //绘制当时请求的时间
        //拿到当前时间对应的北京时间
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        g2.setPaint(Color.decode(rb.getString("timeColor")));
        g2.setFont(new Font(rb.getString("timeFont"), Font.PLAIN, Integer.decode(rb.getString("timeSize"))));
        g2.drawString(new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()),
                Integer.decode(rb.getString("timex")), Integer.decode(rb.getString("timey")));

        //---------------------------以上绘制在线部分完成--------------------------------
        //试图查询数据库中指定日期的user
        if (day > 0) {
            /*
                不带参数：day=1，调用dbUtil拿0天前（日期不变，当天）的数据进行对比（实际上是昨天结束时候的成绩）
                带day = 0:进入本方法，不读数据库，不进行对比
                day>1
             */
            if (day > 1) {
                //只有day>1才会出现文字
                g2.setFont(new Font(rb.getString("diffFont"), 0, Integer.decode(rb.getString("tipSize"))));
                g2.setPaint(Color.decode(rb.getString("tipColor")));
                //TODO 更改提示文字的位置
                if (near) {
                    //如果取到的是模糊数据
                    g2.drawString("Compared with Nearest Data",
                            Integer.decode(rb.getString("tipx")), Integer.decode(rb.getString("tipy")));
                    //+userInDB.getQueryDate().toString()+
                } else {
                    //如果取到的是精确数据
                    g2.drawString("Compared with" + day + "days ago.",
                            Integer.decode(rb.getString("tipx")), Integer.decode(rb.getString("tipy")));
                }

            }


            //这样确保了userInDB不是空的
            //绘制Rank变化
            g2.setFont(new Font(rb.getString("diffFont"), 0, Integer.decode(rb.getString("diffSize"))));
            if (userInDB.getPp_rank() > userFromAPI.getPp_rank()) {
                //如果查询的rank比凌晨中的小
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(userInDB.getPp_rank() - userFromAPI.getPp_rank()) + ")",
                        Integer.decode(rb.getString("rankDiffx")), Integer.decode(rb.getString("rankDiffy")));
            } else if (userInDB.getPp_rank() < userFromAPI.getPp_rank()) {
                //如果掉了rank
                g2.setPaint(Color.decode(rb.getString("downColor")));
                g2.drawString("(↓" + Integer.toString(userFromAPI.getPp_rank() - userInDB.getPp_rank()) + ")",
                        Integer.decode(rb.getString("rankDiffx")), Integer.decode(rb.getString("rankDiffy")));
            } else {
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("rankDiffx")), Integer.decode(rb.getString("rankDiffy")));
            }
            //绘制PP变化
            if (userInDB.getPp_raw() > userFromAPI.getPp_raw()) {
                //如果查询的pp比凌晨中的小
                g2.setPaint(Color.decode(rb.getString("downColor")));
                g2.drawString("(↓" + new DecimalFormat("##0.00").format(userInDB.getPp_raw() - userFromAPI.getPp_raw()) + ")",
                        Integer.decode(rb.getString("ppDiffx")), Integer.decode(rb.getString("ppDiffy")));
            } else if (userInDB.getPp_rank() < userFromAPI.getPp_rank()) {
                //刷了PP
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + new DecimalFormat("##0.00").format(userFromAPI.getPp_raw() - userInDB.getPp_raw()) + ")",
                        Integer.decode(rb.getString("ppDiffx")), Integer.decode(rb.getString("ppDiffy")));
            } else {
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("ppDiffx")), Integer.decode(rb.getString("ppDiffy")));
            }

            //绘制RankedScore变化
            g2.setPaint(Color.decode(rb.getString("upColor")));
            if (userInDB.getRanked_score() < userFromAPI.getRanked_score()) {
                //因为RankedScore不会变少，所以不写蓝色部分
                g2.drawString("(↑" + new DecimalFormat("###,###").format(userFromAPI.getRanked_score() - userInDB.getRanked_score()) + ")",
                        Integer.decode(rb.getString("rScoreDiffx")), Integer.decode(rb.getString("rScoreDiffy")));
            } else {
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("rScoreDiffx")), Integer.decode(rb.getString("rScoreDiffy")));
            }
            //绘制ACC变化
            //在这里把精度砍掉
            if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getAccuracy())) > Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getAccuracy()))) {
                //如果acc降低了
                g2.setPaint(Color.decode(rb.getString("downColor")));
                g2.drawString("(↓" + new DecimalFormat("##0.00").format(userInDB.getAccuracy() - userFromAPI.getAccuracy()) + "%)",
                        Integer.decode(rb.getString("accDiffx")), Integer.decode(rb.getString("accDiffy")));
            } else if (userInDB.getAccuracy() < userFromAPI.getAccuracy()) {
                //提高
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + new DecimalFormat("##0.00").format(userFromAPI.getAccuracy() - userInDB.getAccuracy()) + "%)",
                        Integer.decode(rb.getString("accDiffx")), Integer.decode(rb.getString("accDiffy")));
            } else {
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + new DecimalFormat("##0.00").format(0.00) + "%)",
                        Integer.decode(rb.getString("accDiffx")), Integer.decode(rb.getString("accDiffy")));
            }

            //绘制pc变化
            g2.setPaint(Color.decode(rb.getString("upColor")));
            if (userInDB.getPlaycount() < userFromAPI.getPlaycount()) {
                //同理不写蓝色部分
                g2.drawString("(↑" + new DecimalFormat("###,###").format(userFromAPI.getPlaycount() - userInDB.getPlaycount()) + ")",
                        Integer.decode(rb.getString("pcDiffx")), Integer.decode(rb.getString("pcDiffy")));
            } else {
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("pcDiffx")), Integer.decode(rb.getString("pcDiffy")));
            }

            //绘制tth变化,此处开始可以省去颜色设置
            if (userInDB.getCount50() + userInDB.getCount100() + userInDB.getCount300()
                    < userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300()) {
                //同理不写蓝色部分
                g2.drawString("(↑" + new DecimalFormat("###,###").format(userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300() - (userInDB.getCount50() + userInDB.getCount100() + userInDB.getCount300())) + ")",
                        Integer.decode(rb.getString("tthDiffx")), Integer.decode(rb.getString("tthDiffy")));
            } else {
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("tthDiffx")), Integer.decode(rb.getString("tthDiffy")));
            }
            //绘制level变化
            if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getLevel())) < Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getLevel()))) {
                //同理不写蓝色部分
                g2.drawString("(↑" + (int) ((userFromAPI.getLevel() - userInDB.getLevel()) * 100) + "%)",
                        Integer.decode(rb.getString("levelDiffx")), Integer.decode(rb.getString("levelDiffy")));
            } else {
                g2.drawString("(↑" + Integer.toString(0) + "%)",
                        Integer.decode(rb.getString("levelDiffx")), Integer.decode(rb.getString("levelDiffy")));
            }
            //绘制SS count 变化
            //这里需要改变字体大小
            g2.setFont(new Font(rb.getString("diffFont"), 0, Integer.decode(rb.getString("countDiffSize"))));
            if (userInDB.getCount_rank_ss() > userFromAPI.getCount_rank_ss()) {
                //如果查询的SS比凌晨的少
                g2.setPaint(Color.decode(rb.getString("downColor")));
                g2.drawString("(↓" + Integer.toString(userFromAPI.getCount_rank_ss() - userInDB.getCount_rank_ss()) + ")",
                        Integer.decode(rb.getString("ssCountDiffx")), Integer.decode(rb.getString("ssCountDiffy")));
            } else if (userInDB.getCount_rank_ss() < userFromAPI.getCount_rank_ss()) {
                //如果SS变多了
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(userInDB.getCount_rank_ss() - userFromAPI.getCount_rank_ss()) + ")",
                        Integer.decode(rb.getString("ssCountDiffx")), Integer.decode(rb.getString("ssCountDiffy")));
            } else {
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("ssCountDiffx")), Integer.decode(rb.getString("ssCountDiffy")));
            }
            //s
            if (userInDB.getCount_rank_s() > userFromAPI.getCount_rank_s()) {
                //如果查询的SS比凌晨的少
                g2.setPaint(Color.decode(rb.getString("downColor")));
                g2.drawString("(↓" + Integer.toString(userFromAPI.getCount_rank_s() - userInDB.getCount_rank_s()) + ")",
                        Integer.decode(rb.getString("sCountDiffx")), Integer.decode(rb.getString("sCountDiffy")));
            } else if (userInDB.getCount_rank_s() < userFromAPI.getCount_rank_s()) {
                //如果SS变多了
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(userInDB.getCount_rank_s() - userFromAPI.getCount_rank_s()) + ")",
                        Integer.decode(rb.getString("sCountDiffx")), Integer.decode(rb.getString("sCountDiffy")));
            } else {
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("sCountDiffx")), Integer.decode(rb.getString("sCountDiffy")));
            }
            //a
            if (userInDB.getCount_rank_a() > userFromAPI.getCount_rank_a()) {
                //如果查询的SS比凌晨的少
                g2.setPaint(Color.decode(rb.getString("downColor")));
                g2.drawString("(↓" + Integer.toString(userFromAPI.getCount_rank_a() - userInDB.getCount_rank_a()) + ")",
                        Integer.decode(rb.getString("aCountDiffx")), Integer.decode(rb.getString("aCountDiffy")));
            } else if (userInDB.getCount_rank_a() < userFromAPI.getCount_rank_a()) {
                //如果SS变多了
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(userInDB.getCount_rank_a() - userFromAPI.getCount_rank_a()) + ")",
                        Integer.decode(rb.getString("aCountDiffx")), Integer.decode(rb.getString("aCountDiffy")));
            } else {
                g2.setPaint(Color.decode(rb.getString("upColor")));
                g2.drawString("(↑" + Integer.toString(0) + ")",
                        Integer.decode(rb.getString("aCountDiffx")), Integer.decode(rb.getString("aCountDiffy")));
            }


        }
        g2.dispose();
        try {
            ImageIO.write(bg, "png", new File("E:\\酷Q Pro\\data\\image\\" + userFromAPI.getUsername() + ".png"));
            return userFromAPI.getUsername() + ".png";
        } catch (IOException e) {
            logger.error("绘制图片成品失败");
            logger.error(e.getMessage());
        }
        return "error";
    }

    public String drawUserInfo() {
        return null;
    }


}
