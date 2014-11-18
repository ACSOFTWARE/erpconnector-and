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

import android.util.Log;

public class Trace
{
    public static final int    NONE                         = 0;
    public static final int    ERRORS_ONLY                  = 1;
    public static final int    ERRORS_WARNINGS              = 2;
    public static final int    ERRORS_WARNINGS_INFO         = 3;
    public static final int    ERRORS_WARNINGS_INFO_DEBUG   = 4;

    private static final int          LOGGING_LEVEL   = ERRORS_WARNINGS_INFO_DEBUG;        // Errors + warnings + info + debug (default)

    public static void e(String tag, String msg)
    {
        if ( LOGGING_LEVEL >=1) Log.e(tag,msg);
    }

    public static void e(String tag, String msg, Exception e)
    {
        if ( LOGGING_LEVEL >=1) Log.e(tag,msg,e);
    }

    public static void w(String tag, String msg)
    {
        if ( LOGGING_LEVEL >=2) Log.w(tag, msg);
    }

    public static void i(String tag, String msg)
    {
        if ( LOGGING_LEVEL >=3) Log.i(tag,msg);
    }

    public static void d(String tag, String msg)
    {
        if ( LOGGING_LEVEL >=4) Log.d(tag, msg);
    }

}