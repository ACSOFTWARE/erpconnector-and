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

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class jExt {
	
	public static String getString(JSONObject jObj, String name, String defaultval) {
		
		String result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result = jObj.getString(name);
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static Date getDate(JSONObject jObj, String name, Date defaultval) {
		
		Date result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result = new Date(jObj.getLong(name)*1000);
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static int getInt(JSONObject jObj, String name,int defaultval) {
		
		int result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result = jObj.getInt(name);
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static Boolean getBoolean(JSONObject jObj, String name, Boolean defaultval) {
		
		Boolean result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result = jObj.getBoolean(name);
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static BigDecimal getBigDecimal(JSONObject jObj, String name, BigDecimal defaultval) {
		
		BigDecimal result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result =  new BigDecimal(jObj.getDouble(name));
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static BigDecimal getBigDecimal(JSONObject jObj, String name, double defaultval) {
		
		return jExt.getBigDecimal(jObj, name, new BigDecimal(defaultval));
	}
	
	public static double getDouble(JSONObject jObj, String name, double defaultval) {
		
		double result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result =  jObj.getDouble(name);
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static long getLong(JSONObject jObj, String name, long defaultval) {
		
		long result = defaultval; 
		
		if ( jObj.has(name) 
				&& !jObj.isNull(name) ) {
			try {
				result =  jObj.getLong(name);
			} catch (JSONException e) {
				result = defaultval;
			}
		}
		
		return result;
	}
	
	public static JSONObject newErrObj(String key, int value) {
		JSONObject result = new JSONObject();
		try {
			result.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
