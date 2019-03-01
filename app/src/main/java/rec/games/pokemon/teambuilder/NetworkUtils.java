package rec.games.pokemon.teambuilder;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetworkUtils
{
	private static final OkHttpClient mHttpClient = new OkHttpClient.Builder()
		.addInterceptor(new RateLimitInterceptor(100))
		.build();

	/*
	 * When using a ViewModel, background network calls are necessary
	 * OkHttp can do background network calls without needing to setup an explicit background service
	 * This is done through enqueue()
	 * However enqueue needs a Callback, which is an interface for when the call either succeeds or fails
	 * Since this is a generic utility, pass it in and let caller implement what they need
	 */
	static void doHTTPGet(String url, Callback callback)
	{
		Request request = new Request.Builder().url(url).build();

		mHttpClient.newCall(request).enqueue(callback);
	}
}
