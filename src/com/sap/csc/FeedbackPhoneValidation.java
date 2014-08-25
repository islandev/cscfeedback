package com.sap.csc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Servlet implementation class FeedbackPhoneValidation
 */
public class FeedbackPhoneValidation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
    public FeedbackPhoneValidation() {
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
		
		String phone = request.getParameter("phone");
		
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberProto;
		try {
			//by default, use "CN" which means China to recognize the region
			// TODO the locale should be loaded as input parameter
			numberProto = phoneUtil.parse(phone,"CN");
			boolean isValid = phoneUtil.isValidNumber(numberProto);
			String fnum = phoneUtil.format(numberProto, PhoneNumberFormat.INTERNATIONAL);
			
			// if valid phone number, return the formatted number to page
			if(!isValid){
				response.getWriter().write("invalid");
			}else{
				response.getWriter().write(fnum);
			}
			
		} catch (NumberParseException e) {
			response.getWriter().write("error");
		}
		
	}

}
