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

  		setLogin("Admin");
  		setPassword("Aa123456789");
      }
      
}
