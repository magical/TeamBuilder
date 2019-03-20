package rec.games.pokemon.teambuilder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rec.games.pokemon.teambuilder.model.RateLimitInterceptor;

public class GlobalApplication extends Application implements Application.ActivityLifecycleCallbacks
{
	private static Context appContext;
	private static RateLimitInterceptor pokeAPILimiter;

	@Override
	public void onCreate()
	{
		super.onCreate();
		registerActivityLifecycleCallbacks(this);

		appContext = getApplicationContext();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
		int defaultPermits = Integer.valueOf(getString(R.string.pref_pokeapi_limiter_permits_default));
		int permits = sharedPref.getInt(getString(R.string.pref_pokeapi_limiter_permits_key), defaultPermits);

		long currentTime = System.nanoTime();
		long releaseTimeStamp = sharedPref.getLong(getString(R.string.pref_pokeapi_limiter_release_timestamp_key), currentTime);

		long timePeriod = TimeUnit.MINUTES.toNanos(1);
		if((currentTime - releaseTimeStamp) >= timePeriod)
			permits = defaultPermits;

		Log.d(GlobalApplication.class.getSimpleName(), "Starting permits: " + permits);
		pokeAPILimiter = new RateLimitInterceptor(defaultPermits, timePeriod, permits);
	}

	public static RateLimitInterceptor getPokeAPILimiter()
	{
		return pokeAPILimiter;
	}

	private void savePokeAPILimiter()
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();

		int permits = pokeAPILimiter.currentPermits();
		long releaseTimeStamp = pokeAPILimiter.getReleaseTimeStamp();

		editor.putInt(getString(R.string.pref_pokeapi_limiter_permits_key), permits);
		editor.putLong(getString(R.string.pref_pokeapi_limiter_release_timestamp_key), releaseTimeStamp);

		editor.apply();
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState)
	{

	}

	@Override
	public void onActivityStarted(Activity activity)
	{

	}

	@Override
	public void onActivityResumed(Activity activity)
	{
		//warning but Resumed can get called from either Started or Paused
		pokeAPILimiter.unPause();
	}

	@Override
	public void onActivityPaused(Activity activity)
	{
		pokeAPILimiter.pause();
		savePokeAPILimiter();

	}

	@Override
	public void onActivityStopped(Activity activity)
	{

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState)
	{

	}

	@Override
	public void onActivityDestroyed(Activity activity)
	{

	}
}
