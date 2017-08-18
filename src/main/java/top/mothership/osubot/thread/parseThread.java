package top.mothership.osubot.thread;

import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.util.apiUtil;
import top.mothership.osubot.util.dbUtil;
import top.mothership.osubot.util.imgUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class parseThread extends Thread {
    private String msg;
    private String groupId;
    private WebSocketClient cc;
    private Logger logger = LogManager.getLogger(this.getClass());
    //直接new好不习惯
    //最后大概是调用imgUtil，在imgUtil里调用api工具
    private imgUtil apiUtil = new imgUtil();
    private dbUtil dbUtil = new dbUtil();


    public parseThread(String msg, String groupId, WebSocketClient cc) {
        this.msg = msg;
        this.groupId = groupId;
        this.cc = cc;
    }

    public void run() {
        //如果感叹号后面紧跟stat
        if ("stat".equals(msg.substring(1, 5))) {
            int index = 0;
            int day = 0;
            String username;

            if (msg.contains("#")) {
                index = msg.indexOf("#");
                day = Integer.valueOf(msg.substring(index + 1));
                username = msg.substring(6, index);
            } else {
                username = msg.substring(6);
            }

            logger.info("正在获取玩家" + username + "的信息");
            if ("a".equals(msg.substring(5, 6))) {
                //后期做一个文本版本的，暂时这两个命令没什么区别
                if (msg.contains("#")) {
                    index = msg.indexOf("#");
                    day = Integer.valueOf(msg.substring(index + 1));
                    username = msg.substring(7, index);
                } else {
                    username = msg.substring(8);
                }
            }
            if(dbUtil.getUserName(username)==0){
                logger.info("玩家"+username+"初次使用本机器人，已在userName表中登记");
                dbUtil.addUserName(username);
            }

            try {
                imgUtil imgUtil = new imgUtil();
                String filename = imgUtil.drawUserInfo(username, day);
                String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "[CQ:image,file=" + filename + "]" + "\"}";
                cc.send(resp);

            } catch (JsonSyntaxException e) {
                logger.error("JSON解析失败");
            } finally {
                File d = new File("E:\\酷Q Pro\\data\\image");
                String[] list = d.list();
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        File f2 = new File("E:\\酷Q Pro\\data\\image\\" + list[i]);
                        f2.delete();
                    }
                } else {
                    d.delete();
                }
            }

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
