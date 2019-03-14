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

		Log.d("Hello World", "Initing rate limiter");
		Log.d("Hello World", "default permits: " + defaultPermits);
		Log.d("Hello World", "permits: " + permits);
		Log.d("Hello World", "elapsedSinceClose: " + TimeUnit.NANOSECONDS.toSeconds(currentTime - releaseTimeStamp));
		pokeAPILimiter = new RateLimitInterceptor(defaultPermits, timePeriod, permits);
	}

	public static RateLimitInterceptor getPokeAPILimiter()
	{
		return pokeAPILimiter;
	}

	private void savePokeAPILimiter(boolean commitFlag)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
		SharedPreferences.Editor editor = sharedPref.edit();

		int permits = pokeAPILimiter.currentPermits();
		long releaseTimeStamp = pokeAPILimiter.getReleaseTimeStamp();
		long timeLeftOver = TimeUnit.MINUTES.toSeconds(1) - TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - releaseTimeStamp);
		Log.d("Hello World", "saving permits: " + permits);
		Log.d("Hello World", "timeLeftOver: " + timeLeftOver);

		editor.putInt(getString(R.string.pref_pokeapi_limiter_permits_key), permits);
		editor.putLong(getString(R.string.pref_pokeapi_limiter_release_timestamp_key), releaseTimeStamp);

		if(commitFlag)
			editor.commit();
		else
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

	}

	@Override
	public void onActivityPaused(Activity activity)
	{
		Log.d("Hello World", "Application was paused");
		savePokeAPILimiter(false);

	}

	@Override
	public void onActivityStopped(Activity activity)
	{
		Log.d("Hello World", "Application was stopped");
		savePokeAPILimiter(false);
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState)
	{

	}

	@Override
	public void onActivityDestroyed(Activity activity)
	{
		Log.d("Hello World", "Application was destroyed");
		savePokeAPILimiter(true);
	}
}
