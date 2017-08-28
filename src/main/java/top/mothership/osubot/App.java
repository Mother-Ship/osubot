package top.mothership.osubot;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import top.mothership.osubot.thread.adminThread;
import top.mothership.osubot.thread.entryJob;
import top.mothership.osubot.thread.playerThread;
import top.mothership.osubot.thread.welcomeThread;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 */
public class App {
    public static WebSocketClient cc;
    public static boolean connected = false;
    private static Logger logger = LogManager.getLogger(App.class);
    private static String mainRegex = "[!！]([^ ]+)(.*+)";

    /*
    业务逻辑:监听群消息，当检测到!stat开头的消息时，分割出后面的用户名，开新线程请求屙屎的api

    拿到用户名之后请求username表，如果username表没有记录则写入记录+返回该用户首次使用本机器人查询
    如果用户在username中有记录，那就读取后面的天数，生成date去查userinfo表
    如果有记录就加入对比数据，如果没有记录什么也不做+返回请等待下一次凌晨例行更新

    每天凌晨获取数据库“用户名”列的所有成员，并且对osu api进行查询，写入所有数据

     */

    public static void main(String[] args) {
        logger.info("欢迎使用白菜1.0");
        //定时任务
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.HOUR_OF_DAY) >= 4) {
            c.add(Calendar.DATE, 1);
        }
        //这里用HOUR会出现：在早上6点运行，处理后的c.getTime变成08:00:00的问题
        c.set(Calendar.HOUR_OF_DAY, 4);
        //整点
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Timer timer = new Timer();

        long period = 1000 * 60 * 60 * 24;
        // 从现在开始的下一个UTC 20点，每24小时执行一次
        logger.info("定时任务已添加：于" + c.getTime() + "开始每24小时执行一次");
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

                        //群消息和私聊消息合并

                        if (json.get("act").getString().trim().equals("2")||json.get("act").getString().trim().equals("21")) {
                            String msg = json.get("msg").getString();
                            //对msg进行反转义
                            msg = msg.replaceAll("&#91;", "[");
                            msg = msg.replaceAll("&#93;", "]");

                            //对msg进行识别
                            if (msg.matches(mainRegex)) {
                                Matcher m= Pattern.compile(mainRegex).matcher(msg);
                                m.find();
                                //如果消息匹配正则表达式
                                String groupId=null;
                                String groupName=null;
                                String fromQQ=null;
                                if(json.get("fromGroup")!=null) {
                                    groupId = json.get("fromGroup").getString();
                                    groupName = json.get("fromGroupName").getString();
                                    fromQQ = json.get("fromQQ").getString();
                                }else{
                                    fromQQ = json.get("fromQQ").getString();
                                }

                                if (m.group(1).equals("sudo")) {
                                    adminThread at = new adminThread(msg,groupName,groupId, fromQQ, cc);
                                    at.start();
                                } else {
                                    //开启新线程，将msg传入
                                    playerThread pt = new playerThread(msg,groupName,groupId, fromQQ, cc);
                                    pt.start();
                                }

                            }else{
                                //如果不是感叹号开头的消息，进入禁言识别
                                //TODO 禁言识别

                                // 当同样消息出现五条之后，开始缓冲消息，
                                //到100条谁说了第六条就谁复读，判定到复读之后判定是否群管，如果是群管复读艾特群主，如果是群主。。什么也不做（x
                            }


                        }



                        if (json.get("act").getString().trim().equals("103")) {
                            //群成员增加……
                            //对msg进行识别
                            String beingOperateQQ = json.get("beingOperateQQ").getString();
                            String groupId = json.get("fromGroup").getString();
                            String fromQQ = json.get("fromQQ").getString();

                            welcomeThread wt = new welcomeThread(groupId,beingOperateQQ, cc);
                            logger.info("检测到【" + groupId + "】的由"+fromQQ+"操作的成员新增："
                                    + beingOperateQQ + ",已交给线程" + wt.toString() + "处理");
                            wt.start();


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
