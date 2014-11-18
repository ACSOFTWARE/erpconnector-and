package com.acsoftware.android.erpc;

import java.math.BigDecimal;
import java.util.Date;

public class Payment {
	Date dateofissue;
	Date dateofsale;
	String number;
	String paymentform;
	BigDecimal remaining;
	Date termdate;
	BigDecimal totalgross;
	BigDecimal totalnet;
}
