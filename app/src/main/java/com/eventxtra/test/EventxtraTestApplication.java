package com.eventxtra.test;

import android.app.Application;

import com.eventxtra.test.database.DaoMaster;
import com.eventxtra.test.database.DaoSession;
import com.eventxtra.test.database.ToDoDataDao;

import org.greenrobot.greendao.database.Database;

/**
 * Created by myuser on 2017/1/19.
 */

public class EventxtraTestApplication extends Application {
	public DaoSession daoSession;
	public static final String api_url = "https://sheetsu.com/apis/v1.0/91d8c1cbe904";
	public static final String api_key = "5eJU2HcQGXVyXwJxGQi6";
	public static final String api_secret = "Pf1YjxsVihzQpcpKpoy5ykPzNUCbquaxxA5jxyn7";
	@Override
	public void onCreate() {
		super.onCreate();
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "todo-db");
		Database db = helper.getWritableDb();
		daoSession = new DaoMaster(db).newSession();
	}

	public ToDoDataDao getToDoDataDao(){
		return daoSession.getToDoDataDao();
	}
}
