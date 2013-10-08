package com.dirtyunicorns.updater;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
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
	private String link, fileName;
	
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
		fileName = "du_" + splits[0] + "_" + splits[4] + ".zip";
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
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://dirtyunicorns.com/dusite/download.php?file=" + link));
		request.setDescription("Downloading Dirty Unicorns");
		request.setTitle("DU Download");
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
		
	}
}
