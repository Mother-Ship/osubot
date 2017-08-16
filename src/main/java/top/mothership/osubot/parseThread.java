package top.mothership.osubot;

import org.java_websocket.client.WebSocketClient;


public class parseThread extends Thread {
    private String msg;
    private WebSocketClient cc;

    public parseThread(String msg,String groupId, WebSocketClient cc) {
        this.msg = msg;
        this.cc = cc;
    }
    public void run(){
        //TODO 请求屙屎API拿取数据，并调用imgUtil绘图
        String json = "{\"act\": \"101\", \"groupid\": \"" + 532783765 + "\", \"msg\":\"" + msg.trim() + "\"}";
        cc.send(json);
    }
}
