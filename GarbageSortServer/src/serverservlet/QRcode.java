package serverservlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Constants.SQLInfo;
import net.sf.json.JSONObject;

/**
 * Servlet implementation class QRcoded
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/QRcode" })
public class QRcode extends HttpServlet {
	private static final long serialVersionUID = 1L;

    //doGet会把表单数据存放在url地址后面 安全性差 特别是在查询密码时 且传输规模较小
    //doPost会把表单数据存在HTTP协议的消息体中以实体的方式传送到服务器
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
	        DecimalFormat df=new DecimalFormat("000000");
			int id = Integer.parseInt(request.getParameter("machine_id"));   //获取机器传输的id
	        String filename="QRcode/" + df.format(id) + ".png";
	        input=getServletContext().getResourceAsStream(filename);   //获得文件的输入流
	        if (input != null) {   //若文件名不存在
		        response.setContentType("image/jpeg"); 	  //将响应的类型设置为图片
		        response.setHeader("Content-Disposition", "attachment;filename=" + filename);
		        output = response.getOutputStream();   //获得输出流
		        byte[]buff=new byte[1024*10];   //指定缓冲区的大小
		        int len=0;
		        while((len=input.read(buff))>-1)   //将输入流读入缓冲区
		            output.write(buff,0,len);  //传送缓冲区中的内容
	        }
		}catch(Exception e) {
			e.printStackTrace();  //生吞异常
		}finally {   //释放所有资源
			if (output != null)
				output.close();
			if (input != null)
				input.close();
		}
	}

	
}
