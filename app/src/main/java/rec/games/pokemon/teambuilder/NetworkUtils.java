package rec.games.pokemon.teambuilder;

import org.javatuples.Quartet;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkUtils
{
	private static final OkHttpClient mHttpClient;
	private static final Dispatcher mDispatcher;
	private static final PriorityBlockingQueue<Quartet<NetworkPriority, Request, OnCallStart, Callback>> networkPriorityQueue;
	private static final Lock flushLock;

	static
	{
		mHttpClient = new OkHttpClient.Builder()
			.addInterceptor(new RateLimitInterceptor(100, TimeUnit.MINUTES.toNanos(1)))
			.build();

		networkPriorityQueue = new PriorityBlockingQueue<>();
		flushLock = new ReentrantLock();

		mDispatcher = mHttpClient.dispatcher();
		mDispatcher.setIdleCallback(new Runnable()
		{
			@Override
			public void run()
			{
				flushPriorityQueue();
			}
		});
	}

	/*
	 * When using a ViewModel, background network calls are necessary
	 * OkHttp can do background network calls without needing to setup an explicit background service
	 * This is done through enqueue()
	 * However enqueue needs a Callback, which is an interface for when the call either succeeds or fails
	 * Since this is a generic utility, pass it in and let caller implement what they need
	 */
	static void doHTTPGet(Request request, Callback callback)
	{
		mHttpClient.newCall(request).enqueue(callback);
	}

	static void doHTTPGet(String url, Callback callback)
	{
		Request request = new Request.Builder().url(url).build();

		doHTTPGet(request, callback);
	}

	public interface OnCallStart
	{
		boolean onStart();
	}

	enum NetworkPriority
	{
		CRITICAL,
		USER_INTERACTION,
		USER_IMPORTANT,
		ABOVE_NORMAL,
		NORMAL,
		LOW
	}

	//priority: the priority of the request when added to our priority network queue
	//callStart: interface to run some code before the call occurs, if it returns false then we will drop the call
	//callback: callback that gets called when the priority network queue actually performs the request
	static void doPriorityHTTPGet(String url, NetworkPriority priority, OnCallStart callStart, Callback callback)
	{
		//TODO: what if they give us a bad priority?

		Request request = new Request.Builder().url(url).build();
		Quartet<NetworkPriority, Request, OnCallStart, Callback> queueItem = new Quartet<>(priority, request, callStart, callback);

		networkPriorityQueue.offer(queueItem);
		if(mDispatcher.queuedCallsCount() == 0)
			flushPriorityQueue();
	}

	private static void flushPriorityQueue()
	{
		flushLock.lock();
		if(networkPriorityQueue.isEmpty())
			return;

		Quartet<NetworkPriority, Request, OnCallStart, Callback> queueItem = networkPriorityQueue.peek();
		NetworkPriority lowestPriority = queueItem.getValue0();
		while(!networkPriorityQueue.isEmpty() && queueItem.getValue0().compareTo(lowestPriority) <= 0)
		{
			if(queueItem.getValue2().onStart())
				mHttpClient.newCall(queueItem.getValue1()).enqueue(queueItem.getValue3());

			networkPriorityQueue.poll();
			queueItem = networkPriorityQueue.peek();
		}

		flushLock.unlock();
	}
}
