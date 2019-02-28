package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class PokeAPIViewModel extends ViewModel
{
	private static final String TAG = PokeAPIViewModel.class.getSimpleName();

	private MutableLiveData<String> mPokeListJSON;
	private MutableLiveData<Status> mLoadingStatus;

	public PokeAPIViewModel(){
		mPokeListJSON = new MutableLiveData<>();
		mLoadingStatus = new MutableLiveData<>();
		mLoadingStatus.setValue(Status.LOADING);
	}

	MutableLiveData<String> getPokeListJSON()
	{
		if (mPokeListJSON != null)
			mLoadingStatus.setValue(Status.SUCCESS);
		else
			mLoadingStatus.setValue(Status.ERROR);
		return mPokeListJSON;
	}

	public MutableLiveData<Status> getLoadingStatus()
	{
		return mLoadingStatus;
	}

	void loadPokemonListJSON(String url)
	{
		if(url == null || mPokeListJSON == null || mPokeListJSON.getValue() != null)
			return;

		mLoadingStatus.setValue(Status.LOADING);
		Log.d(this.getClass().getName(), "fetching JSON from pokeapi");
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
		Log.d(TAG, "HERE");
		/*
		if (mPokeListJSON != null)
			mLoadingStatus.setValue(Status.SUCCESS);
		else
			mLoadingStatus.setValue(Status.ERROR);
			*/
	}
}
