package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class dbUtil {
    private Logger logger = LogManager.getLogger(this.getClass());

    //构造方法内初始化驱动
    public dbUtil() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        //蜜汁错误，需要设置时区？？
        return DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/osu?characterEncoding=UTF-8&serverTimezone=UTC", "root",
                "123456");
    }


    public String getuserRole(String userName){
        String querySql = "SELECT `role` FROM `username` WHERE username = ?";
        try (Connection c = getConnection();
             PreparedStatement queryPs = c.prepareStatement(querySql)) {
            queryPs.setString(1, userName);
            ResultSet rs = queryPs.executeQuery();
            if(rs.next()){
                return rs.getString("role");
            }else{
                logger.info("玩家"+userName+"在username表中没有记录");
                return "notFound";
            }
        } catch (SQLException e) {
            logger.error("获取username表中对应id出错");
            logger.debug(e.getMessage());
            return "error";
        }
    }
//TODO 编辑用户角色
//TODO
    //有新查询的时候，将数据写入username表
    public int addUserName(String userName) {

        String sql = "INSERT INTO `username` (`username`) VALUES (?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            //将User对象的username写入数据库
            ps.setString(1, userName);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("写入username表出错");
            logger.error(e.getMessage());
            return 0;
        }

    }


    //遍历username
    public List<String> listUserName() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT `username` FROM `username`";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            //循环从结果集中取出用户名
            while (rs.next()) {
                String userName = rs.getString("username");
                list.add(userName);
            }
            //将list返回
            return list;
        } catch (SQLException e) {
            logger.error("遍历username表出错");
            logger.error(e.getMessage());
            return null;
        }

    }
    //获得用户名对应的id
    public int getUserName(String userName) {
        String querySql = "SELECT `Id` FROM `username` WHERE username = ?";
        try (Connection c = getConnection();
             PreparedStatement queryPs = c.prepareStatement(querySql)) {
            queryPs.setString(1, userName);
            ResultSet rs = queryPs.executeQuery();
            if(rs.next()){
                return rs.getInt("id");
            }else{
                logger.info("玩家"+userName+"在username表中没有记录");
                return 0;
            }
        } catch (SQLException e) {
            logger.error("获取username表中对应id出错");
            logger.debug(e.getMessage());
            return 0;
        }

    }


    //根据osuAPI拿到完整user之后，写入userinfo
    //此方法可以将uname转换为username表中的id，因此只能写入username中存在的用户，只在凌晨更新的时候使用）
    public int addUserInfo(User user) {
        //id用null,存入日期用当天的UTC时间，后期对比传入时间也转换为UTC处理
        String sql = "INSERT INTO `userinfo` VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,utc_date())";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            //将User对象中的用户名转为username表中的id

            //将user对象存入userinfo
            int username = getUserName(user.getUsername());
            if (username == 0) {
                //如果根据用户名查不出用户id
                throw new SQLException("尝试将未使用过本机器人的玩家直接写入userinfo");
            }
            //这里存入数据库的是int类型的id

            ps.setInt(1, username);
            ps.setInt(2, user.getUser_id());
            ps.setInt(3, user.getCount300());
            ps.setInt(4, user.getCount100());
            ps.setInt(5, user.getCount50());
            ps.setInt(6, user.getPlaycount());
            ps.setFloat(7, user.getAccuracy());
            ps.setFloat(8, user.getPp_raw());
            ps.setLong(9, user.getRanked_score());
            ps.setLong(10, user.getTotal_score());
            ps.setFloat(11, user.getLevel());
            ps.setInt(12, user.getPp_rank());
            ps.setInt(13,user.getCount_rank_ss());
            ps.setInt(14,user.getCount_rank_s());
            ps.setInt(15,user.getCount_rank_a());
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("写入用户状态出错");
            logger.error(e.getMessage());
            return 0;
        }

    }


    //根据username和date去userinfo中取出user对象
    public User getUserInfo(String username, Date date) {
        User user = new User();
        String infosql = "SELECT * FROM `userinfo` WHERE `username` = ? AND `queryDate` = ?";
        try (Connection c = getConnection();
             PreparedStatement infoPs = c.prepareStatement(infosql);) {
            //把参数转化为date对象由调用者完成，这里直接传入数据库
            int i = getUserName(username);
            if (i == 0) {
                //如果根据用户名查不出用户id
                throw new SQLException("非法请求：没有使用过本机器人的玩家应该先调用getUserName查询");
            }
            infoPs.setInt(1,i);
            infoPs.setDate(2,date);
            ResultSet infoRs = infoPs.executeQuery();
            if (infoRs.next()) {
                //根据列名来组装user不容易出错
                user.setUsername(username);
                user.setUser_id(infoRs.getInt("user_id"));
                user.setCount300(infoRs.getInt("count300"));
                user.setCount100(infoRs.getInt("count100"));
                user.setCount50(infoRs.getInt("count50"));
                user.setPlaycount(infoRs.getInt("playcount"));
                user.setAccuracy(infoRs.getFloat("accuracy"));
                user.setPp_raw(infoRs.getFloat("pp_raw"));
                user.setRanked_score(infoRs.getLong("ranked_score"));
                user.setTotal_score(infoRs.getLong("total_score"));
                user.setLevel(infoRs.getFloat("level"));
                user.setPp_rank(infoRs.getInt("pp_rank"));
                user.setCount_rank_ss(infoRs.getInt("count_rank_ss"));
                user.setCount_rank_s(infoRs.getInt("count_rank_s"));
                user.setCount_rank_a(infoRs.getInt("count_rank_a"));
                return user;
            }else{
                logger.info("没有查询到玩家"+username+"在"+date.toString()+"的记录");
                return null;
            }
        } catch (SQLException e) {
            logger.error("获取用户状态出错");
            logger.error(e.getMessage());
            return null;
        }

    }

}
