/*
 Copyright (C) 2012-2014 AC SOFTWARE SP. Z O.O.
 (p.zygmunt@acsoftware.pl)
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.acsoftware.android.erpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.jar.JarOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint.Join;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

public class RemoteAction {
	
	private static final String _TAG = "RemoteAction"; 
	
	public static final int SRVCAP_REGISTERDEVICE             =  0x0000001;
	public static final int SRVCAP_LOGIN                      =  0x0000002;
	public static final int SRVCAP_FETCHRECORDSFROMRESULT     =  0x0000004;
	public static final int SRVCAP_FETCHDOCUMENTFROMRESULT    =  0x0000008;
	public static final int SRVCAP_CUSTOMERSIMPLESEARCH       =  0x0000010;
	public static final int SRVCAP_INVOICEBYID                =  0x0000020;
	public static final int SRVCAP_INVOICES                   =  0x0000040;
	public static final int SRVCAP_INVOICEITEMS               =  0x0000080;
	public static final int SRVCAP_INVOICEDOC                 =  0x0000100;
	public static final int SRVCAP_OUTSTANDINGPAYMENTS        =  0x0000200;
	public static final int SRVCAP_ORDERBYID                  =  0x0000400;
	public static final int SRVCAP_ORDERS                     =  0x0000800;
	public static final int SRVCAP_ORDERITEMS                 =  0x0001000;
	public static final int SRVCAP_ORDERDOC                   =  0x0002000;
	public static final int SRVCAP_INDIVIDUALPRICES           =  0x0004000;
	public static final int SRVCAP_ARTICLES                   =  0x0008000;
	public static final int SRVCAP_ARTICLESIMPLESEARCH        =  0x0010000;
	public static final int SRVCAP_ADDCONTRACTOR              =  0x0020000;
	public static final int SRVCAP_NEWINVOICE                 =  0x0040000;
	public static final int SRVCAP_NEWORDER                   =  0x0080000;
	public static final int SRVCAP_GETDICTIONARY              =  0x0100000;
	public static final int SRVCAP_GETLIMITS                  =  0x0200000;
	public static final int SRVCAP_GETUSERDETAILS             =  0x0400000;
	
	public static final int DICTTYPE_CONTRACTOR_COUNTRY         =  1;
	public static final int DICTTYPE_CONTRACTOR_REGION          =  2;
	public static final int DICTTYPE_CONTRACTOR_PAYMENTMETHODS  =  3;
	public static final int DICTTYPE_NEWORDER_STATE             =  4;
	
	public static final int IPRESULT_ERROR               = 0;
	public static final int IPRESULT_ITEMNOTEXISTS       = 1;
	public static final int IPRESULT_CONTRACTORERROR     = 2;
	public static final int IPRESULT_UNKNOWNPRICE        = 3;
	public static final int IPRESULT_OK                  = 4;
	
	public static final int SRESULTCODE_NONE                                = 0;
	public static final int SRESULTCODE_OK                                  = 1;
	public static final int SRESULTCODE_INTERNAL_SERVER_ERROR               = 2;
	public static final int SRESULTCODE_PARAM_ERROR                         = 3;
	public static final int SRESULTCODE_INVALID_ACCESSKEY                   = 4;
	public static final int SRESULTCODE_LOGIN_INCORRECT                     = 5;
	public static final int SRESULTCODE_INSUFF_ACCESS_RIGHTS                = 6;
	public static final int SRESULTCODE_INVALID_PASSWD_RETYPE               = 7;
	public static final int SRESULTCODE_EMAIL_SEND_ERROR                    = 8;
	public static final int SRESULTCODE_INVALID_ADDRESS_OR_ID               = 9;
	public static final int SRESULTCODE_INVALID_KEY                         = 10;
	public static final int SRESULTCODE_TEMPORARILY_UNAVAILABLE             = 11;
	public static final int SRESULTCODE_NOTEXISTS_OR_INSUFF_ACCESS_RIGHTS   = 12;
	public static final int SRESULTCODE_NOTEXISTS                           = 13;
	public static final int SRESULTCODE_OPERATION_NOT_ALLOWED               = 14;
	public static final int SRESULTCODE_ERROR                               = 15;
	public static final int SRESULTCODE_SERVICEUNAVAILABLE                  = 16;
	public static final int SRESULTCODE_ACCESSDENIED                        = 17;
	public static final int SRESULTCODE_UNKNOWN_ACTION                      = 18;
	public static final int SRESULTCODE_WAIT_FOR_REGISTER                   = 19;
	public static final int SRESULTCODE_ACTION_NOT_AVAILABLE                = 20;
	public static final int SRESULTCODE_CONFIRMATION_NEEDED                 = 21;
	public static final int SRESULTCODE_RESULT_NOT_READY                    = 22;
	
	private static final int VERSION_MAJOR = 3;
	private static final int VERSION_MINOR = 6;
	
	private String Server;
	private String Login;
	private String Password;
	private String UDID;
	private String Sign;
	
	enum DevRegState { REGISTERED, UNREGISTERED, WAITING, VERSIONERROR };
	
	public String Base64Encode(byte[] data, int offset, int len) {
		return Base64.encodeToString(data, offset, len, Base64.NO_WRAP|Base64.URL_SAFE);
	}
	
	public String Base64Encode(byte[] data) {
		return Base64.encodeToString(data, Base64.NO_WRAP|Base64.URL_SAFE);
	}
	
	public String Base64Encode(String Str) {
		return Base64Encode(Str.getBytes()); 
	}
	
	private void init(String UDID, String Server, String Login, String Password) {
		this.Server = Server == null ? "" : Server;
		this.Login = Login == null ? "" : Login;
		this.Password = Password == null ? "" : Password;
		this.UDID = UDID;
		this.Sign = sStringToHMACMD5(UDID, "{649EC9FEE0B9}");	
	}
	
	public static String sStringToHMACMD5(String s, String keyString)
    {
        String sEncodedString = null;
        try
        {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);

            byte[] bytes = mac.doFinal(s.getBytes("ASCII"));

            StringBuffer hash = new StringBuffer();

            for (int i=0; i<bytes.length; i++) {
                String hex = Integer.toHexString(0xFF &  bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            sEncodedString = hash.toString();
        }
        catch (UnsupportedEncodingException e) {}
        catch(InvalidKeyException e){}
        catch (NoSuchAlgorithmException e) {}
        return sEncodedString.toUpperCase() ;
    }
	
	public RemoteAction(String UDID, String Server, String Login, String Password) {
	    init(UDID, Server, Login, Password);
	}
	
	public RemoteAction() {
		init(BaseSettings.getUDID(), BaseSettings.getServer(), BaseSettings.getLogin(), BaseSettings.getPassword());
	}
	
	public RemoteAction(String Login, String Password) {
		init(BaseSettings.getUDID(), BaseSettings.getServer(), Login, Password);
	}
		
	private class erpcSSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public erpcSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}
			
	protected HttpClient newHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new erpcSSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	String JSONHttpParams(JSONObject jObj) {
		
		String jStr = jObj.toString();
		
	    if ( jStr.length() < 100 ) {
	        return "&JSONDATA="+Base64Encode(jStr);
	    } else {
	    	
	    	byte[] input;
			try {
				input = jStr.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				input = null;
			}
			
			if ( input != null ) {
		    	jStr = null;
		    	byte[] output = new byte[input.length];

		    	int UncompressedSize = input.length; 
		    	Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION, false);
		    	compresser.setInput(input);
		    	compresser.finish();
		    	int CompressedSize = compresser.deflate(output);
		        compresser.end();
		 
		        if ( output.length > 0 ) {
		        	String x = "&JSONDATA_UncompressedSize="+Integer.toString(UncompressedSize)+"&JSONDATA="+Base64Encode(output, 0, CompressedSize);
		        	//Trace.d("Base64", x);
		        	return x;
		        }	
			}

	        return "";

	    }
		 	
	}
	
	public class Status {
		Boolean success;
		int code;
		String message;
		
		Status(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONException e) {
			
			success = Boolean.FALSE;
			code = -1000;
			message = "JSON: " + e.getMessage();
			
		}
		
		void assign(JSONObject jObj) {
			
			try {
				
				if ( jObj == null ) 
					throw new JSONException("JSON Object is empty!");
				else if ( jObj.has("http_error") ) {
					
					success = Boolean.FALSE;
					code = -1100;
					message = "HTTP ERROR: "+jObj.getString("http_error");
					
				} else if ( jObj.has("connection_error") ) {
						
					success = Boolean.FALSE;
					code = -1200;
					message = "CONNECTION ERROR: "+jObj.getString("connection_error");
					
				} else {
					JSONObject subObj = jObj.getJSONObject("status");	
					success = jExt.getBoolean(subObj, "success", Boolean.FALSE);
					code = jExt.getInt(subObj, "code", -1300); 
					message = jExt.getString(subObj, "message", null); 		
				}
				

	
			} catch (JSONException e) {
				assign(e);
			}

		}
		
	}

	public class UserDetails {
		String name;
		String defaultwarehouse;
		
		public UserDetails(JSONObject jObj) throws JSONException {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) throws JSONException {
			jObj = jObj.getJSONObject("userdetails");
			name = jExt.getString(jObj, "name", "");
			defaultwarehouse = jExt.getString(jObj, "defaultwarehouse", "");
		}
	}
	
	public class _BaseResult {
		Status status;
	}
	
	public class BaseResult extends _BaseResult {
		public BaseResult(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) {
			status = new Status(jObj);
		}
	}
	
	public class HelloResult extends _BaseResult {

		String erp_name;
		String erp_mfr;
		String drv_mfr;
		String drv_ver;
		int ver_major;
		int ver_minor;
		int offline_valitidytime;
		int online_validitytime;
		int cap;
		String auth_type;
		DevRegState dev_regstate;
		Boolean dev_accessgranted;
		String srv_instanceid;
		
		HelloResult(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) {
			
			status = new Status(jObj);
			cap = 0;
			dev_regstate = DevRegState.UNREGISTERED;
			
			if ( status.success == true ) {
				
				try {
					JSONObject subObj = jObj.getJSONObject("erp");	
					erp_name = jExt.getString(subObj, "name", "");
					erp_mfr = jExt.getString(subObj, "mfr", "");
					
					subObj = jObj.getJSONObject("drv");
					drv_mfr = jExt.getString(subObj, "mfr", "");
					drv_ver = jExt.getString(subObj, "ver", ""); 
					
					subObj = jObj.getJSONObject("cap");
					
					if ( jExt.getBoolean(subObj, "RegisterDevice", false) == true ) cap|=SRVCAP_REGISTERDEVICE;
					if ( jExt.getBoolean(subObj, "Login", false) == true ) cap|=SRVCAP_LOGIN;
					if ( jExt.getBoolean(subObj, "FetchRecordsFromResult", false) == true ) cap|=SRVCAP_FETCHRECORDSFROMRESULT;
					if ( jExt.getBoolean(subObj, "FetchDocumentFromResult", false) == true ) cap|=SRVCAP_FETCHDOCUMENTFROMRESULT;
					if ( jExt.getBoolean(subObj, "Customer_SimpleSearch", false) == true ) cap|=SRVCAP_CUSTOMERSIMPLESEARCH;
					if ( jExt.getBoolean(subObj, "InvoiceById", false) == true ) cap|=SRVCAP_INVOICEBYID;
					if ( jExt.getBoolean(subObj, "Invoices", false) == true ) cap|=SRVCAP_INVOICES;
					if ( jExt.getBoolean(subObj, "Invoice_Items", false) == true ) cap|=SRVCAP_INVOICEITEMS;
					if ( jExt.getBoolean(subObj, "Invoice_DOC", false) == true ) cap|=SRVCAP_INVOICEDOC;
					if ( jExt.getBoolean(subObj, "OutstandingPayments", false) == true ) cap|=SRVCAP_OUTSTANDINGPAYMENTS;
					if ( jExt.getBoolean(subObj, "OrderById", false) == true ) cap|=SRVCAP_ORDERBYID;
					if ( jExt.getBoolean(subObj, "Orders", false) == true ) cap|=SRVCAP_ORDERS;
					if ( jExt.getBoolean(subObj, "Order_Items", false) == true ) cap|=SRVCAP_ORDERITEMS;
					if ( jExt.getBoolean(subObj, "Order_DOC", false) == true ) cap|=SRVCAP_ORDERDOC;
					if ( jExt.getBoolean(subObj, "IndividualPrices", false) == true ) cap|=SRVCAP_INDIVIDUALPRICES;
					if ( jExt.getBoolean(subObj, "Articles", false) == true ) cap|=SRVCAP_ARTICLES;
					if ( jExt.getBoolean(subObj, "Article_SimpleSearch", false) == true ) cap|=SRVCAP_ARTICLESIMPLESEARCH;
					if ( jExt.getBoolean(subObj, "AddContractor", false) == true ) cap|=SRVCAP_ADDCONTRACTOR;
					if ( jExt.getBoolean(subObj, "NewInvoice", false) == true ) cap|=SRVCAP_NEWINVOICE;
					if ( jExt.getBoolean(subObj, "NewOrder", false) == true ) cap|=SRVCAP_NEWORDER;
					if ( jExt.getBoolean(subObj, "GetDictionary", false) == true ) cap|=SRVCAP_GETDICTIONARY;
					if ( jExt.getBoolean(subObj, "GetLimits", false) == true ) cap|=SRVCAP_GETLIMITS;
					if ( jExt.getBoolean(subObj, "GetUserDetails", false) == true ) cap|=SRVCAP_GETUSERDETAILS;

					subObj = jObj.getJSONObject("auth");
					auth_type = jExt.getString(subObj, "type", "");

					subObj = jObj.getJSONObject("version");
					ver_major = jExt.getInt(subObj, "major", 0);
					ver_minor = jExt.getInt(subObj, "minor", 0);
					
					subObj = jObj.getJSONObject("server");
					srv_instanceid = jExt.getString(subObj, "InstanceId", null);
					
				    
				    subObj = jObj.getJSONObject("datavaliditytime");
				    online_validitytime = jExt.getInt(subObj, "online", 10);
				    offline_valitidytime = jExt.getInt(subObj, "offline", 1440);
					
					subObj = jObj.getJSONObject("device");
					String rs = jExt.getString(subObj, "regstate", "");
					
			        if ( ver_major == VERSION_MAJOR
			                && ver_minor == VERSION_MINOR ) {
			                
			             if ( rs.equals("waiting") ) {
			                 dev_regstate = DevRegState.WAITING;
			             } else if ( rs.equals("registered") ) {
			                 dev_regstate = DevRegState.REGISTERED;
			             } else {
				             dev_regstate = DevRegState.UNREGISTERED;
			             }
			                
			         } else {
			            dev_regstate = DevRegState.VERSIONERROR;
			         }
			        
					dev_accessgranted = jExt.getBoolean(subObj, "accessgranted", false);
					
				} catch (JSONException e) {
					status.assign(e);
				}			
			}

			
		}
	}

	public class LoginResult extends _BaseResult {

		UserDetails userdetails;
		
		LoginResult(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) {
			status = new Status(jObj);
			if ( status.success ) {
				
				    if ( jObj.has("userdetails") ) {
						try {
							userdetails = new UserDetails(jObj);
						} catch (JSONException e) {
							status.assign(e);
						}			    	
				    } else {
				    	userdetails = null;
				    }
					
			}
		}
	}
	
	public class FetchedResult extends _BaseResult {
		JSONObject jObj;
		
		public FetchedResult(JSONObject jObj) {
			assign(jObj);
		}
		
		public void assign(JSONObject jObj) {
			status = new Status(jObj);			
			this.jObj = null;
			
			if ( status.success == true ) {
				this.jObj = jObj;
			};
		}
	}
	
	public class DataResult {
		
		String name;
		String resultID;
		int rowCount;
		int colCount;
		int totalRowCount;
		int position;
		
		private Object[] record;
		
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {	
			return null;
		}
		
		int recordCount() {
			return record.length;
		}
		
		Object getRecord(int idx) {
			return record[idx];
		}
		
		void assign(JSONObject jObj) throws JSONException {
            
			name = jExt.getString(jObj, "name", "");
			position = jExt.getInt(jObj, "position", 0);
            rowCount = jExt.getInt(jObj, "rowcount", 0);
            colCount = jExt.getInt(jObj, "colcount", 0);
            totalRowCount = jExt.getInt(jObj, "totalrowcount", 0);
            resultID = jExt.getString(jObj, "resultid", null);
            
            if ( jObj.has("content") ) {
    			JSONArray jArr = jObj.getJSONArray("content");
    			if ( jArr.length() > 0 ) {
    				
    				record = new Object[jArr.length()];
    				
    				for(int a=0;a<jArr.length();a++) {
    					record[a] = jsonToRecord(jArr.getJSONObject(a));
    				}			
    			}          	
            }
         
		}
	}
	
	public class DataResults extends _BaseResult {
		
		private Object[] result;
		private int all_count;
		
		protected Object jsonToResult(JSONObject jObj, String Name) throws JSONException {
			return null;
		}
		
		void assign(JSONObject jObj) {
			all_count = 0;
			status = new Status(jObj);
			if ( status.success ) {
				
				try {
					
					JSONArray jArr = jObj.getJSONArray("results");
					if ( jArr.length() > 0 ) {
						
						result = new DataResult[jArr.length()];
						int n=0;
						
						for(int a=0;a<jArr.length();a++) {
                            JSONObject i = jArr.getJSONObject(a);
                            result[n] = jsonToResult(i, jExt.getString(i, "name", "")); 
                            if ( result[n] != null ) {
                            	n++;
                            }
						}			
					}

					for(int a=0;a<result.length;a++) 
						if ( result[a] == null ) {
							break;
						} else if ( ((DataResult)result[a]).record != null ) {
							all_count+=((DataResult)result[a]).record.length;
						}
					
				} catch (JSONException e) {
					status.assign(e);
				}			    	
			}
		}
			
		int recordCount(Boolean fullScope) {
			if ( fullScope ) {
				return all_count;
			} else {
				DataResult r = getResult();
				return r == null || r.record == null ? 0 : r.record.length;
			}
		}
		
		int recordCount() {
			return recordCount(false);
		}
		
		private Object getRecord(int idx, Boolean fullScope) {
			Object r = null;
			
			for(int a=0;a<result.length;a++) 
				if ( result[a] == null || ( fullScope == false && a > 0 ) ) {
					break;
				} else {
					if ( idx < ((DataResult)result[a]).record.length ) {
						r = ((DataResult)result[a]).record[idx];
					} else {
						idx-=((DataResult)result[a]).record.length;
					}
					
					if ( r != null ) break;
				}
			
			return r;
		}
		
		Object getRecord(int idx) {
			return getRecord(idx, false); 
		}
		
		DataResult getResult() {
			return result != null && result.length > 0 ? (DataResult)result[0] : null;
		}
	
		DataResult getResult(int idx) {
			return result != null && idx < result.length ? (DataResult)result[idx] : null;
		}
		
		DataResult getResultById(String Id) {
			
			for(int a=0;a<result.length;a++) 
				if ( result[a] == null ) {
					break;
				} else {
					if ( ((DataResult)result[a]).resultID.equals(Id) ) {
						return (DataResult)result[a];
					}
				}
			return null;
		}
	}
	
	public class ContractorResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);
			
			Contractor c = new Contractor();
			
			c.shortcut = jExt.getString(jObj, "Id", "");
			c.name = jExt.getString(jObj, "Name", "");
			c.nip = jExt.getString(jObj, "VATid", "");
			c.regon = jExt.getString(jObj, "Regon", "");
			c.region = jExt.getString(jObj, "Region", "");
			c.country = jExt.getString(jObj, "Country", "");
			c.postcode = jExt.getString(jObj, "PostCode", "");
			c.city = jExt.getString(jObj, "City", "");
			c.street = jExt.getString(jObj, "Street", "");
			c.houseno = jExt.getString(jObj, "StNo", "");
			c.tel1 = jExt.getString(jObj, "Phone1", "");
			c.tel2 = jExt.getString(jObj, "Phone2", "");
			c.tel3 = jExt.getString(jObj, "Phone3", "");
			c.email1 = jExt.getString(jObj, "Email1", "");
			c.email2 = jExt.getString(jObj, "Email2", "");
			c.www1 = jExt.getString(jObj, "WWW1", "");
			c.www2 = jExt.getString(jObj, "WWW2", "");
			c.www3 = jExt.getString(jObj, "WWW3", "");
			String Lck = jExt.getString(jObj, "TrnLocked", "");
			c.trnlocked = Lck.equals("Yes") || Lck.equals("1") || Lck.equals("Tak");
			
			return c;
		}
		
		Contractor getRecord(int idx)  {
			return (Contractor)super.record[idx];
		}
		
	}
	
	public class ContractorResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				ContractorResult r = new ContractorResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public ContractorResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		Contractor getRecord(int idx)  {
			return (Contractor)super.getRecord(idx);
		}
	}
	
	public class InvoiceResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			Invoice i = new Invoice();
			
		    i.shortcut = jExt.getString(jObj, "Id", "");
		    i.number = jExt.getString(jObj, "Number", "");
		    i.dateofissue = jExt.getDate(jObj, "DateOfIssue", null);
		    i.totalnet = jExt.getBigDecimal(jObj, "TotalNet", 0);
		    i.totalgross = jExt.getBigDecimal(jObj, "TotalGross", 0);
		    i.remaining = jExt.getBigDecimal(jObj, "Remaining", 0);
		    i.paid = jExt.getBoolean(jObj, "Paid", Boolean.FALSE);
		    i.paymentmethod = jExt.getString(jObj, "PaymentMethod", "");
		    i.termdate = jExt.getDate(jObj, "PaymentDeadline", null);
		
			return i;
		}
		
		Invoice getRecord(int idx)  {
			return (Invoice)super.record[idx];
		}
		
	}
	
	public class InvoiceResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				InvoiceResult r = new InvoiceResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public InvoiceResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		Invoice getRecord(int idx)  {
			return (Invoice)super.getRecord(idx);
		}
	}
	
	public class InvoiceItemResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			InvoiceItem ii = new InvoiceItem();
		    
		    ii.shortcut = jExt.getString(jObj, "Code", "");
		    ii.name = jExt.getString(jObj, "Name", "");
		    ii.qty = jExt.getDouble(jObj, "Qty", 0);
		    ii.unit = jExt.getString(jObj, "Unit", "");
		    ii.price = jExt.getBigDecimal(jObj, "Price", 0);
		    ii.discount = jExt.getBigDecimal(jObj, "Discount", 0);
		    ii.discountpercent = jExt.getDouble(jObj, "DiscountPercent", 0);
		    ii.pricenet = jExt.getBigDecimal(jObj, "PriceNet", 0);
		    ii.totalnet = jExt.getBigDecimal(jObj, "TotalNet", 0);
		    ii.vatrate = jExt.getString(jObj, "VATrate", "");
		    ii.vatvalue = jExt.getBigDecimal(jObj, "VATvalue", 0);
		    ii.totalgross = new BigDecimal(ii.totalnet.doubleValue() + ii.vatvalue.doubleValue() );
		
			return ii;
		}
		
		InvoiceItem getRecord(int idx)  {
			return (InvoiceItem)super.record[idx];
		}
		
	}
	
	public class InvoiceItemResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				InvoiceItemResult r = new InvoiceItemResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public InvoiceItemResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		InvoiceItem getRecord(int idx)  {
			return (InvoiceItem)super.getRecord(idx);
		}
	}
	
	public class DocResult extends _BaseResult {

		long totalsize;
		String resultID;
		byte[] data;
		
		DocResult(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) {
			status = new Status(jObj);
			if ( status.success ) {
				
				try {
					resultID = jExt.getString(jObj, "resultid", "");
					totalsize = jObj.getLong("totalsize");
					data = Base64.decode(jObj.getString("DOC").getBytes(), Base64.DEFAULT);
				} catch (JSONException e) {
					status.assign(e);
				}			    	
					
			}
		}
	}
	
	public class OrderResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			Order o = new Order();
		
		    o.shortcut = jExt.getString(jObj, "Id", "");
		    o.number = jExt.getString(jObj, "Number", ""); 
		    o.dateofissue = jExt.getDate(jObj, "DateOfIssue", null);
		    o.totalnet = jExt.getBigDecimal(jObj, "TotalNet", 0);
		    o.totalgross = jExt.getBigDecimal(jObj, "TotalGross", 0);
		    o.paymentmethod = jExt.getString(jObj, "PaymentMethod", "");
		    o.dateofcomplete = jExt.getDate(jObj, "DateOfComplete", null);
		    o.termofcontract = jExt.getDate(jObj, "TermOfContract", null);
		    o.state  = jExt.getString(jObj, "State", "");
		    o.desc = jExt.getString(jObj, "Description", "");
		    o.valuerealized = jExt.getBigDecimal(jObj, "ValueRealized", 0);
		    
			return o;

		}
		
		Order getRecord(int idx)  {
			return (Order)super.record[idx];
		}
		
	}
	
	public class OrderResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				OrderResult r = new OrderResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public OrderResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		Order getRecord(int idx)  {
			return (Order)super.getRecord(idx);
		}
	}
	
	public class OrderItemResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			
			OrderItem oi = new OrderItem();
		    
		    oi.shortcut = jExt.getString(jObj, "Code", "");
		    oi.name = jExt.getString(jObj, "Name", "");
		    oi.qty = jExt.getDouble(jObj, "Qty", 0);
		    oi.unit = jExt.getString(jObj, "Unit", "");
		    oi.price = jExt.getBigDecimal(jObj, "Price", 0);
		    oi.individualprice = null;
		    oi.discount = jExt.getBigDecimal(jObj, "Discount", 0);
		    oi.discountpercent = jExt.getDouble(jObj, "DiscountPercent", 0);
		    oi.pricenet = jExt.getBigDecimal(jObj, "PriceNet", 0);
		    oi.totalnet = jExt.getBigDecimal(jObj, "TotalNet", 0);
		    oi.vatrate = jExt.getString(jObj, "VATrate", "");
		    oi.vatvalue = jExt.getBigDecimal(jObj, "VATvalue", 0);
		    oi.totalgross = new BigDecimal(oi.totalnet.doubleValue() + oi.vatvalue.doubleValue() );
		
			return oi;
		}
		
		OrderItem getRecord(int idx)  {
			return (OrderItem)super.record[idx];
		}
		
	}
	
	public class OrderItemResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				OrderItemResult r = new OrderItemResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public OrderItemResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		OrderItem getRecord(int idx)  {
			return (OrderItem)super.getRecord(idx);
		}
	}
	
	public class OutstandingPaymentResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			Payment p = new Payment();
		    
		    p.number = jExt.getString(jObj, "DocNum", "");
		    p.dateofissue = jExt.getDate(jObj, "DateOfIssue", null);
		    p.dateofsale = jExt.getDate(jObj, "DateOfSale", null);
		    p.paymentform = jExt.getString(jObj, "PaymentMethod", "");
		    p.termdate = jExt.getDate(jObj, "PaymentDeadline", null);
		    p.remaining = jExt.getBigDecimal(jObj, "Left", 0);
		    p.totalnet = jExt.getBigDecimal(jObj, "TotalNet", 0);
		    p.totalgross = jExt.getBigDecimal(jObj, "TotalGross", 0);
		    
			return p;

		}
		
		Payment getRecord(int idx)  {
			return (Payment)super.record[idx];
		}
		
	}
	
	public class OutstandingPaymentResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				OutstandingPaymentResult r = new OutstandingPaymentResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public OutstandingPaymentResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		Payment getRecord(int idx)  {
			return (Payment)super.getRecord(idx);
		}
	}
	
	public class ContractorLimitResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			ContractorLimit cl = new ContractorLimit();
		   
		    cl.currency = jExt.getString(jObj, "Currency", "");
		    cl.limit = jExt.getBigDecimal(jObj, "Limit", 0);
		    cl.overdue = jExt.getBigDecimal(jObj, "Overdue", 0);
		    cl.overdueallowed = jExt.getBigDecimal(jObj, "OverdueAllowed", 0);
		    cl.remain = jExt.getBigDecimal(jObj, "Remain", 0);
		    cl.unlimited = jExt.getBoolean(jObj, "Unlimited", false);
		    cl.used = jExt.getBigDecimal(jObj, "Used", 0);
		    
			return cl;

		}
		
		ContractorLimit getRecord(int idx)  {
			return (ContractorLimit)super.record[idx];
		}
		
	}
	
	public class ContractorLimitResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				ContractorLimitResult r = new ContractorLimitResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public ContractorLimitResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		ContractorLimit getRecord(int idx)  {
			return (ContractorLimit)super.getRecord(idx);
		}
	}
	
	
	public class ArticleResult extends DataResult {
		
		@Override
		protected Object jsonToRecord(JSONObject jObj) throws JSONException {
			super.jsonToRecord(jObj);

			Article a = new Article();
		
		    a.shortcut = jExt.getString(jObj, "Code", "");
		    
		    a.desc = jExt.getString(jObj, "Description", "");
		    a.group = jExt.getString(jObj, "Group", "");
		    a.name = jExt.getString(jObj, "Name", "");
		    a.pkwiu = jExt.getString(jObj, "PKWIU", "");
		    a.unit = jExt.getString(jObj, "Unit", "");
		    a.unitpurchasecurr = jExt.getString(jObj, "UnitPurchaseCurr", "");
		    a.unitpurchaseprice = jExt.getBigDecimal(jObj, "UnitPurchasePrice", 0);
		    a.unitretailcurr = jExt.getString(jObj, "UnitRetailCurr", "");
		    a.unitretailprice = jExt.getBigDecimal(jObj, "UnitRetailPrice", 0);
		    a.unitspecialcurr = jExt.getString(jObj, "UnitSpecialCurr", "");
		    a.unitspecialprice = jExt.getBigDecimal(jObj, "UnitSpecialPrice", 0);
		    a.unitwholesalecurr = jExt.getString(jObj, "UnitPurchaseCurr", "");
		    a.unitwholesaleprice = jExt.getBigDecimal(jObj, "UnitPurchasePrice", 0);
		    a.vatrate = jExt.getString(jObj, "VATrate", "");
		    a.vatpercent = Double.parseDouble(a.vatrate);
		    
		    if ( a.vatpercent < 0 ) {
		        a.vatpercent = 0.00;
		    }
		    
		    BigDecimal price = new BigDecimal(a.unitretailprice.doubleValue());
		    String curr = new String(a.unitretailcurr);
		    
		    if ( price.doubleValue() <= 0 ) {
		        price = new BigDecimal(a.unitwholesaleprice.doubleValue());
		        curr = new String(a.unitwholesalecurr);
		    }
		    
		    if ( price.doubleValue() <= 0  ) {
		        price = new BigDecimal(a.unitspecialprice.doubleValue());
		        curr = new String(a.unitspecialcurr);
		    }
		    
		    a.unitlistpricenet = price;
		    a.unitlistpricecurr = curr;
		    a.unitlistpricegross = new BigDecimal(a.unitlistpricenet.doubleValue() + ( a.unitlistpricenet.doubleValue() * a.vatpercent / 100.00 ));
		  
			return a;

		}
		
		Article getRecord(int idx)  {
			return (Article)super.record[idx];
		}
		
	}
	
	public class ArticleResults extends DataResults {
		
		private String name;
		
		@Override
		protected Object jsonToResult(JSONObject jObj, String Name)
				throws JSONException {
			super.jsonToResult(jObj, Name);
			
			if ( Name.equals(name) ) {
				ArticleResult r = new ArticleResult();
				r.assign(jObj);
				return r;
			}
			
			return null;
		}
		
		
		public ArticleResults(JSONObject jObj, String name) {
			this.name = name;
			assign(jObj);
		}

		Article getRecord(int idx)  {
			return (Article)super.getRecord(idx);
		}
	}
	
	public class DictionaryItem {
		String Shortcut;
		String Name;
		int Pri;
		
		public DictionaryItem(JSONObject jObj) throws JSONException {
			assign(jObj);
		}
		
		void assign(JSONObject jObj)  {
			Shortcut = jExt.getString(jObj, "shortcut", "");
			Name = jExt.getString(jObj, "name", "");
			Pri = jExt.getInt(jObj, "pri", 0);
		}
	}
	
	public class Dictionary extends _BaseResult {
		int DictType;
		Boolean Exists;
		DictionaryItem[] items;
		
		public Dictionary(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) {
			
			status = new Status(jObj);
			
			if ( status.success ) {
				try {
					jObj = jObj.getJSONObject("dictionary");
					DictType = jObj.getInt("type");
					Exists = jObj.getBoolean("exists");
					JSONArray arr = jObj.getJSONArray("items");
					if ( arr != null
						 && arr.length() > 0 ) {
						items = new DictionaryItem[arr.length()];
						for(int a=0;a<arr.length();a++) {
							items[a] = new DictionaryItem(arr.getJSONObject(a));
						}
					}
				} catch (JSONException e) {
					status.assign(e);
				}			
			}
			
		}
	}
	
	public class IndividualPrice extends _BaseResult {
		
		BigDecimal Price;
		int Code;
		String Message;
		
		public IndividualPrice(JSONObject jObj) {
			assign(jObj);
		}
		
		void assign(JSONObject jObj) {
			
			status = new Status(jObj); 
			
			if ( status.success ) {
				try {
					jObj = jObj.getJSONObject("price");
					Price = new BigDecimal(jObj.getDouble("price"));
					Code = jObj.getInt("code");
					Message = jExt.getString(jObj, "message", "");
				} catch (JSONException e) {

					status.assign(e);
				}			
			}
			
		}
	}
	
	public class AsyncPostResult extends _BaseResult {
		String requestId;
		
		public AsyncPostResult(JSONObject jObj) {
			assign(jObj);
		} 
		
		void assign(JSONObject jObj) {
			status = new Status(jObj);
		    if ( status.success ) {
			    try {
			    	requestId = jObj.getString("requestid");
				} catch (JSONException e) {
					status.assign(e);
				}	  	
		    }

		}
	}
	
	public class AsyncResult extends _BaseResult {
		
		JSONObject jAsyncResult;
		
		public AsyncResult(JSONObject jObj) {
			assign(jObj);
		} 
		
		void assign(JSONObject jObj) {
			status = new Status(jObj);
		    if ( status.success ) {
			    try {
			    	jAsyncResult = jObj.getJSONObject("asyncresult");
				} catch (JSONException e) {
					status.assign(e);
				}	  	
		    }

		}
	}
	
	public class NewObjectResult extends _BaseResult {
	
		String Id;
		String DocNum;
		
		public NewObjectResult(JSONObject jObj) {
			assign(jObj);
		} 
		
		void assign(JSONObject jObj) {
			status = new Status(jObj);
	        if ( status.success ) {
	        	Id = jExt.getString(jObj, "ID", null);
	        	DocNum = jExt.getString(jObj, "DocNum", null);
	        }
		}
	}
	
	public class NewObjectAsyncResult extends _BaseResult {
		NewObjectResult asyncresult;
		
		public NewObjectAsyncResult(JSONObject jObj) {
			assing(jObj);
		}
		
		void assing(JSONObject jObj) {
			status = new Status(jObj);
			if ( status.success ) {
				try {
					asyncresult = new NewObjectResult(jObj.getJSONObject("asyncresult"));
				} catch (JSONException e) {
					status.assign(e);
				}
			}
		}
	}
	
	private JSONObject httpPost(String Action, String Params) {
		
		DefaultHttpClient client = (DefaultHttpClient) newHttpClient();
		HttpResponse response;
		JSONObject result = null;

        HttpPost post = new HttpPost("https://"+Server+"/pzWebservice.dll/json");
        
        try {
			post.setEntity(new StringEntity("Compress=true&Namespace=erpConnector&UDID="+UDID+"&Sign="+Sign+"&Login="+Base64Encode(Login)+"&Password="+Base64Encode(Password)+"&Action="+Action+Params, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
  
		try {
			
			response = client.execute(post);
			
			int httpCode = response.getStatusLine().getStatusCode();
			
			if ( httpCode == 200) {
				
		    	HttpEntity entity = response.getEntity();
			    if(entity != null) {
					try {
						if ( entity.getContentType().getValue().equals("application/x-compress") ) {
							Inflater inflater = new Inflater();
							byte[] data = EntityUtils.toByteArray(entity);
							inflater.setInput(data);
							
							ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
							    
						    byte[] buf = new byte[1024];
						    while (!inflater.finished()) {
						        try {
						            int count = inflater.inflate(buf);
						            bos.write(buf, 0, count);
						        } catch (DataFormatException e) {
						        }
						    }
						    try {
						        bos.close();
						    } catch (IOException e) {
						    }
						    Trace.d(_TAG, bos.toString());
						    result = new JSONObject(bos.toString());
							    
						} else {
							
							String r = EntityUtils.toString(entity);
							result = new JSONObject(r);
							Trace.d(_TAG, r);
						}
						
					} catch (ParseException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}			        
			    }	
			    
			} else {
				result = jExt.newErrObj("http_error", httpCode);
			}


			
		} catch (ClientProtocolException e) {
			result = jExt.newErrObj("connection_error", 1);
		} catch (IOException e) {
			result = jExt.newErrObj("connection_error", 2);
		}
		
          
     return result;
    }

	private DocResult ra_DOC(String Action, String DocType, String DocID, long maxBytesCount) {
		return new DocResult(httpPost(Action, "&DocType="+DocType+"&DocID="+Base64Encode(DocID)+"&MaxBytesCount="+Long.toString(maxBytesCount)));
	}
	
	HelloResult ra_Hello() {
		return new HelloResult(httpPost("Hello", ""));
	}
	
	LoginResult ra_Login() {
		return new LoginResult(httpPost("Login", ""));
	}
	
	BaseResult ra_RegisterDevice() {
		String Name = android.os.Build.MANUFACTURER+ " "+android.os.Build.MODEL;
		return new BaseResult(httpPost("RegisterDevice", "&DeviceCaption="+Base64Encode(Name)));
	}
	
	FetchedResult ra_fetchRecordsFromResult(String ResultId, int From, int Count) {
		return new FetchedResult(httpPost("FetchRecordsFromResult", "&ResultID="+ResultId+"&From="+Integer.toString(From)+"&MaxCount="+Integer.toString(Count)));
	}
	
	ContractorResults ra_CustomerSearch(String txt, int maxCount, Boolean onlyByShortcut) {
		return new ContractorResults(httpPost("Customer_SimpleSearch", "&Text="+Base64Encode(txt)+"&MaxCount="+Integer.toString(maxCount)+"&OnlyID="+(onlyByShortcut ? "1" : "0")), "Customers");
	}
	
	ContractorResults ra_CustomerSearch(String txt) {
		return ra_CustomerSearch(txt, -1, false);
	}
	
	ContractorResult ra_fetchCustomersFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			ContractorResults cr = new ContractorResults(fr.jObj, "");
			if ( cr.status.success ) {
				return (ContractorResult)cr.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	InvoiceResults ra_Invoices(String CustomerShortcut, Date FromDate, int maxCount) {
		long unixTime = FromDate == null ? 0 : FromDate.getTime()/1000;
		
		return new InvoiceResults(httpPost("Invoices", "&CID="+Base64Encode(CustomerShortcut)+"&FromDate="+Long.toString(unixTime)+"&MaxCount="+Integer.toString(maxCount)), "Invoices");

	}
	
	InvoiceResults ra_InvoiceByShortcut(String Shortcut) {

		return new InvoiceResults(httpPost("InvoiceById", "&DocID="+Base64Encode(Shortcut)), "Invoices");
	}
	
	InvoiceResults ra_Invoices(String CustomerShortcut) {
		return ra_Invoices(CustomerShortcut, null, -1);
	}

	InvoiceResults ra_Invoices(String CustomerShortcut, Date FromDate) {
		return ra_Invoices(CustomerShortcut, FromDate, -1);
	}
	
	InvoiceResult ra_fetchInvoicesFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			InvoiceResults ir = new InvoiceResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (InvoiceResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	InvoiceItemResults ra_InvoiceItems(String DocID, int maxCount) {
		return new InvoiceItemResults(httpPost("Invoice_Items", "&DocID="+Base64Encode(DocID)+"&MaxCount="+Integer.toString(maxCount)), "InvoiceItems");
	}
	
	InvoiceItemResults ra_InvoiceItems(String DocID) {
		return ra_InvoiceItems(DocID, -1); 
	}
	
	InvoiceItemResult ra_fetchInvoiceItemsFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			InvoiceItemResults ir = new InvoiceItemResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (InvoiceItemResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	DocResult ra_fetchDocumentFromResult(String ResultId, long fromByte, long maxBytesCount) {
		return new DocResult(httpPost("FetchDocumentFromResult", "&ResultID="+ResultId+"&FromByte="+Long.toString(fromByte)+"&MaxBytesCount="+Long.toString(maxBytesCount)));
	}
	
	DocResult ra_InvoiceDOC(String DocID, long maxBytesCount) {
		return ra_DOC("Invoice_DOC", "pdf", DocID, maxBytesCount);
	}
	
	DocResult ra_InvoiceDOC(String DocID) {
		return ra_InvoiceDOC(DocID, -1);
	}
	
	OrderResults ra_Orders(String CustomerShortcut, Date FromDate, int maxCount) {
		long unixTime = FromDate == null ? 0 : FromDate.getTime()/1000;
		
		return new OrderResults(httpPost("Orders", "&CID="+Base64Encode(CustomerShortcut)+"&FromDate="+Long.toString(unixTime)+"&MaxCount="+Integer.toString(maxCount)), "Orders");

	}
	
	OrderResults ra_OrderByShortcut(String Shortcut) {

		return new OrderResults(httpPost("OrderById", "&DocID="+Base64Encode(Shortcut)), "Orders");
	}
	
	OrderResults ra_Orders(String CustomerShortcut) {
		return ra_Orders(CustomerShortcut, null, -1);
	}

	OrderResults ra_Orders(String CustomerShortcut, Date FromDate) {
		return ra_Orders(CustomerShortcut, FromDate, -1);
	}
	
	OrderResult ra_fetchOrdersFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			OrderResults ir = new OrderResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (OrderResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	OrderItemResults ra_OrderItems(String DocID, int maxCount) {
		return new OrderItemResults(httpPost("Order_Items", "&DocID="+Base64Encode(DocID)+"&MaxCount="+Integer.toString(maxCount)), "OrderItems");
	}
	
	OrderItemResults ra_OrderItems(String DocID) {
		return ra_OrderItems(DocID, -1); 
	}
	
	OrderItemResult ra_fetchOrderItemsFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			OrderItemResults ir = new OrderItemResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (OrderItemResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	DocResult ra_OrderDOC(String DocID, long maxBytesCount) {
		return ra_DOC("Order_DOC", "pdf", DocID, maxBytesCount);
	}
	
	DocResult ra_OrderDOC(String DocID) {
		return ra_OrderDOC(DocID, -1);
	}
	
	OutstandingPaymentResults ra_OutstandingPayments(String CustomerShortcut, int maxCount) {
		return new OutstandingPaymentResults(httpPost("OutstandingPayments", "&CID="+Base64Encode(CustomerShortcut)+"&MaxCount="+Integer.toString(maxCount)), "Payments");
	}
	
	OutstandingPaymentResults ra_OutstandingPayments(String CustomerShortcut) {
		return ra_OutstandingPayments(CustomerShortcut, -1); 
	}
	
	OutstandingPaymentResult ra_fetchOutstandingPaymentsFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			OutstandingPaymentResults ir = new OutstandingPaymentResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (OutstandingPaymentResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	ArticleResults ra_ArticleSearch(String Text, int maxCount) {
		return new ArticleResults(httpPost("Article_SimpleSearch", "&CodeOrName="+Base64Encode(Text)+"&MaxCount="+Integer.toString(maxCount)), "Articles");
	}
	
	ArticleResults ra_ArticleSearch(String Text) {
		return ra_ArticleSearch(Text, -1); 
	}
	
	ArticleResult ra_fetchArticlesFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			ArticleResults ir = new ArticleResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (ArticleResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	BaseResult ra_Async_Confirm(String RequestId) {
		return new BaseResult(httpPost("Async_Confirm", "&RequestID="+RequestId));
	}
	
	BaseResult ra_Async_DeleteResult(String RequestId) {
		return new BaseResult(httpPost("Async_DeleteResult", "&RequestID="+RequestId));
	}
	
	Dictionary ra_DictionayOfType(int DictType, String CustomerShortcut) {
		return new Dictionary(httpPost("GetDictionary", "&DictType="+Integer.toString(DictType)+"&CID="+Base64Encode(CustomerShortcut)));
	}
	
	IndividualPrice ra_PriceForContractor(String CustomerShortcut, String ArticleShortcut, String Currency) {		
		return new IndividualPrice(httpPost("IndividualPrice", "&CID="+Base64Encode(CustomerShortcut)+"&Code="+Base64Encode(ArticleShortcut)+"&Currency"+Base64Encode(Currency)));
	}
		
	ContractorLimitResults ra_ContractorLimits(String CustomerShortcut, int maxCount) {
		return new ContractorLimitResults(httpPost("GetLimits", "&CID="+Base64Encode(CustomerShortcut)+"&MaxCount="+Integer.toString(maxCount)), "Limitss");
	}
	
	ContractorLimitResult ra_fetchContractorLimitsFromResult(String ResultId, int From, int Count) {
		
		FetchedResult fr = ra_fetchRecordsFromResult(ResultId, From, Count);
		if ( fr.status.success ) {
			ContractorLimitResults ir = new ContractorLimitResults(fr.jObj, "");
			if ( ir.status.success ) {
				return (ContractorLimitResult)ir.getResultById(ResultId);
			}
		}
		
		return null;
	}
	
	AsyncPostResult ra_AsyncNewOrder(Contractor contractor, Order order, OrderItem[] items) throws JSONException {
		
		JSONObject jObj = new JSONObject();
		
		jObj.put("ContractorShortcut", contractor.shortcut);
		jObj.put("ContractorName", contractor.name);
		jObj.put("PaymentMethod", order.paymentmethod);
		jObj.put("Discount", order.discount == null ? 0.00 : order.discount);
		jObj.put("TotalPrice", order.totalnet == null ? 0.00 : order.totalnet.doubleValue());
		
		if ( items != null 
				&& items.length > 0 ) {
			JSONArray jItems = new JSONArray();
			for(int a=0;a<items.length;a++) {
				JSONObject jItem = new JSONObject();
				jItem.put("Shortcut", items[a].shortcut);
				jItem.put("Name", items[a].name);
				jItem.put("Description", items[a].description);
				jItem.put("WareHouse", items[a].warehouse);
				jItem.put("PriceNet", items[a].pricenet == null ? 0.00 : items[a].pricenet.doubleValue());
				jItem.put("Discount", items[a].discountpercent);
				jItem.put("Qty", items[a].qty);
				jItem.put("TotalPriceNet", items[a].totalnet == null ? 0.00 : items[a].totalnet.doubleValue());
				jItems.put(jItem);
			}
			
			jObj.put("items", jItems);
		}
		
		JSONObject jOrder = new JSONObject();
		jOrder.put("OData", jObj);
		
		
		return new AsyncPostResult(httpPost("NewOrder", "&Async=1"+JSONHttpParams(jOrder)));
	}
	
	NewObjectAsyncResult ra_FetchAsyncNewOrderResult(String ResultId) {	
		return new NewObjectAsyncResult(httpPost("Async_FetchResult", "&RequestID="+ResultId));
	}
	
	AsyncPostResult ra_AsyncNewOrder_Confirm(String RefId)  {		
		return new AsyncPostResult(httpPost("NewOrder_Confirm", "&Async=1&RefID="+RefId));
	}
	
}
