package serverservlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Constants.SQLInfo;
import Constants.UserInfo;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class Scan_QRcode
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/ScanQRcode" })
public class ScanQRcode extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HashMap<Integer,UserInfo> ScanedId = new HashMap<Integer,UserInfo>();   //存放<机器id,用户信息>
	private final String MACHINE = "1";
	private final String USER = "0";
	
    //doGet会把表单数据存放在url地址后面 安全性差 特别是在查询密码时 且传输规模较小
    //doPost会把表单数据存在HTTP协议的消息体中以实体的方式传送到服务器
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = null;
		try {
			out = response.getWriter();
			String Source = request.getParameter("From");  //获取请求来源
			JSONObject json = new JSONObject();   //生成json对象
			if (Source.equals(MACHINE)){      //若发送端为机器 则为机器的轮询
				int machine_id = Integer.parseInt(request.getParameter("machine_id"));   //获取机器传输的id
				if (ScanedId.containsKey(machine_id)) {  //若容器中存在该机器id的键  则代表已被扫描
					UserInfo user = ScanedId.remove(machine_id);   //移除该键值对 remove返回被移除键的值
					json.put("user_id", user.getUser_id());    //返回用户id和用户的昵称
					json.put("user_nickname", user.getUser_nickname());
					json.put("status", true);
				}
				else    //若不存在该键 则未被扫描 返回false
					json.put("status", false);
			}
			else if (Source.equals(USER)) {      //若发送端为用户  则为扫码完毕的信号
				int machine_id = Integer.parseInt(request.getParameter("machine_id"));   //获取用户扫码解析的机器id
				int user_id = Integer.parseInt(request.getParameter("user_id"));   //获取用户传输的id昵称
				String user_nickname = request.getParameter("user_nickname"); 
				UserInfo info = new UserInfo(user_id,user_nickname);   //创建一个用户信息对象
				ScanedId.put(machine_id, info);   //向容器中装入信息
				json.put("status", true);  //返回扫码成功
			}
			else 
				json.put("status", false);   //若源头均不为两者返回false
			out.print(json);  //发送json数据
		} catch (Exception e) {
			e.printStackTrace();  //生吞异常
		}finally {     //释放所有资源
			if (out != null)
				out.close();
		}
	}

}