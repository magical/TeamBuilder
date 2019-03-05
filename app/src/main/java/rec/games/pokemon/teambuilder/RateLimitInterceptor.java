package rec.games.pokemon.teambuilder;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class RateLimitInterceptor implements Interceptor
{
	private final int limit;
	private final long timePeriod;
	private PriorityQueue<Long> responseTimeStamps;
	private long queueTail;

	private Lock readLock;
	private Lock writeLock;

	//limit: maximum number of requests to handle over a given time period
	//timePeriod: the time period in nanoseconds
	//so the maximum call rate = limit/timePeriod
	//also a limit <= # of threads in OkHTTP thread pool is a bad idea (i.e <= 5 and <= 64 when multiple hosts are involved)
	//this means that 120 requests/minute is safer than 2 requests/second
	RateLimitInterceptor(int limit, long timePeriod)
	{
		this.limit = limit;
		this.timePeriod = timePeriod;
		responseTimeStamps = new PriorityQueue<>(limit);
		queueTail = Long.MIN_VALUE;

		ReadWriteLock tailReadWriteLock = new ReentrantReadWriteLock();
		readLock = tailReadWriteLock.readLock();
		writeLock = tailReadWriteLock.writeLock();
	}

	//this is a correction algorithm that will process up to maximum api call rate requests over a given timePeriod
	//it does this by keeping track of the last "limit" amount of requests over a given time period
	//if we are firing too fast, then throttle, else fire away
	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException
	{
		Request request = chain.request();

		//time before request is sent
		boolean slept = false;
		readLock.lock();

		//if the queue is full, we may have to throttle
		if(responseTimeStamps.size() >= limit)
		{
			long elapsedTime = getElapsedTime();

			//if we are at or below the maximum api call rate, then elapsedTime >= NANO_SECONDS_IN_MINUTE
			if(elapsedTime < timePeriod)
			{
				//it would be bad if we held onto the lock while we are sleeping (it will cause writes to queue up)
				//since we aren't doing anything after this and we are done reading responseTimeStamps/queueTail, release the lock
				slept = true;
				readLock.unlock();

				long minNanosecondsToThrottle = timePeriod - elapsedTime;
				sleepAtLeast(minNanosecondsToThrottle);
			}
		}

		//if we didn't sleep, then we still have the lock
		if(!slept)
			readLock.unlock();

		//sending request
		Response response = chain.proceed(request);

		//time immediately after the response was received
		long timeStamp = System.nanoTime();

		//we are gonna mess with responseTimeStamps and queueTail
		writeLock.lock();

		//optimization, if we remove any entries that exceed our time window (NANO_SECONDS_IN_MINUTE)
		//then our queue.size() will be more likely to be < limit. And if queue.size() < limit, then we don't need to throttle
		while(!responseTimeStamps.isEmpty() && getSimulatedElapsedTime(timeStamp) > timePeriod)
			responseTimeStamps.poll();

		//lets guarantee that we are only storing the last limit amount of requests
		while(responseTimeStamps.size() >= limit)
			responseTimeStamps.poll();

		responseTimeStamps.offer(timeStamp);
		if(timeStamp > queueTail)
			queueTail = timeStamp;

		writeLock.unlock();

		return response;
	}

	//we at least have a shared lock when we call this
	//we also make sure that the queue.size() >= 2 before calling this
	private long getElapsedTime()
	{
		long head = responseTimeStamps.peek();
		long tail = queueTail;

		return tail - head;
	}

	//we at least have a shared lock when we call this
	//we also make sure that the queue is not empty before calling this
	private long getSimulatedElapsedTime(long tail)
	{
		long head = responseTimeStamps.peek();

		return tail - head;
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
