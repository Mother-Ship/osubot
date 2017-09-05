package top.mothership.osubot.thread;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

public class entryJob extends TimerTask {
    private Logger logger = LogManager.getLogger(this.getClass());
    private dbUtil dbUtil;
    private apiUtil apiUtil;

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
        List<Integer> nullList = new ArrayList<>();
        for (Integer aList : list) {
            User user = apiUtil.getUser(null, aList);
            if (user != null) {
                //将日期改为一天前写入
                dbUtil.addUserInfo(user, new java.sql.Date(new Date().getTime() - 1000 * 3600 * 24));
                logger.info("将" + user.getUsername() + "的数据录入成功");
            } else {
                nullList.add(aList);
            }
            try {
                //停止500ms，避免触发API频繁操作
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        logger.info("数据录入完成，以下玩家返回null："+nullList+"，请手动查验。");


    }

}




