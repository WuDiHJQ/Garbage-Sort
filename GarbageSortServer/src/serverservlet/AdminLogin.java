package serverservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Constants.SQLInfo;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class AdminLogin
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/AdminLogin" })
public class AdminLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sql = "SELECT * FROM admin_table WHERE admin_account = ?;";  //生成查询语句
	
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
			String account = request.getParameter("account");   //获取客户端传输的账号和密码
			String password = request.getParameter("password");
			statement = connect.prepareStatement(sql);  //预编译和防止sql注入
			statement.setString(1, account);
			rs = statement.executeQuery();   //查询获得结果集合
			JSONObject json = new JSONObject();   //生成json对象
			if (rs.next()) {   //若有下条记录 则判断密码是否正确
				if (password.equals(rs.getString("admin_password"))) {  //密码相同
					//生成json数据
					json.put("status", true);   //向json数据中写入true
					json.put("admin_id", rs.getInt("admin_id"));  //将管理员id返回
				}
				else 
					json.put("status", false);   //向json数据中写入false
			}
			else
				json.put("status", false);   //向json数据中写入false
			out.print(json);  //向客户端发送json数据
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
