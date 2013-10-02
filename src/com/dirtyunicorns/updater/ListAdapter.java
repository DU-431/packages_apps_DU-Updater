package com.dirtyunicorns.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<String> implements OnClickListener{
	
	private final Context context;
	private final String[] values;
	private int layoutResourceId;
	private String link;
    private NotificationManager notificationManager;
    private Notification notification;
    private Intent notificationIntent;
	
	public ListAdapter (Context context, int layoutResourceID, String[] values) {
		super(context, R.layout.list_row, values);
		this.context = context;
		this.values = values;
		this.layoutResourceId = layoutResourceID;
	}
	
	 @Override
    public int getViewTypeCount() {
    	return getCount();
    }
    
    @Override
    public int getItemViewType(int position) {
    	return position;
    }
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View rowView = convertView;
		InfoHolder holder = null;
		if (rowView == null) {
			
			holder = new InfoHolder();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(layoutResourceId, parent, false);
			holder.versNum = (TextView) rowView.findViewById(R.id.VersNum);
			holder.buildNum = (TextView) rowView.findViewById(R.id.BuildNum);
			holder.Dev = (TextView) rowView.findViewById(R.id.Device);
			holder.servDate = (TextView) rowView.findViewById(R.id.ServDate);
			holder.imgB = (ImageButton) rowView.findViewById(R.id.DispImage);
			
		}
		else
		{
			holder = (InfoHolder)rowView.getTag();
		}
		
		String s = values[position];
		String[] splits = s.split(",");
		holder.Dev.setText(splits[0]);
		holder.versNum.setText("DU Version: " + splits[1]);
		holder.buildNum.setText("Build Number: " + splits[2]);
		holder.servDate.setText("Version Date: " + splits[4]);
		link = splits[3];
		holder.imgB.setOnClickListener(this);
		return rowView;
	}
	
	static class InfoHolder {
		TextView versNum;
		TextView buildNum;
		TextView Dev;
		TextView servDate;
		ImageButton imgB;
	}

	@Override
	public void onClick(View v) {
		
		new DownloadFile().execute();
	}
	@SuppressWarnings("deprecation")
	public class DownloadFile extends AsyncTask<String, String, String> {
	    CharSequence contentText;
	    CharSequence contentTitle;
	    PendingIntent contentIntent;
	    int HELLO_ID = 1;
	    long time;
	    int icon;
	    CharSequence tickerText;
	    File file;

	    public void downloadNotification(){
	        try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	String ns = Context.NOTIFICATION_SERVICE;
	        notificationManager = (NotificationManager) context.getSystemService(ns);

	        icon = R.drawable.ic_launcher;
	        //the text that appears first on the status bar
	        tickerText = "Downloading...";
	        time = System.currentTimeMillis();

	        notification = new Notification(icon, tickerText, time);
	        //the bold font
	        contentTitle = "DU downloading...";
	        //the text that needs to change
	        contentText = "0% complete";
	        notificationIntent = new Intent(Intent.ACTION_VIEW);
	        contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	        
	        
	        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	        notificationManager.notify(HELLO_ID, notification);

	    }


	    @Override
	    protected void onPreExecute() {
	        //execute the status bar notification
	        downloadNotification();

	        super.onPreExecute();
	    }

	    @Override
	    protected String doInBackground(String... url) {
	        
	        
	        	Thread t = new Thread() {
	        		public void run() {
	        			try {
	        				int count;
		        			URL url2 = new URL(link);
		    	            HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
		    	            connection.setRequestMethod("GET");
		    	            //connection.setDoOutput(true);
		    	            connection.connect();
	
		    	            int lengthOfFile = connection.getContentLength();
	
		    	            //make the stop drop rave folder
		    	            File sdrFolder = new File(Environment.getExternalStorageDirectory() + "/DirtyUnicorns");
		    	            boolean success = false;
	
		    	            if(!sdrFolder.exists()){
		    	                success = sdrFolder.mkdir();
		    	            }      
		    	            if (!success) { 
		    	                String PATH = Environment.getExternalStorageDirectory()
		    	                        + "/DirtyUnicorns/";
		    	                file = new File(PATH);
		    	                file.mkdirs();
	
		    	            }
		    	            else 
		    	            {
		    	                String PATH = Environment.getExternalStorageDirectory()
		    	                        + "/DirtyUnicorns/";
		    	                file = new File(PATH);
		    	                file.mkdirs();
		    	            }
	
		    	            String[] path = url2.getPath().split("/");
		    	            String mp3 = path[path.length - 1];
		    	            String mp31 = mp3.replace("%20", " ");
		    	            String sdrMp3 = mp31.replace("%28", "(");
		    	            String sdrMp31 = sdrMp3.replace("%29", ")");
		    	            String sdrMp32 = sdrMp31.replace("%27", "'");
	
		    	            File outputFile = new File(file, sdrMp32);
		    	            FileOutputStream fos = new FileOutputStream(outputFile);
	
		    	            InputStream input = connection.getInputStream();
	
		    	            byte[] data = new byte[1024];
		    	            long total = 0;
		    	            while ((count = input.read(data)) != -1) {
		    	                total += count;
		    	                publishProgress(""+(int) (total * 100 / lengthOfFile));
		    	                fos.write(data, 0, count);
		    	            }
		    	            fos.close();
		    	            input.close();

	    	        } catch (IllegalArgumentException e) 
	    	        { e.printStackTrace();
	    	        }catch (IllegalStateException e) { 
	    	            e.printStackTrace();
	    	        }catch (IOException e) {
	    	        e.printStackTrace();
	    	        }
	        		}
	        	};
	        	t.start();
	            

	        return null;
	    }

	    @Override
	    public void onProgressUpdate(String... progress) {
	        if (Integer.parseInt(progress[0]) == 100) {
	        	notification.flags = Notification.FLAG_AUTO_CANCEL;
	        	notificationIntent.setType("application/zip");
	        }
	        else if (Integer.parseInt(progress[0]) == 0) {
	        	notification.flags = Notification.FLAG_AUTO_CANCEL;
	        }
	        else {
	        	notification.flags |= Notification.FLAG_ONGOING_EVENT;
	        }
	    	contentText =  Integer.parseInt(progress[0]) + "% complete";
	        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	        notificationManager.notify(HELLO_ID, notification);
	        super.onProgressUpdate(progress);
	    }
	}
}
