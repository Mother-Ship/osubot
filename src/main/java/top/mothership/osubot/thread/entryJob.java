package top.mothership.osubot.thread;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.*;

import java.io.IOException;
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
        logger.info("开始获取数据");
        List<String> list = dbUtil.listUserName();
        for (String aList : list) {
            User user = null;
            while (user == null) {
                try {
                    //这里的user是保证官网存在的，所以可能的异常只有网络错误一种
                    user = apiUtil.getUser(aList);
                } catch (IOException e) {
                    logger.error("从api获取玩家" + aList + "信息失败");
                    logger.error(e.getMessage());
                }
            }
            dbUtil.addUserInfo(user);
            logger.info("将" + user.getUsername() + "的数据录入成功");
            try {
                //停止2s，避免触发API频繁操作
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}

