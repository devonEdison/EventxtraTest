package com.eventxtra.test.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.eventxtra.test.EventxtraTestApplication;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.entity.StringEntity;

//I use IntentService in this case , because it can stop itself if not working, also has handler, Looper , which allows you to use more friendly in this app.
//there will be only 1 intentservice but we can do multiple jobs in queue from creating new tasks.
public class WaitingToUploadService extends IntentService {
    private static final String TAG = "WaitingToUploadService";

	public WaitingToUploadService() {
		super(TAG);
		Log.d(TAG,"WaitingToUploadService");
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG,"onCreate");
	}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"onStartCommand");
    	return super.onStartCommand(intent, flags, startId);
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG,"onHandleIntent");
		String string_editTextDateTime = intent.getStringExtra("string_editTextDateTime");
		String string_editTextTask = intent.getStringExtra("string_editTextTask");
		String string_editTextIsFinish = intent.getStringExtra("string_editTextIsFinish");

		uploadNewTask(string_editTextDateTime, string_editTextTask, string_editTextIsFinish);

	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"onDestroy ");
		super.onDestroy();
	}
	private void uploadNewTask(final String string_editTextDateTime, final String string_editTextTask, final String string_editTextIsFinish) {
		//compose parameter json
		StringEntity entity = null;
		try {
			JSONObject jsonParams = new JSONObject();
			jsonParams.put("datetime", string_editTextDateTime);
			jsonParams.put("task", string_editTextTask);
			jsonParams.put("isFinish", string_editTextIsFinish);
			entity = new StringEntity(jsonParams.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		SyncHttpClient client = new SyncHttpClient();
		client.setBasicAuth(((EventxtraTestApplication)getApplication()).api_key,((EventxtraTestApplication)getApplication()).api_secret,
				new AuthScope("sheetsu.com", 80, AuthScope.ANY_REALM));
		client.post(WaitingToUploadService.this, ((EventxtraTestApplication)getApplication()).api_url, entity, "application/json",  new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				tryTimes = 0;
				//renew data
				Intent intent = new Intent("data-upload-finished");
				// You can also include some extra data.
				intent.putExtra("message", "This is my message!");
				LocalBroadcastManager.getInstance(WaitingToUploadService.this).sendBroadcast(intent);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				try {
					Log.d(TAG, " statusCode " + statusCode + " " + errorResponse.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				//no matter it's networking issue or other issue, we wait 5 more seconds and do it again
				//I don't check only networking because for user if it's server side issue or networking issue, user's feeling is the same. "not working"!
				if(tryTimes<1000) {
					try {
						Thread.sleep(3000);
						tryTimes ++;
						uploadNewTask(string_editTextDateTime, string_editTextTask, string_editTextIsFinish);
					} catch (InterruptedException e) {
						// Restore interrupt status.
						Thread.currentThread().interrupt();
					}
				}
			}
		});
	}
	int tryTimes=0;
}
