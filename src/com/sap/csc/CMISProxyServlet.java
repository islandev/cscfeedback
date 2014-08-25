package com.sap.csc;

import com.sap.ecm.api.AbstractCmisProxyServlet;

public class CMISProxyServlet extends AbstractCmisProxyServlet {

   @Override
   protected String getRepositoryUniqueName() {
       return "com.sap.csc.documentTest";
   }

   @Override
   //For productive applications, use a secure location to store the secret key.
   protected String getRepositoryKey() {
       return "doctest1234";
   }

}