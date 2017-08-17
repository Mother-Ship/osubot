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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class apiUtil {

    private Logger logger = LogManager.getLogger(this.getClass());
    private final String key = "d259a80f43e73fb5c421fcbeabc7458af822be9f";
    private final String getUserURL = "https://osu.ppy.sh/api/get_user";
    private final String getBPURL = "https://osu.ppy.sh/api/get_user_best";
    private final String getMapURL = "https://osu.ppy.sh/api/get_beatmaps";


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
        //程序不涉及时区转换，这段单纯是判断当前时间在UTC下是否比晚上八点早，是就获取昨天的晚八点，否就获取当天的晚八点

        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar c = Calendar.getInstance();
        //我要取到UTC时间上一个晚上八点，那如果当前时间比20点早，上一个晚八点就是昨天的
        logger.debug(c.getTime());
        //用HOUR会出现前一个getTIme为22:06也进if块
        if(c.get(Calendar.HOUR_OF_DAY)<20) {
            c.add(Calendar.DATE, -1);
        }
        //这里用HOUR会出现：在早上6点运行，处理后的c.getTime变成08:00:00的问题
        c.set(Calendar.HOUR_OF_DAY, 20);
        //整点
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        logger.debug(c.getTime());
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
