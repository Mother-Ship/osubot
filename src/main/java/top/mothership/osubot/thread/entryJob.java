package top.mothership.osubot.thread;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

public class entryJob extends TimerTask {
    private Logger logger = LogManager.getLogger(this.getClass());
    private dbUtil dbUtil;
    private apiUtil apiUtil;
    private boolean done = false;
    public entryJob() {
        //构造器中初始化工具
        dbUtil = new dbUtil();
        apiUtil = new apiUtil();
    }

    @Override
    public void run() {

        //清除当日stat时添加的临时数据
        dbUtil.clearTodayData();

        logger.info("开始获取数据");
        List<Integer> list = dbUtil.listUserId();
        for (Integer aList : list) {
            User user = null;
            while (user == null) {
                    //这里的user是保证官网存在的，所以可能的异常只有网络错误一种
                    user = apiUtil.getUser(null,aList);
                try {
                    //停止500ms，避免触发API频繁操作
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //将日期改为一天前写入
            dbUtil.addUserInfo(user,new java.sql.Date(new Date().getTime()-1000*3600*24));
            logger.info("将" + user.getUsername() + "的数据录入成功");
            try {
                //停止500ms，避免触发API频繁操作
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        logger.info("所有数据录入完成。");
    }


}

