package com.acsoftware.android.erpc;

import java.math.BigDecimal;
import java.util.Date;

public class Invoice {
	Date dateofissue;
	String number;
	Boolean paid;
	String  paymentmethod;
	BigDecimal remaining;
	String  shortcut;
	Date termdate;
	BigDecimal totalgross;
	BigDecimal totalnet;
	Date uptodate;
	Boolean visible;
}
