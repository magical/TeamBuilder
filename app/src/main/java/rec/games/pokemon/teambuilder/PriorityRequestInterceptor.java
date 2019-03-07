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

	PriorityRequestInterceptor(Map<Request, Triplet<Integer, NetworkUtils.OnCallStart, PriorityCallback>> requestMap, AtomicIntegerArray priorityCounts)
	{
		this.requestMap = requestMap;
		this.priorityCounts = priorityCounts;
	}

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

		if(requestData == null)
			return chain.proceed(request);

		for(int i = requestData.first - 1; i >= 0; i--)
		{
			if(priorityCounts.get(i) > 0)
			{
				NetworkUtils.doHTTPGet(request, requestData.third);

				return createEmptyResponse(request);

			}
		}

		priorityCounts.decrementAndGet(requestData.first);
		synchronized(requestMap)
		{
			requestMap.remove(request);
		}

		if(!requestData.second.onStart())
			return createEmptyResponse(request);

		return chain.proceed(request);
	}

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
