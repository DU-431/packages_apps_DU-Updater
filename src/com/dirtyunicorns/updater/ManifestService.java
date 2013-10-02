package com.dirtyunicorns.updater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class ManifestService extends Service{

	private static Timer timer = new Timer();
	private Handler mHandler = new Handler();
	private long UPDATE_TIMER = 2000;
	private Context ctx;
	private String devV, devBM, servV, servBM;
    private ServerComm sc;
    private String data;
	@Override
	public void onCreate() {
		super.onCreate();
		ctx = this;
		Log.v("DU", "Creating Service");
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		
		startService();
	}
	
	public void startService() {

		Log.v("DU", "Starting Service");
		UPDATE_TIMER = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean oneday, twoday, threeday, fiveday, week;
		oneday = sharedPref.getBoolean("pref_key_update_interval_1_day", false);
		twoday = sharedPref.getBoolean("pref_key_update_interval_2_day", false);
		threeday = sharedPref.getBoolean("pref_key_update_interval_3_day", false);
		fiveday = sharedPref.getBoolean("pref_key_update_interval_5_day", false);
		week = sharedPref.getBoolean("pref_key_update_interval_7_day", false);
		
		if(oneday)
		{
			UPDATE_TIMER = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
			System.out.println("Update Interval set to 1 day");
		}
		else if (twoday)
		{
			UPDATE_TIMER = TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS);
			System.out.println("Update Interval set to 2 days");
		}
		else if (threeday)
		{
			UPDATE_TIMER = TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS);
			System.out.println("Update Interval set to 3 days");
		}
		else if (fiveday)
		{
			UPDATE_TIMER = TimeUnit.MILLISECONDS.convert(5, TimeUnit.DAYS);
			System.out.println("Update Interval set to 5 days");
		}
		else if (week)
		{
			UPDATE_TIMER = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
			System.out.println("Update Interval set to 7 days");
		}
		Log.v("DU", "Setting service to run every " + UPDATE_TIMER + " miliseconds");
		timer.scheduleAtFixedRate(new Updater(), 0, UPDATE_TIMER);
	}
	
	public class Updater extends TimerTask
	{
		public void run() {
			
			mHandler.post(new Runnable() {
				
				public void run() {
					System.out.println("DU UPDATING");
					Thread t = new Thread() {
			        	@SuppressLint("SimpleDateFormat")
						public void run() {
			        		
			        		try {
			        			if (IsOnline()) {
			        	    		try{
			        	            	data = sc.getLatest();
			        	            }
			        	            catch (Exception e) {
			        	            	
			        	            }
			        	    		SplitStrings();
			        	    		GetInfo();
			        	    	}
			                   
				            } catch (Exception e) {
				            	
				            }
			        	}
			        };
			        t.start();
				}
			});
		}
	}
	
	public void SplitStrings(){
    	String[] splitJSON = data.split(",");
    	servV = splitJSON[1];
    	servBM = splitJSON[2];
    }
    
    public void GetInfo()
    {
    	devBM = GetBuildNum();
    	devV = GetVersNum();
    	int dB, sB;
    	double sV, dV;
    	sV = Double.valueOf(servV);
    	dV = Double.valueOf(devV);
    	dB = Integer.valueOf(devBM);
    	sB = Integer.valueOf(servBM);
    	if (sV > dV)
    	{
    		//Notify
    	}
    	else
    	{
    		if (sB > dB){
        		//Notify
    		}
    	}
        
    }
    
    public String GetBuildNum() {
		String line = "";
		try {
			Process ifc = Runtime.getRuntime().exec("getprop ro.du.buildnum");
			BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
			line = bis.readLine();
			ifc.destroy();
		} catch (java.io.IOException e) {
			line = e.getMessage();
		}
		
		return line;
	}
    
    public String GetVersNum() {
		String line = "";
		try {
			Process ifc = Runtime.getRuntime().exec("getprop ro.romstats.version");
			BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
			line = bis.readLine();
			ifc.destroy();
		} catch (java.io.IOException e) {
			line = e.getMessage();
		}
		
		return line;
	}
    
    public boolean IsOnline()
	{
		boolean online = false;
		
		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] activeNet = conMgr.getAllNetworkInfo();
		for (NetworkInfo adapter : activeNet)
		{
			if (adapter.isConnected()){
				Log.v("DU", "Device is online, getting info");
				online = true;
				break;
			}
		}
		
		return online;
	}
	
    public void NotifyUpdate()
    {
    	Intent i = new Intent(ctx, MainActivity.class);
    	NotificationManager noficationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
    		.setSmallIcon(R.drawable.ic_launcher)
    		.setContentTitle("DU Update")
    		.setContentText("Version: " + servV + " Build Number: " + servBM);
    	
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
    	stackBuilder.addParentStack(MainActivity.class);
    	stackBuilder.addNextIntent(i);
    	PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(pendingIntent);
    	noficationManager.notify(0,mBuilder.build());
    }
    
	public IBinder onBind(Intent intent)
	{
		return null;
	}   
}
