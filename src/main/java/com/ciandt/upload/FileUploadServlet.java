package com.ciandt.upload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.glassware.AuthUtil;
import com.google.glassware.MirrorClient;
import com.google.glassware.WebUtil;


/**
 * Class to process a file upload to Google Glass
 * @author CI&T
 *
 */
public class FileUploadServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(FileUploadServlet.class.getSimpleName());

  private static final long DATASTORE_BLOB_MAX_SIZE = 1000000; // in datastore max size of a blob is 1Mb

  /* (non-Javadoc)
 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 */
@Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
	  
	// get user credentials
    String userId = AuthUtil.getUserId(request);
    Credential credential = AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
    Mirror mirror = MirrorClient.getMirror(credential);
	  
	// Check that we have a file upload request
	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	if (isMultipart) {
	  ServletFileUpload upload = new ServletFileUpload();
	  
	  // Set overall request size constraint
	  upload.setSizeMax(DATASTORE_BLOB_MAX_SIZE);
  
	  // Parse the request
	  try {
		FileItemIterator iter = upload.getItemIterator(request);
		
		// Process the uploaded items
		MyFile myFile = new MyFile();

		while (iter.hasNext()) {
		    FileItemStream item = iter.next();

		    // process the form 
		    if (item.isFormField()) {
		        processFormField(item, myFile);
		    // and the file content
		    } else {
		       myFile =  processUploadedFile(item, myFile);
		    }
		}
		
		// send it to Glass
		insertTimelineItem(mirror,myFile.getNote(), myFile.getContentType(), 
				(InputStream)new ByteArrayInputStream(myFile.getContent()), null);

	  } catch (FileUploadException e1) {
		  LOG.log(Level.SEVERE,"doPost. An error occurred: " + e1.getMessage(), e1);
	  }
	}
	response.sendRedirect(WebUtil.buildUrl(request, "/"));
  }

/**
 * Retrieve "notes" from upload form and assign it the note field in MyFile object
 * @param item FileItemStream from upload form
 * @param myFile MyFile object where values from the upload from will be set
 */
private void processFormField(FileItemStream item, MyFile myFile) {

	    try {
	    	String note = IOUtils.toString(item.openStream());
	    	 myFile.setNote(note);
		} catch (IOException e) {
			 LOG.log(Level.SEVERE,"processFormField. An error occurred: " + e.getMessage(), e);
		}
  }
  
/**
 * Create a MyFile object from upload form data
 * @param item FileItemStream from upload form
 * @param myFile MyFile object
 * @return myFile populated with data from upload form
 */
private MyFile processUploadedFile(FileItemStream item, MyFile myFile) {
	// get info from the form
    String fileName = item.getName();
    String contentType = item.getContentType();

    myFile.setName(fileName);
    myFile.setContentType(contentType);

    // Process a file upload in memory
    try {
		byte[] data = IOUtils.toByteArray(item.openStream());
	    myFile.setContent(data);
	} catch (IOException e) {
		//e.printStackTrace();
		 LOG.log(Level.SEVERE,"processUploadedFile. An error occurred: " + e);
	}

    return myFile;
  }
  
	
  /**
   * Insert a new timeline item in the user's glass with an optional
   * notification and attachment.
   * 
   * @param service Authorized Mirror service.
   * @param text timeline item's text.
   * @param contentType Optional attachment's content type (supported content
   *        types are "image/*", "video/*" and "audio/*").
   * @param attachment Optional attachment stream.
   * @param notificationLevel Optional notification level, supported values are
   *        {@code null} and "AUDIO_ONLY".
   * @return Inserted timeline item on success, {@code null} otherwise.
   */
  public  void insertTimelineItem(Mirror service, String text, String contentType,
      InputStream attachment, String notificationLevel) {
	  
	// Creates a menu with options Delete and Share 
	MenuItem menuItemDel = new MenuItem();
	menuItemDel.setAction("DELETE");
	MenuItem menuItemShare = new MenuItem();
	menuItemShare.setAction("SHARE");
	List<MenuItem> menuItens = new ArrayList<MenuItem>(2);
	menuItens.add(menuItemDel);
	menuItens.add(menuItemShare);
    TimelineItem timelineItem = new TimelineItem();
    // Add the menu to the item
    timelineItem.setMenuItems(menuItens);
    timelineItem.setText(text);
    if (notificationLevel != null && notificationLevel.length() > 0) {
      timelineItem.setNotification(new NotificationConfig().setLevel(notificationLevel));
    }
    try {
      if (contentType != null && contentType.length() > 0 && attachment != null) {
        // Insert both metadata and attachment.
        InputStreamContent mediaContent = new InputStreamContent(contentType, attachment);
	    
	    service.timeline().insert(timelineItem, mediaContent).execute();
        return ;
      } else {
        // Insert metadata only.
    	  service.timeline().insert(timelineItem).execute();
        return ;
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE,"An error occurred: " + e);
      LOG.log(Level.INFO, "insertTimeline exiting");
      return ;
    }
  }

}
