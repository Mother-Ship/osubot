package top.mothership.osubot.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;

import java.util.Arrays;
import java.util.ResourceBundle;

public class welcomeThread extends Thread {
    private String beingOperateQQ;
    private String groupId;
    private WebSocketClient cc;
    private ResourceBundle rb;
    private String resp;
    private Logger logger = LogManager.getLogger(this.getClass());

    public welcomeThread(String groupId,String beingOperateQQ, WebSocketClient cc){
        this.beingOperateQQ = beingOperateQQ;
        this.cc = cc;
        this.groupId = groupId;
        if(groupId.equals("201872650")) {
            resp = "，欢迎来到mp5。请修改一下你的群名片(包含osu! id)，并读一下置顶的群规。另外欢迎参加mp群系列活动Chart(详见公告)，成绩高者可以赢取奖励。";
        }else if(groupId.equals("564679329")){
            resp = "，欢迎来到mp4。请修改一下你的群名片(包含osu! id)，并读一下置顶的群规。另外欢迎参加mp群系列活动Chart(详见公告)，成绩高者可以赢取奖励。";
        }else{
            resp = "，欢迎。";
        }

    }
    public void sendGroupMsg(String text) {
        String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + text + "\"}";
        cc.send(resp);
    }
    @Override
    public void run() {
        logger.info("开始发送对"+beingOperateQQ+"的欢迎消息");
        resp = "[CQ:at,qq="+beingOperateQQ+"]"+resp;
        sendGroupMsg(resp);
        logger.info("线程" + this.getName() + "处理完毕，已经退出");
    }

}
