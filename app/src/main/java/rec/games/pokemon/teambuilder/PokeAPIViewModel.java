package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;

class PokeAPIViewModel extends ViewModel
{
	private MutableLiveData<HashMap<Integer, LiveData<PokemonType>>> typeCache;
	private MutableLiveData<HashMap<Integer, LiveData<PokemonMove>>> moveCache;
	private MutableLiveData<HashMap<Integer, LiveData<Pokemon>>> pokemonCache;

	PokeAPIViewModel()
	{
		typeCache = new MutableLiveData<>();
		moveCache = new MutableLiveData<>();
		pokemonCache = new MutableLiveData<>();

		getNewPokemonList();
	}

	public void getNewPokemonList(){
		//asynchronously load the type list
		NetworkUtils.doHTTPGet(PokeAPIUtils.buildTypeListURL(10000, 0), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				typeCache.postValue(null);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					typeCache.postValue(null);
					return;
				}

				//parse the json
				String typeListJSON = body.string();
				PokeAPIUtils.NamedAPIResourceList typeList = PokeAPIUtils.parseNamedAPIResourceListJSON(typeListJSON);

				//create a temporary variable, we don't want to post it until we are done processing
				HashMap<Integer, LiveData<PokemonType>> tempTypeCache = new HashMap<>();
				for(PokeAPIUtils.NamedAPIResource r: typeList.results)
				{
					MutableLiveData<PokemonType> pokemonType = new MutableLiveData<>();

					int id = PokeAPIUtils.getId(r.url);
					pokemonType.postValue(new DeferredPokemonTypeResource(id, r.name, r.url));

					tempTypeCache.put(id, pokemonType);
				}

				//now we can finally post the value
				typeCache.postValue(tempTypeCache);
			}
		});

		//asynchronously load the move list
		NetworkUtils.doHTTPGet(PokeAPIUtils.buildMoveListURL(10000, 0), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				moveCache.postValue(null);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					moveCache.postValue(null);
					return;
				}

				String moveListJSON = body.string();
				PokeAPIUtils.NamedAPIResourceList moveList = PokeAPIUtils.parseNamedAPIResourceListJSON(moveListJSON);

				HashMap<Integer, LiveData<PokemonMove>> tempMoveCache = new HashMap<>();
				for(PokeAPIUtils.NamedAPIResource r: moveList.results)
				{
					MutableLiveData<PokemonMove> pokemonMove = new MutableLiveData<>();

					int id = PokeAPIUtils.getId(r.url);
					pokemonMove.postValue(new DeferredPokemonMoveResource(id, r.name, r.url));

					tempMoveCache.put(id, pokemonMove);
				}

				moveCache.postValue(tempMoveCache);
			}
		});

		//asynchronously load the pokemon list
		NetworkUtils.doHTTPGet(PokeAPIUtils.buildPokemonListURL(10000, 0), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				pokemonCache.postValue(null);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					pokemonCache.postValue(null);
					return;
				}

				String pokemonListJSON = body.string();
				PokeAPIUtils.NamedAPIResourceList pokemonList = PokeAPIUtils.parseNamedAPIResourceListJSON(pokemonListJSON);

				HashMap<Integer, LiveData<Pokemon>> tempPokemonCache = new HashMap<>();
				for(PokeAPIUtils.NamedAPIResource r: pokemonList.results)
				{
					MutableLiveData<Pokemon> pokemon = new MutableLiveData<>();

					int id = PokeAPIUtils.getId(r.url);
					pokemon.postValue(new DeferredPokemonResource(id, r.name, r.url));

					tempPokemonCache.put(id, pokemon);
				}

				pokemonCache.postValue(tempPokemonCache);
			}
		});
	}

	LiveData<HashMap<Integer, LiveData<PokemonType>>> getTypeCache()
	{
		return typeCache;
	}

	LiveData<HashMap<Integer, LiveData<PokemonMove>>> getMoveCache()
	{
		return moveCache;
	}

	LiveData<HashMap<Integer, LiveData<Pokemon>>> getPokemonCache()
	{
		return pokemonCache;
	}

	//TODO: later we should refactor away List<Pokemon>, just doing the minimum amount to get the application to compile/run
	List<Pokemon> extractPokemonListFromCache()
	{
		if(pokemonCache.getValue() == null)
			return null;

		Collection<LiveData<Pokemon>> pokemonReferences = pokemonCache.getValue().values();
		List<Pokemon> pokemonList = new ArrayList<>(pokemonReferences.size());

		for(LiveData<Pokemon> pokemonReference: pokemonReferences)
			pokemonList.add(pokemonReference.getValue());

		return pokemonList;
	}
}
