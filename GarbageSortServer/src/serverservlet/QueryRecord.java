package serverservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Constants.SQLInfo;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class GetData
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/QueryRecord" })  //异步处理支持
public class QueryRecord extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sql1 = "SELECT bin_table.bin_loc,time,trash_weight,trash_type FROM bin_table INNER JOIN "
			+ "record_table ON bin_table.bin_id = record_table.bin_id AND user_id = ? ORDER BY time DESC;";   //生成sql语句
	private String sql2 = "SELECT user_score FROM user_table WHERE user_id = ?;";
	
    //doGet会把表单数据存放在url地址后面 安全性差 特别是在查询密码时 且传输规模较小
    //doPost会把表单数据存在HTTP协议的消息体中以实体的方式传送到服务器
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		Connection connect = null;
		PreparedStatement statement1 = null;
		PreparedStatement statement2 = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		try {
			out = response.getWriter();
			Class.forName(SQLInfo.driver);  //com.mysql.jdbc.Driver已被弃用
			connect = DriverManager.getConnection(SQLInfo.url, SQLInfo.username, SQLInfo.password);
			String user_id = request.getParameter("user_id");
			statement1 = connect.prepareStatement(sql1);  //预编译和防止sql注入
			statement1.setInt(1, Integer.parseInt(user_id));  //为参数赋值
			rs1 = statement1.executeQuery();   //查询获得结果集合
			JSONObject json = new JSONObject();   //生成json对象
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  //生成日期格式化字符
			int count = 0;
			while(rs1.next()) {
				JSONObject j = new JSONObject();   //生成json对象
				//生成json数据
				Timestamp time = rs1.getTimestamp("time");
				j.put("bin_loc", rs1.getString("bin_loc"));
				j.put("time", dateformat.format(time));
				j.put("trash_weight", rs1.getInt("trash_weight"));
				j.put("trash_type", rs1.getInt("trash_type"));
				json.put("data", j);   //将每条记录转化为json写入data
				count++;
			}
			json.put("count", count);   //向json数据中记录条数
			
			statement2 = connect.prepareStatement(sql2);
			statement2.setInt(1, Integer.parseInt(user_id));  //为参数赋值
			rs2 = statement2.executeQuery(); 
			if (rs2.next()) {
				json.put("user_score", rs2.getInt("user_score"));
			}
			out.print(json);  //向客户端发送json数据
		}catch(Exception e) {
			e.printStackTrace();  //生吞异常
		}finally {     //释放所有资源
			if (out != null)
				out.close();
			if (rs1 != null)
				try {
					rs1.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (rs2 != null)
				try {
					rs2.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (statement1 != null)
				try {
					statement1.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (statement2 != null)
				try {
					statement2.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (connect != null)
				try {
					connect.close();
				} catch (SQLException e) {e.printStackTrace();}
		}
	}

}
