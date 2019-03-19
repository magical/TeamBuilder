package rec.games.pokemon.teambuilder.model.repository;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;
import rec.games.pokemon.teambuilder.model.DeferredPokemonMoveResource;
import rec.games.pokemon.teambuilder.model.DeferredPokemonResource;
import rec.games.pokemon.teambuilder.model.DeferredPokemonTypeResource;
import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.NetworkPriority;
import rec.games.pokemon.teambuilder.model.NetworkUtils;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonMove;
import rec.games.pokemon.teambuilder.model.PokemonMoveResource;
import rec.games.pokemon.teambuilder.model.PokemonResource;
import rec.games.pokemon.teambuilder.model.PokemonType;
import rec.games.pokemon.teambuilder.model.PokemonTypeResource;
import rec.games.pokemon.teambuilder.model.PriorityCallback;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

class CacheEntry<E>
{
	E data;
	MutableLiveData<E> liveObserver;

	CacheEntry(E data)
	{
		this.data = data;

		this.liveObserver = new MutableLiveData<>();
		liveObserver.postValue(data);
	}

	void setData(E data)
	{
		this.data = data;
		liveObserver.postValue(data);
	}
}

public class PokeAPIRepository
{
	private static HashMap<Integer, CacheEntry<PokemonType>> typeCache;
	private static HashMap<Integer, CacheEntry<PokemonMove>> moveCache;
	private static HashMap<Integer, CacheEntry<Pokemon>> pokemonCache;

	private static CacheEntry<Boolean> typeCacheObserver;
	private static CacheEntry<Boolean> moveCacheObserver;
	private static CacheEntry<Boolean> pokemonCacheObserver;

	static
	{
		typeCache = new HashMap<>();
		moveCache = new HashMap<>();
		pokemonCache = new HashMap<>();

		typeCacheObserver = new CacheEntry<>(null);
		moveCacheObserver = new CacheEntry<>(null);
		pokemonCacheObserver = new CacheEntry<>(null);

		getNewPokemonList();
	}

