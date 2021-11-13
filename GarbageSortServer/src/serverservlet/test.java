package serverservlet;

import java.text.SimpleDateFormat;
import java.util.Date;
public class test {

	public static void main(String[] args) {

        SimpleDateFormat dateformat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(new Date(System.currentTimeMillis()));  //格式化当前时间
		System.out.println(time);
	}

}
