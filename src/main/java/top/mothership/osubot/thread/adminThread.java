package top.mothership.osubot.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.apiUtil;
import top.mothership.osubot.util.dbUtil;
import top.mothership.osubot.util.imgUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by QHS on 2017/8/19.
 */
public class adminThread extends Thread {
    private String msg;
    private String groupId;
    private String fromQQ;
    private WebSocketClient cc;
    private top.mothership.osubot.util.apiUtil apiUtil = new apiUtil();
    private top.mothership.osubot.util.dbUtil dbUtil = new dbUtil();
    private top.mothership.osubot.util.imgUtil imgUtil = new imgUtil();
    private Logger logger = LogManager.getLogger(this.getClass());
    private ResourceBundle rb;
    private List<String> admin;
    private boolean group = false;

    public adminThread(String msg, String groupId, String fromQQ, WebSocketClient cc) {
        this.msg = msg;
        this.groupId = groupId;
        this.cc = cc;
        this.fromQQ = fromQQ;
        rb = ResourceBundle.getBundle("cabbage");
        admin = Arrays.asList(rb.getString("admin").split(","));
        group = true;
    }

    //重载构造方法，提供对私聊消息的处理
    public adminThread(String msg, String fromQQ, WebSocketClient cc) {
        this.msg = msg;
        this.cc = cc;
        this.fromQQ = fromQQ;
        rb = ResourceBundle.getBundle("cabbage");
        admin = Arrays.asList(rb.getString("admin").split(","));
    }


    public void sendMsg(String text) {
        if (group) {
            String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + text + "\"}";
            cc.send(resp);
        } else {
            String resp = "{\"act\": \"106\", \"QQID\": \"" + fromQQ + "\", \"msg\":\"" + text + "\"}";
            cc.send(resp);
        }
    }

    //    public void kick(String QQID){
//        if(group){
//            String resp = "{\"act\": \"121\", \"QQID\": \"" + QQID + "\", \"groupid\": \"" + groupId + "\", \"rejectaddrequest\": \"" + "false" + "\",}";
//            cc.send(resp);
//        }else{
//        }
//    }
    public void smoke(String QQID, String duration) {

    }

