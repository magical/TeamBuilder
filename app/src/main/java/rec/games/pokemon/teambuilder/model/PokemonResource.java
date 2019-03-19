package rec.games.pokemon.teambuilder.model;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PokemonResource extends Pokemon
{
	//TODO: later down the road we should use the pokemon-species name or the pokemon-form name
	protected String resourceName;

	protected ArrayList<LiveData<PokemonType>> types;
	protected ArrayList<LiveData<PokemonMove>> moves;

	protected MutableLiveData<HashMap<String, String>> speciesLocaleMap;

	public PokemonResource(int id, String resourceName, ArrayList<LiveData<PokemonType>> types, ArrayList<LiveData<PokemonMove>> moves)
	{
		super(id);

		this.resourceName = resourceName;
		this.types = types;
		this.moves = moves;

		speciesLocaleMap = new MutableLiveData<>();
	}

	//could always restructure to access in a different way, just for testing right now
	public List<LiveData<PokemonMove>> getMoves(){ return moves; }
	public List<LiveData<PokemonType>> getTypes(){ return types; }

	@Override
	public String getName()
	{
		return resourceName;
	}

	@Override
	public boolean isLoaded()
	{
		for(LiveData<PokemonType> pokemonTypeReference : types)
			if(pokemonTypeReference.getValue() == null || pokemonTypeReference.getValue().isDeferred())
				return false;

		for(LiveData<PokemonMove> pokemonMoveReference : moves)
			if(pokemonMoveReference.getValue() == null || pokemonMoveReference.getValue().isDeferred())
				return false;

		return true;
	}

	public LiveData<String> getLocaleName(final String locale)
	{
		return Transformations.map(speciesLocaleMap, new Function<HashMap<String, String>, String>()
		{
			@Override
			public String apply(HashMap<String, String> input)
			{
				if(input == null)
					return null;

				return input.get(locale);
			}
		});
	}

	public void setSpeciesLocaleMap(HashMap<String, String> speciesLocaleMap)
	{
		this.speciesLocaleMap.postValue(speciesLocaleMap);
	}
}
