package Constants;

public class UserInfo {

	public UserInfo(int user_id, String user_nickname) {
		this.user_id = user_id;
		this.user_nickname = user_nickname;
	}
	
	private int user_id;
	private String user_nickname;
	
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getUser_nickname() {
		return user_nickname;
	}
	public void setUser_nickname(String user_nickname) {
		this.user_nickname = user_nickname;
	}
	
}
