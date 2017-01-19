package com.eventxtra.test.activities;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.eventxtra.test.R;
import com.eventxtra.test.adapters.TodoAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	TodoAdapter mTodoAdapter;
	ListView mListViewTodo;
	SmoothProgressBar smoothprogressbar_main;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setup download progress dialog
		smoothprogressbar_main = (SmoothProgressBar) findViewById(R.id.smoothprogressbar_main);
		//setup listview
		mListViewTodo = (ListView) findViewById(R.id.listview_todo);
		//setup adapter
		mTodoAdapter = new TodoAdapter(MainActivity.this);
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
		mTodoAdapter.add(new TodoAdapter.SampleItem("2016-02-28", "fake", "TRUE"));
		mTodoAdapter.add(new TodoAdapter.SampleItem("2016-02-28", "fake", "TRUE"));
		mTodoAdapter.add(new TodoAdapter.SampleItem("2016-02-28", "fake", "TRUE"));

		//set mListView
		mListViewTodo.setAdapter(mTodoAdapter);

		//check internet is available
		if (isNetworkAvailable(MainActivity.this)) {
			getData();
		} else {
			Snackbar snack = Snackbar.make(findViewById(android.R.id.content), "need to check your network is available", Snackbar.LENGTH_LONG);
			View view = snack.getView();
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
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
		Toast.makeText(MainActivity.this, "updating", Toast.LENGTH_SHORT).show();
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(MainActivity.this, getString(R.string.api_url), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				super.onSuccess(statusCode, headers, response);
				smoothprogressbar_main.setVisibility(View.GONE);
				//clear data
				mTodoAdapter.clear();
				int dataLength = response.length();
				try {
					for (int index = 0; index < dataLength; index++) {
						JSONObject rowData = response.getJSONObject(index);
						String string_datetime = rowData.getString("datetime");
						String string_task = rowData.getString("task");
						String string_isFinish = rowData.getString("isFinish");
						mTodoAdapter.add(new TodoAdapter.SampleItem(string_datetime, string_task, string_isFinish));
					}
					//set adapter
					mTodoAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					Log.d(TAG, " statusCode " + statusCode + " " + errorResponse.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				smoothprogressbar_main.setVisibility(View.GONE);
			}
		});
	}
}
