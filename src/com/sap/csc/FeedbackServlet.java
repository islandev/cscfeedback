package com.sap.csc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.JSONObject;


import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Servlet implementation class FeedbackServlet
 */

public class FeedbackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	private FeedbackItemDAOimpl fbiDaOimpl;
	@Resource(name = "mail/Session")
	private Session mailSession;
	
	
	public void init() {
		try {
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx
					.lookup("java:comp/env/jdbc/DefaultDB");
			fbiDaOimpl = new FeedbackItemDAOimpl(ds);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public FeedbackServlet() {

		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				request.getInputStream()));
		String json = "";
		if (br != null) {
			json = br.readLine();
		}
		System.out.println("User input values:"+json);

		JSONObject obj = new JSONObject(json);
		
		String contextPath = request.getServletContext().getRealPath("/");
		
		try {
			//Insert to DB
			doAdd(obj);
	
			//Sending mail
			sendMail(obj,contextPath);
			
			response.getWriter().write("success");
		} catch (Exception e) {
			response.getWriter().write("error");
			System.out.println(e.toString());
		}

	}

	// Insert user data into HANA Database
	private void doAdd(JSONObject obj) throws ServletException {

		try{
			FeedbackItem fbi = new FeedbackItem();
			fbi.setType(Integer.parseInt(obj.getString("type")));
			fbi.setFullName(obj.getString("fullName"));
			fbi.setSubject(obj.getString("subject"));
			fbi.setContent(obj.getString("content"));
			fbi.setCustomerEmail(obj.getString("customerEmail"));
			fbi.setMobileNum(obj.getString("mobileNum"));
			fbi.setSeverity(0);
			fbi.setReplied(0);
			fbi.setDatetime(new java.sql.Date(new java.util.Date().getTime()));
			fbiDaOimpl.insert(fbi);
		}catch (SQLException e) {
			System.out.println("insert failed."+e.toString());
			throw new ServletException(e);
		}
		
	}
	
	private void sendMail(JSONObject obj, String contextPath) throws ServletException{
		
		Transport transport = null;
		try{
			
			int type_t = Integer.parseInt(obj.getString("type"));
			String type_s = "";
			switch (type_t) {
			case 0:
				type_s = "Suggestion";
				break;
			case 1:
				type_s = "Question";
				break;
			case 2:
				type_s = "Bugs";
				break;
			default:
				break;
			}

			String subjectText = "[SAP CSC Feedback] "+type_s +" From "+obj.getString("fullName");

			MimeMessage mimeMessage = new MimeMessage(mailSession);
			
			// Read "To" addresses from properties file
			Properties prop = new Properties();
	        
	        FileReader reader = new FileReader(contextPath+"WEB-INF/mail_config.properties");
	        
	        prop.load(reader);
	        
	        String from = prop.getProperty("To");
	        String toAddress = prop.getProperty("To");
	        
	        if (toAddress.isEmpty()) {
				throw new RuntimeException(
						"To Address may not be empty!");
			}
	        String[] toMore = toAddress.split(",");
	        
	        InternetAddress[] adr=new InternetAddress[toMore.length];  
	        for(int i=0;i<toMore.length;i++){ 
	           adr[i]=new InternetAddress(toMore[i]); 
	        } 

	        InternetAddress[] fromAddress = InternetAddress.parse(from);
	        mimeMessage.setFrom(fromAddress[0]);
			mimeMessage.setRecipients(RecipientType.TO, adr);
			mimeMessage.setSubject(subjectText, "UTF-8");
			MimeMultipart multiPart = new MimeMultipart("alternative");
			MimeBodyPart part = new MimeBodyPart();
			
			// Read mail template
			Configuration cfg = new Configuration();
	        cfg.setServletContextForTemplateLoading(getServletContext(), "WEB-INF/templates");
	        
	        Template template = cfg.getTemplate("mail.ftl"); 
	        
	        // Calculate UTC datetime
	        java.util.Calendar cal = java.util.Calendar.getInstance();
			int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
			int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
			cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));  
			
	        Map<String, String> root = new HashMap<String, String>();  
	        root.put("datetime", new Date(cal.getTimeInMillis()).toString());
	        root.put("type", type_s);
	        root.put("subject", obj.getString("subject"));  
	        root.put("message", obj.getString("content"));  
	        root.put("fullname", obj.getString("fullName"));  
	        root.put("email", obj.getString("customerEmail"));  
	        root.put("telephone", obj.getString("mobileNum"));  
	        
	        StringWriter writer = new StringWriter();
	        
	        template.process(root, writer);  
	        
	        String mailText = writer.toString();
	        
	        part.setText(mailText, "utf-8", "html");
			multiPart.addBodyPart(part);
			
			
			// Embed logo image
			File contextDir = new File(contextPath);  
	        File emailImage = new File(contextDir, "img/icon.png"); 
	        MimeBodyPart mbp2=new MimeBodyPart();  
	        FileDataSource fds=new FileDataSource(emailImage);  
	        DataHandler dh=new DataHandler(fds);  
	        mbp2.setDataHandler(dh);  
	        mbp2.setHeader("Content-ID", "logo_png");
	        mbp2.setFileName(MimeUtility.encodeText(fds.getName())); 
	        multiPart.addBodyPart(mbp2);
	        
			
			mimeMessage.setContent(multiPart);
			// Send mail
			transport = mailSession.getTransport();
			transport.connect();
			transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		} catch (Exception e) {
			System.out.println("insert failed."+e.toString());
			
		} finally {
			// Close transport layer
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					throw new ServletException(e);
				}
			}
		}
	}
	
	

}
