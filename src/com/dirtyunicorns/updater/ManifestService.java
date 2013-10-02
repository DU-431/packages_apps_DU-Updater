package com.dirtyunicorns.updater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class ManifestService extends Service{

	private static Timer timer = new Timer();
	private Context ctx;
	private Handler mHandler = new Handler();
	private long UPDATE_TIMER = 2000;
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
			                    //update code will go here
			                   
				            } catch (Exception e) {
				            	
				            }
			        	}
			        };
			        t.start();
				}
			});
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
		}
		
		return line;
	}
	
	public IBinder onBind(Intent intent)
	{
		return null;
	}   
}