	public static void getNewPokemonList()
	{
		final CountDownLatch typeLock = new CountDownLatch(1);
		final CountDownLatch moveLock = new CountDownLatch(1);

		//asynchronously load the type list
		NetworkUtils.doPriorityHTTPGet(PokeAPIUtils.buildTypeListURL(), NetworkPriority.CRITICAL, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return true;
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				typeCacheObserver.setData(false);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					typeCacheObserver.setData(false);
					return;
				}

				//parse the json
				String typeListJSON = body.string();
				PokeAPIUtils.NamedAPIResourceList typeList = PokeAPIUtils.parseNamedAPIResourceListJSON(typeListJSON);

				for(PokeAPIUtils.NamedAPIResource namedResource : typeList.results)
				{
					int id = PokeAPIUtils.getId(namedResource.url);
					if(id >= 10000)
						continue;

					String url = PokeAPIUtils.fixStaticAPIUrl(namedResource.url);
					PokemonType pokemonType = new DeferredPokemonTypeResource(id, namedResource.name, url);

					updatePokemonType(id, pokemonType);
				}

				//alert that the data has changed
				typeCacheObserver.setData(true);
				typeLock.countDown();

				//start loading the types
				for(int key : typeCache.keySet())
				{
					int result = loadType(key, NetworkPriority.ABOVE_NORMAL);
					if(result != 0)
						Log.d(PokeAPIViewModel.class.getSimpleName(), "bad result " + result + " for type: " + key);
				}
			}
		});

		//asynchronously load the move list
		NetworkUtils.doPriorityHTTPGet(PokeAPIUtils.buildMoveListURL(), NetworkPriority.CRITICAL, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return true;
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				moveCacheObserver.setData(false);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					moveCacheObserver.setData(false);
					return;
				}

				String moveListJSON = body.string();
				PokeAPIUtils.NamedAPIResourceList moveList = PokeAPIUtils.parseNamedAPIResourceListJSON(moveListJSON);

				for(PokeAPIUtils.NamedAPIResource namedResource : moveList.results)
				{
					int id = PokeAPIUtils.getId(namedResource.url);
					String url = PokeAPIUtils.fixStaticAPIUrl(namedResource.url);
					PokemonMove pokemonMove = new DeferredPokemonMoveResource(id, namedResource.name, url);

					updatePokemonMove(id, pokemonMove);
				}

				moveCacheObserver.setData(true);
				moveLock.countDown();

				try
				{
					typeLock.await();
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}

				/*for(int key: moveCache.keySet())
				{
					int result = loadMove(key, NetworkPriority.LOW);
					if(result != 0)
						Log.d(PokeAPIViewModel.class.getSimpleName(), "bad result " + result + " for move: " + key);
				}*/
			}
		});

		//asynchronously load the pokemon list
		NetworkUtils.doPriorityHTTPGet(PokeAPIUtils.buildPokemonListURL(), NetworkPriority.CRITICAL, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return true;
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				pokemonCacheObserver.setData(false);
			}

			@EverythingIsNonNull
			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				ResponseBody body = response.body();
				if(body == null)
				{
					pokemonCacheObserver.setData(false);
					return;
				}

				String pokemonListJSON = body.string();
				PokeAPIUtils.NamedAPIResourceList pokemonList = PokeAPIUtils.parseNamedAPIResourceListJSON(pokemonListJSON);

				for(PokeAPIUtils.NamedAPIResource namedResource : pokemonList.results)
				{
					int id = PokeAPIUtils.getId(namedResource.url);
					String url = PokeAPIUtils.fixStaticAPIUrl(namedResource.url);
					Pokemon pokemon = new DeferredPokemonResource(id, namedResource.name, url);

					updatePokemon(id, pokemon);
				}

				pokemonCacheObserver.setData(true);

				try
				{
					typeLock.await();
					moveLock.await();
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}

				/*for(int key: pokemonCache.keySet())
				{
					int result = loadPokemon(key, NetworkPriority.NORMAL);
					if(result != 0)
						Log.d(PokeAPIViewModel.class.getSimpleName(), "bad result " + result + " for pokemon: " + key);
				}*/
			}
		});
	}

	private static void updatePokemonType(int id, PokemonType type)
	{
		CacheEntry<PokemonType> cacheItem = typeCache.get(id);
		if(cacheItem == null)
		{
			typeCache.put(id, new CacheEntry<>(type));
			return;
		}

		cacheItem.setData(type);
	}

	private static void updatePokemonMove(int id, PokemonMove move)
	{
		CacheEntry<PokemonMove> cacheItem = moveCache.get(id);
		if(cacheItem == null)
		{
			moveCache.put(id, new CacheEntry<>(move));
			return;
		}

		cacheItem.setData(move);
	}

	private static void updatePokemon(int id, Pokemon pokemon)
	{
		CacheEntry<Pokemon> cacheItem = pokemonCache.get(id);
		if(cacheItem == null)
		{
			pokemonCache.put(id, new CacheEntry<>(pokemon));
			return;
		}

		cacheItem.setData(pokemon);
	}

	public static Set<Integer> getTypeListIds()
	{
		return typeCache.keySet();
	}

	public static LiveData<Boolean> getTypeListObserver()
	{
		return typeCacheObserver.liveObserver;
	}

	public static LiveData<Boolean> getPokemonListObserver()
	{
		return pokemonCacheObserver.liveObserver;
	}

	public static LiveData<PokemonType> getLiveType(final int id)
	{
		return Transformations.switchMap(typeCacheObserver.liveObserver, new Function<Boolean, LiveData<PokemonType>>()
		{
			@Override
			public LiveData<PokemonType> apply(Boolean input)
			{
				CacheEntry<PokemonType> cacheEntry = typeCache.get(id);
				if(cacheEntry == null)
					return null;

				return cacheEntry.liveObserver;
			}
		});
	}

	public static LiveData<PokemonMove> getLiveMove(final int id)
	{
		return Transformations.switchMap(moveCacheObserver.liveObserver, new Function<Boolean, LiveData<PokemonMove>>()
		{
			@Override
			public LiveData<PokemonMove> apply(Boolean result)
			{
				CacheEntry<PokemonMove> cacheEntry = moveCache.get(id);
				if(cacheEntry == null)
					return null;

				return cacheEntry.liveObserver;
			}
		});
	}

	public static LiveData<Pokemon> getLivePokemon(final int id)
	{
		return Transformations.switchMap(pokemonCacheObserver.liveObserver, new Function<Boolean, LiveData<Pokemon>>()
		{
			@Override
			public LiveData<Pokemon> apply(Boolean result)
			{
				CacheEntry<Pokemon> cacheEntry = pokemonCache.get(id);
				if(cacheEntry == null)
					return null;

				return cacheEntry.liveObserver;
			}
		});
	}

	/*
	 * return codes:
	 * 0 - operation has been queued
	 * 1 - typeCache hasn't finished loading yet and/or could not find item in map (a bad id)
	 * 2 - data is not Deferred. Hypothetically cacheEntry.data could return null, but that would be a bug in the constructor
	 */
	public static int loadType(int id, NetworkPriority priority)
	{
		final CacheEntry<PokemonType> cacheEntry = typeCache.get(id);
		if(cacheEntry == null)
			return 1;

		if(cacheEntry.data == null || !cacheEntry.data.isDeferred())
			return 2;

		final String url = ((DeferredPokemonTypeResource) cacheEntry.data).getUrl();
		NetworkUtils.doPriorityHTTPGet(url, priority, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return cacheEntry.data.isDeferred();
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request type at url: " + url);
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
					PokemonTypeResource.generateDamageMultipliers(typeData.damage_relations, typeCache.keySet()),
					PokeAPIUtils.createLocaleMap(typeData.names)
				);
				updatePokemonType(newPokemonType.getId(), newPokemonType);
			}
		});

		return 0;
	}

	/*
	 * return codes:
	 * 0 - operation has been queued
	 * 1 - moveCache and it's dependencies haven't finished loading yet and/or could not find item in map (a bad id)
	 * 2 - data is not Deferred. Hypothetically cacheEntry.data could return null, but that would be a bug in the constructor
	 */
	public static int loadMove(int id, NetworkPriority priority)
	{
		final CacheEntry<PokemonMove> cacheEntry = moveCache.get(id);
		if(!typeCacheObserver.data || cacheEntry == null)
			return 1;

		if(cacheEntry.data == null || !cacheEntry.data.isDeferred())
			return 2;

		final String url = ((DeferredPokemonMoveResource) cacheEntry.data).getUrl();
		NetworkUtils.doPriorityHTTPGet(url, priority, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return cacheEntry.data.isDeferred();
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request type at url: " + url);
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

				PokemonMoveResource newPokemonMove = new PokemonMoveResource(
					moveData.id,
					moveData.name,
					moveData.power,
					getLiveType(typeId),
					PokeAPIUtils.createLocaleMap(moveData.names)
				);
				updatePokemonMove(newPokemonMove.getId(), newPokemonMove);
			}
		});

		return 0;
	}

	/*
	 * return codes:
	 * 0 - operation has been queued
	 * 1 - pokemonCache and it's dependencies haven't finished loading yet and/or could not find item in map (a bad id)
	 * 2 - data is not Deferred. Hypothetically cacheEntry.data could return null, but that would be a bug in the constructor
	 */
	public static int loadPokemon(int id, final NetworkPriority priority)
	{
		//did they give us a bad id?
		final CacheEntry<Pokemon> cacheEntry = pokemonCache.get(id);
		if(!typeCacheObserver.data || !moveCacheObserver.data || cacheEntry == null)
			return 1;

		//is this thing actually deferred?
		if(cacheEntry.data == null || !cacheEntry.data.isDeferred())
			return 2;

		//so now we know it is deferred, we can make a request for the data
		final String url = ((DeferredPokemonResource) cacheEntry.data).getUrl();
		NetworkUtils.doPriorityHTTPGet(url, priority, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return cacheEntry.data.isDeferred();
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request pokemon at url: " + url);
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
				Arrays.sort(pokemonData.types, new Comparator<PokeAPIUtils.PokemonType>()
				{
					@Override
					public int compare(PokeAPIUtils.PokemonType o1, PokeAPIUtils.PokemonType o2)
					{
						return o1.slot - o2.slot;
					}
				});
				ArrayList<LiveData<PokemonType>> types = new ArrayList<>(pokemonData.types.length);
				for(PokeAPIUtils.PokemonType pokeAPIType : pokemonData.types)
				{
					int typeId = PokeAPIUtils.getId(pokeAPIType.type.url);
					types.add(getLiveType(typeId));
				}

				//grab the move references from the cache
				Arrays.sort(pokemonData.moves, new Comparator<PokeAPIUtils.PokemonMove>()
				{
					@Override
					public int compare(PokeAPIUtils.PokemonMove o1, PokeAPIUtils.PokemonMove o2)
					{
						return o1.move.name.compareTo(o2.move.name);
					}
				});
				ArrayList<LiveData<PokemonMove>> moves = new ArrayList<>(pokemonData.moves.length);
				for(PokeAPIUtils.PokemonMove pokeAPIMove : pokemonData.moves)
				{
					int moveId = PokeAPIUtils.getId(pokeAPIMove.move.url);
					moves.add(getLiveMove(moveId));
				}

				//create a non-deferred and post it to our LiveData
				PokemonResource newPokemon = new PokemonResource(pokemonData.id, pokemonData.name, types, moves);
				updatePokemon(newPokemon.getId(), newPokemon);

				String speciesUrl = PokeAPIUtils.fixStaticAPIUrl(pokemonData.species.url);
				loadPokemonSpeciesNames(speciesUrl, newPokemon, priority);
			}
		});

		return 0;
	}

	private static void loadPokemonSpeciesNames(final String speciesUrl, final PokemonResource pokemon, NetworkPriority priority)
	{
		NetworkUtils.doPriorityHTTPGet(speciesUrl, priority, new PriorityCallback()
		{
			@Override
			public boolean onStart()
			{
				return true;
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call call, IOException e)
			{
				Log.d(PokeAPIViewModel.class.getSimpleName(), "unable to request pokemonSpecies at url: " + speciesUrl);
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

				String pokemonSpeciesJSON = body.string();
				PokeAPIUtils.PokemonSpecies pokemonSpecies = PokeAPIUtils.parsePokemonSpeciesJSON(pokemonSpeciesJSON);

				pokemon.setSpeciesLocaleMap(PokeAPIUtils.createLocaleMap(pokemonSpecies.names));
				//updatePokemon(pokemon.getId(), pokemon);
			}
		});
	}

	public static LiveDataList<Pokemon> extractPokemonListFromCache()
	{
		LiveDataList<Pokemon> liveList = new LiveDataList<>();
		for(CacheEntry<Pokemon> cacheEntry : pokemonCache.values())
			liveList.add(cacheEntry.liveObserver);

		return liveList;
	}

	/**
	 * Returns a list of type objects. Only call this if you know all the types have been loaded already.
	 */
	public static LiveDataList<PokemonType> extractTypeListFromCache()
	{
		LiveDataList<PokemonType> liveList = new LiveDataList<>();
		for(CacheEntry<PokemonType> cacheEntry : typeCache.values())
			liveList.add(cacheEntry.liveObserver);

		return liveList;
	}
}
