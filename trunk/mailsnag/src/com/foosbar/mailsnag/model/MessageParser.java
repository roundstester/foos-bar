package com.foosbar.mailsnag.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import com.foosbar.mailsnag.util.MessageStore;

public class MessageParser {

	private static final String MIME_PLAIN = "text/plain";
	private static final String MIME_HTML  = "text/html";
	
	public static final Message parse(String message) {
		Message m = new Message();
		
		Session session = Session.getDefaultInstance(new Properties());
		InputStream is = null;

		try {
			is = new ByteArrayInputStream(message.getBytes());
			MimeMessage mimeMessage = new MimeMessage(session, is);
			
			// Set From Addresses
			m.setFrom(parseAddresses(mimeMessage.getFrom()));

			// Set To Addresses
			m.setTo(parseAddresses(mimeMessage.getRecipients(RecipientType.TO)));

			// Set To Addresses
			m.setCc(parseAddresses(mimeMessage.getRecipients(RecipientType.CC)));

			// Set the ID
			m.setId(mimeMessage.getMessageID());
			
			// Set Subject
			m.setSubject(mimeMessage.getSubject());
			
			// Set Received Date (Actually the Sent Date)
			parseDate(m, mimeMessage);
			
			// Parse the attachments
			parseAttachments(m, mimeMessage);
			
		//} catch(IOException e) {
		} catch(MessagingException e) {
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception e) { }
		}

		return m;
	}
	
	public static final MessageData parseData(Message message) {
		
		MessageData data = new MessageData();
		
		String rawContent = MessageStore.load(message);
		
		data.setMessage(rawContent);
		
		InputStream is = null;
		
		try {

			Session session = Session.getDefaultInstance(new Properties());
			
			is = new ByteArrayInputStream(rawContent.getBytes());
			
			MimeMessage mimeMessage = new MimeMessage(session, is);			
		
			parseAttachments(message, mimeMessage);
			
			if(mimeMessage.getContent() instanceof Multipart)
				parseMultipart(mimeMessage, data);
			else
				parseSinglepart(mimeMessage, data);

		} catch(Exception e) {
			
			e.printStackTrace();
			
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception e) { }
		}
		
		return data;
	}

	private static void parseAttachments(Message message, MimeMessage mimeMessage) {
		try {
			
			if(!(mimeMessage.getContent() instanceof Multipart))
				return;
			
			MimeMultipart content = (MimeMultipart)mimeMessage.getContent();
			int count = content.getCount();
			
			for(int x = 0; x < count; x++) {
				BodyPart bp = content.getBodyPart(x);
				
				if(BodyPart.ATTACHMENT.equals(bp.getDisposition())) {
					String filename = bp.getFileName();
					message.addAttachment(Integer.toString(x), filename, bp.getContentType(), bp.getSize());
				}
			}
		} catch(MessagingException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void parseMultipart(MimeMessage mimeMessage, MessageData messageData) throws IOException, MessagingException {
		Multipart content = (Multipart)mimeMessage.getContent();
		int count = content.getCount();
		
		for(int x = 0; x < count; x++) {
			BodyPart bp = content.getBodyPart(x);
			if(bp.isMimeType(MIME_PLAIN)) {
				messageData.setTextMessage(bp.getContent().toString());
				continue;
			}
			if(bp.isMimeType(MIME_HTML)) {
				messageData.setHtmlMessage(bp.getContent().toString());
				continue;
			}
		}
	}

	private static void parseSinglepart(MimeMessage mimeMessage, MessageData messageData) throws IOException, MessagingException {
		
		String content = mimeMessage.getContent().toString();
		
		if(mimeMessage.isMimeType(MIME_PLAIN))
			messageData.setTextMessage(content);
		
		if(mimeMessage.isMimeType(MIME_HTML))
			messageData.setHtmlMessage(content);
	}
	
	private static void parseDate(Message m, MimeMessage mimeMessage) throws MessagingException {
		
		Date d = mimeMessage.getSentDate();
		
		if(d == null)
			d = mimeMessage.getReceivedDate();
		
		if(d == null)
			d = new Date(System.currentTimeMillis());
		
		m.setReceived(d);
	}
	
	private static String parseAddresses(Address[] addresses) throws MessagingException {
		StringBuilder sb = new StringBuilder();
		if(addresses != null && addresses.length > 0) {
			for(Address a : addresses) {
				if(sb.length() > 0)
					sb.append("; ");
				sb.append(a.toString());
			}
		}
		return sb.toString();
	}
}