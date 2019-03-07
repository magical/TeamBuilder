package rec.games.pokemon.teambuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

//This class the defines the behavior of our callbacks for our priority network calls
//if the network call actually gets processed, then call the real callback
public class PriorityCallback implements Callback
{
	private Callback realCallback;

	public PriorityCallback(Callback callback)
	{
		this.realCallback = callback;
	}

	@EverythingIsNonNull
	@Override
	public void onFailure(Call call, IOException e)
	{
		realCallback.onFailure(call, e);
	}

	@EverythingIsNonNull
	@Override
	public void onResponse(Call call, Response response) throws IOException
	{
		//if response was not PriorityRequestInterceptors emptyResponse, then it actually finished so call the real callback
		//if we don't do this then the callback that they passed in will fire every time we re-queue the request
		if(response.code() != 418)
			realCallback.onResponse(call, response);
	}
}
