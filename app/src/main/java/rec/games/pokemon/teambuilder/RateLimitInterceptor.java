package rec.games.pokemon.teambuilder;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class RateLimitInterceptor implements Interceptor
{
	private final int period;
	private Deque<Long> responseTimeStamps;

	private final static long NANO_SECONDS_IN_MINUTE = TimeUnit.MINUTES.toNanos(1);

	RateLimitInterceptor(int period)
	{
		this.period = period;
		responseTimeStamps = new ArrayDeque<>(period);
	}

	//this is a correction algorithm that will process up to maximum api call rate requests per minute
	//it does this by keeping track of the last "period" amount of requests (where the period = maximum api calls per minute)
	//if we are firing too fast, then throttle, else fire away
	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException
	{
		Request request = chain.request();

		//time before request is sent

		//if the queue is full, we may have to throttle
		if(responseTimeStamps.size() >= period)
		{
			long elapsedTime = getElapsedTime();

			//if we are at or below the maximum api call rate, then elapsedTime >= NANO_SECONDS_IN_MINUTE
			if(elapsedTime < NANO_SECONDS_IN_MINUTE)
			{
				try
				{
					long minNanosecondsToThrottle = NANO_SECONDS_IN_MINUTE - elapsedTime;
					sleepAtLeast(minNanosecondsToThrottle);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//sending request
		Response response = chain.proceed(request);

		//time immediately after the response was received
		long newTail = System.nanoTime();

		//optimization, if we remove any entries that exceed our time window (NANO_SECONDS_IN_MINUTE)
		//then our queue.size() will be more likely to be < period. And if queue.size() < period, then we don't need to throttle
		while(responseTimeStamps.size() >= 1 && getSimulatedElapsedTime(newTail) > NANO_SECONDS_IN_MINUTE)
			responseTimeStamps.pollFirst();

		//lets guarantee that we are only storing the last period amount of requests
		//while # of requests >= period then remove head
		while(responseTimeStamps.size() >= period)
			responseTimeStamps.pollFirst();
		responseTimeStamps.offerLast(newTail);

		return response;
	}

	//here we are assuming that their are at least two items in the queue
	private long getElapsedTime()
	{
		long head = responseTimeStamps.peekFirst();
		long tail = responseTimeStamps.peekLast();

		return tail - head;
	}

	//here we are assuming that their are at least one item in the queue
	private long getSimulatedElapsedTime(long tail)
	{
		long head = responseTimeStamps.peekFirst();

		return tail - head;
	}

	//java makes no guarantees about how long a thread will sleep
	//if we request 1 second, it could actually sleep for 0.9 seconds or 1.1 seconds
	//lets make sure that we at least sleep for the time that we specified
	private void sleepAtLeast(long nanoseconds) throws InterruptedException
	{
		long start = System.nanoTime();
		long timeLeft = nanoseconds;

		while(timeLeft > 0)
		{
			TimeUnit.NANOSECONDS.sleep(timeLeft);

			long elapsedTime = System.nanoTime() - start;
			timeLeft = nanoseconds - elapsedTime;
		}
	}
}
