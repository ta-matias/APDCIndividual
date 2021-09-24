package pt.unl.fct.di.apdcindiv.util;

import java.util.logging.Logger;

public class UserRegisterData {
	
	public String userId;
	public String name;
	public String userUrl;
	public String accountEmail;
	public String repEmail;
	public String NIF;
	public String phone;
	public String address;
	public String description;
	
	public static final String EMAIL_REGEX = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,6}))?$";
	String nifRegEx = "^[0-9]{9}$";
	 Logger log = Logger.getLogger(UserRegisterData.class.getName());
	
	
	public UserRegisterData() { }
	
	public UserRegisterData(String userId, String name, String userUrl, String publicEmail, String privateEmail, String NIF, String phone, String address, String description) {
		this.userId = userId;
		this.name = name;
		this.userUrl = userUrl;
		this.accountEmail = publicEmail;
		this.repEmail = privateEmail;
		this.NIF = NIF;
		this.phone = phone;
		this.address = address;
		this.description = description;
	}
	
	public boolean validate() {
		if(badString(userId)) {
			log.info("Bad userId");
			return false;
		}
		if(badString(name)) {
			log.info("Bad name");
			return false;
		}
		if(badString(userUrl)) {
			log.info("Bad userUrl");
			return false;
		}
		if(!this.accountEmail.matches(EMAIL_REGEX)) {
			log.info("Bad accountEmail");
			return false;
		}
		if(!this.repEmail.matches(EMAIL_REGEX) || this.repEmail.equals(this.accountEmail)) {
			log.info("Bad repEmail");
			return false;
		}
		if(!this.NIF.matches(nifRegEx)) {
			log.info("Bad nif");
		
			return false;
		}
		if(badString(phone)) {
			log.info("Bad phone");
		
			return false;
		}
		if(badString(address)) {
			log.info("Bad address");
			return false;
		}
		if(badString(description)) {
			log.info("Bad description");
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
