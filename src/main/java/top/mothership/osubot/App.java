package top.mothership.osubot;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hello world!
 *
 */
public class App {
    public static WebSocketClient cc;
    public static boolean connected = false;
    private static Logger logger = LogManager.getLogger(App.class);
/*
业务逻辑:监听群消息，当检测到!std开头的消息时，分割出后面的用户名，开新线程请求屙屎的api
拿到数据之后读取数据库，如果数据库没有记录就把当前的用户名写入数据库生成空记录，在结果图片加一句“没有记录”如果有记录就加入对比数据
每天凌晨获取数据库“用户名”列的所有成员，并且对osu api进行查询，写入所有数据

 */
    public static void main( String[] args )
    {
        try {
            //实现了抽象类就得实现抽象方法，websocket有四个抽象方法：连接，断连，收到消息和出错
            cc = new WebSocketClient(new URI("ws://localhost:25303"), (Draft) new Draft_17()) {
                public void onOpen(ServerHandshake serverHandshake) {
                    logger.info("连接成功，地址为"+getURI());
                }

                public void onMessage(String message) {
                    try {
                        //
                        JSON json = JSON.parse(message);
                        //原作者使用了String.format，我尝试使用.getString方法。其实我没用过这个json解析器……
                        //如果这条消息是群消息的话
                        if(json.get("act").getString().trim().equals("2"))
                        {
                            String msg = json.get("msg").getString();
                            //对msg进行识别
                            //TODO 定义参数
                            if (msg.startsWith("!")) {
                                //如果消息满足上面的条件，才获取群名/群号并且进行处理
                                String groupId = json.get("fromGroup").getString();
                                String groupName = json.get("fromGroupName").getString();
                                //开启新线程，将msg传入
                                parseThread pt = new parseThread(msg,groupId,cc);
                                logger.info("检测到来自【"+groupName+"】的群消息："
                                        +msg+",已交给线程"+pt.toString()+"处理");
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

                public void onClose(int code , String reason, boolean b) {
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
