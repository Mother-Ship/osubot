package top.mothership.osubot.thread;

import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.apiUtil;
import top.mothership.osubot.util.dbUtil;
import top.mothership.osubot.util.imgUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class playerThread extends Thread {
    private String msg;
    private String groupId;
    private WebSocketClient cc;
    private Logger logger = LogManager.getLogger(this.getClass());
    //直接new好不习惯
    //最后大概是调用imgUtil，在imgUtil里调用api工具
    private imgUtil imgUtil = new imgUtil();
    private apiUtil apiUtil = new apiUtil();
    private dbUtil dbUtil = new dbUtil();


    public playerThread(String msg, String groupId, WebSocketClient cc) {
        this.msg = msg;
        this.groupId = groupId;
        this.cc = cc;
    }

    public void sendGroupMsg(String text) {
        String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + text + "\"}";
        cc.send(resp);
    }

    public void run() {
        //如果感叹号后面紧跟stat
        if ("stat".equals(msg.substring(1, 5))) {
            int index = 0;
            //默认初始化为1
            int day = 1;
            String username;

            if (msg.contains("#")) {
                //潜在风险：如果消息长度超过整型限制会出异常，而且不会有任何回复，考虑到这种情况实在太少见不做处理
                index = msg.indexOf("#");
                try {
                    day = Integer.valueOf(msg.substring(index + 1));
                    if (day > (int) ((new Date().getTime() - new SimpleDateFormat("yyyy-MM-dd").parse("2007-09-16").getTime()) / 1000 / 60 / 60 / 24)) {
                        sendGroupMsg("你要找史前时代的数据吗。");
                        logger.info("指定的日期早于osu!首次发布日期");
                        logger.info("线程" + this.getName() + "处理完毕，已经退出");
                        return;
                    }
                } catch (java.lang.NumberFormatException e) {
                    sendGroupMsg("天数超过整型上限。");
                    logger.info("天数超过整型上限");
                    logger.info("线程" + this.getName() + "处理完毕，已经退出");
                    return;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                username = msg.substring(6, index - 1);
            } else {
                username = msg.substring(6);
            }
            if (day < 0) {
                sendGroupMsg("天数不能为负值。");
                logger.info("天数不能为负值");
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }


            logger.info("接收到玩家" + username + "的查询请求");
            if ("a".equals(msg.substring(5, 6))) {
                //后期做一个文本版本的，暂时这两个命令没什么区别
                if (msg.contains("#")) {
                    index = msg.indexOf("#");
                    day = Integer.valueOf(msg.substring(index + 1));
                    username = msg.substring(7, index - 1);
                } else {
                    username = msg.substring(8);
                }
            }

            User userFromAPI;

            try {
                userFromAPI = apiUtil.getUser(username);
                //优化流程，在此处直接判断用户存不存在
                if (userFromAPI == null) {
                    sendGroupMsg("没有在官网查到这个玩家。");
                    throw new IOException("没有这个玩家");
                }
            } catch (IOException e) {
                logger.error("从api获取玩家" + username + "信息失败");
                logger.error(e.getMessage());
                sendGroupMsg("出错了：" + e.getMessage());
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }

            if (dbUtil.getUserName(username) == 0) {
                //玩家初次使用本机器人，直接在数据库登记当天数据
                logger.info("玩家" + username + "初次使用本机器人，开始登记");
                dbUtil.addUserName(username);
                dbUtil.addUserInfo(userFromAPI);

            }

            boolean near = false;
            User userInDB = dbUtil.getUserInfo(username, day - 1);
            if (userInDB == null) {
                //如果第一次没取到
                userInDB = dbUtil.getNearestUserInfo(username, day - 1);
                near = true;
            }


            String filename = imgUtil.drawUserInfo(userFromAPI, userInDB, day, near);
            sendGroupMsg("[CQ:image,file=" + filename + "]");
            try {
                logger.info("线程暂停两秒，以免发送成功前删除文件");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File f = new File("E:\\酷Q Pro\\data\\image\\" + filename);
            f.delete();


        }


        //TODO 将bp改造为图片
        if ("bp".equals(msg.substring(1, 3))) {
            String username = msg.substring(4);
            try {
                //调用方法拿到返回的今日BP
                logger.info("正在获取玩家" + username + "的今日BP");
//                List<BP> result = apiUtil.getBP(username);
                List<BP> result = new ArrayList<>();
                String resp = null;
                if (result.size() > 0) {
                    resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "该玩家今日没有更新BP" + "\"}";
                    logger.info("玩家" + username + "发起了对今日BP的请求，获取了" + result.size() + "条BP");
                    cc.send(resp);
                } else {
                    resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "该玩家今日没有更新BP" + "\"}";
                    logger.info("玩家" + username + "发起了对今日BP的请求，但是今天没有更新BP");
                    cc.send(resp);
                }
            } catch (JsonSyntaxException e) {
                logger.error("JSON解析失败");
                logger.error(e.getMessage());
            }


        }
        logger.info("线程" + this.getName() + "处理完毕，已经退出");
    }

}
