package serverservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Constants.SQLInfo;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class Register
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/UserRegister" })  //异步处理支持
public class UserRegister extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final int STEP_1 = 0;
    private static final int STEP_3 = 2;
	private String sql1 = "SELECT * FROM user_table WHERE user_account = ?;";  //生成查询语句
	private String sql2 = "INSERT INTO user_table VALUES (DEFAULT,?,?,?,\"新用户\",DEFAULT);"; //向数据库插入新账号数据
	
    //doGet会把表单数据存放在url地址后面 安全性差 特别是在查询密码时 且传输规模较小
    //doPost会把表单数据存在HTTP协议的消息体中以实体的方式传送到服务器
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		Connection connect = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			out = response.getWriter();
			Class.forName(SQLInfo.driver);  //com.mysql.jdbc.Driver已被弃用
			connect = DriverManager.getConnection(SQLInfo.url, SQLInfo.username, SQLInfo.password);
			String step = request.getParameter("step");   //获取用户注册步骤
			String account = request.getParameter("account");
			if (Integer.parseInt(step) == STEP_1) {   //若为账号验证阶段
				JSONObject json = new JSONObject();   //生成json对象
				statement = connect.prepareStatement(sql1);  //预编译和防止sql注入
				statement.setString(1,account);
				rs = statement.executeQuery();   //查询获得结果集合
				if (rs.next())    //若有下条记录 则表明该账号已注册
					json.put("status", false);    //返回false
				else
					json.put("status", true);    //若未被注册返回true
				out.print(json);  //向客户端发送json数据
			}
			else if (Integer.parseInt(step) == STEP_3) {  //若为账号注册阶段
				JSONObject json = new JSONObject();   //生成json对象
				String password = request.getParameter("password");   //获取用户提交的密码
				SimpleDateFormat dateformat =new SimpleDateFormat("yyyy-MM-dd");
				String today = dateformat.format(new Date());  //格式化今日时间
				statement = connect.prepareStatement(sql2);  //预编译和防止sql注入
				statement.setString(1,account);
				statement.setString(2,password);
				statement.setString(3,today);
				if (statement.executeUpdate() == 1)  //判断是否成功添加
					json.put("status", true);    //返回注册成功
				else   //若添加数据失败
					json.put("status", false);    //返回注册失败		
				out.print(json);  //向客户端发送json数据
			}
		} catch (Exception e) {
			e.printStackTrace();  //生吞异常
		}finally {     //释放所有资源
			if (out != null)
				out.close();
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (connect != null)
				try {
					connect.close();
				} catch (SQLException e) {e.printStackTrace();}
		}
	}
	
	
}
