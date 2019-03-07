package rec.games.pokemon.teambuilder;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

class PriorityRequestInterceptor implements Interceptor
{
	private final Map<Request, Triplet<Integer, NetworkUtils.OnCallStart, PriorityCallback>> requestMap;
	private final AtomicIntegerArray priorityCounts;

	//requestMap: extra request data that is needed for our network priority queue. Needs to be a synchronized map
	//priorityCounts: the counts of the given priorities, used to in our re-queueing logic
	PriorityRequestInterceptor(Map<Request, Triplet<Integer, NetworkUtils.OnCallStart, PriorityCallback>> requestMap, AtomicIntegerArray priorityCounts)
	{
		this.requestMap = requestMap;
		this.priorityCounts = priorityCounts;
	}

	//interceptor to handle priority network queueing
	//if it is a regular old request, let it through
	//if it is a priority request, prioritize it
	//it accomplishes this by re-enqueueing our request with the priority callback if it is not the lowest priority in our priority queue
	//the priority callback is aware of this and will only call its true callback when the request was actually processed by chain.proceed()
	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException
	{
		Request request = chain.request();
		Triplet<Integer, NetworkUtils.OnCallStart, PriorityCallback> requestData;

		synchronized(requestMap)
		{
			requestData = requestMap.get(request);
		}

		//if not a priority request, let it through
		if(requestData == null)
			return chain.proceed(request);

		//if there are any requests with a lower priority, then re-enqueue
		//process lower priorities last to be safe, in case they add something while we are checking these
		//it is possible that if this is a higher-priority request and a lower priority is added after we checked it, then this could slip through
		//ex: we are a 5, we check 4, we then check 3, while checking 3, a 4 comes in
		//this is a rare anomaly and is probably fine to let it through
		for(int i = requestData.first - 1; i >= 0; i--)
		{
			if(priorityCounts.get(i) > 0)
			{
				//re-enqueue, the request is already in the data structures so no need to mess with those
				NetworkUtils.doHTTPGet(request, requestData.third);

				return createEmptyResponse(request);
			}
		}

		//this request has the lowest priority, we will process it so remove it from the data structures
		priorityCounts.decrementAndGet(requestData.first);
		synchronized(requestMap)
		{
			requestMap.remove(request);
		}

		//run the OnCallStart callback
		//if the state of the application changed and they want to cancel (without firing the callback they passed in), we will let them do so
		if(!requestData.second.onStart())
			return createEmptyResponse(request);

		return chain.proceed(request);
	}

	//create an empty okhttp response, this is mainly to please okhttp's insane error checking
	//if any of these fields are not set then we could get exceptions
	//this is used to detect if we re-queued a request
	//okhttp will always fire a callback when this interceptor returns
	//but our PriorityCallback will only fire the real callback when this request was actually processed (and not re-queued)
	private static Response createEmptyResponse(Request request)
	{
		return new Response.Builder()
			.body(ResponseBody.create(MediaType.get("text/html; charset=utf-8"), ""))
			.code(418)
			.message("")
			.protocol(Protocol.HTTP_2)
			.request(request)
			.build();
	}
}
