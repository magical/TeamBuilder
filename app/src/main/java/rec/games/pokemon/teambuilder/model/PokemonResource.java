package rec.games.pokemon.teambuilder.model;

import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class PokemonResource extends Pokemon
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
}
