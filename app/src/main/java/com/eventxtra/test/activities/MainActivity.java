package com.eventxtra.test.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.eventxtra.test.R;
import com.eventxtra.test.adapters.ReminderAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	ReminderAdapter mReminderAdapter;
	ListView mListViewMain;
	SmoothProgressBar smoothprogressbar_main;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setup download progress dialog
		smoothprogressbar_main = (SmoothProgressBar)findViewById(R.id.smoothprogressbar_main);
		//setup listview
		mListViewMain = (ListView)findViewById(R.id.listview_main);
		//setup adapter
		mReminderAdapter = new ReminderAdapter(MainActivity.this);
		//setup floating button to add new event
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.actionbutton_main);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		//fake data first showed up
		mReminderAdapter.add(new ReminderAdapter.SampleItem("2016-02-28", "fake", "TRUE"));
		mReminderAdapter.add(new ReminderAdapter.SampleItem("2016-02-28", "fake", "TRUE"));
		mReminderAdapter.add(new ReminderAdapter.SampleItem("2016-02-28", "fake", "TRUE"));

		//set mListView
		mListViewMain.setAdapter(mReminderAdapter);

		//check internet is available
		if(isNetworkAvailable(MainActivity.this)){
			getData();
		} else{
			Snackbar snack = Snackbar.make(findViewById(android.R.id.content), "need to check your network is available", Snackbar.LENGTH_LONG);
			View view = snack.getView();
			FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
			params.gravity = Gravity.TOP;
			view.setLayoutParams(params);
			view.setBackgroundColor(Color.RED);
			snack.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @param context
	 * @return true if  network is not only availabe but also connected!!!.
	 */
	public boolean isNetworkAvailable(final Context context) {
		final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
		return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
	}


	/**
	 * get Data from sheetsu.com
	 */
	private void getData() {
		//get data
		smoothprogressbar_main.setVisibility(View.VISIBLE);
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(MainActivity.this, "https://sheetsu.com/apis/v1.0/91d8c1cbe904", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				super.onSuccess(statusCode, headers, response);
				smoothprogressbar_main.setVisibility(View.GONE);
				//clear data
				mReminderAdapter.clear();
				int dataLength = response.length();
				try {
					for(int index=0 ; index< dataLength; index++){
						JSONObject rowData = response.getJSONObject(index);
						String string_datetime = rowData.getString("datetime");
						String string_task = rowData.getString("task");
						String string_isFinish = rowData.getString("isFinish");
						mReminderAdapter.add(new ReminderAdapter.SampleItem(string_datetime, string_task, string_isFinish));
					}
					//set adapter
					mReminderAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				Log.d(TAG,statusCode+" "+responseString);
				smoothprogressbar_main.setVisibility(View.GONE);
			}
		});
	}
}
