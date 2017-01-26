package com.example.bus_track;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class bus_data_send extends Activity implements LocationListener {
	EditText et1;
	Button b1;
	TextView tv1, tv2, tv3;
	SharedPreferences pref;
	LocationManager mgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.bus_data_send);
		et1 = (EditText) findViewById(R.id.etTag);
		b1 = (Button) findViewById(R.id.btnSend);
		tv1 = (TextView) findViewById(R.id.tvGPStag);
		tv2 = (TextView) findViewById(R.id.tvLatitude);
		tv3 = (TextView) findViewById(R.id.tvLongitude);

		pref = getSharedPreferences("GPS_Tag", MODE_WORLD_WRITEABLE);
		String pref_tag = pref.getString("tagID", null);
		if (pref_tag != null)
		{
			b1.setEnabled(false);
			et1.setText(pref_tag);
			et1.setEnabled(false);
			tv1.setText(pref_tag);
		}
		
		String locationService = Context.LOCATION_SERVICE;
		String networkService = LocationManager.NETWORK_PROVIDER;
		
		mgr = (LocationManager)getSystemService(locationService);
		
		mgr.requestLocationUpdates(mgr.GPS_PROVIDER, 2000, 10, this);

		Location lastLocation = mgr.getLastKnownLocation(mgr.GPS_PROVIDER);
		if(lastLocation != null)
		{
			tv2.setText("" + lastLocation.getLatitude());
			tv3.setText(""+ lastLocation.getLongitude());

			
		}

		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String s = et1.getText().toString();
				if (s.length() > 0) {
					ValidateTag o = new ValidateTag();
					o.execute(s);
				}
			}
		});

	}

	class ValidateTag extends AsyncTask<String, String, String> {
		String answer = "";
		private String url = "http://ctg.comeze.com/tagValidate.php";

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String value = params[0];

			try {
				URL location = new URL(url);
				URLConnection con = location.openConnection();
				con.setDoOutput(true);
				String req_set = URLEncoder.encode("val", "UTF-8") + "="
						+ URLEncoder.encode(value, "UTF-8");

				OutputStreamWriter writer = new OutputStreamWriter(
						con.getOutputStream());
				writer.write(req_set);

				writer.flush();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(con.getInputStream()));

				String result = "";
				String tmp = "";

				while ((tmp = reader.readLine()) != null) {
					result += tmp + "\n";
				}

				answer = result;
			} catch (Exception e) {
				answer = "";
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			String[] temp = answer.split("\n");
			String output = temp[0];

			Toast.makeText(bus_data_send.this, output, Toast.LENGTH_LONG)
					.show();

			if (output.equals("Valid")) {
				SharedPreferences.Editor gps_tag = pref.edit();
				String text = et1.getText().toString();
				et1.setEnabled(false);
				b1.setEnabled(false);
				gps_tag.putString("tagID", text);// ("tagID",
													// Integer.parseInt(text));
				gps_tag.commit();

				pref = getSharedPreferences("GPS_Tag", MODE_WORLD_READABLE);
				String deviceId = pref.getString("tagID", "");
				tv1.setText(deviceId);
			
			}

		}
	}
	
	class UpdateLocationTask extends AsyncTask<String, String, String>
	{
		String output = "";
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try
			{
				String latt = params[0];
				String lngt = params[1];
				String tag = params[2];
				String url = "http://ctg.comeze.com/bus_data.php";
				String params1 = URLEncoder.encode("lat", "UTF-8")+"="+URLEncoder.encode(latt, "UTF-8")
						+"&"+URLEncoder.encode("long", "UTF-8")+"="+URLEncoder.encode(lngt, "UTF-8")
						+"&"+URLEncoder.encode("device", "UTF-8")+"="+URLEncoder.encode(tag, "UTF-8");
				URL server_location = new URL(url);
				URLConnection con = server_location.openConnection();
				con.setDoOutput(true);
				
				BufferedReader reader = null;
				OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
				writer.write(params1);
				writer.flush();
				
				reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String temp = "";
				while ((temp = reader.readLine()) != null)
				{
					output += temp + "\n";
				}
				
			}
			catch (Exception e)
			{
				output = "";
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		tv2.setText("" + location.getLatitude());
		tv3.setText("" + location.getLongitude());
		String tag = tv1.getText().toString();
		String lat = tv2.getText().toString();
		String longi = tv3.getText().toString();
		UpdateLocationTask task = new UpdateLocationTask();
		task.execute(""+location.getLatitude() , ""+location.getLongitude(), tag);
		Toast.makeText(bus_data_send.this, "Changing Location", 500).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
	
}
