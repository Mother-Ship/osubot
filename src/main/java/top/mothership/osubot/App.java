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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
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
    private static String mainRegex = "[!！]([^ \\u4e00-\\u9fa5]+)([^\\u4e00-\\u767c\\u767e-\\u83db\\u83dd-\\u9fa5]*+)";
    private static String imgRegex = "\\[CQ:image,file=(.+)\\](.*)";
    private static String[] msgs = new String[200];
    private static int start = 0;
    private static int end = 0;
    private static int len = 0;
    private static List<String> qunAdmin = Arrays.asList("2643555740", "290514894", "2307282906", "2055805091", "735862173",
                                                "1142592265", "263202941", "992931505","1335734657","526942417","1012621328");
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

                        if (json.get("act").getString().trim().equals("2") || json.get("act").getString().trim().equals("21")) {
                            String msg = json.get("msg").getString();
                            //对msg进行反转义
                            msg = msg.replaceAll("&#91;", "[");
                            msg = msg.replaceAll("&#93;", "]");
                            String groupId = null;
                            String groupName = null;
                            String fromQQ = null;
                            if (json.get("fromGroup") != null) {
                                groupId = json.get("fromGroup").getString();
                                groupName = json.get("fromGroupName").getString();
                                fromQQ = json.get("fromQQ").getString();
                            } else {
                                fromQQ = json.get("fromQQ").getString();
                            }
                            //对msg进行识别
                            if (msg.matches(mainRegex)) {
                                Matcher m = Pattern.compile(mainRegex).matcher(msg);
                                m.find();
                                //如果消息匹配正则表达式


                                if (m.group(1).equals("sudo")) {
                                    adminThread at = new adminThread(msg, groupName, groupId, fromQQ, cc);
                                    at.start();
                                } else {
                                    //开启新线程，将msg传入
                                    playerThread pt = new playerThread(msg, groupName, groupId, fromQQ, cc);
                                    pt.start();
                                }

                            } else {
                                //如果不是感叹号开头的消息，进入禁言识别

                                //如果是群消息
                                if (json.get("act").getString().trim().equals("2")) {
                                    int count = 0;
                                    //如果消息带图片就刮掉
                                    Matcher m = Pattern.compile(imgRegex).matcher(msg);
                                    if (m.find()) {
                                        msg = m.group(2);
                                        if (msg.equals("")) {
                                            msg = msg.concat("Image");
                                        }
                                    }
                                    //刮掉除了中文英文数字之外的东西
                                    msg = msg.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
                                    //循环数组
                                    if (groupId.equals("201872650")||groupId.equals("564679329")) {
                                        len++;
                                        if (len >= 200) {
                                            len = 200;
                                            start++;
                                        }
                                        if (end == 200) {
                                            end = 0;
                                        }
                                        if (start == 200) {
                                            start = 0;
                                        }
                                        //把群号拼在字符串上
                                        msgs[end] = groupId+msg;
                                        end++;

                                        if (start < end) {
                                            //复读不抓三个字以下的和纯图片
                                            for (int i = 0; i < end; i++) {
                                                if ((groupId+msg).equals(msgs[i]) && !msg.equals("Image") && msg.length() >= 3) {
                                                    count++;
                                                }
                                            }
                                        } else {
                                            for (int i = 0; i < start - 1; i++) {
                                                if ((groupId+msg).equals(msgs[i]) && !msg.equals("Image") && msg.length() >= 3) {
                                                    count++;
                                                }
                                            }
                                            for (int i = end; i < msgs.length; i++) {
                                                if ((groupId+msg).equals(msgs[i]) && !msg.equals("Image") && msg.length() >= 3) {
                                                    count++;
                                                }
                                            }
                                        }

                                    }

                                    if (count >= 6) {
                                        String resp;
                                        if (qunAdmin.contains(fromQQ)) {
                                            logger.info("检测到群管" + fromQQ + "的复读");
                                            resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + "[CQ:at,qq=2643555740] 检测到群管" + "[CQ:at,qq=" + fromQQ + "] 复读。" + "\"}";

                                        } else {
                                            logger.info("正在尝试禁言" + fromQQ);
                                            resp = "{\"act\": \"121\", \"QQID\": \"" + fromQQ + "\", \"groupid\": \"" + groupId + "\", \"duration\":\"" + 600 + "\"}";
                                        }
                                        cc.send(resp);
                                    }

                                }
                                /*
                                一点小笔记：这里如果采用ArrayList：
                                比对消息：遍历数组x次(x<50)
                                插入消息：System.arraycopy(elementData, index, elementData, index + 1,size - index);

                                如果采用LinkedList：
                                比对消息：遍历半个数组，25*x次
                                插入消息：直接在链表最后加上数据

                                而如果采用循环的Array：
                                比对只需要对数组进行遍历，遍历x次
                                插入消息：直接指定Array的第x个数

                                HashMap：(HashSet实际上是value固定的HashMap,但是无序、不能重复、只有迭代器才能查找)
                                计算所有value的HashCode，如果遇到重复的，就原地创建一个LinkedList,把key关联它，把后来的value放在后面，这样最大限度的保证了根据key查找value的效率
                                */

                            }


                        }


                        if (json.get("act").getString().trim().equals("103")) {
                            //群成员增加……
                            //对msg进行识别
                            String beingOperateQQ = json.get("beingOperateQQ").getString();
                            String groupId = json.get("fromGroup").getString();
                            String fromQQ = json.get("fromQQ").getString();

                            welcomeThread wt = new welcomeThread(groupId, beingOperateQQ, cc);
                            logger.info("检测到【" + groupId + "】的由" + fromQQ + "操作的成员新增："
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
