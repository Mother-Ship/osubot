package top.mothership.osubot.thread;

import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.util.apiUtil;

import java.io.IOException;
import java.util.*;


public class parseThread extends Thread {
    private String msg;
    private String groupId;
    private WebSocketClient cc;
    private Logger logger = LogManager.getLogger(this.getClass());
    //直接new好不习惯
    //最后大概是调用imgUtil，在imgUtil里调用api工具
    private top.mothership.osubot.util.apiUtil apiUtil = new apiUtil();


    public parseThread(String msg, String groupId, WebSocketClient cc) {
        this.msg = msg;
        this.groupId = groupId;
        this.cc = cc;
    }

    public void run() {
        //如果感叹号后面紧跟stat
        if ("stat".equals(msg.substring(1, 5))) {
            String username = msg.substring(6);
            logger.info("正在获取玩家"+username+"的信息");
            //如果要求的是详细统计，将用户名改为第七位开始的字符串
            if ("a".equals(msg.substring(5, 6))) {
                username = msg.substring(7);
            }
            try {
                Float pp = apiUtil.getUser(username).getPp_raw();
                //TODO 调用imgUtil绘图
                String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + pp + "\"}";
                logger.info(username);
                cc.send(resp);

            } catch (IOException e) {
                logger.error("IO错误");
                logger.error(e.getMessage());
            } catch (JsonSyntaxException e) {
                logger.error("JSON解析失败");
            }
        }

        if ("bp".equals(msg.substring(1, 3))) {
            String username = msg.substring(4);
            try {
                //调用方法拿到返回的今日BP
                logger.info("正在获取玩家"+username+"的今日BP");
                List<BP> result = apiUtil.getBP(username);
                String resp = null;
                String beatmap_name = null;
                if (result.size() > 0) {
                    resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"";
                    for (int i = 0; i < result.size(); i++) {
                        logger.info("正在把第" + (i + 1) + "个谱面的id转换为名字");
                        beatmap_name = apiUtil.getMapName(result.get(i).getBeatmap_id());
                        //坑，这里要用\\n，后面还要加空格，否则酷Q不识别。。
                        //不多搞排版了，反正最后完全体是图片，先想想数据库的事吧
                        resp = resp + beatmap_name + "\\n ";
                    }
                    //把最后一个逗号削掉
                    resp = resp.substring(0, resp.length() - 1);
                    resp = resp + "\"}";
                    logger.info("玩家" + username + "发起了对今日BP的请求，获取了" + result.size() + "条BP");
                    cc.send(resp);
                } else {
                    resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "该玩家今日没有更新BP" + "\"}";
                    logger.info("玩家" + username + "发起了对今日BP的请求，但是今天没有更新BP");
                    cc.send(resp);
                }
            } catch (IOException e) {
                logger.error("IO错误");
                logger.error(e.getMessage());
            } catch (JsonSyntaxException e) {
                logger.error("JSON解析失败");
                logger.error(e.getMessage());
            }
        }

        logger.info("线程"+this.getName()+"处理完毕，已经退出");
    }



}
