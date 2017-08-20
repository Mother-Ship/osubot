package top.mothership.osubot.util;

import com.google.common.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class imgUtil {
    private Logger logger = LogManager.getLogger(this.getClass());

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

    public void draw(Graphics2D g2, String color, String font, String size, String text, String x, String y) {
        g2.setPaint(Color.decode(rb.getString(color)));
        //指定字体
        g2.setFont(new Font(rb.getString(font), 0, Integer.decode(rb.getString(size))));
        //指定坐标
        g2.drawString(text, Integer.decode(rb.getString(x)), Integer.decode(rb.getString(y)));

    }

    public String drawUserInfo(User userFromAPI, User userInDB, String role, int day, boolean near) {
        //准备资源：背景图和用户头像，以及重画之后的用户头像
        BufferedImage bg = null;
        BufferedImage layout = null;
        BufferedImage ava = null;
        BufferedImage resizedAva = null;
        //TODO 先读取这个用户组对应的图片,如果是creep就用默认的


        try {
            //使用guava的类读取路径
            layout = ImageIO.read(new File(Resources.getResource(rb.getString("userlayout")).toURI()));
        } catch (IOException | URISyntaxException e) {
            logger.error("读取布局图片失败");
            logger.error(e.getMessage());
            return "error";
        }

        try {
            bg = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\bg\\" + role + ".png"));
        } catch (IOException e) {
            //所有没有独立bg的都采用默认bg
            try {
                bg = ImageIO.read(new File(Resources.getResource(rb.getString("defaultbg")).toURI()));
            } catch (IOException | URISyntaxException e1) {
                logger.error("读取背景图片失败");
                logger.error(e.getMessage());
                return "error";
            }
        }


        //将布局图片初始化
        Graphics2D g2 = (Graphics2D) bg.getGraphics();
        //把布局画到bg上
        g2.drawImage(layout, 0, 0, null);

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
        //绘制用户名
        draw(g2, "unameColor", "unameFont", "unameSize", userFromAPI.getUsername(), "namex", "namey");

        //绘制Rank
        draw(g2, "defaultColor", "numberFont", "rankSize", "#" + userFromAPI.getPp_rank(), "rankx", "ranky");

        //绘制PP
        draw(g2, "ppColor", "numberFont", "ppSize", "#" + userFromAPI.getPp_raw().toString(), "ppx", "ppy");


        //绘制RankedScore
        draw(g2, "defaultColor", "numberFont", "numberSize",
                "#" + new DecimalFormat("###,###").format(userFromAPI.getRanked_score()), "rScorex", "rScorey");
        //绘制acc
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("###.00").format(userFromAPI.getAccuracy()) + "%", "accx", "accy");

        //绘制pc
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("###,###").format(userFromAPI.getPlaycount()), "pcx", "pcy");

        //绘制tth
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("###,###").format(userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300()), "tthx", "tthy");
        //绘制Level

        draw(g2, "defaultColor", "numberFont", "numberSize",
                Integer.toString((int) Math.floor(userFromAPI.getLevel())) + " (" + (int) ((userFromAPI.getLevel() - Math.floor(userFromAPI.getLevel())) * 100) + "%)", "levelx", "levely");

        //绘制SS计数
        draw(g2, "defaultColor", "numberFont", "countSize", Integer.toString(userFromAPI.getCount_rank_ss()), "ssCountx", "ssCounty");

        //绘制S计数
        draw(g2, "defaultColor", "numberFont", "countSize", Integer.toString(userFromAPI.getCount_rank_s()), "sCountx", "sCounty");

        //绘制A计数
        draw(g2, "defaultColor", "numberFont", "countSize", Integer.toString(userFromAPI.getCount_rank_a()), "aCountx", "aCounty");
        //绘制当时请求的时间
        //拿到当前时间对应的北京时间
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        draw(g2, "timeColor", "timeFont", "timeSize",
                new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()), "timex", "timey");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        //---------------------------以上绘制在线部分完成--------------------------------
        //试图查询数据库中指定日期的user
        if (day > 0) {
            /*
                不带参数：day=1，调用dbUtil拿0天前（日期不变，当天）的数据进行对比（实际上是昨天结束时候的成绩）
                带day = 0:进入本方法，不读数据库，不进行对比
                day>1
             */
            if (day > 1) {
                //临时关闭平滑
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                //只有day>1才会出现文字
                draw(g2, "tipColor", "tipFont", "tipSize", "#" + userFromAPI.getPp_raw().toString(), "ppx", "ppy");
                if (near) {
                    //如果取到的是模糊数据,输出具体日期
                    draw(g2, "tipColor", "tipFont", "tipSize", "#" + "请求的日期没有数据", "tipx", "tipy");
                    //算出天数差别然后加一天
                    draw(g2, "tipColor", "tipFont", "tipSize", "#" + "『对比于" + Long.valueOf(((Calendar.getInstance().getTime().getTime() -
                            userInDB.getQueryDate().getTime()) / 1000 / 60 / 60 / 24) + 1).toString() + "天前』", "tip2x", "tip2y");
                } else {
                    //如果取到的是精确数据
                    draw(g2, "tipColor", "tipFont", "tipSize", "#" + "『对比于" + day + "天前』", "tip2x", "tip2y");
                }

            }


            //这样确保了userInDB不是空的
            //绘制Rank变化
            if (userInDB.getPp_rank() > userFromAPI.getPp_rank()) {
                //如果查询的rank比凌晨中的小
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(userInDB.getPp_rank() - userFromAPI.getPp_rank()) + ")", "rankDiffx", "rankDiffy");
            } else if (userInDB.getPp_rank() < userFromAPI.getPp_rank()) {
                //如果掉了rank
                draw(g2, "downColor", "diffFont", "diffSize",
                        "(↓" + Integer.toString(userFromAPI.getPp_rank() - userInDB.getPp_rank()) + ")", "rankDiffx", "rankDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(0) + ")", "rankDiffx", "rankDiffy");
            }
            //绘制PP变化
            if (userInDB.getPp_raw() > userFromAPI.getPp_raw()) {
                //如果查询的pp比凌晨中的小
                draw(g2, "downColor", "diffFont", "diffSize",
                        "(↓" + new DecimalFormat("##0.00").format(userInDB.getPp_raw() - userFromAPI.getPp_raw()) + ")", "ppDiffx", "ppDiffy");
            } else if (userInDB.getPp_raw() < userFromAPI.getPp_raw()) {
                //刷了PP
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + new DecimalFormat("##0.00").format(userFromAPI.getPp_raw() - userInDB.getPp_raw()) + ")", "ppDiffx", "ppDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(0) + ")", "ppDiffx", "ppDiffy");
            }

            //绘制RankedScore变化
            if (userInDB.getRanked_score() < userFromAPI.getRanked_score()) {
                //因为RankedScore不会变少，所以不写蓝色部分
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + new DecimalFormat("###,###").format(userFromAPI.getRanked_score() - userInDB.getRanked_score()) + ")", "rScoreDiffx", "rScoreDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(0) + ")", "rScoreDiffx", "rScoreDiffy");
            }
            //绘制ACC变化
            //在这里把精度砍掉
            if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getAccuracy())) > Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getAccuracy()))) {
                //如果acc降低了
                draw(g2, "downColor", "diffFont", "diffSize",
                        "(↓" + new DecimalFormat("##0.00").format(userInDB.getAccuracy() - userFromAPI.getAccuracy()) + "%)", "accDiffx", "accDiffy");
            } else if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getAccuracy())) < Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getAccuracy()))) {
                //提高
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + new DecimalFormat("##0.00").format(userFromAPI.getAccuracy() - userInDB.getAccuracy()) + "%)", "accDiffx", "accDiffy");

            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + new DecimalFormat("##0.00").format(0.00) + "%)", "accDiffx", "accDiffy");
            }

            //绘制pc变化
            if (userInDB.getPlaycount() < userFromAPI.getPlaycount()) {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + new DecimalFormat("###,###").format(userFromAPI.getPlaycount() - userInDB.getPlaycount()) + ")", "pcDiffx", "pcDiffy");

            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(0) + ")", "pcDiffx", "pcDiffy");

            }

            //绘制tth变化,此处开始可以省去颜色设置
            if (userInDB.getCount50() + userInDB.getCount100() + userInDB.getCount300()
                    < userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300()) {
                //同理不写蓝色部分
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + new DecimalFormat("###,###").format(userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300() - (userInDB.getCount50() + userInDB.getCount100() + userInDB.getCount300())) + ")", "tthDiffx", "tthDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(0) + ")", "tthDiffx", "tthDiffy");
            }
            //绘制level变化
            if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getLevel())) < Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getLevel()))) {
                //同理不写蓝色部分
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + (int) ((userFromAPI.getLevel() - userInDB.getLevel()) * 100) + "%)", "levelDiffx", "levelDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "(↑" + Integer.toString(0) + "%)", "levelDiffx", "levelDiffy");
            }
            //绘制SS count 变化
            //这里需要改变字体大小
            if (userInDB.getCount_rank_ss() > userFromAPI.getCount_rank_ss()) {
                //如果查询的SS比凌晨的少
                draw(g2, "downColor", "diffFont", "countDiffSize",
                        "(↓" + Integer.toString(userInDB.getCount_rank_ss() - userFromAPI.getCount_rank_ss()) + ")", "ssCountDiffx", "ssCountDiffy");
            } else if (userInDB.getCount_rank_ss() < userFromAPI.getCount_rank_ss()) {
                //如果SS变多了
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "(↑" + Integer.toString(userFromAPI.getCount_rank_ss() - userInDB.getCount_rank_ss()) + ")", "ssCountDiffx", "ssCountDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "(↑" + Integer.toString(0) + ")", "ssCountDiffx", "ssCountDiffy");
            }
            //s
            if (userInDB.getCount_rank_s() > userFromAPI.getCount_rank_s()) {
                //如果查询的S比凌晨的少
                draw(g2, "downColor", "diffFont", "countDiffSize",
                        "(↓" + Integer.toString(userInDB.getCount_rank_s() - userFromAPI.getCount_rank_s()) + ")", "sCountDiffx", "sCountDiffy");
            } else if (userInDB.getCount_rank_s() < userFromAPI.getCount_rank_s()) {
                //如果S变多了
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "(↑" + Integer.toString(userFromAPI.getCount_rank_s() - userInDB.getCount_rank_s()) + ")", "sCountDiffx", "sCountDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "(↑" + Integer.toString(0) + ")", "sCountDiffx", "sCountDiffy");
            }
            //a
            if (userInDB.getCount_rank_a() > userFromAPI.getCount_rank_a()) {
                //如果查询的S比凌晨的少
                draw(g2, "downColor", "diffFont", "countDiffSize",
                        "(↓" + Integer.toString(userInDB.getCount_rank_a() - userFromAPI.getCount_rank_a()) + ")", "aCountDiffx", "aCountDiffy");
            } else if (userInDB.getCount_rank_a() < userFromAPI.getCount_rank_a()) {
                //如果S变多了
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "(↑" + Integer.toString(userFromAPI.getCount_rank_a() - userInDB.getCount_rank_a()) + ")", "aCountDiffx", "aCountDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "(↑" + Integer.toString(0) + ")", "aCountDiffx", "aCountDiffy");
            }
        }
        g2.dispose();
        try {
            ImageIO.write(bg, "png", new File(rb.getString("path") + "\\data\\image\\" + userFromAPI.getUser_id() + ".png"));
            return userFromAPI.getUser_id() + ".png";
        } catch (IOException e) {
            logger.error("绘制图片成品失败");
            logger.error(e.getMessage());
        }
        return "error";
    }

    //TODO recentBP的绘制
    public String drawUserBP(String username, BP bp) {
        return null;
    }


}
