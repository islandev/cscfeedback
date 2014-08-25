package com.sap.csc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;

import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

/**
 * Servlet implementation class FileUploadServlet
 */
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// this will store uploaded files
    private static List<FileMeta> files = new LinkedList<FileMeta>();
    
    // Use a unique name with package semantics
    //private static String uniqueName = "com.sap.csc.feedback.upload";
    private static String uniqueName = "com.sap.csc.documentTest";
    // Use a secret key only known to your application
    private static String secretKey = "doctest1234";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 1. Upload File Using Java Servlet API
        files.addAll(MultipartRequestHandler.uploadByJavaServletAPI(request));    
		
        // 2. Upload File Using Apache FileUpload
        //files.addAll(MultipartRequestHandler.uploadByApacheFileUpload(request));
        
        /*for(int i=0;i<files.size();i++){
        	System.out.println("Username:"+files.get(i).getUserName());
        	System.out.println("Username:"+IOUtils.toString(files.get(i).getContent()));
        	
        }*/
 
        // Remove some files
        while(files.size() > 20)
        {
            files.remove(0);
        }
        
        uploadToHCP(files);
 
        // 2. Set response type to json
        //response.setContentType("application/json");
 
        // 3. Convert List<FileMeta> into JSON format
        //ObjectMapper mapper = new ObjectMapper();
 
        // 4. Send resutl to client
        //mapper.writeValue(response.getOutputStream(), files);
	}
	
	
	// upload files to HCP by using HCP Document Service
	private void uploadToHCP(List<FileMeta> files) throws ServletException{
		
		try {
		      Session openCmisSession = null;
		      InitialContext ctx = new InitialContext();
		      String lookupName = "java:comp/env/" + "EcmService";
		      EcmService ecmSvc = (EcmService) ctx.lookup(lookupName);
		      try {
		        // connect to my repository
		        openCmisSession = ecmSvc.connect(uniqueName, secretKey);
		      }
		      catch (CmisObjectNotFoundException e) {
		        // repository does not exist, so try to create it
		        RepositoryOptions options = new RepositoryOptions();
		        options.setUniqueName(uniqueName);
		        options.setRepositoryKey(secretKey);
		        options.setVisibility(Visibility.PROTECTED);
		        ecmSvc.createRepository(options);
		        // should be created now, so connect to it
		        openCmisSession = ecmSvc.connect(uniqueName, secretKey);
		      }

		      // access the root folder of the repository
		      Folder root = openCmisSession.getRootFolder();
		      
		      Folder userFolder = root;
		      
		      if(files.size()==0) return;
		      
		      //String userName = files.get(0).getUserName();
		      String folderName = "cscfeedback";

		      // create a new folder
		      Map<String, String> newFolderProps = new HashMap<String, String>();
		      newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		      newFolderProps.put(PropertyIds.NAME, folderName);
		      try {
		    	  userFolder = root.createFolder(newFolderProps);
		    	  uploadFiles(files,userFolder,openCmisSession);
		    	  
		      } catch (CmisNameConstraintViolationException e) {
		        // Folder exists already, nothing to do
		    	  ItemIterable<CmisObject> children = root.getChildren();
		    	  for (CmisObject o : children) {
		    		  if (o instanceof Folder && folderName.equals(o.getName())) {
		    			  userFolder=(Folder)o;
		    			  uploadFiles(files,userFolder,openCmisSession);
		    			  return;
						}
		    	  }
		      }

		      
		      // Display the root folder's children objects
		      /*ItemIterable<CmisObject> children = root.getChildren();
		      
		      out.println("The root folder of the repository with id " + root.getId()
				                      + " contains the following objects:<ul>");
				for (CmisObject o : children) {
					out.print("<li>" + o.getName());
				if (o instanceof Folder) {
					out.println(" createdBy: " + o.getCreatedBy() + "</li>");
				} else {
				Document doc = (Document) o;
				out.println(" createdBy: " + o.getCreatedBy() + " filesize: "
				                          + doc.getContentStreamLength() + " bytes"
				                          + "</li>");
				}
				}
				out.println("</ul>");*/
		    } catch (Exception e) {
		      throw new ServletException(e);
		    } finally {
		      
		    }
	}
	
	
	private void uploadFiles(List<FileMeta> files,Folder folder,Session openCmisSession) throws ServletException{
		
		try{
			for(int i=0;i<files.size();i++){
		  		  FileMeta file = files.get(i);
		  		  
		  		  String filename = String.valueOf(new Date().getTime()).concat("_").concat(file.getFileName());
		  		  
		  		  //byte[] helloContent = "Hello World!".getBytes("UTF-8");
				      //InputStream stream = new ByteArrayInputStream(helloContent);
				      InputStream stream = file.getContent();
				      byte[] bytes = IOUtils.toByteArray(stream);
				      stream = new ByteArrayInputStream(bytes);
				      //byte[] bytes = IOUtils.toString(stream).getBytes("UTF-8");
				      
				      //byte[] bytes = file.getByteArray();
				      //InputStream stream = new ByteArrayInputStream(bytes);
		  		  
				      /*Metadata metadata = new Metadata();
					  ContentHandler handler = new DefaultHandler();
					  Parser parser = new ImageParser();
					  ParseContext context = new ParseContext();
					 
					  
					  metadata.set(Metadata.CONTENT_TYPE, file.getFileType());
					 
					  parser.parse(stream, handler, metadata, context);
					  String lat = metadata.get("geo:lat");
					  String lon = metadata.get("geo:long");
					  
		  		  
		  		  // create a new file in the root folder
				      Map<String, Object> properties = new HashMap<String, Object>();
				      properties.put(PropertyIds.OBJECT_TYPE_ID, "D:cmisbook:image");
				      properties.put(PropertyIds.NAME, filename);
				      
				      //List<String> tags = Arrays.asList("Hello","OpenSAP team");
				      List<String> tags = Arrays.asList(filename);
				      properties.put("sap:tags", tags);
				      
				      if (lat != null && lon != null) {
					        properties.put("cmisbook:gpsLatitude", BigDecimal.valueOf(Float.parseFloat(lat)));
					        properties.put("cmisbook:gpsLongitude", BigDecimal.valueOf(Float.parseFloat(lon)));
						}
				      
				     
				      
				      
			    	  ContentStream contentStream = openCmisSession.getObjectFactory()
                              .createContentStream(filename,
                              		bytes.length, file.getFileType(), stream);
	    			  folder.createDocument(properties, contentStream, VersioningState.NONE);
	    			  stream.close();*/
				      
				      
				      
				   // Work for txt document.   
				      
				   // create a new file in the root folder
				      Map<String, Object> properties = new HashMap<String, Object>();
				      properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
				      properties.put(PropertyIds.NAME, filename);
				      
				      //List<String> tags = Arrays.asList("Hello","OpenSAP team");
				      List<String> tags = Arrays.asList("hello");
				      properties.put("sap:tags", tags);
				      
				      stream = new ByteArrayInputStream(bytes);
				      
				      
			    	  ContentStream contentStream = openCmisSession.getObjectFactory()
                              .createContentStream(filename,
                              		bytes.length, "text/plain; charset=UTF-8", stream);
	    			  folder.createDocument(properties, contentStream, VersioningState.NONE);
	    			  stream.close();
				     
		  	  }
		}catch(Exception e){
			throw new ServletException(e);
		}
		
	}

}
