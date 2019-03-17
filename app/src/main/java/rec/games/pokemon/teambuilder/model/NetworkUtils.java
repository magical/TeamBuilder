package rec.games.pokemon.teambuilder.model;

import android.util.Log;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rec.games.pokemon.teambuilder.GlobalApplication;

public class NetworkUtils
{
	private static final String TAG = NetworkUtils.class.getSimpleName();

	private static final OkHttpClient mHttpClient;
	private static final Dispatcher mDispatcher;
	private static final PriorityBlockingQueue<PriorityNetworkRequest> networkPriorityQueue;
	private static final Lock flushLock;

	static
	{
		mHttpClient = new OkHttpClient.Builder()
			.addInterceptor(GlobalApplication.getPokeAPILimiter())
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
	public static void doHTTPGet(Request request, Callback callback)
	{
		mHttpClient.newCall(request).enqueue(callback);
	}

	public static void doHTTPGet(String url, Callback callback)
	{
		Request request = new Request.Builder().url(url).build();

		//Log.d(TAG, request.toString());
		doHTTPGet(request, callback);
	}

	//priority: the priority of the request when added to our priority network queue
	//callStart: interface to run some code before the call occurs, if it returns false then we will drop the call
	//callback: callback that gets called when the priority network queue actually performs the request
	public static void doPriorityHTTPGet(String url, NetworkPriority priority, PriorityCallback callback)
	{
		Request request = new Request.Builder().url(url).build();
		PriorityNetworkRequest queueItem = new PriorityNetworkRequest(priority, request, callback);

		networkPriorityQueue.offer(queueItem);
		if(mDispatcher.queuedCallsCount() == 0)
			flushPriorityQueue();
	}

	//due to race conditions, only let one thread flush at a given time
	private static void flushPriorityQueue()
	{
		flushLock.lock();
		if(networkPriorityQueue.isEmpty())
		{
			flushLock.unlock();
			return;
		}

		PriorityNetworkRequest queueItem = networkPriorityQueue.peek();
		NetworkPriority lowestPriority = queueItem.priority;
		while(!networkPriorityQueue.isEmpty()
			&& mDispatcher.queuedCallsCount() <= (2 * mDispatcher.getMaxRequestsPerHost())
			&& queueItem.priority.compareTo(lowestPriority) <= 0)
		{
			if(queueItem.callback.onStart())
				mHttpClient.newCall(queueItem.request).enqueue(queueItem.callback);

			networkPriorityQueue.poll();
			queueItem = networkPriorityQueue.peek();
		}

		flushLock.unlock();
	}
}
