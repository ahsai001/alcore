package com.zaitunlabs.zlcore.core;

import android.content.Context;

import com.ahsailabs.alcore.core.AlCoreApplication;
import com.ahsailabs.alcore.events.ReInitializeDatabaseEvent;
import com.ahsailabs.alutils.DebugUtil;
import com.ahsailabs.sqlitewrapper.SQLiteWrapper;
import com.zaitunlabs.zlcore.tables.AppListDataModel;
import com.zaitunlabs.zlcore.tables.AppListModel;
import com.zaitunlabs.zlcore.tables.AppListPagingModel;
import com.zaitunlabs.zlcore.tables.StoreDataModel;
import com.zaitunlabs.zlcore.tables.StoreModel;
import com.zaitunlabs.zlcore.tables.StorePagingModel;

import org.greenrobot.eventbus.Subscribe;

public class BaseApplication extends AlCoreApplication {
	public static final String DATABASE_NAME = "zlcore.db";

	@Override
	public void onCreate() {
		super.onCreate();

		dbInitialize();
	}

	private void dbInitialize(){
		SQLiteWrapper.addDatabase(new SQLiteWrapper.Database() {
			@Override
			public Context getContext() {
				return BaseApplication.this;
			}

			@Override
			public String getDatabaseName() {
				return BaseApplication.DATABASE_NAME;
			}

			@Override
			public int getDatabaseVersion() {
				return 1;
			}

			@Override
			public void configure(SQLiteWrapper sqLiteWrapper) {
				//Store
				sqLiteWrapper.addTable(new SQLiteWrapper.Table(StoreModel.class)
						.addIntField("status")
						.addStringField("message")
						.enableRecordLog());

				sqLiteWrapper.addTable(new SQLiteWrapper.Table(StorePagingModel.class)
						.addIntField("countperpage")
						.addIntField("prev")
						.addIntField("next")
						.addLongField("store_model_id")
						.enableRecordLog()
						.addForeignKey("store_model_id",
								null, StoreModel.class,SQLiteWrapper.ID,
								SQLiteWrapper.ForeignKey.CASCADE, SQLiteWrapper.ForeignKey.CASCADE));

				sqLiteWrapper.addTable(new SQLiteWrapper.Table(StoreDataModel.class)
						.addStringField("image")
						.addStringField("title")
						.addStringField("desc")
						.addStringField("uniq")
						.addStringField("url")
						.addLongField("store_model_id")
						.enableRecordLog()
						.addForeignKey("store_model_id",
								null, StoreModel.class, SQLiteWrapper.ID,
								SQLiteWrapper.ForeignKey.CASCADE, SQLiteWrapper.ForeignKey.CASCADE)
						.addIndex("store_model_id"));


				//AppList
				sqLiteWrapper.addTable(new SQLiteWrapper.Table(AppListModel.class)
						.addIntField("status")
						.addStringField("message")
						.enableRecordLog());

				sqLiteWrapper.addTable(new SQLiteWrapper.Table(AppListPagingModel.class)
						.addIntField("countperpage")
						.addIntField("prev")
						.addIntField("next")
						.addLongField("applist_model_id")
						.enableRecordLog()
						.addForeignKey("applist_model_id",
								null, AppListModel.class,SQLiteWrapper.ID,
								SQLiteWrapper.ForeignKey.CASCADE, SQLiteWrapper.ForeignKey.CASCADE)
						.addIndex("applist_model_id"));

				sqLiteWrapper.addTable(new SQLiteWrapper.Table(AppListDataModel.class)
						.addStringField("image")
						.addStringField("title")
						.addStringField("desc")
						.addStringField("uniq")
						.addStringField("url")
						.addLongField("applist_model_id")
						.enableRecordLog()
						.addForeignKey("applist_model_id",
								null, AppListModel.class, SQLiteWrapper.ID,
								SQLiteWrapper.ForeignKey.CASCADE, SQLiteWrapper.ForeignKey.CASCADE));

			}
		});
	}

	@Subscribe
	public void onEvent(ReInitializeDatabaseEvent event){
		SQLiteWrapper.removeAllDatabase();
		dbInitialize();
	}

	@Override
	public void onLowMemory() {
		DebugUtil.logD("Application", this.getClass().getSimpleName()
				+ ":onLowMemory");
		DebugUtil.logE("LOW_MEMORY", "low memory occured");
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		DebugUtil.logD("Application", this.getClass().getSimpleName()
				+ ":onTerminate");
		SQLiteWrapper.removeAllDatabase();
		super.onTerminate();
	}

}
