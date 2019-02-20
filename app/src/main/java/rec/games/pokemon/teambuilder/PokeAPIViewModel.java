package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class PokeAPIViewModel extends ViewModel
{
	private MutableLiveData<String> mPokeListJSON = new MutableLiveData<>();

	MutableLiveData<String> getPokeListJSON()
	{
		return mPokeListJSON;
	}

	void loadPokemonListJSON(String url)
	{
		if(url == null || mPokeListJSON == null || mPokeListJSON.getValue() != null)
			return;

		NetworkUtils.doHTTPGet(url, new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				mPokeListJSON.postValue(null);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body != null)
					mPokeListJSON.postValue(body.string());
			}
		});
	}
}
