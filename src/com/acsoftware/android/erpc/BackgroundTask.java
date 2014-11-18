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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.acsoftware.android.erpc.RemoteAction.DataResult;
import com.acsoftware.android.erpc.RemoteAction.DataResults;
import com.acsoftware.android.erpc.RemoteAction.DocResult;
import com.acsoftware.android.erpc.RemoteAction._BaseResult;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public class BackgroundTask {

	private static final int SCOPE_BASE            =  0x1;
	@SuppressWarnings("unused")
	private static final int SCOPE_EXPORT          =  0x2;
	@SuppressWarnings("unused")
	private static final int SCOPE_ALL             =  0xff;

	public static final int EVENT_TASKDONE         =  1;
	public static final int EVENT_ONDATAITEM       =  2;
	public static final int EVENT_ONPROGRESS       =  3;

	public static final int ACTION_REGISTERDEVICE        =  1;
	public static final int ACTION_LOGIN                 =  2;
	public static final int ACTION_CUSTOMERSEARCH        =  3;
	public static final int ACTION_INVOICES              =  4;
	public static final int ACTION_INVOICE               =  5;
	public static final int ACTION_INVOICEITEMS          =  6;
	public static final int ACTION_INVOICEDOCUMENT       =  7;
	public static final int ACTION_OUTSTANDINGPAYMENTS   =  8;
	public static final int ACTION_ORDERS                =  9;
	public static final int ACTION_ORDER                 =  10;
	public static final int ACTION_ORDERITEMS            =  11;
	public static final int ACTION_ORDERDOCUMENT         =  12;
	public static final int ACTION_ARTICLESEARCH         =  13;
	public static final int ACTION_DICTIONARY            =  14;
	public static final int ACTION_PRICEFORCONTRACTOR    =  15;
	public static final int ACTION_CONTRACTORLIMIT       =  16;
	
	private static final int MAX_COUNT = 3;
	private static final int MAX_BYTES_COUNT = 32768;
	
	private static List<bgTask> Queue = new ArrayList<bgTask>();
	private static List<OnTaskEventListener> Listeners = new ArrayList<OnTaskEventListener>();
	
	private static ExecutorService baseTaskExecutor = Executors.newCachedThreadPool();
	private static ExecutorService exportTaskExecutor = Executors.newCachedThreadPool();
	
	private static Handler eventHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			throwIfNotOnMainThread();
			
			if ( msg.obj instanceof BackgroundTask.Event ) {
				for(int a=0;a<Listeners.size();a++) {
					Listeners.get(a).onEvent(msg.what, (BackgroundTask.Event)msg.obj);
				}	
			}
			

			
		}
	};
	
	private static synchronized void addTask(bgTask task) {
	   Queue.add(task);
	   
	   if ( task.GetScope() == SCOPE_BASE ) {
		   baseTaskExecutor.execute(task);
	   } else {
		   exportTaskExecutor.execute(task);
	   }
	}
	
	private static synchronized void removeTask(bgTask task) {
		Queue.remove(task);
	}
	
	private static void throwIfNotOnMainThread() {
		
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Must be invoked from the main thread.");
        }
    }
	
	public static void addEventListener(OnTaskEventListener listener) {
		throwIfNotOnMainThread();
		
		if ( Listeners.indexOf(listener) == -1 ) {
			Listeners.add(listener);
		}
		
	}
	
	public static void removeEventListener(OnTaskEventListener listener) {
		throwIfNotOnMainThread();
		Listeners.remove(listener);
	}
	
	public interface OnTaskEventListener {
		void onEvent(int EventType, BackgroundTask.Event event);
	}
	
	public interface ProcessedDataFunctions {
		RemoteAction.DataResult fetchRemoteDataFromResult(String ResultId, int From, int Count);
		void onDataItem(Object dataItem);
	}
	
	public interface ProcessedDocumentFunctions {
		RemoteAction.DocResult fetchRemoteDocumentFromResult(String ResultId, long fromByte, long maxBytesCount);
		void onDocument(byte[] doc);
	}
	
	public static class Event {
		Boolean Success;
		int Action;
		Object Obj;
	}
	
	private static class bgTask implements Runnable {
		
		private Boolean canceled = false;
		private int _Scope;
		
		
		public bgTask() {
			_Scope = SCOPE_BASE;
		}
		
		@SuppressWarnings("unused")
		public bgTask(int Scope) {
			_Scope = Scope;
		}
		
		synchronized int GetScope() {
			return _Scope;
		}
		
		synchronized Boolean isCanceled() {
			return canceled || Thread.currentThread().isInterrupted();
		}
		
		synchronized void cancel() {
		    canceled = true;
		}
		
		public void run() {
			if ( !isCanceled() ) {
				
				try {
					task();
				} catch (InterruptedException e) {
					cancel();
				}
			}
			
			removeTask(this);
		}
		
		protected void SendEvent(int EventType, Event Event) {
			eventHandler.sendMessage(eventHandler.obtainMessage(EventType, Event));
		}
		
		protected void SendEvent(int Action, int EventType, Boolean Success, Object obj) {
			
			Event e = new Event();
			e.Action = Action;
			e.Success = Success;
			e.Obj = obj;
			
			SendEvent(EventType, e);
		}
		
		protected void SendEvent(int Action, int EventType, _BaseResult result) {
			SendEvent(Action, EventType, result != null ? result.status.success : false, result);
		}
				
		protected void SendEvent(int Action, _BaseResult result) {
			SendEvent(Action, EVENT_TASKDONE, result); 
		}
		
		protected void ProcessDocumentResult(int Action, DocResult result, ProcessedDocumentFunctions pd) {
			
			if ( result != null
					&& result.status.success == true ) {
				
				if ( result.totalsize > 0 ) {
					
					if ( result.totalsize > result.data.length ) {
						 if ( result.resultID != null
							      && result.resultID.length() > 0 ) {
								 
									int totalsize = (int)result.totalsize;
									String resultID = result.resultID;

									byte[] data = new byte[totalsize];
									int offset = 0;
									
									do {
										for(int a=0;a<result.data.length;a++) {
											data[offset] = result.data[a];
											offset++;
										}
										
										SendEvent(Action, EVENT_ONPROGRESS, true, Float.valueOf((float)(offset*100/totalsize))); 

										if ( offset < totalsize ) {
											
											result = pd.fetchRemoteDocumentFromResult(resultID, offset, MAX_BYTES_COUNT);
											
											if ( result.status.success == false ) {
												offset = 0;
												break;
											}
											
										} else {
											break;
										}


					 					
									} while(!isCanceled());
								
									result.data = data;
									result.totalsize = offset;

						}						
					} else {
						SendEvent(Action, EVENT_ONPROGRESS, true, Float.valueOf((float)100.0)); 
					}
				}
				

				result.status.success = result.data != null && result.totalsize > 0 && result.data.length == result.totalsize;

				if ( result.status.success == true ) {
					pd.onDocument(result.data);
				}

			}
			
			
			SendEvent(Action, result);
			
		}
		
		protected void ProcessDataResults(int Action, DataResults results, ProcessedDataFunctions pd) {
			
			if ( results != null ) {
				for(int a=0;a<results.recordCount();a++) {
					Object obj = results.getRecord(a);
					pd.onDataItem(obj);
					SendEvent(Action, EVENT_ONDATAITEM, true, obj); 
					
					if ( isCanceled() ) break;
				}
				
							
				DataResult DR = results.getResult();
				if ( DR != null 
						&& DR.resultID != null
						&& DR.resultID.length() > 0 ) {
					
					int Left = DR.totalRowCount - DR.rowCount;
					int Offset = DR.rowCount;
					String resultID = DR.resultID; 
					
					while(Left > 0 && !isCanceled()) {
						DR = pd.fetchRemoteDataFromResult(resultID, Offset, MAX_COUNT);
						if ( DR == null ) {
							Left = 0;
						} else {
							Left-=DR.rowCount;
							Offset+=DR.rowCount;

							for(int a=0;a<DR.recordCount();a++) {
								Object obj = DR.getRecord(a);
								pd.onDataItem(obj);
								SendEvent(Action, EVENT_ONDATAITEM, true, obj); 
								
								if ( isCanceled() ) break;
							}					
						}

					}
				}
			}
			
			SendEvent(Action, results);
		}
	
		public void task() throws InterruptedException {
			
		}
		
	}
	
	static int activeCount(int Scope) {
		int result = 0;
		synchronized(Queue) {
			for(int a=0;a<Queue.size();a++) {
				bgTask t = Queue.get(a);
				if ( (Scope & t.GetScope()) != 0 ) {
					result++;
				}
			}
				
		}
		return result;
	}
	
	static void cancelAll(int Scope) {
		
		synchronized (Queue) {
			for(int a=0;a<Queue.size();a++) {
				bgTask t = Queue.get(a);
				if ( (Scope & t.GetScope()) != 0 ) {
					Queue.get(a).cancel();
				}
			}
		}
		
	}
	
	static void shutdownBase(int minActive) {
		cancelAll(SCOPE_BASE);
		
		if ( activeCount(SCOPE_BASE) >= minActive ) {
			baseTaskExecutor.shutdownNow();
			baseTaskExecutor = Executors.newCachedThreadPool();		
		}
	}
	
	static void shutdownExport() {
		exportTaskExecutor.shutdownNow();
		exportTaskExecutor = Executors.newCachedThreadPool();		
	}
	
	static void shutdownBase() {
		shutdownBase(2);
	}
	
	static void shutdownAll()  {
		shutdownBase(0);
		shutdownExport();
	}	
	
	static void RegisterDevice() {
		
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				RemoteAction RA = new RemoteAction();
				SendEvent(ACTION_REGISTERDEVICE, RA.ra_RegisterDevice());
			};
		});
		
	}
	
	static void Login(final String Login, final String Password) {
		

		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				RemoteAction RA = new RemoteAction(Login, Password);
				SendEvent(ACTION_LOGIN, RA.ra_Login());
			};
		});	
	}
	
	static void CustomerSearch(final String Text, final Boolean OnlyByShortcut) {
	
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDataResults(ACTION_CUSTOMERSEARCH, RA.ra_CustomerSearch(Text, MAX_COUNT, OnlyByShortcut), new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchCustomersFromResult(ResultId, From, Count);
					}
				});

			};
		});	
	}
	
	static void CustomerSearch(final String Text) {
		CustomerSearch(Text, false);
	}
	
	private static void Invoices(final String Shortcut, final Date FromDate, final Boolean ForCustomer) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				
				ProcessedDataFunctions pd = new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchInvoicesFromResult(ResultId, From, Count);
					}
				};
				
				if ( ForCustomer ) {
					ProcessDataResults(ACTION_INVOICES, RA.ra_Invoices(Shortcut, FromDate, MAX_COUNT), pd);
				} else {
					ProcessDataResults(ACTION_INVOICE, RA.ra_InvoiceByShortcut(Shortcut), pd);
				}
				

			};
		});	
	}
	
	static void Invoices(final String Shortcut, final Date FromDate) {
		Invoices(Shortcut, FromDate, true);
	}
	
	static void Invoices(final String CustomerShortcut)  {
		Invoices(CustomerShortcut, null);
	}
	
	static void InvoiceByShortcut(final String Shortcut) {
		Invoices(Shortcut, null, false);
	}
	
	static void InvoiceItems(final String DocID) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDataResults(ACTION_INVOICEITEMS, RA.ra_InvoiceItems(DocID, MAX_COUNT), new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchInvoiceItemsFromResult(ResultId, From, Count);
					}
				});

			};
		});	
	}
	
	static void OutstandingPayments(final String CustomerShortcut) {
		
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDataResults(ACTION_OUTSTANDINGPAYMENTS, RA.ra_OutstandingPayments(CustomerShortcut, MAX_COUNT), new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchOutstandingPaymentsFromResult(ResultId, From, Count);
					}
				});

			};
		});		
	}
	
	static void getInvoiceDocuent(final String DocID) {
		
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDocumentResult(ACTION_INVOICEDOCUMENT, RA.ra_InvoiceDOC(DocID, MAX_BYTES_COUNT), new ProcessedDocumentFunctions() {
					
					@Override
					public void onDocument(byte[] doc) {	
					}
					
					@Override
					public DocResult fetchRemoteDocumentFromResult(String ResultId,
							long fromByte, long maxBytesCount) {
						return RA.ra_fetchDocumentFromResult(ResultId, fromByte, maxBytesCount);
					}
				});

			};
		});	
	}
	
	private static void Orders(final String Shortcut, final Date FromDate, final Boolean ForCustomer) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				
				ProcessedDataFunctions pd = new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchOrdersFromResult(ResultId, From, Count);
					}
				};
				
				if ( ForCustomer ) {
					ProcessDataResults(ACTION_ORDERS, RA.ra_Orders(Shortcut, FromDate, MAX_COUNT), pd);
				} else {
					ProcessDataResults(ACTION_ORDER, RA.ra_OrderByShortcut(Shortcut), pd);
				}
				

			};
		});	
	}
	
	static void Orders(final String Shortcut, final Date FromDate) {
		Orders(Shortcut, FromDate, true);
	}
	
	static void Orders(final String CustomerShortcut)  {
		Orders(CustomerShortcut, null);
	}
	
	static void OrderByShortcut(final String Shortcut) {
		Orders(Shortcut, null, false);
	}
	
	static void OrderItems(final String DocID) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDataResults(ACTION_ORDERITEMS, RA.ra_OrderItems(DocID, MAX_COUNT), new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchOrderItemsFromResult(ResultId, From, Count);
					}
				});

			};
		});	
	}
	
	static void ArticleSearch(final String Text) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDataResults(ACTION_ARTICLESEARCH, RA.ra_ArticleSearch(Text, MAX_COUNT), new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchArticlesFromResult(ResultId, From, Count);
					}
				});

			};
		});	
	}
	
	static void DictionaryOfType(final int DictType, final String CustomerShortcut) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
			    SendEvent(ACTION_DICTIONARY, (new RemoteAction()).ra_DictionayOfType(DictType, CustomerShortcut));
			};
		});		
	}
	
	static void PriceForContractor(final String CustomerShortcut, final String ArticleShortcut, final String Currency) {
		
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
			    SendEvent(ACTION_PRICEFORCONTRACTOR, (new RemoteAction()).ra_PriceForContractor(CustomerShortcut, ArticleShortcut, Currency));
			};
		});	
	
	}
	
	static void ContractorLimits(final String CustomerShortcut) {
		addTask(new bgTask() {
			@Override
			public void task() throws InterruptedException {
				
				final RemoteAction RA = new RemoteAction();
				ProcessDataResults(ACTION_CONTRACTORLIMIT, RA.ra_ContractorLimits(CustomerShortcut, MAX_COUNT), new ProcessedDataFunctions() {
					
					@Override
					public void onDataItem(Object dataItem) {
					}
					
					@Override
					public DataResult fetchRemoteDataFromResult(String ResultId, int From,
							int Count) {
						return RA.ra_fetchContractorLimitsFromResult(ResultId, From, Count);
					}
				});

			};
		});	
	}
	
	static void doExport() {
		
	}
	
}
