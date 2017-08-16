package top.mothership.osubot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;


public class DAO {
    private Logger logger = LogManager.getLogger(this.getClass());
    public DAO() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/how2java?characterEncoding=UTF-8", "root",
                "123456");
    }
//TODO 定义对数据库的操作
    //将user对象写入数据库
    //根据username去数据库取出user对象
}
