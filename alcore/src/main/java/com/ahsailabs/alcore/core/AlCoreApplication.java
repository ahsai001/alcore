package com.ahsailabs.alcore.core;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.ahsailabs.alcore.R;
import com.ahsailabs.alcore.constants.AlCoreConstanta;
import com.ahsailabs.alcore.tables.InformationModel;
import com.ahsailabs.alcore.utils.ApplicationWacther;
import com.ahsailabs.alcore.utils.audio.BackSoundService;
import com.ahsailabs.alutils.CommonUtil;
import com.ahsailabs.alutils.DebugUtil;
import com.ahsailabs.alutils.EventsUtil;
import com.ahsailabs.alutils.PlayServiceUtil;
import com.ahsailabs.sqlitewrapper.Lookup;
import com.ahsailabs.sqlitewrapper.SQLiteWrapper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.ahsailabs.alcore.events.ReInitializeDatabaseEvent;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.sender.ReportSenderFactory;
import org.greenrobot.eventbus.Subscribe;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public class AlCoreApplication extends Application {
	public static final String DATABASE_NAME = "zlcore.db";
	public static final String TLSV_MIN = "TLSv1.2";
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		DebugUtil.logD("Application", this.getClass().getSimpleName() + ":onCreate");
		// inisialisasi untuk crash done engine
		if(!PlayServiceUtil.isGooglePlayServicesAvailable(this)) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(this);
			configurationBuilder.setReportSenderFactoryClasses(CustomACRASenderFactory.class);
			configurationBuilder.setMailTo(AlCoreConstanta.getCrashMailTo(this));
			configurationBuilder.setResToastText(R.string.zlcore_crash_toast_text);
			configurationBuilder.setResDialogText(R.string.zlcore_crash_dialog_text);
			configurationBuilder.setReportingInteractionMode(ReportingInteractionMode.NOTIFICATION);
			ACRA.init(this, configurationBuilder);
		} else {
			try {
				// Google Play will install latest OpenSSL
				ProviderInstaller.installIfNeeded(getApplicationContext());
				SSLContext sslContext;
				sslContext = SSLContext.getInstance(TLSV_MIN);
				sslContext.init(null, null, null);
				sslContext.createSSLEngine();
			} catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
					| NoSuchAlgorithmException | KeyManagementException e) {
				e.printStackTrace();
			}
		}

		ApplicationWacther.initialize(this).registerAppWatcherListener(this,
				new ApplicationWacther.AppWatcherListener() {
					@Override
					public void appVisible(boolean visible) {
						if (visible) {
							BackSoundService.resumeBackSound(AlCoreApplication.this);
						} else {
							BackSoundService.pauseBackSound(AlCoreApplication.this);
						}
					}

					@Override
					public void noActivityExistInApp() {
						BackSoundService.stopBackSound(AlCoreApplication.this);
					}

					@Override
					public void connectivityChanged(boolean isOnline) {
					}
				});

		dbInitialize();

		Lookup.init(this, true);

		EventsUtil.register(this);


	}

	private void dbInitialize(){
		SQLiteWrapper.addDatabase(new SQLiteWrapper.Database() {
			@Override
			public Context getContext() {
				return AlCoreApplication.this;
			}

			@Override
			public String getDatabaseName() {
				return AlCoreApplication.DATABASE_NAME;
			}

			@Override
			public int getDatabaseVersion() {
				return 1;
			}

			@Override
			public void configure(SQLiteWrapper sqLiteWrapper) {
				//infomation
				sqLiteWrapper.addTable(new SQLiteWrapper.Table(InformationModel.class)
						.addStringField("title")
						.addStringField("body")
						.addBooleanField("read")
						.addIntField("type")
						.addStringField("photoUrl")
						.addStringField("infoUrl")
						.enableRecordLog()
						.addIndex("read"));

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
		
		if (BackSoundService.isRunning()) {
			BackSoundService.stopBackSound(AlCoreApplication.this);
		}
		ApplicationWacther.getInstance(this).unregisterAppWatcherListener(this);

		EventsUtil.unregister(this);

		SQLiteWrapper.removeAllDatabase();
		super.onTerminate();
	}



	private class CustomACRASender implements ReportSender {
		@Override
		public void send(Context context, CrashReportData report) throws ReportSenderException {
			CommonUtil.sendEmail(AlCoreApplication.this, AlCoreConstanta.getCrashMailTo(context), AlCoreApplication.this.getPackageName()+" Crash Report", report.toString(), "An error has occurred! Send an error done?");
		}
	}

	private class CustomACRASenderFactory implements ReportSenderFactory {

		public CustomACRASenderFactory(){

		}

		@Override
		public ReportSender create(Context context, ACRAConfiguration config) {
			return new CustomACRASender();
		}
	}

}
