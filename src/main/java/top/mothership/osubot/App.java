package top.mothership.osubot;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.thread.parseThread;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
        //业务逻辑：每到凌晨四点，关闭cc，启动db线程进行数据录入，在db线程工作完成后
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
                        //如果这条消息是群消息的话
                        if (json.get("act").getString().trim().equals("2")) {
                            String msg = json.get("msg").getString();
                            //对msg进行识别
                            //如果开头是!stat  statd  bp
                            if (msg.startsWith("!")) {
                                //如果消息由半角感叹号开头，才获取群名/群号并且进行处理
                                String groupId = json.get("fromGroup").getString();
                                String groupName = json.get("fromGroupName").getString();
                                //开启新线程，将msg传入
                                parseThread pt = new parseThread(msg, groupId, cc);
                                logger.info("检测到来自【" + groupName + "】的群消息："
                                        + msg + ",已交给线程" + pt.toString() + "处理");

                                pt.start();
                            }

                        }

                    } catch (ParserException e) {
                        //e.printStackTrace();
                        logger.fatal(e.getMessage());
                    } catch (IOException e) {
                        //e.printStackTrace();
                        logger.fatal(e.getMessage());
                    }
                }

                public void onClose(int code, String reason, boolean b) {
                    logger.info("你已经断开连接: " + getURI() + "; 错误代码: " + code + " " + reason);
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
