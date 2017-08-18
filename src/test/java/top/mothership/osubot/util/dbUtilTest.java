package top.mothership.osubot.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import top.mothership.osubot.pojo.User;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class dbUtilTest extends TestCase {
/*
模拟业务逻辑，新来一个玩家叫Mother Ship
 */
    public void testAddUserName() throws Exception {
        //2.如果不存在，将玩家写入username(通过)
        dbUtil dbUtil = new dbUtil();
        int i = dbUtil.addUserName("Mother Ship");
        Assert.assertEquals(i,1);
    }

    public void testListUserName() throws Exception {
        //3.午时已到（雾），遍历所有username(通过)
        dbUtil dbUtil = new dbUtil();
        List<String> list = dbUtil.listUserName();

        for(int i=0;i<list.size();i++){
            System.out.print(list.get(i));
        }
    }

    public void testGetUserName() throws Exception {
        //1.调用getUserName判断是否存在在username表(通过)
        dbUtil dbUtil = new dbUtil();
        int unid = dbUtil.getUserName("Mother Ship");
        Assert.assertEquals(unid,0);


    }

    public void testAddUserInfo() throws Exception {
        //4.午时已到（雾x2），把List中的数据取出来请求API(通过)
        List<String> list = new ArrayList<>();
        list.add("Mother Ship");
        apiUtil apiUtil = new apiUtil();
        User user = apiUtil.getUser(list.get(0));
        dbUtil dbUtil = new dbUtil();
        int i = dbUtil.addUserInfo(user);
        Assert.assertEquals(i,1);
    }

    public void testGetUserInfo() throws Exception {
        //5.当天凌晨4点刷新过后，根据用户名去取出User对象
        dbUtil dbUtil = new dbUtil();
        //传入参数是距离今天的天数。
        //需要抹掉sql.date对象里的时区信息
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE,-7);
        /*遇到的问题是，直接用Date.valueOf("2017-08-17")SQL语句会变成8.16
        指定了MySQL使用UTC时间，为什么会自动减一天呢？
        发现这样生成的时间会带北京时间的信息，在这里也需要设置时区
        */
        User user  =  dbUtil.getUserInfo("Mother Ship",new Date(c.getTimeInMillis()));



    }

}