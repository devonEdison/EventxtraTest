package com.eventxtra.test.database;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by myuser on 2017/1/19.
 */
@Entity
public class ToDoData {
	@Id(autoincrement = true)
	private Long id;

	@NonNull
	private String datetime;
	private String task;
	private String isFinish;

	@Generated(hash = 1149299407)
	public ToDoData(Long id, @NonNull String datetime, String task,
					String isFinish) {
		this.id = id;
		this.datetime = datetime;
		this.task = task;
		this.isFinish = isFinish;
	}

	@Generated(hash = 871631935)
	public ToDoData() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDatetime() {
		return this.datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public String getTask() {
		return this.task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getIsFinish() {
		return this.isFinish;
	}

	public void setIsFinish(String isFinish) {
		this.isFinish = isFinish;
	}
}
