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
	private static final Map<Request, Triplet<Integer, OnCallStart, PriorityCallback>> requestMap = Collections.synchronizedMap(new HashMap<Request, Triplet<Integer, OnCallStart, PriorityCallback>>());
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

	static void doPriorityHTTPGet(String url, int priority, OnCallStart callStart, Callback callback)
	{
		Request request = new Request.Builder().url(url).build();
		PriorityCallback priorityCallback = new PriorityCallback(callback);
		Triplet<Integer, OnCallStart, PriorityCallback> requestData = Triplet.create(priority, callStart, priorityCallback);

		priorityCounts.incrementAndGet(priority);
		synchronized(requestMap)
		{
			requestMap.put(request, requestData);
		}

		doHTTPGet(request, priorityCallback);
	}

	public interface OnCallStart
	{
		boolean onStart();
	}
}
