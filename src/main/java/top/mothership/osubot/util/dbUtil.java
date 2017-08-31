package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;


public class dbUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    private ResourceBundle rb;
    //构造方法内初始化驱动
    public dbUtil() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        rb = ResourceBundle.getBundle("cabbage");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(rb.getString("jdbcURL"), rb.getString("jdbcUser"),
                rb.getString("jdbcPwd"));
    }
    //TODO 改造数据库工具，把SQL当做参数传入某个方法
    //客串查询userid有没有被存入过
    public String getUserRole(int userId) {
        String querySql = "SELECT `role` FROM `userrole` WHERE `user_id` = ?";
        try (Connection c = getConnection();
             PreparedStatement queryPs = c.prepareStatement(querySql)) {
            queryPs.setInt(1, userId);
            ResultSet rs = queryPs.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            } else {
                return "notFound";
            }
        } catch (SQLException e) {
            logger.error("获取userRole表中权限出错");
            logger.debug(e.getMessage());
            return "error";
        }
    }
    public int getId(String fromQQ) {
        String querySql = "SELECT `user_id` FROM `userrole` WHERE `QQ` = ?";
        try (Connection c = getConnection();
             PreparedStatement queryPs = c.prepareStatement(querySql)) {
            queryPs.setString(1, fromQQ);
            ResultSet rs = queryPs.executeQuery();
            if(rs.next()) {
                return rs.getInt("user_id");
            }else{
                return 0;
            }
        } catch (SQLException e) {
            logger.error("获取userRole表中QQ出错");
            logger.debug(e.getMessage());
            return 0;
        }
    }
    public String getQQ(int userId) {
        String querySql = "SELECT `QQ` FROM `userrole` WHERE `user_id` = ?";
        try (Connection c = getConnection();
             PreparedStatement queryPs = c.prepareStatement(querySql)) {
            queryPs.setInt(1, userId);
            ResultSet rs = queryPs.executeQuery();
            if(rs.next()) {
                return rs.getString("QQ");
            }else{
                return null;
            }
        } catch (SQLException e) {
            logger.error("获取userRole表中QQ出错");
            logger.debug(e.getMessage());
            return null;
        }
    }
    public int setId(String fromQQ,int userId) {
        String querySql = "UPDATE `userrole` SET `QQ` = ? WHERE `user_id` = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(querySql)) {
            ps.setString(1, fromQQ);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("修改userRole表中QQ出错");
            logger.debug(e.getMessage());
            return 0;
        }
    }



    //为!褪裙功能使用
    public List<Integer> listUserInfoByRole(String role) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT `user_id` FROM `userrole` WHERE `role` = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            //循环从结果集中取出id
            while (rs.next()) {
                Integer id = rs.getInt("user_id");
                list.add(id);
            }
            //将list返回
            return list;
        } catch (SQLException e) {
            logger.error("遍历username表出错");
            logger.error(e.getMessage());
            return null;
        }

    }

    public int editUserRole(int userId, String role) {
        String sql = "UPDATE `userrole` SET `role` = ? WHERE `user_id` = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新玩家角色出错");
            logger.error(e.getMessage());
            return 0;
        }

    }

    //有新查询的时候，将数据写入username表
    public int addUserId(int userId) {
        String sql = "INSERT INTO `userrole` (`user_id`) VALUES (?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            //将User对象的username和id写入数据库
            ps.setInt(1,userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("写入userrole表出错");
            logger.error(e.getMessage());
            return 0;
        }

    }


    //遍历username
    public List<Integer> listUserId() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT `user_id` FROM `userrole`";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            //循环从结果集中取出用户id
            while (rs.next()) {
                Integer user_id = rs.getInt("user_id");
                list.add(user_id);
            }
            //将list返回
            return list;
        } catch (SQLException e) {
            logger.error("遍历userrole表出错");
            logger.error(e.getMessage());
            return null;
        }

    }


    //根据osuAPI拿到完整user之后，写入userinfo
    public int addUserInfo(User user,Date date) {
        //id用null,日期改为前一天
        String sql = "INSERT INTO `userinfo` VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            //将user对象存入userinfo

            ps.setInt(1, user.getUser_id());
            ps.setInt(2, user.getCount300());
            ps.setInt(3, user.getCount100());
            ps.setInt(4, user.getCount50());
            ps.setInt(5, user.getPlaycount());
            ps.setFloat(6, user.getAccuracy());
            ps.setFloat(7, user.getPp_raw());
            ps.setLong(8, user.getRanked_score());
            ps.setLong(9, user.getTotal_score());
            ps.setFloat(10, user.getLevel());
            ps.setInt(11, user.getPp_rank());
            ps.setInt(12, user.getCount_rank_ss());
            ps.setInt(13, user.getCount_rank_s());
            ps.setInt(14, user.getCount_rank_a());
            ps.setDate(15, date);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("写入用户信息出错");
            logger.error(e.getMessage());
            return 0;
        }

    }

    public User getNearestUserInfo(int userId, int day) {
        Calendar cl = Calendar.getInstance();
        cl.add(Calendar.DATE, -day);
        Date date = new Date(cl.getTimeInMillis());
        User user = new User();
        //根据距离给定日期的距离来排序
        String infosql = "SELECT * , abs(UNIX_TIMESTAMP(queryDate) - UNIX_TIMESTAMP(?)) AS ds FROM `userinfo`  WHERE `user_id` = ? ORDER BY ds ASC ";
        //本来是提取返回值中的username，修改表结构之后改为

        try (Connection c = getConnection();
             PreparedStatement infoPs = c.prepareStatement(infosql)) {
            infoPs.setDate(1, date);
            infoPs.setInt(2, userId);
            ResultSet infoRs = infoPs.executeQuery();
            if (infoRs.next()) {
                //根据列名来组装user不容易出错
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
                user.setQueryDate(infoRs.getDate("queryDate"));
                logger.info("查询到玩家" + userId + "最接近于" + new Date(cl.getTimeInMillis()).toString()
                        + "的位于" + infoRs.getDate("queryDate").toString() + "的记录");
                return user;
            } else {
                logger.info("没有查询到玩家" + userId + "最接近于" + new Date(cl.getTimeInMillis()).toString() + "的记录");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    //根据userId和date去userinfo中取出user对象
    public User getUserInfo(int userId, int day) {
        //约定参数，传入dbutil的和传入imgutil的得一致
        /*
                不带参数：day=1，调用dbUtil拿当天凌晨（数据库记载着昨天）的数据进行对比，不需要-1，直接在日历里提早一天
                带day = 0:进入本方法，不读数据库，不进行对比
                day>1，例如day=2，21号进入本方法，查的是19号结束时候的成绩
                */
        //打了日期补丁
        Calendar cl = Calendar.getInstance();
        if(cl.get(Calendar.HOUR_OF_DAY)<4){
            cl.add(Calendar.DAY_OF_MONTH,-1);
        }
        cl.add(Calendar.DATE, -day);
        //去tmdUTC
        User user = new User();
        String infosql = "SELECT * FROM `userinfo` WHERE `user_id` = ? AND `queryDate` = ?";
        try (Connection c = getConnection();
             PreparedStatement infoPs = c.prepareStatement(infosql)) {
            //把参数转化为date对象由调用者完成，这里直接传入数据库
            infoPs.setInt(1, userId);
            infoPs.setDate(2, new Date(cl.getTimeInMillis()));
            ResultSet infoRs = infoPs.executeQuery();
            if (infoRs.next()) {
                //根据列名来组装user不容易出错
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
            } else {
                logger.info("没有查询到玩家" + userId + "在" + new Date(cl.getTimeInMillis()).toString() + "的记录");
                return null;
            }
        } catch (SQLException e) {
            logger.error("获取用户状态出错");
            logger.error(e.getMessage());
            return null;
        }
    }

    public void clearTodayData() {
        //24号所有合法数据是23号，25号早上录入数据应该删除所有24号的数据
        Calendar cl = Calendar.getInstance();
        cl.add(Calendar.DATE,-1);
        String sql = "DELETE FROM `userinfo` WHERE `queryDate` = ?";
        logger.info("正在清除当日临时数据");
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, new Date(cl.getTimeInMillis()));
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}


