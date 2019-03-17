package rec.games.pokemon.teambuilder.model;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RateLimitInterceptor implements Interceptor
{
	private Semaphore limiter;
	private long releaseTimeStamp;

	private boolean isPaused;
	private Lock pauseLock;
	private Condition pauseCondition;

	//limit: maximum number of requests to handle over a given time period
	//timePeriod: the time period in nanoseconds
	//so the maximum call rate = limit/timePeriod
	public RateLimitInterceptor(final int limit, final long timePeriod, int startingPermits)
	{
		limiter = new Semaphore(startingPermits);
		releaseTimeStamp = System.nanoTime();

		isPaused = false;
		pauseLock = new ReentrantLock();
		pauseCondition = pauseLock.newCondition();

		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				sleepAtLeast(timePeriod);

				limiter.drainPermits();
				limiter.release(limit);
				releaseTimeStamp = System.nanoTime();
			}
		};
		thread.start();
	}

	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException
	{
		pauseLock.lock();
		while(isPaused)
			pauseCondition.awaitUninterruptibly();
		pauseLock.unlock();

		limiter.acquireUninterruptibly();

		Request request = chain.request();
		Log.d(RateLimitInterceptor.class.getSimpleName(), "processing: " + request.url());
		Response response = chain.proceed(request);
		if(!response.isSuccessful())
			Log.d(RateLimitInterceptor.class.getSimpleName(), "error code: " + response.code() + ", request: " + request.url());

		return response;
	}

	public void pause()
	{
		isPaused = true;
	}

	public void unPause()
	{
		pauseLock.lock();
		isPaused = false;
		pauseCondition.signal();
		pauseLock.unlock();
	}

	//java makes no guarantees about how long a thread will sleep
	//if we request 1 second, it could actually sleep for 0.9 seconds or 1.1 seconds
	//lets make sure that we at least sleep for the time that we specified
	private void sleepAtLeast(long nanoseconds)
	{
		long start = System.nanoTime();
		long timeLeft = nanoseconds;

		while(timeLeft > 0)
		{
			try
			{
				TimeUnit.NANOSECONDS.sleep(timeLeft);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				long elapsedTime = System.nanoTime() - start;
				timeLeft = nanoseconds - elapsedTime;
			}
		}
	}

	public int currentPermits()
	{
		return limiter.availablePermits();
	}

	public long getReleaseTimeStamp()
	{
		return releaseTimeStamp;
	}
}
