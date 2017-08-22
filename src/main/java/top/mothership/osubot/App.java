package top.mothership.osubot;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.thread.adminThread;
import top.mothership.osubot.thread.entryJob;
import top.mothership.osubot.thread.playerThread;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;

/**
 * Hello world!
 */
public class App {
    public static WebSocketClient cc;
    public static boolean connected = false;
    private static Logger logger = LogManager.getLogger(App.class);

    /*
    业务逻辑:监听群消息，当检测到!stat开头的消息时，分割出后面的用户名，开新线程请求屙屎的api

    拿到用户名之后请求username表，如果username表没有记录则写入记录+返回该用户首次使用本机器人查询
    如果用户在username中有记录，那就读取后面的天数，生成date去查userinfo表
    如果有记录就加入对比数据，如果没有记录什么也不做+返回请等待下一次凌晨例行更新

    每天凌晨获取数据库“用户名”列的所有成员，并且对osu api进行查询，写入所有数据

     */

    public static void main(String[] args) {
        logger.info("欢迎使用白菜1.0");
        Calendar c = Calendar.getInstance();
        if(c.get(Calendar.HOUR_OF_DAY)>=4) {
            c.add(Calendar.DATE, 1);
        }
        //这里用HOUR会出现：在早上6点运行，处理后的c.getTime变成08:00:00的问题
        c.set(Calendar.HOUR_OF_DAY, 4);
        //整点
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Timer timer = new Timer();

        long period = 1000*60*60*24;
        // 从现在开始的下一个UTC 20点，每24小时执行一次
        logger.info("定时任务已添加：于"+c.getTime()+"开始每24小时执行一次");
        timer.schedule(new entryJob(), c.getTime(), period);

        try {
            //实现了抽象类就得实现抽象方法，websocket有四个抽象方法：连接，断连，收到消息和出错
            cc = new WebSocketClient(new URI("ws://localhost:25303"), (Draft) new Draft_17()) {
                public void onOpen(ServerHandshake serverHandshake) {
                    logger.info("连接成功，地址为" + getURI());
                }

                public void onMessage(String message) {
                    try {
                        //
                        JSON json = JSON.parse(message);
                        //原作者使用了String.format，我尝试使用.getString方法。其实我没用过这个json解析器……
                        //新增一个入口，检测是否是管理员消息
                        // 原有逻辑：如果是群消息，对msg进行识别，如果感叹号开头就开启新线程传入。
                        // 现有逻辑：如果是群消息，识别是否是管理员专用的消息，如果是就交给adminThread，其他消息交给playerThread
                        //如果这条消息是群消息的话
                        if (json.get("act").getString().trim().equals("2")) {
                            String msg = json.get("msg").getString();
                            //对msg进行反转义
                            msg  = msg.replaceAll("&#91;","[");
                            msg  = msg.replaceAll("&#93;","]");
                            //对msg进行识别
                            if (msg.startsWith("!") || msg.startsWith("！")) {
                                //如果消息由半角/全角感叹号开头，才获取群名/群号并且进行处理
                                String groupId = json.get("fromGroup").getString();
                                String groupName = json.get("fromGroupName").getString();
                                //如果是需要提权的操作
                                if ("sudo".equals(msg.substring(1, 5))) {
                                    String fromQQ = json.get("fromQQ").getString();
                                    adminThread at = new adminThread(msg,groupId,fromQQ,cc);
                                    logger.info("检测到来自【" + groupName + "】的提权操作群消息："
                                            + msg + ",已交给线程" + at.getName() + "处理");
                                    at.start();
                                }else {
                                    //开启新线程，将msg传入
                                    playerThread pt = new playerThread(msg, groupId, cc);
                                    logger.info("检测到来自【" + groupName + "】的群消息："
                                            + msg + ",已交给线程" + pt.getName() + "处理");

                                    pt.start();
                                }
                            }
                        }

                        if (json.get("act").getString().trim().equals("21")) {
                            //处理私聊消息
                            String msg = json.get("msg").getString();
                            //对msg进行识别
                            if (msg.startsWith("!") || msg.startsWith("！")) {
                                //如果消息由半角/全角感叹号开头，获取消息发送者并且进行处理
                                String fromQQ = json.get("fromQQ").getString();
                                //如果是需要提权的操作
                                if ("sudo".equals(msg.substring(1, 5))) {
                                    adminThread at = new adminThread(msg, fromQQ, cc);
                                    logger.info("检测到来自【" + fromQQ + "】的提权操作私聊消息："
                                            + msg + ",已交给线程" + at.toString() + "处理");
                                    at.start();
                                }
                            }
                        }

                    } catch (ParserException | IOException e) {
                        logger.fatal(e.getMessage());
                    }
                }

                public void onClose(int code, String reason, boolean b) {
                    logger.warn("你已经断开连接: " + getURI() + "; 错误代码: " + code + "原因：" + reason);
                    connected = false;


                }

                public void onError(Exception e) {
                    logger.error(e.getMessage());
                    connected = false;
                }
            };
            //重要
            connected = true;
            cc.connect();
        } catch (URISyntaxException e) {
            logger.error("WebSocket服务器地址无效");
        }
    }



}
