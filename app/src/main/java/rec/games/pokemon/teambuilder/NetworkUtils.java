package rec.games.pokemon.teambuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkUtils
{
	//data store for the requests that enter our "priority" network queue
	private static final Map<Request, Triplet<Integer, OnCallStart, PriorityCallback>> requestMap = Collections.synchronizedMap(new HashMap<Request, Triplet<Integer, OnCallStart, PriorityCallback>>());
	//request counts for the priorities in our priority network queue
	//TODO: lets define the maximum priority somewhere in a variable
	private static final AtomicIntegerArray priorityCounts = new AtomicIntegerArray(5);

	private static final OkHttpClient mHttpClient = new OkHttpClient.Builder()
		.addInterceptor(new PriorityRequestInterceptor(requestMap, priorityCounts))
		.addInterceptor(new RateLimitInterceptor(100, TimeUnit.MINUTES.toNanos(1)))
		.build();

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

	//priority: the priority of the request when added to our priority network queue
	//callStart: interface to run some code before the call occurs, if it returns false then we will drop the call
	//callback: callback that gets called when the priority network queue actually performs the request
	static void doPriorityHTTPGet(String url, int priority, OnCallStart callStart, Callback callback)
	{
		//TODO: what if they give us a bad priority?

		Request request = new Request.Builder().url(url).build();
		PriorityCallback priorityCallback = new PriorityCallback(callback);
		Triplet<Integer, OnCallStart, PriorityCallback> requestData = Triplet.create(priority, callStart, priorityCallback);

		//update the priority network queue's data structures
		//make sure the priority counts are updated as soon as possible, look at PriorityRequestInterceptor as to why
		priorityCounts.incrementAndGet(priority);
		synchronized(requestMap)
		{
			requestMap.put(request, requestData);
		}

		//the priority network queue and the regular network calls use the same interface and okhttp dispatcher
		//but if it is a priority network call, then we will treat it a wee bit differently
		doHTTPGet(request, priorityCallback);
	}

	public interface OnCallStart
	{
		boolean onStart();
	}
}
