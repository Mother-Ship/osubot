package top.mothership.osubot.thread;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.Map;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.apiUtil;
import top.mothership.osubot.util.dbUtil;
import top.mothership.osubot.util.imgUtil;
import top.mothership.osubot.util.pageUtil;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class playerThread extends Thread {
    private static String mainRegex = "[!！]([^ ]+)(.*)";
    private static String mainRegexWithNum = "[!！]([^ ]+)([^#]*) #(.+)";
    private String msg;
    private String groupId;
    private String fromQQ;
    private WebSocketClient cc;
    private Logger logger = LogManager.getLogger(this.getClass());
    //直接new好不习惯
    //最后大概是调用imgUtil，在imgUtil里调用api工具
    private imgUtil imgUtil = new imgUtil();
    private apiUtil apiUtil = new apiUtil();
    private dbUtil dbUtil = new dbUtil();
    private pageUtil pageUtil = new pageUtil();
    private ResourceBundle rb = ResourceBundle.getBundle("cabbage");
    private Matcher m;
    private boolean group = false;
    private Date startDate;


    public playerThread(String msg, String groupName, String groupId, String fromQQ, WebSocketClient cc) {
        this.msg = msg;
        this.fromQQ = fromQQ;
        this.cc = cc;
        startDate = Calendar.getInstance().getTime();
        if (groupId != null) {
            this.groupId = groupId;

            group = true;
            logger.info("检测到来自群：" + groupName + "中【" + fromQQ + "】用户的操作群消息："
                    + msg + ",已交给线程" + this.getName() + "处理");
        } else {
            logger.info("检测到来自【" + fromQQ + "】用户的操作消息："
                    + msg + ",已交给线程" + this.getName() + "处理");
        }
        m = Pattern.compile(mainRegex).matcher(msg);
        m.find();
    }

    private void sendMsg(String text) {
        if (group) {
            String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + text + "\"}";
            cc.send(resp);
        } else {
            String resp = "{\"act\": \"106\", \"QQID\": \"" + fromQQ + "\", \"msg\":\"" + text + "\"}";
            cc.send(resp);
        }
    }

    public void run() {
        //如果感叹号后面紧跟stat
        if (m.group(1).equals("stat")) {
            //默认初始化为1
            int day = 1;
            String username;
            if (msg.matches(mainRegexWithNum)) {
                m = Pattern.compile(mainRegexWithNum).matcher(msg);
                m.find();

                if (!checkDay(m.group(3))) {
                    return;
                }
                day = Integer.valueOf(m.group(3));
                username = m.group(2).substring(1);
            } else {
                username = m.group(2).substring(1);
            }
            if ("白菜".equals(username)) {
                sendMsg("唉，没人疼没人爱，我是地里一颗小白菜。");
                return;
            }
            logger.info("开始调用API查询" + username + "的信息");
            User userFromAPI = apiUtil.getUser(username, 0);
            if (userFromAPI == null) {
                sendMsg("没有在官网查到这个玩家。");
                return;
            }
            if (userFromAPI.getUser_id() == 3) {
                sendMsg("你们总是想查BanchoBot。\\n可是BanchoBot已经很累了，她不想被查。\\n她想念自己的小ppy，而不是被逼着查PP。\\n你有考虑过这些吗？没有！你只考虑过你自己。");
                return;
            }
            statUser(userFromAPI, day);

        }


        if (m.group(1).equals("bp")) {
            int num = 0;
            String username;
            if (msg.matches(mainRegexWithNum)) {
                m = Pattern.compile(mainRegexWithNum).matcher(msg);
                m.find();

                if (!checknum(m.group(3))) {
                    return;
                }
                num = Integer.valueOf(m.group(3));
                username = m.group(2).substring(1);
            } else {
                username = m.group(2).substring(1);
            }
            logger.info("接收到玩家" + username + "的BP查询请求");

            if ("白菜".equals(username)) {
                sendMsg("大白菜（学名：Brassica rapa pekinensis，异名Brassica campestris pekinensis或Brassica pekinensis）是一种原产于中国的蔬菜，又称“结球白菜”、“包心白菜”、“黄芽白”、“胶菜”等。(via 维基百科)");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return;
            }
            User user = apiUtil.getUser(username, 0);
            if (user == null) {
                sendMsg("没有在官网查到这个玩家。");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return;
            }

            printBP(user, num);

        }

        if (m.group(1).equals("setid")) {
            String username = m.group(2).substring(1);
            logger.info("尝试将" + username + "绑定到" + fromQQ + "上");
            if ("白菜".equals(username)) {
                if (fromQQ.equals("1335734657")) {
                    sendMsg("将" + username + "绑定到" + fromQQ + "成功");
                } else {
                    sendMsg("这个osu账号已经绑定了" + 1335734657 + "，如果发生错误请联系妈妈船。");
                }
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return;
            }
            User user = apiUtil.getUser(username, 0);
            if (user == null) {
                sendMsg("没有在官网找到该玩家。");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return;
            }
            checkFirst(user);
            int userId = user.getUser_id();
            //只有这个QQ对应的id是0
            int userIdFromDB = dbUtil.getId(fromQQ);
            if (userIdFromDB == 0) {
                //只有这个id对应的QQ是null
                if (dbUtil.getQQ(userId) == null) {
                    dbUtil.setId(String.valueOf(fromQQ), userId);
                    sendMsg("将" + username + "绑定到" + fromQQ + "成功");
                } else {
                    sendMsg("你的osu账号已经绑定了" + fromQQ + "，如果发生错误请联系妈妈船。");
                }
            } else {
                username = apiUtil.getUser(null, userIdFromDB).getUsername();
                sendMsg("你的QQ已经绑定了" + username + "，如果发生错误请联系妈妈船。");
            }
        }


        if (m.group(1).equals("statme")) {
            int userId = dbUtil.getId(fromQQ);
            if (userId == 0) {
                sendMsg("你没有绑定默认id。请使用!setid命令。");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return;
            }
            logger.info("检测到对" + userId + "的查询");
            int day = 1;
            if (msg.matches(mainRegexWithNum)) {
                //把空格和#去掉

                if (!checkDay(m.group(2).substring(2))) {
                    return;
                }
                day = Integer.valueOf(m.group(2).substring(2));
            }
            User user = apiUtil.getUser(null, userId);
            statUser(user, day);
        }

        if (m.group(1).equals("bpme")) {
            int num = 0;
            int userId = dbUtil.getId(fromQQ);
            if (userId == 0) {
                sendMsg("你没有绑定默认id。请使用!setid命令。");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return;
            }
            logger.info("检测到对" + userId + "的BP查询");
            if (msg.matches(mainRegexWithNum)) {
                if (!checknum(m.group(2).substring(2))) {
                    return;
                }
                num = Integer.valueOf(m.group(2).substring(2));
            }
            User user = apiUtil.getUser(null, userId);
            printBP(user, num);
        }


        if (m.group(1).equals("recent")) {
            int userId = dbUtil.getId(fromQQ);
            if (userId == 0) {
                sendMsg("你没有绑定默认id。请使用!setid命令。");
                return;
            }

            User user = apiUtil.getUser(null, userId);
            logger.info("检测到对" + user.getUsername() + "的最近游戏记录查询");
            BP bp = apiUtil.getRecentScore(null, userId);
            if (bp == null) {
                sendMsg("玩家" + user.getUsername() + "最近没有游戏记录。");
                return;
            }


            Map map = apiUtil.getMapDetail(bp.getBeatmap_id());
            String filename = imgUtil.drawOneBP(user, bp, map);
            if (filename.equals("error")) {
                sendMsg("[CQ:at,qq=1335734657]你摊上事了，赶紧去看日志");
                return;
            }
            sendMsg("[CQ:image,file=" + filename + "]");

        }
        if(m.group(1).equals("help")){
            logger.info("正在发送帮助信息");
            if((int)(Math.random()*10)==1) {
                logger.info("QQ"+fromQQ+"抽中了1/10的几率，触发了Trick");
                sendMsg("[CQ:image,file=!helpTrick.png]");
            }else{
                sendMsg("[CQ:image,file=!help.png]");
            }
        }
        logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");

    }


    private boolean checknum(String numString) {
        try {
            int num = Integer.valueOf(numString);
            if (num < 0 || num > 100) {
                sendMsg("其他人看不到的东西，白菜也看不到啦。");
                logger.info("BP不能大于100或者小于0");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return false;
            } else {
                return true;
            }
        } catch (java.lang.NumberFormatException e) {
            sendMsg("Ай-ай-ай-ай-ай, что сейчас произошло!");
            logger.info("给的BP数目不是int");
            logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
            return false;
        }
    }

    private boolean checkDay(String dayString) {
        try {
            //取正则第三个参数为3
            int day = Integer.valueOf(dayString);
            if (day > (int) ((new java.util.Date().getTime() - new SimpleDateFormat("yyyy-MM-dd").parse("2007-09-16").getTime()) / 1000 / 60 / 60 / 24)) {
                sendMsg("你要找史前时代的数据吗。");
                logger.info("指定的日期早于osu!首次发布日期");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return false;
            }
            if (day < 0) {
                sendMsg("白菜不会预知未来。");
                logger.info("天数不能为负值");
                logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
                return false;
            }

        } catch (java.lang.NumberFormatException e) {
            sendMsg("假使这些完全……不能用的参数，你再给他传一遍，你等于……你也等于……你也有泽任吧？");
            logger.info("给的天数不是int值");
            logger.info("线程" + this.getName() + "处理完毕，共耗费" + (Calendar.getInstance().getTimeInMillis() - startDate.getTime()) + "ms。");
            return false;
        } catch (ParseException e) {
            //由于解析的是固定字符串，不会出异常，无视
        }
        return true;
    }


    private void printBP(User user, int num) {
        String filename;
        checkFirst(user);
        logger.info("开始获取玩家" + user.getUsername() + "的BP");
        List<BP> list = apiUtil.getAllBP(user.getUsername(), 0);

        Calendar c = Calendar.getInstance();
//        凌晨四点之前，将日期减一
        if (c.get(Calendar.HOUR_OF_DAY) < 4) {
            c.add(Calendar.DATE, -1);
        }
        c.set(Calendar.HOUR_OF_DAY, 4);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        //需要同时传递BP的位置和本体
        java.util.Map<BP, Integer> result = new HashMap<>();

        for (int i = 0; i < list.size(); i++) {
            //对BP进行遍历，如果产生时间晚于当天凌晨4点
            if (list.get(i).getDate().after(c.getTime())) {
                result.put(list.get(i), i);
            }
        }

        if (num == 0) {
            logger.info("筛选今日BP成功");
            if (result.size() == 0) {
                sendMsg("玩家" + user.getUsername() + "今天没有更新BP。");
                logger.info("没有查到该玩家今天更新的BP");
                return;
            }

            for (BP aList : result.keySet()) {
                //对BP进行遍历，请求API将名称写入
                logger.info("正在获取Beatmap id为" + aList.getBeatmap_id() + "的谱面的名称");
                Map map = apiUtil.getMapDetail(aList.getBeatmap_id());
                aList.setBeatmap_name(map.getArtist() + " - " + map.getTitle() + " [" + map.getVersion() + "]");
            }
            filename = imgUtil.drawUserBP(user, result);
            if (filename.equals("error")) {
                sendMsg("[CQ:at,qq=1335734657]你摊上事了，赶紧去看日志");
                return;
            }
            sendMsg("[CQ:image,file=" + filename + "]");
        } else {
            if (num > list.size()) {
                sendMsg("该玩家没有打出指定的bp……");
                logger.info("请求的bp数比玩家bp总数量大");
                return;
            } else {
                //list基于0，得-1
                BP bp = list.get(num - 1);
                logger.info("获得了玩家" + user.getUsername() + "的第" + num + "个BP：" + bp.getBeatmap_id() + "，正在获取歌曲名称");
                Map map = apiUtil.getMapDetail(bp.getBeatmap_id());

                filename = imgUtil.drawOneBP(user, bp, map);
                if (filename.equals("error")) {
                    sendMsg("[CQ:at,qq=1335734657]你摊上事了，赶紧去看日志");
                    return;
                }
                sendMsg("[CQ:image,file=" + filename + "]");
            }
        }

    }


    private void statUser(User userFromAPI, int day) {

        //进行检验，是否初次使用notFound
        //如果day=0则不检验（不写入数据库，避免预先查过之后add不到）
        boolean near = false;
        User userInDB = null;

        if(day>0) {
            checkFirst(userFromAPI);
            userInDB = dbUtil.getUserInfo(userFromAPI.getUser_id(), day);
            if (userInDB == null) {
                //如果第一次没取到
                userInDB = dbUtil.getNearestUserInfo(userFromAPI.getUser_id(), day);
                near = true;
            }

        }

        String  role = dbUtil.getUserRole(userFromAPI.getUser_id());
        if("notFound".equals(role)){
            role = "creep";
        }

        int scoreRank;
        if (userFromAPI.getUser_id() == 1244312
                || userFromAPI.getUser_id() == 6149313
                || userFromAPI.getUser_id() == 3213720) {
            scoreRank = pageUtil.getRank(userFromAPI.getRanked_score(), 1, 10000);
        } else {
            scoreRank = pageUtil.getRank(userFromAPI.getRanked_score(), 1, 2000);
        }

        String filename = imgUtil.drawUserInfo(userFromAPI, userInDB, role, day, near, scoreRank);
        if (filename.equals("error")) {
            sendMsg("绘图过程中发生致命错误。");
        }
        logger.info("绘制完成，正在发送……");
        //由于带[]的玩家，生成的文件名会导致返回出错，直接在imgUtil改为用数字id生成文件
        sendMsg("[CQ:image,file=" + filename + "]");
    }


    //减少代码重复量，这个user必须包含用户名
    private void checkFirst(User user) {
        if (dbUtil.getUserRole(user.getUser_id()).equals("notFound")) {
            //是个没用过白菜的人呢
            //玩家初次使用本机器人，直接在数据库登记当天数据
            logger.info("玩家" + user.getUsername() + "初次使用本机器人，开始登记");
            dbUtil.addUserId(user.getUser_id());
            //需求是把12点之后4点之前的减一天
            Calendar c = Calendar.getInstance();
            if (c.get(Calendar.HOUR_OF_DAY) < 4) {
                c.add(Calendar.DAY_OF_MONTH, -1);
            }
            dbUtil.addUserInfo(user, new java.sql.Date(c.getTime().getTime()));
        }
    }



}
