package serverservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Constants.SQLInfo;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class InfoChange
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/InfoChange" })  //异步处理支持
public class InfoChange extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sqlnickname = "UPDATE user_table SET user_nickname = ? WHERE user_id = ?;";  //生成查询语句
	
    //doGet会把表单数据存放在url地址后面 安全性差 特别是在查询密码时 且传输规模较小
    //doPost会把表单数据存在HTTP协议的消息体中以实体的方式传送到服务器
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		Connection connect = null;
		PreparedStatement statement = null;
		try {
			out = response.getWriter();
			Class.forName(SQLInfo.driver);  //com.mysql.jdbc.Driver已被弃用
			connect = DriverManager.getConnection(SQLInfo.url, SQLInfo.username, SQLInfo.password);
			int userid = Integer.parseInt(request.getParameter("user_id"));  //获取客户端传来的用户id
			String changekey = request.getParameter("Key");     //获取客户端传来的需要更改的项
			JSONObject json = new JSONObject();   //生成json对象
			if (changekey.equals("user_nickname")) {  //若更改信息为昵称
				String changeValue = request.getParameter("Value");
				statement = connect.prepareStatement(sqlnickname); //预编译和防止sql注入
				statement.setString(1, changeValue);
				statement.setInt(2, userid);
				if (statement.executeUpdate() == 1)   //判断是否更改信息
					json.put("status", true);
				else
					json.put("status", false);    //返回注册失败		
			}
			else {
				json.put("status", false);   //向json数据中写入false
			}
			out.print(json);  //向客户端发送json数据
		} catch (Exception e) {
			e.printStackTrace();  //生吞异常
		}finally {     //释放所有资源
			if (out != null)
				out.close();
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
