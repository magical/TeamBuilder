package rec.games.pokemon.teambuilder.model;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Response;

class RateLimitInterceptor implements Interceptor
{
	private Semaphore limiter;

	//limit: maximum number of requests to handle over a given time period
	//timePeriod: the time period in nanoseconds
	//so the maximum call rate = limit/timePeriod
	RateLimitInterceptor(final int limit, final long timePeriod)
	{
		limiter = new Semaphore(limit);
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				sleepAtLeast(timePeriod);

				limiter.drainPermits();
				limiter.release(limit);
			}
		};

		thread.start();
	}

	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException
	{
		limiter.acquireUninterruptibly();

		return chain.proceed(chain.request());
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
}
