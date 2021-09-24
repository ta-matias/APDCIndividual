package pt.unl.fct.di.apdcindiv.resources;

import static pt.unl.fct.di.apdcindiv.resources.EmailLinkResource.sendAccountPassword;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

@Path("/restricted")
@Consumes(MediaType.APPLICATION_JSON)
public class RestrictedOperationsResource {
	
	private static final Logger log = Logger.getLogger(RestrictedOperationsResource.class.getName());
	
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory accountKeyFactory = datastore.newKeyFactory().setKind("Account");
	private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");
	
	public RestrictedOperationsResource() {}
	
	/**
	 * Verifies the account with userId.
	 * @param userId
	 * @param token
	 * @return 200 if the operation is successful.
	 * 		   403 if the token is not of backOffice user or not valid.
	 * 		   404 if the userId does not exist.
	 * 		   500 if an error has occurred or the transaction was still active.
	 */
	@POST
	@Path("/{userId}/verify")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response verifyAccount(@PathParam("userId") String userId, @QueryParam("tokenId") String tokenId) {
		log.info(String.format("Verifying account whit ID: [%s].\n", userId));
		
		Key accountKey = accountKeyFactory.newKey(userId);
		Key tokenKey = tokenKeyFactory.newKey(Long.valueOf(tokenId));
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			Entity account = txn.get(accountKey);
			
			if(account == null) {
				txn.rollback();
				log.warning(String.format("Account with ID [%s] does not exist.", userId));
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity token = txn.get(tokenKey);
			
			if(token == null) {
				txn.rollback();
				log.warning("Token used is not valid.");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(!token.getString("role").equals("BACKOFFICE")) {
				txn.rollback();
				log.warning("The role of the token provided does not have permission to execute this method.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			String generatedPassword = UUID.randomUUID().toString();
			if(sendAccountPassword(userId, account.getString("repEmail"), generatedPassword)) {
				Entity activeAccount = Entity.newBuilder(account)
						.set("password", DigestUtils.sha512Hex(generatedPassword))
						.set("active", true)
						.build();
				txn.update(activeAccount);
				txn.commit();
				log.info("Account is now active.");
				return Response.ok().build();
			}
			
			
			txn.rollback();
			log.warning("There was an error sending the email.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			
		}catch(DatastoreException e) {
			txn.rollback();
			log.severe("An error has occurred.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				log.severe("Transacion was still active.");
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}	
	}
	
	
	@PUT
	@Path("/{userId}/reactivate")
	public Response reactivateAccount(@PathParam("userId") String userId, @QueryParam("tokenId") String tokenId) {
		log.info("Backoffice reactivating account.");
		
		Key accountKey = accountKeyFactory.newKey(userId);
		Key tokenKey = tokenKeyFactory.newKey(Long.valueOf(tokenId));
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			Entity token = txn.get(tokenKey);
			
			if(token == null) {
				log.warning("Token was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(!token.getString("role").equals("BACKOFFICE")) {
				log.warning("Token does not have permissions to execute this action.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity account = txn.get(accountKey);
			
			if(account == null) {
				log.warning("Account not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(account.getBoolean("active")) {
				log.warning("Account is already active");
				txn.rollback();
				return Response.status(Status.CONFLICT).build();
			}
			
			Entity updatedAccount = Entity.newBuilder(account)
					.set("active", true)
					.build();
			
			log.info("Account reativated.");
			txn.update(updatedAccount);
			txn.commit();
			return Response.ok().build();
		}
		catch(DatastoreException e) {
			log.severe("An error has occurred.");
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		finally {
			if(txn.isActive()) {
				log.severe("Transaction was still active.");
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		
	}
	
	@DELETE
	@Path("/{userId}/delete")
	public Response deleteAccount(@PathParam("userId") String userId, @QueryParam("tokenId") String tokenId) {
		log.info(String.format("Removing account [%s].", userId));
		
		Key accountKey = accountKeyFactory.newKey(userId);
		Key tokenKey = tokenKeyFactory.newKey(Long.valueOf(tokenId));
		Key profileKey = datastore.newKeyFactory().addAncestor(PathElement.of("Account", userId)).setKind("Profile").newKey(String.format("%s_profile", userId));
		
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity token = txn.get(tokenKey);
			
			if(token == null) {
				log.warning("Token was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(token.getString("role").equals("USER")) {
				log.warning("Token does not have sufficient permissions.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity account = txn.get(accountKey);
			
			if(account == null) {
				log.warning("Account was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity profile = txn.get(profileKey);
			
			if(profile == null) {
				log.warning("Profile was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			txn.delete(accountKey, profileKey);
			txn.commit();
			log.info("Account successfully removed.");
			return Response.ok().build();
			
			
		}catch(DatastoreException e) {
			log.severe("An error has occurred.");
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		finally {
			if(txn.isActive()) {
				log.severe("Transaction was still active.");
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

}
