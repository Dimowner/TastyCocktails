/*
 * Copyright 2017 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package task.softermii.tastycocktails;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import task.softermii.tastycocktails.dagger.application.AppComponent;
import task.softermii.tastycocktails.dagger.application.AppModule;
import task.softermii.tastycocktails.dagger.application.DaggerAppComponent;
import task.softermii.tastycocktails.util.AndroidUtils;
import timber.log.Timber;

/**
 * Created on 25.07.2017.
 * @author Dimowner
 */
public class TCApplication extends Application {

	final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private NetworkStateChangeReceiver networkStateChangeReceiver;
	private static boolean isConnectedToNetwork = false;

	public static boolean isConnected() {
		return isConnectedToNetwork;
	}

	// dagger2 appComponent
	@SuppressWarnings("NullableProblems")
	@NonNull
	private AppComponent appComponent;

	@Override
	public void onCreate() {
		super.onCreate();

		appComponent = prepareAppComponent().build();

		if (BuildConfig.DEBUG) {
			//Timber initialization
			Timber.plant(new Timber.DebugTree() {
				@Override
				protected String createStackElementTag(StackTraceElement element) {
					return super.createStackElementTag(element) + ":" + element.getLineNumber();
				}
			});
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONNECTIVITY_ACTION);
		networkStateChangeReceiver = new NetworkStateChangeReceiver();
		registerReceiver(networkStateChangeReceiver, intentFilter);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterReceiver(networkStateChangeReceiver);
	}

	@NonNull
	public static TCApplication get(@NonNull Context context) {
		return (TCApplication) context.getApplicationContext();
	}

	@NonNull
	private DaggerAppComponent.Builder prepareAppComponent() {
		return DaggerAppComponent.builder()
				.appModule(new AppModule(this));
	}

	@NonNull
	public AppComponent applicationComponent() {
		return appComponent;
	}


	private class NetworkStateChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String actionOfIntent = intent.getAction();
			if(actionOfIntent.equals(CONNECTIVITY_ACTION)){
				if(AndroidUtils.isConnectedToNetwork(context)){
					Timber.d("network state changed - Connected");
					isConnectedToNetwork = true;
				} else {
					Timber.d("network state changed - Disconnected");
					isConnectedToNetwork = false;
				}
			}
		}
	}
}
