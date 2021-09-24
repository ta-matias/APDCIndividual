package pt.unl.fct.di.apdcindiv.util;

public class UserData {
	
	public String userId;
	public boolean active;
	public String role;
	
	public UserData() {}
	
	public UserData(String userId, boolean active, String role) {
		this.userId = userId;
		this.active = active;
		this.role = role;
	}

}
