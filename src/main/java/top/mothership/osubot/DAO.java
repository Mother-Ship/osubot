package top.mothership.osubot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DAO {
    private Logger logger = LogManager.getLogger(this.getClass());

    //构造方法内初始化驱动
    public DAO() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/osu?characterEncoding=UTF-8", "root",
                "123456");
    }


    //有新查询的时候，将数据写入username表
    public int addUser(User user) {

        String sql = "INSERT INTO `username` (`username`) VALUES (?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            //将User对象的username写入数据库
            ps.setString(1, user.getUsername());
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("写入用户出错");
            logger.error(e.getMessage());
            return 0;
        }

    }


    //遍历username
    public List<String> listUser() {
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
            logger.error("遍历用户名出错");
            logger.error(e.getMessage());
            return null;
        }

    }

    //根据osuAPI拿到完整user之后，写入userinfo
    public int addInfo(User user) {
        //id用null,存入日期用当天的UTC时间，后期对比传入时间也转换为UTC处理
        String sql = "INSERT INTO `userinfo` VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?,?,utc_date())";
        String querySql = "SELECT `Id` FROM `username` WHERE username = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             PreparedStatement queryPs = c.prepareStatement(querySql)) {
            //将User对象中的用户名转为username表中的id
            queryPs.setString(1, user.getUsername());
            ResultSet rs = queryPs.executeQuery();
            int username = rs.getInt("id");
            //将user对象存入userinfo
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
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("写入用户状态出错");
            logger.error(e.getMessage());
            return 0;
        }

    }


    //根据username去数据库两个表中取出user对象
    public User getUser() {
        User user = new User();
        String infosql = "";
        return user;
    }


}
