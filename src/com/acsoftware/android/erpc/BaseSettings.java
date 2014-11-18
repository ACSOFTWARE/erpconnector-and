package com.acsoftware.android.erpc;
import java.util.UUID;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class BaseSettings {
	
      private static String Server;
      private static String Login;
      private static String Password;
      private static String UDID;


      synchronized static String getServer() {
    	  return Server;   
      }
      
      synchronized static void setServer(String _Server) {
    	  Server = _Server;
      }
      
      synchronized static String getLogin() {
    	  return Login;   
      }
      
      synchronized static void setLogin(String _Login) {
    	  Login = _Login;
      }
      
      synchronized static String getPassword() {
    	  return Password;   
      }
      
      synchronized static void setPassword(String _Password) {
    	  Password = _Password;
      }
      
      synchronized static String getUDID() {
    	  return UDID;   
      }
      
      synchronized static void setUDID(String _UDID) {
    	  UDID = _UDID;
      }
      
      static void loadPrefs(Context context) {
      
  		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
  		String _UDID = prefs.getString("pref_udid", "");
  		if ( _UDID.isEmpty() ) {
  			_UDID = UUID.randomUUID().toString().toUpperCase();
  			
  			Editor editor = prefs.edit();
  			editor.putString("pref_udid", _UDID);
  			editor.commit();
  		}
  		
  		setUDID(_UDID);
  		//setServer(prefs.getString("pref_server", ""));
  		setServer("192.168.178.133");
  		//setServer("soap.transcom.com.pl/SOAP");
  		setLogin("Admin");
  		setPassword("Aa123456789");
      }
      
}
