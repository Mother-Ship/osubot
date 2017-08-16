package top.mothership.osubot;

import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.Map;
import top.mothership.osubot.pojo.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class parseThread extends Thread {
    private String msg;
    private String groupId;
    private WebSocketClient cc;
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String key = "d259a80f43e73fb5c421fcbeabc7458af822be9f";
    private final String getUserURL = "https://osu.ppy.sh/api/get_user";
    private final String getBPURL = "https://osu.ppy.sh/api/get_user_best";
    private final String getMapURL = "https://osu.ppy.sh/api/get_beatmaps";


    public parseThread(String msg, String groupId, WebSocketClient cc) {
        this.msg = msg;
        this.groupId = groupId;
        this.cc = cc;
    }

    public void run() {


        //如果感叹号后面紧跟stat
        if ("stat".equals(msg.substring(1, 5))) {
            String username = msg.substring(6);
            //如果要求的是详细统计，将用户名改为第七位开始的字符串
            if ("a".equals(msg.substring(5, 6))) {
                username = msg.substring(7);
            }

            try {
                Float pp = getUser(username).getPp_raw();
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
                List<BP> result = getBP(username);
                String resp = null;
                String beatmap_name = null;
                if (result.size() > 0) {

                    resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"";
                    for (int i = 0; i < result.size(); i++) {
                        logger.info("正在把第" + (i + 1) + "个谱面的id转换为名字");
                        beatmap_name = getMapName(result.get(i).getBeatmap_id());
                        //坑，这里要用\\n，后面还要加空格，否则酷Q不识别。。
                        //不多搞排版了，反正最后完全体是图片，先想想数据库的事吧
                        resp = resp + beatmap_name + "\\n ";
                    }
                    //把最后一个逗号削掉
                    resp = resp.substring(0, resp.length() - 1);
                    resp = resp + "\"}";
                    logger.debug(resp);
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


    }

    //用来请求API，获取用户数据的方法
    public User getUser(String username) throws IOException, JsonSyntaxException {

        HttpURLConnection httpConnection =
                (HttpURLConnection) new URL(getUserURL + "?k=" + key + "&u=" + username).openConnection();
        //设置请求头
        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty("Accept", "application/json");
        //如果ppy的泡面撒了
        if (httpConnection.getResponseCode() != 200) {
            throw new IOException("HTTP GET请求失败: "
                    + httpConnection.getResponseCode());
        }
        //读取返回结果
        BufferedReader responseBuffer =
                new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
        String output = responseBuffer.readLine();
        //手动关闭流
        httpConnection.disconnect();
        responseBuffer.close();
        //去掉两侧的中括号
        output = output.substring(1, output.length() - 1);

        //么个叽，什么jsonlib什么org.json，连个api文档都没有，用Gson算了
        return new Gson().fromJson(output, User.class);
    }


    //用来请求API获取今日BP的方法
    public List<BP> getBP(String username) throws IOException {
        HttpURLConnection httpConnection =
                (HttpURLConnection) new URL(getBPURL + "?k=" + key + "&limit=100&u=" + username).openConnection();
        //设置请求头
        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty("Accept", "application/json");
        //如果ppy的泡面撒了
        if (httpConnection.getResponseCode() != 200) {
            throw new IOException("HTTP GET请求失败: "
                    + httpConnection.getResponseCode());
        }
        //读取返回结果
        BufferedReader responseBuffer =
                new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
        //BP的返回结果有时候会有换行，必须手动拼接
        String output = "";
        String tmp;
        while ((tmp = responseBuffer.readLine()) != null) {
            output = output + tmp;
        }
        //手动关闭流
        httpConnection.disconnect();
        responseBuffer.close();
        logger.info("获得了" + username + "玩家的前100BP");
        //定义返回的List
        Type listType = new TypeToken<List<BP>>() {
        }.getType();
        List<BP> list = new Gson().fromJson(output, listType);
        //构造当日凌晨4点的date对象(UTC时间昨晚八点)
        Calendar c = Calendar.getInstance();
        //日期-1,时间设为晚上八点
        c.add(Calendar.DATE, -1);
        c.set(Calendar.HOUR, 8);
        //整点
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        List<BP> result = new ArrayList<BP>();

        for (int i = 0; i < list.size(); i++) {
            //对BP进行遍历，如果产生时间晚于当天凌晨4点(UTC时间昨晚八点)
            if (list.get(i).getDate().after(c.getTime())) {
                result.add(list.get(i));
            }
        }
        return result;
    }


    public String getMapName(int bid) throws IOException {
        HttpURLConnection httpConnection =
                (HttpURLConnection) new URL(getMapURL + "?k=" + key + "&b=" + bid).openConnection();
        //设置请求头
        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty("Accept", "application/json");
        //如果ppy的泡面撒了
        if (httpConnection.getResponseCode() != 200) {
            throw new IOException("HTTP GET请求失败: "
                    + httpConnection.getResponseCode());
        }
        //读取返回结果
        BufferedReader responseBuffer =
                new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
        String output = responseBuffer.readLine();
        //手动关闭流
        httpConnection.disconnect();
        responseBuffer.close();
        //去掉两侧的中括号
        output = output.substring(1, output.length() - 1);
        //组装实体类
        Map map = new Gson().fromJson(output, Map.class);
        //组装返回字符串
        return map.getArtist() + " - " + map.getTitle() + " [" + map.getVersion() + "]";
    }

}
