package pt.unl.fct.di.apdcindiv.resources;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;


import pt.unl.fct.di.apdcindiv.util.AuthToken;
import pt.unl.fct.di.apdcindiv.util.LoginData;
import pt.unl.fct.di.apdcindiv.util.UpdateInfo;
import pt.unl.fct.di.apdcindiv.util.User;
import pt.unl.fct.di.apdcindiv.util.UserRegisterData;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AccountResource {
	
	
	private static final Logger LOG = Logger.getLogger(AccountResource.class.getName());
	private final Gson g = new Gson();
	
	private Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory accountKeyFactory = datastore.newKeyFactory().setKind("Account");
	private KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");
	
	public AccountResource() {} //Empty constructor to make Jersey work
	
	/**
	 * Register of an account
	 * @param data - the data of the account to be created
	 * @return 200 if the operation was successful.
	 * 		   400 if data is invalid.
	 * 		   403 if the ID already is registered.
	 * 		   500 otherwise.
	 */
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createAccount(UserRegisterData data) {
		LOG.info("Attempting to create an account with userId: " + data.userId);
		
		//Invalid Data
		if(!data.validate()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		Key accountKey = this.accountKeyFactory.newKey(data.userId);
		Key profileKey = datastore.newKeyFactory().addAncestor(PathElement.of("Account", data.userId)).setKind("Profile").newKey(String.format("%s_profile", data.userId));
		
		
		
		
		Query<Key> idQuery = Query.newKeyQueryBuilder().setKind("Account").setFilter(PropertyFilter.eq("userid", data.userId)).build();
		Query<Key> emailQuery = Query.newKeyQueryBuilder().setKind("Account").setFilter(PropertyFilter.eq("accountEmail", data.accountEmail)).build();
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			QueryResults<Key> checkId = txn.run(idQuery);	
			
			if(checkId.hasNext()) {
				//if userId already exists in the datastore
				txn.rollback();
				LOG.warning(String.format("Already exists an account with ID: [%s]\n", data.userId));
				return Response.status(Status.CONFLICT).build();
			}
			
			QueryResults<Key> emailCheck = txn.run(emailQuery);
			
			if(emailCheck.hasNext()) {
				txn.rollback();
				LOG.warning(String.format("The email [%s] used for rep already exists.", data.repEmail));
				return Response.status(Status.CONFLICT).build();
			}
			
			Entity newAccount = Entity.newBuilder(accountKey)
					.set("userid", data.userId)
					.set("name", data.name)
					.set("password", StringValue.of(DigestUtils.sha512Hex("password1")))
					.set("url", data.userUrl)
					.set("accountEmail", data.accountEmail)
					.set("repEmail", data.repEmail)
					.set("nif", data.NIF)
					.set("phone", data.phone)
					.set("address", data.address)
					.set("description", data.description)
					.set("active", false)
					.set("role", "USER")
					.build();
			
			Entity accountProfile = Entity.newBuilder(profileKey)
					.set("capital", 0)
					.set("date_of_creation", System.currentTimeMillis())
					.set("PnL", 0)
					.build();
			
			
			txn.add(newAccount, accountProfile);
			LOG.info(String.format("Created user with ID: [%s]", data.userId));
			txn.commit();
			return Response.ok().build();
			
		}
		catch(DatastoreException e) {
			txn.rollback();
			LOG.severe(String .format("Occurred a DatastoreException while creating the account with ID: [%s]\n", data.userId));
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}
		catch(Exception e) {
            txn.rollback();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		}
		finally {
			if(txn.isActive()) {
				txn.rollback();
				LOG.severe(String.format("Transaction was still active after the creation of the account with ID: [%s]\n", data.userId));
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		
	}
	
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(LoginData data) {
		if(!data.validate()) {
			LOG.warning("Data input is invalid");
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		Query<Key> idQuery = Query.newKeyQueryBuilder().setKind("Account").setFilter(PropertyFilter.eq("userid", data.userId)).build();
		Key tokenKey = datastore.allocateId(tokenKeyFactory.newKey());
		
		Timestamp creation = Timestamp.now();
		
		Instant creationInstant = creation.toDate().toInstant();
		
		Timestamp expires = Timestamp.of(Date.from(creationInstant.plus(2, ChronoUnit.HOURS)));
		
		Transaction txn = datastore.newTransaction();
		
		try {
			QueryResults<Key> idCheck = txn.run(idQuery);
			
			if(!idCheck.hasNext()) {
				txn.rollback();
				LOG.warning("UserId does not exist.");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity account = txn.get(idCheck.next());
			
			if(!account.getBoolean("active")) {
				txn.rollback();
				LOG.warning("The account is not active.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			if(!account.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
				txn.rollback();
				LOG.warning(String.format("Incorrect password for user [%s]", data.userId));
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity token = Entity.newBuilder(tokenKey)
					.set("userId", data.userId)
					.set("role", account.getString("role"))
					.set("creation", creation)
					.set("expiration", expires)
					.build();
			
			AuthToken info = new AuthToken(data.userId, account.getString("role"), Long.toString(token.getKey().getId()), expires.toString());
			
			txn.put(token);
			txn.commit();
			LOG.info(String.format("User [%s] logged in successfully.", info.userId));
			return Response.ok(g.toJson(info)).build();
		}catch(DatastoreException e) {
			txn.rollback();
			LOG.severe("An error as occurred at the datastore.");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		catch(Exception e){
			LOG.severe(e.toString());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			
		}finally {
			if(txn.isActive()) {
				txn.rollback();
				LOG.severe("Transaction is still active.");
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
	
	
	@PUT
	@Path("/{userId}/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAccountInfo(@PathParam("userId") String userId, @QueryParam("tokenId") String tokenId, UpdateInfo data) {
		LOG.info(String.format("Updating information of user [%s]", userId));
		
		if(!data.validate()) {
			LOG.warning("Data is invalid");
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		Key accountKey = accountKeyFactory.newKey(userId);
		Key tokenKey = tokenKeyFactory.newKey(Long.valueOf(tokenId));
		Key profileKey = datastore.newKeyFactory().addAncestor(PathElement.of("Account", userId)).setKind("Profile").newKey(String.format("%s_profile", userId));
		
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity account = txn.get(accountKey);
			
			if(account == null) {
				txn.rollback();
				LOG.warning(String.format("Account with Id [%s] was not found", userId));
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity token = txn.get(tokenKey);
			
			if(token == null) {
				txn.rollback();
				LOG.warning("Token was not found.");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(!token.getString("userId").equals(userId)) {
				txn.rollback();
				LOG.warning(String.format("Token does not belong to user [%s]", userId));
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity profile = txn.get(profileKey);
			
			if(profile == null) {
				txn.rollback();
				LOG.warning("Profile was not found");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity updatedProfile = Entity.newBuilder(profile)
					.set("capital", data.capital)
					.set("PnL", data.PnL)
					.build();
			
			txn.update(updatedProfile);
			txn.commit();
			LOG.info("Profile updated successfully.");
			return Response.ok().build();
			
		}catch(DatastoreException e) {
			txn.rollback();
			LOG.severe("An error has occurred");
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				txn.rollback();
				LOG.severe("Transaction was still active.");
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
				
			}
			
		}
		
	}
	
	@DELETE
	@Path("/{userId}/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@PathParam("userId") String userId, @QueryParam("tokenId") String tokenId) {
		LOG.info(String.format("Logging out of the account [%s]", userId));
		
		Key accountKey = accountKeyFactory.newKey(userId);
		Key tokenKey = tokenKeyFactory.newKey(Long.valueOf(tokenId));
		
		Transaction txn = datastore.newTransaction();
		
		try { 
			
			Entity account = txn.get(accountKey);
			
			if(account == null) {
				LOG.warning("Account was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity token = txn.get(tokenKey);
			
			if(token == null) {
				LOG.warning("Token was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(!token.getString("userId").equals(userId)) {
				LOG.warning(String.format("Token provided is not of the user [%s].", userId));
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			
			txn.delete(tokenKey);
			txn.commit();
			LOG.info(String.format("User [%s] logged out successfully.", userId));
			return Response.ok().build();
			
		}catch(DatastoreException e) {
			LOG.severe("An error has occurred.");
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				LOG.severe("Transaction was still active.");
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
	
	@PUT
	@Path("/{userId}/deactivate")
	public Response deactivate(@PathParam("userId") String userId, @QueryParam("tokenId") String tokenId) {
		LOG.info(String.format("Deactivating account [%s]", userId));
		
		Key accountKey = accountKeyFactory.newKey(userId);
		Key tokenKey = tokenKeyFactory.newKey(Long.valueOf(tokenId));
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			Entity account = txn.get(accountKey);
			
			if(account == null) {
				LOG.warning(String.format("Account %s was not found.", userId));
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity token = txn.get(tokenKey);
			
			if(token == null) {
				LOG.warning("Token was not found.");
				txn.rollback();
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(!token.getString("userId").equals(userId)) {
				LOG.warning(String.format("Token provided is not of the user [%s].", userId));
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity updatedAccount = Entity.newBuilder(account)
					.set("active", false)
					.build();
			
			txn.update(updatedAccount);
			txn.delete(tokenKey);
			txn.commit();
			LOG.info("Account was deactivated successfully.");
			return Response.ok().build();
			
		}catch(DatastoreException e) {
			LOG.severe("An error has occurred.");
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				LOG.severe("Transaction was still active.");
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
	
	@GET
	@Path("/listAll")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll(@QueryParam("tokenId") String tokenId) {
		
		Query<Entity> userQuery = Query.newEntityQueryBuilder().setKind("Account").setFilter(PropertyFilter.eq("role", "USER")).build();
		
		Transaction txn = datastore.newTransaction();
		
		try {
			QueryResults<Entity> results = txn.run(userQuery);
			
			txn.commit();
			
			List<User> data = new LinkedList<>();
			
			results.forEachRemaining(user -> data.add(new User(user.getString("name"), user.getString("nif"), user.getString("url"), user.getString("address"), user.getString("phone"))));
			
			LOG.info("Listing all users");
			return Response.ok(g.toJson(data)).build();
			
		}catch(DatastoreException e) {
			LOG.severe("An error has occurred");
			txn.rollback();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				LOG.severe("Transaction was still active.");
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		
	}
	


}
