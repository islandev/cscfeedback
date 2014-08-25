/**
 * 
 */
package com.sap.csc;

import java.sql.Date;

/**
 * @author I307658
 * 
 */
public class FeedbackItem {

	private int type;
	private String content;
	private String subject;
	private String fullName;
	private String customerEmail;
	private String mobileNum;
	private int severity;
	private int isReplied;
	private Date datetime;

	
	
	public int getIsReplied() {
		return isReplied;
	}
	public void setIsReplied(int isReplied) {
		this.isReplied = isReplied;
	}
	public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getMobileNum() {
		return mobileNum;
	}
	public void setMobileNum(String mobileNum) {
		this.mobileNum = mobileNum;
	}
	public int getSeverity() {
		return severity;
	}
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	public int isReplied() {
		return isReplied;
	}
	public void setReplied(int isReplied) {
		this.isReplied = isReplied;
	}

}
