package com.dirtyunicorns.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class MainActivity extends Activity {

	private TextView deviceModel, deviceVers, deviceBuildNum, serverVers, serverBuildNum, serverBuildDate, UpdateAvail;
	private ImageButton btnDLatest;
	private ListView list;
	private String devM, devV, devBM, servV, servBM, servL, servD, fileName;
    private Intent intent;
	private SharedPreferences settings;
    private String model;
    private ServerComm sc;
    private String data;
    private String[] all;
    private NotificationManager notificationManager;
    private Notification notification;
    private Intent notificationIntent;
    private double dV;
    private String error = "";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       
        //Create dialog mode for widgets
        /*Boolean fDialogMode = getIntent().hasExtra("dialog_mode");
        if (!fDialogMode) {
        	super.setTheme(android.R.style.Theme_Holo);
        }*/
        
        //Start the View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Initialize Variables
        settings = getSharedPreferences("UpdateInfo",0);
        deviceModel = (TextView) findViewById(R.id.DeviceModel);
        deviceVers = (TextView) findViewById(R.id.DeviceVers);
        deviceBuildNum = (TextView) findViewById(R.id.DeviceBuildnum);
        serverVers = (TextView) findViewById(R.id.ServerVers);
        serverBuildNum = (TextView) findViewById(R.id.ServerBuildNum);
        serverBuildDate = (TextView) findViewById(R.id.ServerBuildDate);
        UpdateAvail = (TextView) findViewById(R.id.UpdateAvail);
        btnDLatest = (ImageButton) findViewById(R.id.btnDownloadLatest);
        list = (ListView) findViewById(android.R.id.list);
    	btnDLatest.setVisibility(View.GONE);
        model = GetProduct();
        
        System.out.println(model);
        
        sc = new ServerComm(model);
        sc.start();
        
        //Set Text Fields
        devBM = GetBuildNum();
    	devV = GetVersNum();
    	dV = Double.valueOf(devV);
    	deviceBuildNum.setText("Device Build Number: " + devBM);
    	deviceModel.setText("Device: " + model);
        deviceVers.setText("Device Version: " +  devV);
        
        
        if (IsOnline()) {
            		try{
            			
                    	data = sc.getLatest();
                    	if (data != "Device not found in the database")
	                    {
	                    	all = sc.getAll();
	                    	ListAdapter adapter = new ListAdapter(getApplicationContext(),R.layout.list_row, all);
	                        list.setAdapter(adapter);
	                        startService(new Intent(getApplicationContext() ,ManifestService.class));
	                        SplitStrings();
	                		GetInfo();
                    	}
                    	else
                    	{
                    		Toast.makeText(this, "This is not an officially supported device", Toast.LENGTH_LONG).show();

                    		UpdateAvail.setText("This is not an officially supported device");
                    		UpdateAvail.setTextColor(Color.RED);
                    	}
                    }
                    catch (Exception e) {
                    	e.printStackTrace();
                    }
            		
            	}
                else
                {
                	btnDLatest.setVisibility(View.GONE);
                	error = "offline";
                }
        	
        //Check if online
        if (error == "offline") {
        	Toast.makeText(this, "Not online, Please try again later",Toast.LENGTH_LONG).show();
        }
        
        btnDLatest.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v){
        		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://dirtyunicorns.com/dusite/download.php?file=" + servL));
        		request.setDescription("Downloading Dirty Unicorns");
        		request.setTitle("DU Download");
        		// in order for this if to run, you must use the android 3.2 to compile your app
        		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        		    request.allowScanningByMediaScanner();
        		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        		}
        		
        		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        		// get download service and enqueue file
        		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        		manager.enqueue(request);
        	}
        });
    }
    
    public void DownloadGapps() {
    	
    	DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://dirtyunicorns.com/roms/gapps/gapps-jb-20130813-signed.zip"));
		request.setDescription("Downloading GAPPS!");
		request.setTitle("Downloading GAPPS");
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "latest_GApps.zip");

		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
    }
    
    public void SplitStrings(){
    	String[] splitJSON = data.split(",");
    	servV = splitJSON[1];
    	servBM = splitJSON[2];
    	servL = splitJSON[3];
    	servD = splitJSON[4];
    	
    	fileName = "du_" + model + "_" + servD + ".zip";
    }
    
    public void GetInfo()
    {
    	
    	int dB, sB;
    	double sV;
    	sV = Double.valueOf(servV);
    	dB = Integer.valueOf(devBM);
    	sB = Integer.valueOf(servBM);
    	if (sV > dV)
    	{
    		UpdateAvail.setText("An Update is Available");
    		UpdateAvail.setTextColor(Color.GREEN);
    	}
    	else
    	{
    		if (sB > dB){
        		UpdateAvail.setText("An Update is Available");
        		UpdateAvail.setTextColor(Color.GREEN);
    		}
    		else
    		{
        		UpdateAvail.setText("You are on the most current version");
        		UpdateAvail.setTextColor(Color.RED);
    		}
    	}
    	
    	if (UpdateAvail.getText() == "An Update is Available") {

        	btnDLatest.setVisibility(View.VISIBLE);
    	}
    	
        serverVers.setText("Server Version: " +  servV);
        serverBuildNum.setText("Server Build Number: " + servBM);
        serverBuildDate.setText("Latest Build Date: " + servD);
        
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
    
    public String GetProduct() {
    	String line = "";
		try {
			Process ifc = Runtime.getRuntime().exec("getprop ro.build.product");
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
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.action_gapps:
	    		DownloadGapps();
	    		return true;
	    	case R.id.action_settings:
	    		OpenSettings();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }

    public void OpenSettings() {
    	Log.i("DU MENU", "Settings Clicked");
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
}