    @Override
    public void run() {
        //处理修改
//        !sudo add xx,xx,xx,xx:<role>
//        !sudo del xx,xx,xx(将权限回到默认)
//        !sudo 退群
//        !sudo bg <role> [图片]
        if (!admin.contains(fromQQ)) {

            sendMsg("需要管理员权限");

            return;
        }

        if ("add".equals(msg.substring(6, 9)) || "del".equals(msg.substring(6, 9))) {
            //将所有用户名存入数组
            String[] usernames = null;
            String role = null;
            int index = 0;
            if ("add".equals(msg.substring(6, 9))) {
                try {
                    index = msg.indexOf(":");
                    if (index == -1) {
                        //如果拿不到
                        throw new IndexOutOfBoundsException("字符串不含冒号");
                    }
                } catch (IndexOutOfBoundsException e) {
                    logger.error("字符串处理错误");
                    logger.error(e.getMessage());
                    sendMsg("输入格式错误。");
                    logger.info("线程" + this.getName() + "处理完毕，已经退出");
                    return;
                }

                usernames = msg.substring(10, index).split(",");
                role = msg.substring(index + 1);
            } else {
                usernames = msg.substring(10).split(",");
                role = "creep";
            }
            logger.info("分隔字符串完成，用户：" + Arrays.toString(usernames) + "，用户组：" + role);
            List<String> errorList = new ArrayList<>();
            List<String> nullList = new ArrayList<>();
            List<String> doneList = new ArrayList<>();
            List<String> addList = new ArrayList<>();
            String img = null;
            for (int i = 0; i < usernames.length; i++) {
                User user = null;
                int retry = 0;
                while (retry < 3) {
                    //用网上抄来的retry机制
                    try {
                        logger.info("开始从API获取"+usernames[i]+"的信息");
                        user = apiUtil.getUser(usernames[i]);
                        //如果成功就跳出循环
                        break;
                    } catch (IOException e) {
                        logger.error("从api获取玩家" + usernames[i] + "信息失败");
                        logger.error(e.getMessage());
                        logger.error("开始重试，第" + (retry + 1) + "次");
                        //如果失败就计数器+1
                        retry++;
                    }
                }
                if (retry == 3) {
                    logger.error("玩家" + usernames[i] + "重试三次失败，已记录并跳出本次for循环");
                    errorList.add(usernames[i]);
                    break;
                }
                //如果user不是空的(官网存在记录)
                if (user != null) {
                    //查找username数据库
                    if (dbUtil.getUserName(user.getUsername()) == 0) {
                        //如果username库中没有这个用户
                        dbUtil.addUserName(user.getUsername());
                        dbUtil.addUserInfo(user);
                        logger.info("将用户"+user.getUsername()+"添加到数据库。");
                        if (usernames.length == 1) {
                            logger.info("新增单个用户，绘制名片");
                            img = imgUtil.drawUserInfo(user, null, 0, false);
                        }
                        addList.add(user.getUsername());
                    } else {
                        doneList.add(user.getUsername());
                    }
                    dbUtil.editUserRole(user.getUsername(), role);

                } else {
                    nullList.add(usernames[i]);
                }

            }
            String resp;
            if ("add".equals(msg.substring(6, 9))) {
                resp = "用户组修改完成。";
                if (doneList.size() > 0) {
                    resp = resp.concat("\\n修改成功：" + doneList.toString());
                }
            } else {
                resp = "用户组重置完成。";
                if (doneList.size() > 0) {
                    resp = resp.concat("\\n重置成功：" + doneList.toString());
                }
            }
            if (addList.size() > 0) {
                resp = resp.concat("\\n新增成功：" + addList.toString());
            }
            if (nullList.size() > 0) {
                resp = resp.concat("\\n不存在的：" + nullList.toString());
            }
            if (errorList.size() > 0) {
                resp = resp.concat("\\n网络错误：" + errorList.toString());
            }
            if (usernames.length == 0) {
                resp = "没有做出改动。";
            }
            if (img != null) {
                //这时候是只有单个用户，而且绘制名片,相当于usernames.length==1
                resp = resp.concat("\\n[CQ:image,file=" + img + "]");
            }

            sendMsg(resp);

            logger.info("线程" + this.getName() + "处理完毕，已经退出");
        }
        if ("check".equals(msg.substring(6, 11))) {
            String username;
            String resp;
            try {
                username = msg.substring(12);
            } catch (IndexOutOfBoundsException e) {
                logger.error("字符串处理出错");
                logger.error(e.getMessage());
                sendMsg("输入格式错误。");
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }

            if (dbUtil.getUserName(username) > 0) {
                String role = dbUtil.getUserRole(username);
                logger.info("获取了玩家" + username + "的用户组" + role + "。");
                sendMsg("玩家" + username + "的用户组" + "是" + role + "。");
            } else {
                logger.info("玩家" + username + "没有使用过白菜，请先使用add命令添加。");
                sendMsg("玩家" + username + "没有使用过白菜，请先使用add命令添加。");
            }

        }

        if ("退群".equals(msg.substring(6, 8)) || "褪裙".equals(msg.substring(6, 8))) {
            String resp;
            String role;
            try {
                role = msg.substring(9);
            } catch (IndexOutOfBoundsException e) {
                logger.error("字符串处理出错");
                logger.error(e.getMessage());
                sendMsg("输入格式错误。");
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }
            List<String> list = dbUtil.listUserInfoByRole(role);
            List<String> overflowList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                //拿到用户当天的数据
                User user = dbUtil.getUserInfo(list.get(i), 1);
                //如果PP超过了警戒线，请求API拿到最新PP
                if (user.getPp_raw() > Integer.valueOf(rb.getString("mp5RiskPP"))) {
                    user = null;
                    int retry = 0;
                    while (retry < 3) {
                        //用网上抄来的retry机制
                        try {
                            logger.info("开始从API获取"+list.get(i)+"的信息");
                            user = apiUtil.getUser(list.get(i));
                            //如果成功就跳出循环
                            break;
                        } catch (IOException e) {
                            logger.error("从api获取玩家" + list.get(i) + "信息失败");
                            logger.error(e.getMessage());
                            logger.error("开始重试，第" + (retry + 1) + "次");
                            //如果失败就计数器+1
                            retry++;
                        }
                    }
                    if (retry == 3) {
                        logger.error("玩家" + list.get(i) + "重试三次失败，直接返回网络错误");
                        sendMsg("网络错误。");
                        return;
                    }
                    //这时候user不会空了，因为如果还是空就已经跳出去了
                    if (user.getPp_raw() > Integer.valueOf(rb.getString("mp5PP")) + 0.49) {
                        logger.info("玩家" + list.get(i) + "超限，已记录");
                        overflowList.add(list.get(i));
                    }else{
                        logger.info("玩家" + list.get(i) + "没有超限");
                    }
                }
            }
            resp = "查询PP溢出玩家完成。";
            if (overflowList.size() > 0) {
                resp = resp.concat("\\n查询到"+role+"用户组中，以下玩家：" + overflowList.toString()+"PP超出了设定的限制。");
            }else{
                resp = resp.concat("\\n没有检测"+role+"用户组中PP溢出的玩家。" );
            }
            sendMsg(resp);
        }

        if ("bg".equals(msg.substring(6, 8))) {

        }


    }
}
