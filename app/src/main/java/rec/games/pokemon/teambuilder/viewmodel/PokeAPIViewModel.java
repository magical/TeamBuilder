package rec.games.pokemon.teambuilder.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Set;

import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.NetworkPriority;
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
		return PokeAPIRepository.extractPokemonListFromCache();
	}

	public LiveDataList<PokemonType> extractTypeListFromCache()
	{
		return PokeAPIRepository.extractTypeListFromCache();
	}

	public Set<Integer> getTypeListIds()
	{
		return PokeAPIRepository.getTypeListIds();
	}

	public LiveData<Boolean> getTypeListCache()
	{
		return PokeAPIRepository.getTypeListObserver();
	}

	public LiveData<Boolean> getPokemonListCache()
	{
		return PokeAPIRepository.getPokemonListObserver();
	}

	public LiveData<PokemonType> getLiveType(int id)
	{
		return PokeAPIRepository.getLiveType(id);
	}

	public LiveData<PokemonMove> getLiveMove(int id)
	{
		return PokeAPIRepository.getLiveMove(id);
	}

	public LiveData<Pokemon> getLivePokemon(int id)
	{
		return PokeAPIRepository.getLivePokemon(id);
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
		return PokeAPIRepository.loadType(id, NetworkPriority.USER_INTERACTION);
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
		return PokeAPIRepository.loadMove(id, NetworkPriority.USER_INTERACTION);
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
		return PokeAPIRepository.loadPokemon(id, NetworkPriority.USER_INTERACTION);
	}
}
