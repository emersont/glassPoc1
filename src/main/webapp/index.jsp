<%--
Copyright (C) 2013 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>
<%@ page import="com.google.api.services.mirror.model.Contact" %>
<%@ page import="com.google.glassware.MirrorClient" %>
<%@ page import="com.google.glassware.WebUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.api.services.mirror.model.TimelineItem" %>
<%@ page import="com.google.api.services.mirror.model.Subscription" %>
<%@ page import="com.google.api.services.mirror.model.Attachment" %>
<%@ page import="com.google.glassware.MainServlet" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!doctype html>
<%
  String userId = com.google.glassware.AuthUtil.getUserId(request);
  String appBaseUrl = WebUtil.buildUrl(request, "/");

  Credential credential = com.google.glassware.AuthUtil.getCredential(userId);

  Contact contact = MirrorClient.getContact(credential, MainServlet.CONTACT_ID);

  List<TimelineItem> timelineItems = MirrorClient.listItems(credential, 6L).getItems();


  List<Subscription> subscriptions = MirrorClient.listSubscriptions(credential).getItems();
  boolean timelineSubscriptionExists = false;
  boolean locationSubscriptionExists = false;


  if (subscriptions != null) {
    for (Subscription subscription : subscriptions) {
      if (subscription.getId().equals("timeline")) {
        timelineSubscriptionExists = true;
      }
      if (subscription.getId().equals("locations")) {
        locationSubscriptionExists = true;
      }
    }
  }

%>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Glass POC</title>
  <link href="/static/bootstrap/css/bootstrap.min.css" rel="stylesheet"
        media="screen">
  <link href="/static/bootstrap/css/bootstrap-responsive.min.css"
        rel="stylesheet" media="screen">
  <link href="/static/main.css" rel="stylesheet" media="screen">
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="#">Glass POC</a>
    </div>
  </div>
</div>

<div class="container">

  <% String flash = WebUtil.getClearFlash(request);
    if (flash != null) { %>
  <div class="alert alert-info"><%= StringEscapeUtils.escapeHtml4(flash) %></div>
  <% } %>

  <h1>Your Recent Timeline</h1>
  <div class="row">

    <div style="margin-top: 5px;">

      <% if (timelineItems != null && !timelineItems.isEmpty()) {
        for (TimelineItem timelineItem : timelineItems) { %>
      <div class="span4">
        <table class="table table-bordered">
          <tbody>
            <tr>
              <th>ID</th>
              <td><%= timelineItem.getId() %></td>
            </tr>
            <tr>
              <th>Text</th>
              <td><%= StringEscapeUtils.escapeHtml4(timelineItem.getText()) %></td>
            </tr>
			<% if (timelineItem.getHtml() != null) { %>
            <tr>
              <th>HTML</th>
              <td><%= StringEscapeUtils.escapeHtml4(timelineItem.getHtml()) %></td>
            </tr>
			<%}%>
            <tr>
              <th>Attachments</th>
              <td>
                <%
                if (timelineItem.getAttachments() != null) {
                  for (Attachment attachment : timelineItem.getAttachments()) {
                    if (MirrorClient.getAttachmentContentType(credential, timelineItem.getId(), attachment.getId()).startsWith("")) { %>
                <img src="<%= appBaseUrl + "attachmentproxy?attachment=" +
                  attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">
                <%  } else { %>
                <a href="<%= appBaseUrl + "attachmentproxy?attachment=" +
                  attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">
                  Download</a>
                <%  }
                  }
                } else { %>
                <span class="muted">None</span>
                <% } %>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                <form class="form-inline"
                      action="<%= WebUtil.buildUrl(request, "/main") %>"
                      method="post">
                  <input type="hidden" name="itemId"
                         value="<%= timelineItem.getId() %>">
                  <input type="hidden" name="operation"
                         value="deleteTimelineItem">
                  <button class="btn btn-block btn-danger"
                          type="submit">Delete</button>
                </form>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <% }
      } %>
    </div>
    <div style="clear:both;"></div>
  </div>
 </div>

  <hr/>

  <div class="row">
    <div class="span4">
      <h2>Timeline</h2>

      <p>Use these controls to insert more text items into your timeline.</p>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertItem">
        <textarea class="span4" name="message">Hello World!</textarea><br/>
        <button class="btn btn-block btn-success" type="submit">
          Insert the above message
        </button>
      </form>
    </div>


  <!-- file upload -->
  <div class="row">
    <div class="span4">
      <h2>Files</h2>
	   <p>Use these controls to insert more image items into your timeline.</p>
		<form method="POST" enctype="multipart/form-data" action="<%= WebUtil.buildUrl(request, "/upload") %>">
			File (with less than 300Kb) to upload: <input type="file" name="upfile" id="upfile"><br/>
			Notes about the file: <input type="text" name="note" id="note"><br/>
			<br/>
			<input class="btn btn-block btn-success" type="submit" value="Press"> to upload the file!
		</form>
	</div>
  </div>
</div>

<script
    src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>