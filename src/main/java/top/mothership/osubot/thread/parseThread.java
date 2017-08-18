package top.mothership.osubot.thread;

import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.util.*;

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
    private imgUtil imgUtil = new imgUtil();
    private apiUtil apiUtil = new apiUtil();
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
            //默认初始化为1
            int day = 1;
            String username;

            if (msg.contains("#")) {
                index = msg.indexOf("#");
                day = Integer.valueOf(msg.substring(index + 1));
                username = msg.substring(6, index-1);
            } else {
                username = msg.substring(6);
            }
            if(day<0){
                String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "天数不能为负值" + "\"}";
                cc.send(resp);
                //return void 退出线程
                return;
            }



            logger.info("接收到玩家" + username + "的查询请求");
            if ("a".equals(msg.substring(5, 6))) {
                //后期做一个文本版本的，暂时这两个命令没什么区别
                if (msg.contains("#")) {
                    index = msg.indexOf("#");
                    day = Integer.valueOf(msg.substring(index + 1));
                    username = msg.substring(7, index-1);
                } else {
                    username = msg.substring(8);
                }
            }
            if(dbUtil.getUserName(username)==0){
                logger.info("玩家"+username+"初次使用本机器人，开始登记");
                dbUtil.addUserName(username);
                try {
                    dbUtil.addUserInfo(apiUtil.getUser(username));
                } catch (IOException e) {
                    logger.error("玩家"+username+"初次登记失败");
                    logger.error(e.getMessage());
                }
            }

            try {
                String filename = imgUtil.drawUserInfo(username, day);
                String resp;
                if(filename.equals("notExist")) {
                    resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "玩家不存在" + "\"}";
                }
                resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "[CQ:image,file=" + filename + "]" + "\"}";
                cc.send(resp);
            } catch (JsonSyntaxException e) {
                logger.error("JSON解析失败");
            } finally {
                try {
                    logger.info("线程暂停两秒，以免发送成功前删除文件");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
