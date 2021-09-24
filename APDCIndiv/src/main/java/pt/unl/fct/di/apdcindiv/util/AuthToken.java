package pt.unl.fct.di.apdcindiv.util;

public class AuthToken {
	
	public static final long EXPIRATION_TIME = 1000*60*60*2; //2h
	public String userId;
	public String role;
	public String tokenId;
	public String expirationData;
	
	
	public AuthToken(String username, String role, String tokenId, String expiration) {
		this.userId = username;
		this.role = role;
		this.tokenId = tokenId;
		this.expirationData = expiration;
	}
	
	

}
