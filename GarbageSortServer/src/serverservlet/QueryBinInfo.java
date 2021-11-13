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
 * Servlet implementation class QueryBinInfo
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/QueryBinInfo" })
public class QueryBinInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sql = "SELECT * FROM bin_table WHERE admin_id = ?;";   //生成sql语句

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
			String admin_id = request.getParameter("admin_id");
			statement = connect.prepareStatement(sql);  //预编译和防止sql注入
			statement.setInt(1, Integer.parseInt(admin_id));  //为参数赋值
			rs = statement.executeQuery();   //查询获得结果集合
			JSONObject json = new JSONObject();   //生成json对象
			int count = 0;
			while(rs.next()) {
				JSONObject j = new JSONObject();   //生成json对象
				//生成json数据
				j.put("bin_id", rs.getInt("bin_id"));
				j.put("bin_loc", rs.getString("bin_loc"));
				j.put("takeaway_weight", rs.getInt("takeaway_weight"));
				j.put("express_weight", rs.getInt("express_weight"));
				json.put("data", j);   //将每条记录转化为json写入data
				count++;
			}
			json.put("count", count);   //向json数据中记录条数
			out.print(json);  //向客户端发送json数据
		}catch(Exception e) {
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
