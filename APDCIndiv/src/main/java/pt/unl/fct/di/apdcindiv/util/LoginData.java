package pt.unl.fct.di.apdcindiv.util;

import java.util.logging.Logger;

public class LoginData {
	
	public String userId;
	public String password;
	
	private Logger log = Logger.getLogger(LoginData.class.getName());
	
	public LoginData() {
		
	}
	
	public LoginData(String username, String password) {
		this.userId = username;
		this.password = password;
	}
	
	public boolean validate() { 
		if(badString(this.userId)) {
			log.warning("Bad username");
			return false;
		}
		if(badString(this.password)) {
			log.warning("Bad password");
			return false;
		}
		return true;
	}
	
	public boolean badString(String str){
		if(str == null)return true;
		if(str.isEmpty()) return true;
		return false;
	}

}
