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
import java.util.*;
import java.util.List;

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
        //指定颜色
        g2.setPaint(Color.decode(rb.getString(color)));
        //指定字体
        g2.setFont(new Font(rb.getString(font), 0, Integer.decode(rb.getString(size))));
        //指定坐标
        g2.drawString(text, Integer.decode(rb.getString(x)), Integer.decode(rb.getString(y)));

    }

    public String convertMOD(Integer bp) {
        String modBin = Integer.toBinaryString(bp);
        //反转mod
        modBin = new StringBuffer(modBin).reverse().toString();
        List<String> mods = new ArrayList<>();
        char[] c = modBin.toCharArray();
        for (int i = c.length - 1; i >= 0; i--) {
            if (c[i] == '1') {
                //字符串中第i个字符是1,意味着第i+1个mod被开启了
                switch (i) {
                    case 0:
                        mods.add("NF");
                        break;
                    case 1:
                        mods.add("EZ");
                        break;
                    case 3:
                        mods.add("HD");
                        break;
                    case 4:
                        mods.add("HR");
                        break;
                    case 5:
                        mods.add("SD");
                        break;
                    case 6:
                        mods.add("DT");
                        break;
                    case 8:
                        mods.add("HT");
                        break;
                    case 9:
                        mods.add("NC");
                        break;
                    case 10:
                        mods.add("FL");
                        break;
                    case 12:
                        mods.add("SO");
                        break;
                    case 14:
                        mods.add("PF");
                        break;
                }

            }
        }
        return mods.toString().substring(1, mods.toString().length());
    }

    public String drawUserInfo(User userFromAPI, User userInDB, String role, int day, boolean near) {
        //准备资源：背景图和用户头像，以及重画之后的用户头像
        BufferedImage bg = null;
        BufferedImage layout = null;
        BufferedImage ava = null;
        BufferedImage resizedAva = null;
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

        //开启平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
                不带参数：day=1，调用dbUtil拿当天凌晨（UTC时间日期是昨天，同时也是昨天结束时候的成绩）的数据进行对比
                带day = 0:进入本方法，不读数据库，不进行对比
                day>1，例如day=2，21号进入本方法，查的是19号结束时候的成绩
                */
            if (day > 1) {
                //临时关闭平滑
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                //只有day>1才会出现文字
                if (near) {
                    //如果取到的是模糊数据,输出具体日期
                    draw(g2, "tipColor", "tipFont", "tipSize",  "请求的日期没有数据", "tipx", "tipy");
                    //算出天数差别然后加一天
                    draw(g2, "tipColor", "tipFont", "tipSize",  "『对比于" + Long.valueOf(((Calendar.getInstance().getTime().getTime() -
                            userInDB.getQueryDate().getTime()) / 1000 / 60 / 60 / 24) + 1).toString() + "天前』", "tip2x", "tip2y");
                } else {
                    //如果取到的是精确数据
                    draw(g2, "tipColor", "tipFont", "tipSize", "『对比于" + day + "天前』", "tip2x", "tip2y");
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
            return "error";
        }
    }

    public String drawUserBP(User user, List<BP> list) {
        //思路：获取list的大小，把每个list成员的.getbeatmapName信息绘制到图片上
        logger.info("开始绘制"+user.getUsername()+"的今日BP信息");
        BufferedImage bpTop;
        BufferedImage A;
        BufferedImage B;
        BufferedImage C;
        BufferedImage D;
        BufferedImage X;
        BufferedImage XH;
        BufferedImage S;
        BufferedImage SH;
        //根据谱面名称+难度的长度，将所有BP分为两个List
        List<BP> bp2 = new ArrayList<>();
        List<BP> bp3 = new ArrayList<>();
        List<BufferedImage> bpmids2 = new ArrayList<>();
        List<BufferedImage> bpmids3 = new ArrayList<>();
        int width = 0;
        int bpTopHeight = 0;
        int bpMid2Height = 0;
        int bpMid3Height = 0;
        try {
            //使用guava的类读取路径
            bpTop = ImageIO.read(new File(Resources.getResource(rb.getString("bptop")).toURI()));
            A = ImageIO.read(new File(Resources.getResource(rb.getString("A")).toURI()));
            B = ImageIO.read(new File(Resources.getResource(rb.getString("B")).toURI()));
            C = ImageIO.read(new File(Resources.getResource(rb.getString("C")).toURI()));
            D = ImageIO.read(new File(Resources.getResource(rb.getString("D")).toURI()));
            X = ImageIO.read(new File(Resources.getResource(rb.getString("X")).toURI()));
            XH = ImageIO.read(new File(Resources.getResource(rb.getString("XH")).toURI()));
            S = ImageIO.read(new File(Resources.getResource(rb.getString("S")).toURI()));
            SH = ImageIO.read(new File(Resources.getResource(rb.getString("SH")).toURI()));
            for (int i = 0; i < list.size(); i++) {
                //准备好和BP数量相同的List
                if (list.get(i).getBeatmap_name().length() < Integer.valueOf(rb.getString("bplimit"))) {
                    //根据谱面名称+难度的长度读取BG
                    BufferedImage bpmidTmp = ImageIO.read(new File(Resources.getResource(rb.getString("bpmid2")).toURI()));
                    bpmids2.add(bpmidTmp);
                    bp2.add(list.get(i));
                    bpMid2Height = bpmidTmp.getHeight();
                } else {
                    BufferedImage bpmidTmp = ImageIO.read(new File(Resources.getResource(rb.getString("bpmid3")).toURI()));
                    bpmids3.add(bpmidTmp);
                    bp3.add(list.get(i));
                    bpMid3Height = bpmidTmp.getHeight();
                }
                width = bpTop.getWidth();
                bpTopHeight = bpTop.getHeight();
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("读取BP布局图片失败");
            logger.error(e.getMessage());
            return "error";
        }
        logger.info("载入背景、rank小图标完成");
        //规划出结果图的尺寸(2行BP数量*2行BP图高度+3行BP数量*3行BP高度)
        BufferedImage result = new BufferedImage(width, bpMid2Height * (1+bp2.size()) + bpMid3Height * +bp3.size(), BufferedImage.TYPE_INT_RGB);


        //在头部图片上绘制用户名
        Graphics2D g2 = (Graphics2D) bpTop.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw(g2, "bpUnameColor", "bpUnameFont", "bpUnameSize", user.getUsername()+"'s Today BP", "bpUnamex", "bpUnamey");
        g2.dispose();
        //将头部图片转换为数组
        int[] ImageArrayTop = new int[width * bpTopHeight];
        ImageArrayTop = bpTop.getRGB(0, 0, width, bpTopHeight, ImageArrayTop, 0, width);
        //将头部图片先画上去
        result.setRGB(0, 0, width, bpTopHeight, ImageArrayTop, 0, width);


        for (int i = 0; i < bp2.size(); i++) {
            String mods;
            if (bp2.get(i).getEnabled_mods() > 0) {
                mods = convertMOD(bp2.get(i).getEnabled_mods());
            } else {
                mods = "None";
            }
            //准备将字符串写入图片
            Graphics2D g = (Graphics2D) bpmids2.get(i).getGraphics();
            //绘制小图
            switch (bp2.get(i).getRank()) {
                case "A":
                    g.drawImage(A, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "B":
                    g.drawImage(B, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "C":
                    g.drawImage(C, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "D":
                    g.drawImage(D, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "X":
                    g.drawImage(X, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "XH":
                    g.drawImage(XH, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "S":
                    g.drawImage(S, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "SH":
                    g.drawImage(SH, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
            }

            //开启平滑
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //开始绘制谱面名称，因为是二行的数据所以不需要分割
            draw(g, "bpNameColor", "bpNameFont", "bpNameSize",
                    bp2.get(i).getBeatmap_name() + "(" + new DecimalFormat("###.00").format(100.0 * (6 * bp2.get(i).getCount300() + 2 * bp2.get(i).getCount100() + bp2.get(i).getCount50()) / (6 * (bp2.get(i).getCount50() + bp2.get(i).getCount100() + bp2.get(i).getCount300() + bp2.get(i).getCountmiss()))) + "%)", "bp2Namex", "bp2Namey");
            //绘制日期(给的就是北京时间，不转)
            draw(g, "bpDateColor", "bpDateFont", "bpDateSize",
                    new SimpleDateFormat("MM-dd HH:mm").format(bp2.get(i).getDate().getTime()), "bp2Datex", "bp2Datey");
            //绘制MOD
            draw(g, "bpModColor", "bpModFont", "bpModSize", mods, "bp2Modx", "bp2Mody");
            //绘制PP
            draw(g, "bpPPColor", "bpPPFont", "bpPPSize", Integer.toString(Math.round(bp2.get(i).getPp()))+"pp", "bp2PPx", "bp2PPy");
            g.dispose();
            //将它变成数组
            int[] ImageArray = new int[width * bpMid2Height];
            ImageArray = bpmids2.get(i).getRGB(0, 0, width, bpMid2Height, ImageArray, 0, width);
            //横坐标是0，纵坐标是i+1*每个格子的高度，大小是每个格子的宽高
            result.setRGB(0, bpMid2Height * (i + 1), width, bpMid2Height, ImageArray, 0, width);//将数组写入缓冲图片
            logger.info("绘制"+bp2.get(i).getBeatmap_name()+"完成");
        }
        logger.info("无需使用大背景的BP绘制完成");
        for (int i = 0; i < bp3.size(); i++) {
            String mods;
            if (bp3.get(i).getEnabled_mods() > 0) {
                mods = convertMOD(bp3.get(i).getEnabled_mods());
            } else {
                mods = "None";
            }
            //准备将字符串写入图片
            Graphics2D g = (Graphics2D) bpmids3.get(i).getGraphics();
            //绘制小图
            switch (bp3.get(i).getRank()) {
                case "A":
                    g.drawImage(A, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "B":
                    g.drawImage(B, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "C":
                    g.drawImage(C, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "D":
                    g.drawImage(D, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "X":
                    g.drawImage(X, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "XH":
                    g.drawImage(XH, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "S":
                    g.drawImage(S, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "SH":
                    g.drawImage(SH, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
            }

            //开启平滑
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //开始绘制谱面名称，三行BP需要分割字符串
            //取80个字符中的最后一个空格
            draw(g, "bpNameColor", "bpNameFont", "bpNameSize", bp3.get(i).getBeatmap_name().substring(0, bp3.get(i).getBeatmap_name().substring(0, Integer.valueOf(rb.getString("bplimit"))+1).lastIndexOf(" ") + 1),
            "bp3Namex", "bp3Namey");
            //第二行
            draw(g, "bpNameColor", "bpNameFont", "bpNameSize", bp3.get(i).getBeatmap_name().substring(bp3.get(i).getBeatmap_name().substring(0, Integer.valueOf(rb.getString("bplimit"))+1).lastIndexOf(" ") + 1, bp3.get(i).getBeatmap_name().length())
                            + "(" + new DecimalFormat("###.00").format(100.0 * (6 * bp3.get(i).getCount300() + 2 * bp3.get(i).getCount100() + bp3.get(i).getCount50()) / (6 * (bp3.get(i).getCount50() + bp3.get(i).getCount100() + bp3.get(i).getCount300() + bp3.get(i).getCountmiss()))) + "%)",
                    "bp3Name+1x", "bp3Name+1y");
            //绘制日期(给的就是北京时间，不转)
            draw(g, "bpDateColor", "bpDateFont", "bpDateSize",
                    new SimpleDateFormat("MM-dd HH:mm").format(bp3.get(i).getDate().getTime()), "bp3Datex", "bp3Datey");
            //绘制MOD
            draw(g, "bpModColor", "bpModFont", "bpModSize", mods, "bp3Modx", "bp3Mody");
            //绘制PP
            draw(g, "bpPPColor", "bpPPFont", "bpPPSize", Integer.toString(Math.round(bp3.get(i).getPp()))+"pp", "bp3PPx", "bp3PPy");
            g.dispose();
            //将它变成数组
            int[] ImageArray = new int[width * bpMid3Height];
            ImageArray = bpmids3.get(i).getRGB(0, 0, width, bpMid3Height, ImageArray, 0, width);
            //横坐标是0，纵坐标是i*每个格子的高度（没有头图不用+1），还要加上bp2占用的高度+1（头图。+1在这里），大小是每个格子的宽高
            result.setRGB(0, bpMid3Height * (i) + bpMid2Height * (bp2.size()+1), width, bpMid3Height, ImageArray, 0, width);
            logger.info("绘制"+bp3.get(i).getBeatmap_name()+"完成");
        }
        //生成新图片
        try {
            ImageIO.write(result, "png", new File(rb.getString("path") + "\\data\\image\\" + user.getUser_id() + ".png"));
            return user.getUser_id() + ".png";
        } catch (IOException e) {
            logger.error("绘制图片成品失败");
            logger.error(e.getMessage());
            return "error";
        }

    }


}
