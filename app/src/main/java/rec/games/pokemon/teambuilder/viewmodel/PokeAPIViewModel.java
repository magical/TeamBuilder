package rec.games.pokemon.teambuilder.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Collection;
import java.util.HashMap;

import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonMove;
import rec.games.pokemon.teambuilder.model.PokemonType;
import rec.games.pokemon.teambuilder.model.repository.PokeAPIRepository;

public class PokeAPIViewModel extends ViewModel
{
	public void getNewPokemonList()
	{
		PokeAPIRepository.getNewPokemonList();
	}

	public LiveDataList<Pokemon> extractPokemonListFromCache()
	{
		HashMap<Integer, LiveData<Pokemon>> pokemonCache = getPokemonCache().getValue();
		if(pokemonCache == null)
			return null;

		Collection<LiveData<Pokemon>> pokemonReferences = pokemonCache.values();
		return new LiveDataList<>(pokemonReferences);
	}

	public LiveData<HashMap<Integer, LiveData<Pokemon>>> getPokemonCache()
	{
		return PokeAPIRepository.getPokemonCache();
	}

	public LiveData<PokemonType> getTypeReference(int id)
	{
		return PokeAPIRepository.getTypeReferenceFromCache(id);
	}

	public LiveData<PokemonMove> getMoveReference(int id)
	{
		return PokeAPIRepository.getMoveReferenceFromCache(id);
	}

	public LiveData<Pokemon> getPokemonReference(int id)
	{
		return PokeAPIRepository.getPokemonReferenceFromCache(id);
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
		return PokeAPIRepository.loadType(id);
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
		return PokeAPIRepository.loadMove(id);
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
		return PokeAPIRepository.loadPokemon(id);
	}
}
