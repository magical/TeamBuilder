package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

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

	LiveDataList<Pokemon> extractPokemonListFromCache()
	{
		if(pokemonCache.getValue() == null)
			return null;

		Collection<LiveData<Pokemon>> pokemonReferences = pokemonCache.getValue().values();
		return new LiveDataList<>(pokemonReferences);
	}

	private LiveData<PokemonType> getTypeReferenceFromCache(int id)
	{
		if(typeCache.getValue() == null)
			return null;

		return typeCache.getValue().get(id);
	}

	private LiveData<PokemonMove> getMoveReferenceFromCache(int id)
	{
		if(moveCache.getValue() == null)
			return null;

		return moveCache.getValue().get(id);
	}

	private LiveData<Pokemon> getPokemonReferenceFromCache(int id)
	{
		if(pokemonCache.getValue() == null)
			return null;

		return pokemonCache.getValue().get(id);
	}

	PokemonType getTypeFromCache(int id)
	{
		LiveData<PokemonType> pokemonTypeReference = getTypeReferenceFromCache(id);
		if(pokemonTypeReference == null)
			return null;

		return pokemonTypeReference.getValue();
	}

	PokemonMove getMoveFromCache(int id)
	{
		LiveData<PokemonMove> pokemonMoveReference = getMoveReferenceFromCache(id);
		if(pokemonMoveReference == null)
			return null;

		return pokemonMoveReference.getValue();
	}

	Pokemon getPokemonFromCache(int id)
	{
		LiveData<Pokemon> pokemonReference = getPokemonReferenceFromCache(id);
		if(pokemonReference == null)
			return null;

		return pokemonReference.getValue();
	}

	void loadType(int id, final Semaphore sem)
	{
		PokemonType pokemonType = getTypeFromCache(id);
		if(pokemonType.isLoaded())
		{
			sem.release();
			return;
		}

		final DeferredPokemonTypeResource deferredPokemonType = (DeferredPokemonTypeResource) pokemonType;
		NetworkUtils.doHTTPGet(deferredPokemonType.getUrl(), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request type at url: " + deferredPokemonType.getUrl());
				sem.release();
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					Log.d(PokeAPIViewModel.class.getSimpleName(), "Body was null");
					sem.release();
					return;
				}

				String pokemonTypeJSON = body.string();
				PokeAPIUtils.Type typeData = PokeAPIUtils.parseTypeJSON(pokemonTypeJSON);

				//TODO: actually parse out the damageMultipliers
				PokemonTypeResource newPokemonType = new PokemonTypeResource(typeData.id, typeData.name, new HashMap<Integer, Double>());
				MutableLiveData<PokemonType> pokemonTypeReference = (MutableLiveData<PokemonType>) getTypeReferenceFromCache(newPokemonType.id);
				pokemonTypeReference.postValue(newPokemonType);

				sem.release();
			}
		});
	}

	void loadMove(int id, final Semaphore sem)
	{
		PokemonMove pokemonMove = getMoveFromCache(id);
		if(pokemonMove.isLoaded())
		{
			sem.release();
			return;
		}

		final DeferredPokemonMoveResource deferredPokemonMove = (DeferredPokemonMoveResource) pokemonMove;
		NetworkUtils.doHTTPGet(deferredPokemonMove.getUrl(), new Callback()
		{
			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request type at url: " + deferredPokemonMove.getUrl());
				sem.release();
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					Log.d(PokeAPIViewModel.class.getSimpleName(), "Body was null");
					sem.release();
					return;
				}

				String pokemonMoveJSON = body.string();
				PokeAPIUtils.Move moveData = PokeAPIUtils.parseMoveJSON(pokemonMoveJSON);

				Semaphore waitUntil = new Semaphore(0);

				int typeId = PokeAPIUtils.getId(moveData.type.url);
				loadType(typeId, waitUntil);

				waitUntil.acquireUninterruptibly();

				PokemonMoveResource newPokemonMove = new PokemonMoveResource(moveData.id, moveData.name, moveData.power, getTypeReferenceFromCache(typeId));
				MutableLiveData<PokemonMove> pokemonMoveReference = (MutableLiveData<PokemonMove>) getMoveReferenceFromCache(moveData.id);
				pokemonMoveReference.postValue(newPokemonMove);

				sem.release();
			}
		});
	}

	void loadPokemon(int id)
	{
		Pokemon pokemon = getPokemonFromCache(id);
		if(pokemon.isLoaded())
			return;

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


				//we can only acquire a semaphore when permits >= 1
				//so 1 - thingsToWaitOn is the magic number for
				Semaphore waitUntil = new Semaphore(1 - pokemonData.types.length - pokemonData.moves.length);

				for(PokeAPIUtils.PokemonType pokeAPIType: pokemonData.types)
				{
					int id = PokeAPIUtils.getId(pokeAPIType.type.url);
					loadType(id, waitUntil);
				}

				for(PokeAPIUtils.PokemonMove pokeAPIMove: pokemonData.moves)
				{
					int id = PokeAPIUtils.getId(pokeAPIMove.move.url);
					loadMove(id, waitUntil);
				}

				waitUntil.acquireUninterruptibly();

				ArrayList<LiveData<PokemonType>> types = new ArrayList<>(pokemonData.types.length);
				for(PokeAPIUtils.PokemonType pokeAPIType: pokemonData.types)
				{
					int id = PokeAPIUtils.getId(pokeAPIType.type.url);
					types.add(getTypeReferenceFromCache(id));
				}
				ArrayList<LiveData<PokemonMove>> moves = new ArrayList<>(pokemonData.moves.length);
				for(PokeAPIUtils.PokemonMove pokeAPIMove: pokemonData.moves)
				{
					int id = PokeAPIUtils.getId(pokeAPIMove.move.url);
					moves.add(getMoveReferenceFromCache(id));
				}
				PokemonResource newPokemon = new PokemonResource(pokemonData.id, pokemonData.name, types, moves);
				MutableLiveData<Pokemon> pokemonReference = (MutableLiveData<Pokemon>) getPokemonReferenceFromCache(pokemonData.id);
				pokemonReference.postValue(newPokemon);
			}
		});
	}
}