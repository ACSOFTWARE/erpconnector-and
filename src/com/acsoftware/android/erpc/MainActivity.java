package com.acsoftware.android.erpc;

import java.math.BigDecimal;

import com.acsoftware.android.erpc.RemoteAction.DataResult;
import com.acsoftware.android.erpc.Trace;
import com.acsoftware.android.erpc.BackgroundTask.OnTaskEventListener;
import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener, OnTaskEventListener {

	static int counter = 0;
	Button btn1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BaseSettings.loadPrefs(this);
		setContentView(R.layout.activity_main);
		
        btn1 = (Button) findViewById(R.id.button1);;
        btn1.setOnClickListener(this);
        
        BackgroundTask.addEventListener(this);
        
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    	BackgroundTask.removeEventListener(this);
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	BackgroundTask.addEventListener(this);
    }
	
	void test(String s) {
		s = "aaa";
	}

	@Override
	public void onClick(View v) {
		BackgroundTask.ArticleSeatch("Tele");
	}
	
	//@Override
	public void old_onClick(View v) {
		
		
	
		
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	
		        	
		    		//RemoteAction RA = new RemoteAction(UDID, "soap.transcom.com.pl/SOAP", "Admin", "Aa123456789");
		    		RemoteAction RA = new RemoteAction();
		    		
		    		//RemoteAction.HelloResult R = RA.ra_Hello();
		    		//RemoteAction.BaseResult R = RA.ra_RegisterDevice();
		    		//RemoteAction.LoginResult R = RA.ra_Login();
		    		//RemoteAction.ContractorResults R = RA.ra_CustomerSearch("insert");
		    		//RemoteAction.OrderItemResults R = RA.ra_OrderItems("ZK 1/SF/2013", 1);
		    		//RemoteAction.InvoiceResults R = RA.ra_InvoiceByShortcut("FS 4/SF/2014");
		    		//RemoteAction.InvoiceItemResults R = RA.ra_InvoiceItems("FS 4/SF/2014", 1);
		    		//RemoteAction.DocResult R = RA.ra_InvoiceDOC("FS 4/SF/2014", 100);
		    		//RemoteAction.ArticleResults R = RA.ra_ArticleSearch("TEL");
		    		//RemoteAction.Dictionary R = RA.ra_DictionayOfType(RemoteAction.DICTTYPE_CONTRACTOR_PAYMENTMETHODS, "KOGUCIK"); 
		    		//RemoteAction.IndividualPrice R = RA.ra_PriceForContractor("KOGUCIK", "ZBORZ", "PLN");
		    		
		    		Contractor c = new Contractor();
		    		c.shortcut = "ABCD SP. Z O.O.";
		    		
		    		Order o = new Order();
		    		OrderItem[] items = new OrderItem[1];
		    		items[0] = new OrderItem();
		    		items[0].shortcut = "KAT00001";
		    		items[0].pricenet = new BigDecimal(100);
		    		items[0].qty = 1;
		    		items[0].discountpercent = 10.0;
		    		
		    		//RemoteAction.AsyncResult  R = RA.ra_AsyncNewOrder(c, o, items);
		    		RemoteAction.BaseResult  R = RA.ra_Async_DeleteResult("3x883ea706213e55d556d9c2871f7d8562x1440"); 
		    		
	    			Trace.d("Msg", R.status.message);
	    			Trace.d("Code", Integer.toString(R.status.code));
	    			
		    		if ( R.status.success ) {
		    			Trace.d("Result", "Success");
		    			
		    		
		    			

		    			/*
		    			for(int a=0;a<R.recordCount();a++) {
		    				Article ar = R.getRecord(a);
		    				if ( ar != null ) {
		    					Trace.d("A-"+Integer.toString(a), ar.shortcut);
		    				}
		    			}
		    			*/
		    			/*
		    			Trace.d("Count", Integer.toString(R.recordCount()));
		    			if ( R.getResult().resultID != null ) {
		    				
			    			Trace.d("ResulltId", R.getResult().resultID);
			    			
			    			RemoteAction.ArticleResult ir = RA.ra_fetchArticlesFromResult(R.getResult().resultID, 0, 10);
			    			if ( ir != null ) {
				    			for(int a=0;a<ir.recordCount();a++) {
				    				Article ar = ir.getRecord(a);
				    				if ( ar != null ) {
				    					Trace.d("A2-"+Integer.toString(a), ar.name);
				    				}
				    			}
			    			}
			    			
		    			}


*/

		    		} else {
		    			Trace.d("Result", "Unsuccess");
		    		}
		    	
		    		
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		});

		thread.start();

		

	}

	@Override
	public void onEvent(int EventType, BackgroundTask.Event event) {
				
		if ( EventType == BackgroundTask.EVENT_ONDATAITEM ) {
			if ( event.Obj != null && event.Obj instanceof Article ) {
				counter++;
				Trace.d("A("+Integer.toString(counter)+")", ((Article)event.Obj).name);
				//Trace.d("Progress", ((Float)event.Obj).toString());
			} else if ( event.Obj != null && event.Obj instanceof OrderItem ) {
				counter++;
				Trace.d("OI("+Integer.toString(counter)+")", ((OrderItem)event.Obj).name);		
			}
		} else if ( EventType == BackgroundTask.EVENT_TASKDONE ) {
			if ( event.Obj != null && event.Obj instanceof RemoteAction.DocResult ) {
				Trace.d("Success:", ((RemoteAction.DocResult)event.Obj).status.success.toString());
			}
			
			
		}

		
	}
}
