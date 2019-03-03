package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;

import java.util.ArrayList;

// Pokemon is a data class representing a pokemon
abstract class Pokemon
{
	protected int id;

	public Pokemon(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public boolean isDeferred()
	{
		return this instanceof DeferredPokemonResource;
	}

	//subclasses may or may not have these. Or they could return different values
	public abstract String getName();

	public boolean isLoaded()
	{
		return false;
	}
}

class DeferredPokemonResource extends Pokemon
{
	protected String resourceName;
	protected String url;

	public DeferredPokemonResource(int id, String resourceName, String url)
	{
		super(id);

		this.resourceName = resourceName;
		this.url = url;
	}

	@Override
	public String getName()
	{
		return resourceName;
	}

	public String getUrl()
	{
		return url;
	}
}

class PokemonResource extends Pokemon
{
	//TODO: later down the road we should use the pokemon-species name or the pokemon-form name
	protected String resourceName;

	protected ArrayList<LiveData<PokemonType>> types;

	//in java, object arrays default array value is null
	//so if a move in this array is not null, then this pokemon is using that move
	protected ArrayList<LiveData<PokemonMove>> moves;

	//TODO: maybe also store a reference to the sprite's here as well

	public PokemonResource(int id, String resourceName, ArrayList<LiveData<PokemonType>> types, ArrayList<LiveData<PokemonMove>> moves)
	{
		super(id);

		this.resourceName = resourceName;
		this.types = types;
		this.moves = moves;
	}

	@Override
	public String getName()
	{
		return resourceName;
	}

	@Override
	public boolean isLoaded()
	{
		for(LiveData<PokemonType> pokemonTypeReference: types)
			if(pokemonTypeReference.getValue() == null || pokemonTypeReference.getValue().isDeferred())
				return false;

		for(LiveData<PokemonMove> pokemonMoveReference: moves)
			if(pokemonMoveReference.getValue() == null || pokemonMoveReference.getValue().isDeferred())
				return false;

		return true;
	}
}
