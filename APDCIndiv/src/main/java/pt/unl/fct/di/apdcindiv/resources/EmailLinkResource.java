package pt.unl.fct.di.apdcindiv.resources;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Transaction;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Path("/links")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EmailLinkResource {
	
	private static final long EMAIL_API_KEY = 5634161670881280l;
	
	private static final String ACCOUNT_CREATION_SUBJECT = "Account created successfully.";
	
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger log = Logger.getLogger(EmailLinkResource.class.getName());
	
	public EmailLinkResource() { }
	
	
	public static boolean sendAccountPassword( String id, String email, String password) {
		
		log.info("Start sending email to " + email);
		
		Transaction txn = datastore.newTransaction();
		log.info("Transaction started.");
		
		try {
			Entity apiKey = txn.get(datastore.newKeyFactory().setKind("ApiKey").newKey(EMAIL_API_KEY));
			
			if(apiKey == null) {
				txn.rollback();
				log.severe("API key was not found.");
				return false;
			}
			
			log.info("Building email.");
			Email from = new Email("tiagomatiasapdc@gmail.com");
			Email to = new Email(email);
			Content content = new Content("text/html", password);
			Mail mail = new Mail(from, ACCOUNT_CREATION_SUBJECT, to, content);
			
			SendGrid sg = new SendGrid(apiKey.getString("SecretKey"));
			
			Request request = new Request();
			try {
				log.info("starting request.");
				request.setMethod(Method.POST);
				request.setEndpoint("mail/send");
				request.setBody(mail.build());
				com.sendgrid.Response response = sg.api(request);
				if(response.getStatusCode() > 300 || response.getStatusCode() < 200) {
					log.severe("An error ocorred while sending the email." + response.getStatusCode());
					return false;
				}
				
			}
			catch(IOException e) {
					log.severe("An error ocorred while sending the email.");
			    	return false;
			}
			txn.commit();
			log.info("Email was sent successfully.");
			return true;
			
		}catch(DatastoreException e) {
			txn.rollback();
			log.severe("An error has occurred in the datastore.");
			return false;
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				log.severe("Transaction was still active.");
				return false;
			}
		}
		
	}

}
