package com.eventxtra.test.activities;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eventxtra.test.EventxtraTestApplication;
import com.eventxtra.test.R;
import com.eventxtra.test.adapters.TodoAdapter;
import com.eventxtra.test.database.ToDoData;
import com.eventxtra.test.database.ToDoDataDao;
import com.eventxtra.test.service.WaitingToUploadService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yalantis.phoenix.PullToRefreshView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.entity.StringEntity;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	TodoAdapter mTodoAdapter;
	com.baoyz.swipemenulistview.SwipeMenuListView mListViewTodo;
	SmoothProgressBar smoothprogressbar_main;
	ToDoDataDao mToDoDataDao;
	PullToRefreshView mPullToRefreshView;
	/**
	 * we do all the setup in onCreate
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents with actions named "data-upload-finished".
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("data-upload-finished"));
		//get database dao.
		mToDoDataDao =  ((EventxtraTestApplication)getApplication()).getToDoDataDao();
		//setup download progress dialog
		smoothprogressbar_main = (SmoothProgressBar) findViewById(R.id.smoothprogressbar_main);
		//setup pull to refresh view
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
		mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
			@Override
			public void onRefresh() {
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
		});
		//setup listview
		mListViewTodo = (com.baoyz.swipemenulistview.SwipeMenuListView) findViewById(R.id.listview_todo);
		mListViewTodo.setMenuCreator(creator);
		mListViewTodo.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
				switch (index) {
					case 0:
						// edit
						reviseDataDialog(position);
						break;
					case 1:
						// delete
						deleteDataDialog(position);
						break;
				}
				// false : close the menu; true : not close the menu
				return false;
			}
		});
		//setup adapter
		mTodoAdapter = new TodoAdapter(MainActivity.this);
		//setup floating button to add new task
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.actionbutton_main);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//						.setAction("Action", null).show();
				addDataDialog();
			}
		});
	}

	/**
	 * we do most things here. onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		//we can show the local database at first.
		//this is also important!! we don't do the loading data when main thread is busy, but do it at last, so we use Handler().post
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				List<ToDoData> mList = mToDoDataDao.loadAll();
				for (int i = 0 ; i < mList.size() ; i ++){
					mTodoAdapter.add(new TodoAdapter.SampleItem(mList.get(i).getDatetime()
							, mList.get(i).getTask(), mList.get(i).getIsFinish()));
				}
			}
		});

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
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}

	/**
	 * this option menu is used for search~ currently search only
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		// Retrieve the SearchView and plug it into SearchManager
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
//				callSearch(query);
				// Check if no view has focus:
				View view = MainActivity.this.getCurrentFocus();
				if (view != null) {
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				callSearch(newText);
				return true;
			}

			public void callSearch(String query) {
				//Do searching
				if (query.isEmpty()){
					//if query is empty , then load all
					List<ToDoData> mList = mToDoDataDao.loadAll();
					for (int i = 0 ; i < mList.size() ; i ++){
						mTodoAdapter.add(new TodoAdapter.SampleItem(mList.get(i).getDatetime()
								, mList.get(i).getTask(), mList.get(i).getIsFinish()));
					}
					mTodoAdapter.notifyDataSetChanged();
					//if it's not the search.
				}else {
					List<ToDoData> mList = mToDoDataDao.queryBuilder()
							.whereOr(ToDoDataDao.Properties.Datetime.like(query), ToDoDataDao.Properties.Task.like(query),
									ToDoDataDao.Properties.IsFinish.like(query))
							.listLazy();
					if (mList != null) {
						mTodoAdapter.clear();
						for (int i = 0; i < mList.size(); i++) {
							mTodoAdapter.add(new TodoAdapter.SampleItem(mList.get(i).getDatetime()
									, mList.get(i).getTask(), mList.get(i).getIsFinish()));
						}
						mTodoAdapter.notifyDataSetChanged();
					}
				}
			}

		});
		return true;
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
		client.setBasicAuth(((EventxtraTestApplication)getApplication()).api_key,((EventxtraTestApplication)getApplication()).api_secret,
				new AuthScope("sheetsu.com", 80, AuthScope.ANY_REALM));
		client.get(MainActivity.this, ((EventxtraTestApplication)getApplication()).api_url, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				super.onSuccess(statusCode, headers, response);
				mPullToRefreshView.setRefreshing(false);
				smoothprogressbar_main.setVisibility(View.GONE);
				//clear data
				mTodoAdapter.clear();
				mToDoDataDao.deleteAll();
				int dataLength = response.length();
				try {
					for (int index = 0; index < dataLength; index++) {
						JSONObject rowData = response.getJSONObject(index);
						String string_datetime = rowData.getString("datetime");
						String string_task = rowData.getString("task");
						String string_isFinish = rowData.getString("isFinish");
						if(!string_datetime.isEmpty() && !string_task.isEmpty() && !string_isFinish.isEmpty()) {
							//data add adapter
							mTodoAdapter.add(new TodoAdapter.SampleItem(string_datetime, string_task, string_isFinish));
							//data save to database
							long id = mToDoDataDao.insert(new ToDoData(null, string_datetime, string_task, string_isFinish));
						}
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
					mPullToRefreshView.setRefreshing(false);
					smoothprogressbar_main.setVisibility(View.GONE);
				}
				mPullToRefreshView.setRefreshing(false);
				smoothprogressbar_main.setVisibility(View.GONE);
			}
		});
	}

	private void uploadData(final String string_editTextDateTime, final String string_editTextTask, final String string_editTextIsFinish){
		//uploadData
		smoothprogressbar_main.setVisibility(View.VISIBLE);
		Toast.makeText(MainActivity.this, "uploading new task", Toast.LENGTH_SHORT).show();

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

		AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth(((EventxtraTestApplication)getApplication()).api_key,((EventxtraTestApplication)getApplication()).api_secret,
				new AuthScope("sheetsu.com", 80, AuthScope.ANY_REALM));
		client.post(MainActivity.this, ((EventxtraTestApplication)getApplication()).api_url, entity, "application/json",  new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				//get data again
				getData();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				super.onSuccess(statusCode, headers, response);
				//get data again
				getData();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				smoothprogressbar_main.setVisibility(View.GONE);
				try {
					Log.d(TAG, " statusCode " + statusCode + " " + errorResponse.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				//start offline Service
				Intent intent = new Intent(MainActivity.this, WaitingToUploadService.class);
				intent.putExtra("string_editTextDateTime", string_editTextDateTime);
				intent.putExtra("string_editTextTask", string_editTextTask);
				intent.putExtra("string_editTextIsFinish", string_editTextIsFinish);
				startService(intent);
				//store data into local database.
				mTodoAdapter.add(new TodoAdapter.SampleItem(string_editTextDateTime, string_editTextTask, string_editTextIsFinish));
				//set adapter
				mTodoAdapter.notifyDataSetChanged();
				//store data into local database.
				long id = mToDoDataDao.insert(new ToDoData(null,string_editTextDateTime, string_editTextTask, string_editTextIsFinish));
			}
		});
	}

	private void reviseData(final String string_oldTask, final String string_editTextDateTime, final String string_editTextTask, final String string_editTextIsFinish){
		//uploadData
		smoothprogressbar_main.setVisibility(View.VISIBLE);
		Toast.makeText(MainActivity.this, "revise tasks", Toast.LENGTH_SHORT).show();

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

		AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth(((EventxtraTestApplication)getApplication()).api_key , ((EventxtraTestApplication)getApplication()).api_secret,
				new AuthScope("sheetsu.com", 80, AuthScope.ANY_REALM));
		client.put(MainActivity.this, ((EventxtraTestApplication)getApplication()).api_url + "/task/"+string_oldTask, entity, "application/json",  new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				//get data again
				getData();
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				super.onSuccess(statusCode, headers, response);
				//get data again
				getData();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				smoothprogressbar_main.setVisibility(View.GONE);
				try {
					Log.d(TAG, " statusCode " + statusCode + " " + errorResponse.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Toast.makeText(MainActivity.this, "no task revised! ", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void deleteData(final String string_oldTask){
		//uploadData
		smoothprogressbar_main.setVisibility(View.VISIBLE);
		Toast.makeText(MainActivity.this, "delete tasks", Toast.LENGTH_SHORT).show();
		AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth(((EventxtraTestApplication)getApplication()).api_key , ((EventxtraTestApplication)getApplication()).api_secret,
				new AuthScope("sheetsu.com", 80, AuthScope.ANY_REALM));
		client.delete(MainActivity.this, ((EventxtraTestApplication)getApplication()).api_url + "/task/"+string_oldTask, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				//get data again
				getData();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				smoothprogressbar_main.setVisibility(View.GONE);
				try {
					Log.d(TAG, " statusCode " + statusCode + " " + errorResponse.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				Toast.makeText(MainActivity.this, "no task delete! ", Toast.LENGTH_LONG).show();
			}
		});
	}
	//action from add button and create a dialog
	private void addDataDialog() {
		final View item = LayoutInflater.from(MainActivity.this).inflate(R.layout.todo_list_dialog, null);
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.add_new_task)
				.setView(item)
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText editTextDateTime = (EditText) item.findViewById(R.id.edit_text_datetimevalue);
						String string_editTextDateTime = editTextDateTime.getText().toString();

						EditText editTextTask = (EditText) item.findViewById(R.id.edit_text_taskvalue);
						String string_editTextTask = editTextTask.getText().toString();

						EditText editTextIsFinish = (EditText) item.findViewById(R.id.edit_text_isfinishvalue);
						String string_editTextIsFinish = editTextIsFinish.getText().toString();

						if (isNetworkAvailable(MainActivity.this)) {
							//online upload
							//update to server
							uploadData(string_editTextDateTime, string_editTextTask, string_editTextIsFinish);
						}else{
							//offline Service
							Intent intent = new Intent(MainActivity.this, WaitingToUploadService.class);
							intent.putExtra("string_editTextDateTime", string_editTextDateTime);
							intent.putExtra("string_editTextTask", string_editTextTask);
							intent.putExtra("string_editTextIsFinish", string_editTextIsFinish);
							startService(intent);
							//store data into local database.
							mTodoAdapter.add(new TodoAdapter.SampleItem(string_editTextDateTime, string_editTextTask, string_editTextIsFinish));
							//set adapter
							mTodoAdapter.notifyDataSetChanged();
							//store data into local database.
							long id = mToDoDataDao.insert(new ToDoData(null,string_editTextDateTime, string_editTextTask, string_editTextIsFinish));
						}
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				})
				.show();
	}

	private void reviseDataDialog(final int position){
		final List<ToDoData> mList = mToDoDataDao.loadAll();
		final View item = LayoutInflater.from(MainActivity.this).inflate(R.layout.todo_list_dialog, null);
		final EditText editTextDateTime = (EditText) item.findViewById(R.id.edit_text_datetimevalue);
		editTextDateTime.setText(mList.get(position).getDatetime());
		final EditText editTextTask = (EditText) item.findViewById(R.id.edit_text_taskvalue);
		editTextTask.setText(mList.get(position).getTask());
		final EditText editTextIsFinish = (EditText) item.findViewById(R.id.edit_text_isfinishvalue);
		editTextIsFinish.setText(mList.get(position).getIsFinish());

		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.revise_task)
				.setView(item)
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String string_editTextDateTime = editTextDateTime.getText().toString();
						String string_editTextTask = editTextTask.getText().toString();
						String string_editTextIsFinish = editTextIsFinish.getText().toString();

						if (isNetworkAvailable(MainActivity.this)) {
							//online upload update to server
							reviseData(mList.get(position).getTask(), string_editTextDateTime, string_editTextTask, string_editTextIsFinish);
						}else{
							Toast.makeText(MainActivity.this, "internet needed thanks",Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				})
				.show();
	}

	private void deleteDataDialog(final int position){
		final List<ToDoData> mList = mToDoDataDao.loadAll();
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.delete_task)
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (isNetworkAvailable(MainActivity.this)) {
							//online upload update to server
							deleteData(mList.get(position).getTask());
						}else{
							Toast.makeText(MainActivity.this, "internet needed thanks",Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				})
				.show();
	}
	// Our handler for received Intents. This will be called whenever an Intent with an action named "data-upload-finished" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			String message = intent.getStringExtra("message");
			Log.d(TAG, "Got message: " + message);
			//get data again
			getData();
		}
	};

	/**
	 * this method is used for customize the swipe list view
	 */
	SwipeMenuCreator creator = new SwipeMenuCreator() {
		@Override
		public void create(SwipeMenu menu) {
			// create "open" item
			SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
			// set item background
			openItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x00, 0x00)));
			// set item width
			openItem.setWidth(dp2px(90));
			// set item title
			openItem.setTitle("Edit");
			// set item title fontsize
			openItem.setTitleSize(18);
			// set item title font color
			openItem.setTitleColor(Color.WHITE);
			// add to menu
			menu.addMenuItem(openItem);

			// create "delete" item
			SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
			// set item background
			deleteItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x00, 0x00)));
			// set item width
			deleteItem.setWidth(dp2px(90));
			// set a icon
			deleteItem.setIcon(android.R.drawable.ic_delete);
			// add to menu
			menu.addMenuItem(deleteItem);
		}
	};
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
}
