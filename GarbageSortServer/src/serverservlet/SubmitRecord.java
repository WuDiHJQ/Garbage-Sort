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
 * Servlet implementation class SubmitRecord
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/SubmitRecord" })
public class SubmitRecord extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public static final int EXPRESS_TYPE = 101;
    public static final int TAKEAWAY_TYPE = 102;
	private String sql = "INSERT INTO record_table VALUES(?,?,?,?,?);";  //插入记录语句
	private String Expresssql = "UPDATE bin_table SET express_weight = express_weight + ? WHERE bin_id = ?;";
	private String TakeAwaysql = "UPDATE bin_table SET takeaway_weight = takeaway_weight + ? WHERE bin_id = ?;";
	
    //doGet会把表单数据存放在url地址后面 安全性差 特别是在查询密码时 且传输规模较小
    //doPost会把表单数据存在HTTP协议的消息体中以实体的方式传送到服务器
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		Connection connect = null;
		PreparedStatement recordstatement = null;
		PreparedStatement binstatement = null;
		ResultSet rs = null;
		try {
			out = response.getWriter();
			Class.forName(SQLInfo.driver);  //com.mysql.jdbc.Driver已被弃用
			connect = DriverManager.getConnection(SQLInfo.url, SQLInfo.username, SQLInfo.password);
			//解析传送的数据
			int user_id = Integer.parseInt(request.getParameter("user_id"));
			int bin_id = Integer.parseInt(request.getParameter("bin_id"));
			String time = request.getParameter("time");
			int trash_weight = Integer.parseInt(request.getParameter("trash_weight"));
			int trash_type = Integer.parseInt(request.getParameter("trash_type"));
			
			//生成记录更新语句
			recordstatement = connect.prepareStatement(sql);  //预编译和防止sql注入
			recordstatement.setInt(1, user_id);
			recordstatement.setInt(2, bin_id);
			recordstatement.setString(3, time);
			recordstatement.setInt(4, trash_weight);
			recordstatement.setInt(5, trash_type);

			//生成垃圾桶重量更新语句
			if (trash_type == EXPRESS_TYPE)
				binstatement = connect.prepareStatement(Expresssql);  //预编译和防止sql注入
			else if (trash_type == TAKEAWAY_TYPE)
				binstatement = connect.prepareStatement(TakeAwaysql);  //预编译和防止sql注入
			binstatement.setInt(1, trash_weight);
			binstatement.setInt(2, bin_id);
			
			JSONObject json = new JSONObject();   //生成json对象
			if (recordstatement.executeUpdate() == 1 && binstatement.executeUpdate() == 1)  //判断两条记录是否升级成功
				json.put("status", true);    //返回提交成功
			else   //若添加数据失败
				json.put("status", false);    //返回提交失败
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
			if (recordstatement != null)
				try {
					recordstatement.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (binstatement != null)
				try {
					binstatement.close();
				} catch (SQLException e) {e.printStackTrace();}
			if (connect != null)
				try {
					connect.close();
				} catch (SQLException e) {e.printStackTrace();}
		}
	}
	
}
