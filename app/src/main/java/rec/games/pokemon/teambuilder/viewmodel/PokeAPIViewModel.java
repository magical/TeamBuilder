package rec.games.pokemon.teambuilder.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;
import rec.games.pokemon.teambuilder.model.DeferredPokemonMoveResource;
import rec.games.pokemon.teambuilder.model.DeferredPokemonResource;
import rec.games.pokemon.teambuilder.model.DeferredPokemonTypeResource;
import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.NetworkUtils;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonMove;
import rec.games.pokemon.teambuilder.model.PokemonMoveResource;
import rec.games.pokemon.teambuilder.model.PokemonResource;
import rec.games.pokemon.teambuilder.model.PokemonType;
import rec.games.pokemon.teambuilder.model.PokemonTypeResource;

public class PokeAPIViewModel extends ViewModel
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

	public void getNewPokemonList()
	{
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
				HashMap<Integer, LiveData<PokemonType>> tempTypeCache = new HashMap<>(typeList.results.length);
				for(PokeAPIUtils.NamedAPIResource r : typeList.results)
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

				HashMap<Integer, LiveData<PokemonMove>> tempMoveCache = new HashMap<>(moveList.results.length);
				for(PokeAPIUtils.NamedAPIResource r : moveList.results)
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

				HashMap<Integer, LiveData<Pokemon>> tempPokemonCache = new HashMap<>(pokemonList.results.length);
				for(PokeAPIUtils.NamedAPIResource r : pokemonList.results)
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

	public LiveDataList<Pokemon> extractPokemonListFromCache()
	{
		if(pokemonCache.getValue() == null)
			return null;

		Collection<LiveData<Pokemon>> pokemonReferences = pokemonCache.getValue().values();
		return new LiveDataList<>(pokemonReferences);
	}

	public LiveData<HashMap<Integer, LiveData<Pokemon>>> getPokemonCache()
	{
		return pokemonCache;
	}

	public LiveData<PokemonType> getTypeReferenceFromCache(int id)
	{
		if(typeCache.getValue() == null)
			return null;

		return typeCache.getValue().get(id);
	}

	public LiveData<PokemonMove> getMoveReferenceFromCache(int id)
	{
		if(moveCache.getValue() == null)
			return null;

		return moveCache.getValue().get(id);
	}

	public LiveData<Pokemon> getPokemonReferenceFromCache(int id)
	{
		if(pokemonCache.getValue() == null)
			return null;

		return pokemonCache.getValue().get(id);
	}

	/*
	 * return codes:
	 * 0 - operation has been queued
	 * 1 - typeCache hasn't finished loading yet
	 * 2 - could not find LiveData reference, a bad id
	 * 3 - LiveData is not Deferred. Hypothetically LiveData.getValue() could return null, but that would be a bug in the constructor
	 */
	public int loadType(int id)
	{
		if(typeCache.getValue() == null)
			return 1;

		final MutableLiveData<PokemonType> pokemonTypeReference = (MutableLiveData<PokemonType>) typeCache.getValue().get(id);
		if(pokemonTypeReference == null)
			return 2;

		PokemonType pokemonType = pokemonTypeReference.getValue();
		if(pokemonType == null || !pokemonType.isDeferred())
			return 3;

		final DeferredPokemonTypeResource deferredPokemonType = (DeferredPokemonTypeResource) pokemonType;
		NetworkUtils.doHTTPGet(deferredPokemonType.getUrl(), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request type at url: " + deferredPokemonType.getUrl());
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					Log.d(PokeAPIViewModel.class.getSimpleName(), "Body was null");
					return;
				}

				String pokemonTypeJSON = body.string();
				PokeAPIUtils.Type typeData = PokeAPIUtils.parseTypeJSON(pokemonTypeJSON);

				PokemonTypeResource newPokemonType = new PokemonTypeResource(
					typeData.id,
					typeData.name,
					PokemonTypeResource.generateDamageMultipliers(typeData.damage_relations, typeCache.getValue().keySet())
				);
				pokemonTypeReference.postValue(newPokemonType);
			}
		});

		return 0;
	}

	/*
	 * return codes:
	 * 0 - operation has been queued
	 * 1 - moveCache and it's dependencies haven't finished loading yet
	 * 2 - could not find LiveData reference, a bad id
	 * 3 - LiveData is not Deferred. Hypothetically LiveData.getValue() could return null, but that would be a bug in the constructor
	 */
	public int loadMove(int id)
	{
		if(typeCache.getValue() == null || moveCache.getValue() == null)
			return 1;

		final MutableLiveData<PokemonMove> pokemonMoveReference = (MutableLiveData<PokemonMove>) moveCache.getValue().get(id);
		if(pokemonMoveReference == null)
			return 2;

		PokemonMove pokemonMove = pokemonMoveReference.getValue();
		if(pokemonMove == null || !pokemonMove.isDeferred())
			return 3;

		final DeferredPokemonMoveResource deferredPokemonMove = (DeferredPokemonMoveResource) pokemonMove;
		NetworkUtils.doHTTPGet(deferredPokemonMove.getUrl(), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request type at url: " + deferredPokemonMove.getUrl());
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					Log.d(PokeAPIViewModel.class.getSimpleName(), "Body was null");
					return;
				}

				String pokemonMoveJSON = body.string();
				PokeAPIUtils.Move moveData = PokeAPIUtils.parseMoveJSON(pokemonMoveJSON);

				int typeId = PokeAPIUtils.getId(moveData.type.url);

				PokemonMoveResource newPokemonMove = new PokemonMoveResource(moveData.id, moveData.name, moveData.power, getTypeReferenceFromCache(typeId));
				pokemonMoveReference.postValue(newPokemonMove);
			}
		});

		return 0;
	}

	/*
	 * return codes:
	 * 0 - operation has been queued
	 * 1 - pokemonCache and it's dependencies haven't finished loading yet
	 * 2 - could not find LiveData reference, a bad id
	 * 3 - LiveData is not Deferred. Hypothetically LiveData.getValue() could return null, but that would be a bug in the constructor
	 */
	public int loadPokemon(int id)
	{
		//have the dependencies loaded yet?
		if(typeCache.getValue() == null || moveCache.getValue() == null || pokemonCache.getValue() == null)
			return 1;

		//did they give us a bad id?
		final MutableLiveData<Pokemon> pokemonReference = (MutableLiveData<Pokemon>) pokemonCache.getValue().get(id);
		if(pokemonReference == null)
			return 2;

		//is this thing actually deferred?
		Pokemon pokemon = pokemonReference.getValue();
		if(pokemon == null || !pokemon.isDeferred())
			return 3;

		//so now we know it is deferred, we can make a request for the data
		final DeferredPokemonResource deferredPokemon = (DeferredPokemonResource) pokemon;
		NetworkUtils.doHTTPGet(deferredPokemon.getUrl(), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request pokemon at url: " + deferredPokemon.getUrl());
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					Log.d(PokeAPIViewModel.class.getSimpleName(), "Body was null");
					return;
				}

				String pokemonJSON = body.string();
				PokeAPIUtils.Pokemon pokemonData = PokeAPIUtils.parsePokemonJSON(pokemonJSON);

				//grab the type references from the cache
				ArrayList<LiveData<PokemonType>> types = new ArrayList<>(pokemonData.types.length);
				for(PokeAPIUtils.PokemonType pokeAPIType : pokemonData.types)
				{
					int typeId = PokeAPIUtils.getId(pokeAPIType.type.url);
					types.add(getTypeReferenceFromCache(typeId));
				}

				//grab the move references from the cache
				ArrayList<LiveData<PokemonMove>> moves = new ArrayList<>(pokemonData.moves.length);
				for(PokeAPIUtils.PokemonMove pokeAPIMove : pokemonData.moves)
				{
					int moveId = PokeAPIUtils.getId(pokeAPIMove.move.url);
					moves.add(getMoveReferenceFromCache(moveId));
				}

				//create a non-deferred and post it to our LiveData
				PokemonResource newPokemon = new PokemonResource(pokemonData.id, pokemonData.name, types, moves);
				pokemonReference.postValue(newPokemon);
			}
		});

		return 0;
	}
}
