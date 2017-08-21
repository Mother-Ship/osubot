package top.mothership.osubot.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.Map;
import top.mothership.osubot.pojo.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class apiUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String getUserURL = "https://osu.ppy.sh/api/get_user";
    private final String getBPURL = "https://osu.ppy.sh/api/get_user_best";
    private final String getMapURL = "https://osu.ppy.sh/api/get_beatmaps";
    private String key;
    //构造器内读取配置文件
    public apiUtil() {
        ResourceBundle rb = ResourceBundle.getBundle("cabbage");
        this.key = rb.getString("key");
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
    public List<BP> getTodayBP(String username) throws IOException {
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
        logger.info("获得了" + username + "玩家的前100BP，正在筛选今日BP");
        //定义返回的List
        Type listType = new TypeToken<List<BP>>() {
        }.getType();
        List<BP> list = new Gson().fromJson(output, listType);
        //传入的是北京时间……

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,4);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        List<BP> result = new ArrayList<>();

        for (BP aList : list) {
            //对BP进行遍历，如果产生时间晚于当天凌晨4点
            if (aList.getDate().after(c.getTime())) {
                result.add(aList);
            }
        }
        logger.info("筛选今日BP成功");
        return result;
    }


    public String getMapName(int bid) throws IOException {
        HttpURLConnection httpConnection =
                (HttpURLConnection) new URL(getMapURL + "?k=" + key + "&b=" + bid).openConnection();
        //设置请求头
        httpConnection.setRequestMethod("GET");
        httpConnection.setRequestProperty("Accept", "application/json");
        httpConnection.setConnectTimeout(1000);
        httpConnection.setReadTimeout(1000);
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
