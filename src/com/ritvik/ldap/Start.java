package com.ritvik.ldap;

import java.io.ObjectInputStream.GetField;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import javax.swing.*;

public class Start {

	private final static boolean full_details = true;
	private final static boolean spec_ops = false;
	private final static ArrayList<String> attributes = new ArrayList<>();
	private final static TreeSet<String> list = new TreeSet<>();
	static {
		String tags[] = new String[] {
			"displayName",
			//"thumbnailPhoto",
			//"msExchResourceSearchProperties",
			//"amdocs-employee-id",
			//"amdocs-division",
			//"sAMAccountName",
			//"telephoneNumber",
			//"physicalDeliveryOfficeName",
			//"mobile",
			//"mail",
			//"distinguishedName",
			//"info",
			//"msExchDelegateListBL",
			//"amdocs-site-name",
			//"department",
			//"telephoneNumber",
		};
		for(String tag : tags) {
			attributes.add(tag);		
		}
	}
	private static String getSearchFilter() {
		String searchFilter;
		//searchFilter = "sAMAccountName=ritvikc";  //NTNET ID
		//searchFilter = "(&(displayName=*Boston*)(msExchResourceSearchProperties=Room))"; //NAME for MEETING ROOM
		//searchFilter = "displayName=*(*MFS*)*"; //DISPLAY NAME
		//searchFilter = "displayName=*Sharma*"; //DISPLAY NAME
		//searchFilter = "(&(amdocs-division=Emerging offering)(!(physicalDeliveryOfficeName=*Pune*))(!(physicalDeliveryOfficeName=*Gurgaon*))(!(physicalDeliveryOfficeName=*Prague*))(!(physicalDeliveryOfficeName=*Raanana*))(!(physicalDeliveryOfficeName=*Melbourne*))(!(physicalDeliveryOfficeName=*WFH*)))"; //UNIT
		//searchFilter = "(amdocs-division=Emerging offering)"; //UNIT
		//searchFilter = "mail=Ritvik.Chauhan@amdocs.com"; //EMAIL
		//searchFilter = "telephoneNumber=*55768*"; //EXTENSION
		//searchFilter = "physicalDeliveryOfficeName=Pune, Tower 2 South 1 / S1-310"; //OFFICE LOCATION
		searchFilter = "employeeID=149298"; //EMPLOYEE ID
		//searchFilter = "(department=3920)"; //department
		//searchFilter = "managedObjects=CN=RITVIKC01,OU=Pune (DVCI),OU=APAC,OU=Desktops,OU=Machines,DC=corp,DC=amdocs,DC=com"; //MANAGED OBJECTS
		//https://performancemanager4.successfactors.com/sf/liveprofile?selected_user=149298
		return searchFilter;
	}

	public static void main(String[] args) {
		
		//Login Information
		String vLoginUsername = System.getProperty("user.name");
		char [] vLoginPassword = null;
		//vLoginPassword = "Enter Your Password".toCharArray();
		
		if(vLoginPassword == null || vLoginPassword.length==0) {
			JPanel p = new JPanel();
				p.add(new JLabel("Password for NTID\\"+vLoginUsername+" :"));
				JPasswordField pass = new JPasswordField(20);
				p.add(pass);
					
			String[] options = new String[] {"Okay","Cancel"};
			
			int choice = JOptionPane.showOptionDialog(null, p, "Can you please confirm NTID password?", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if(choice==0) {
				vLoginPassword = pass.getPassword();
			}
			else System.exit(-1);
		}
		
		// Ldap Details
		Hashtable<Object,String> env = new Hashtable<Object, String>();		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "Simple");
		//env.put(Context.SECURITY_PRINCIPAL, "ntnet\\" + vLoginUsername);
		env.put(Context.SECURITY_PRINCIPAL, vLoginUsername + "@ntnet");
		env.put(Context.SECURITY_CREDENTIALS, String.valueOf(vLoginPassword));
		env.put(Context.PROVIDER_URL, "ldap://ldap.corp.amdocs.com:389");
		
		// Login in
		LdapContext ctx = null;
		Boolean loginSuccess = false;
		try {
			ctx = new InitialLdapContext(env, null);
			loginSuccess = true;
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		if(loginSuccess){
			//p("Login Success");
			
			// Querying Details
			try {
				// Ldap Strings
				final String ldapSearchBase = "dc=corp,dc=amdocs,dc=com";

				// Query User/Group
				final String searchFilter;
				searchFilter = getSearchFilter();
				
				//Searching Criteria
				SearchControls searchControls  = new SearchControls();
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchControls.setTimeLimit(30000);

				
				NamingEnumeration queryResult = ctx.search(ldapSearchBase, searchFilter, searchControls);
				if(queryResult != null) {
					int i=1;
					while (queryResult.hasMoreElements()) {
						if(!spec_ops) System.out.println("==================== RESULT "+i+" START ====================");
						SearchResult result = (SearchResult) queryResult.nextElement(); 
						Attributes attrs = result.getAttributes();
						//p(attrs.size()+": ");
						print(attrs,"\t");
						if(!spec_ops) System.out.println("==================== RESULT "+i+" END ====================\n");
						i++;
					} 
					//StringJoiner sj = new StringJoiner(":","[","]");
					if(spec_ops) System.out.print(String.join("\n", list));
				}
				else {
					System.out.println("==================== NO RESULT FOUND ====================\n");
				}
				queryResult.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
		else {
			p("Login Failed");
		}
		
	}
	
	private static void print(Attributes attrs, String sep) throws NamingException {		
		boolean print = true;
		NamingEnumeration<? extends Attribute> ids = attrs.getAll();
		if(ids != null) {
			while(ids.hasMoreElements()) {
				Attribute attr = ids.nextElement();
				if(attr != null) {
					print=true;
					if(!full_details) {
						if(!attributes.contains(attr.getID())){							
							print=false;
						}
					}
					if(print) {
						print(attr, sep);
					}
				}
			}
		}
	}

	private static void print(Attribute attr, String sep) throws NamingException{
		p(sep+attr.getID()+": ");
		sep=sep+"\t";
		
		NamingEnumeration<?> ids = attr.getAll();
		if(ids != null) {
			while(ids.hasMoreElements()) {
				Object a = ids.nextElement();
				String p = null;
				if(a != null) {
					if(a.getClass().getCanonicalName().equalsIgnoreCase("java.lang.String")) {
						p=a.toString().replaceAll("[\t\r\n]+", "; ");
						
					}
					else if(a.getClass().getCanonicalName().equalsIgnoreCase("byte[]")){
						byte[] bytes = (byte[]) a;
						p = "<img src=\"data:image/jpeg;base64,"+Base64.getEncoder().encodeToString(bytes)+"\"/>";
					}
					else {
						p = "<<"+a.getClass().getCanonicalName()+">>";
						
					}
					p(sep+p);
					if(spec_ops) list.add(p.toString().split(",")[0]);
				}
			}
		}
		
	}
	
	private static void p(String s) {
		if(!spec_ops) System.out.println(s);
	}

}
