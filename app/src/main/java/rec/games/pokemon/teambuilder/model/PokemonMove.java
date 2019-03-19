package rec.games.pokemon.teambuilder.model;

import android.arch.lifecycle.LiveData;

//Data class representing pokemon moves
public abstract class PokemonMove
{
	protected int id;

	public PokemonMove(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public boolean isDeferred()
	{
		return this instanceof DeferredPokemonMoveResource;
	}

	//subclasses may or may not have these. Or they could return different values
	public abstract String getName();

	public abstract LiveData<PokemonType> getType();

	public abstract Integer getPower();

	public boolean isLoaded()
	{
		return false;
	}

	public boolean isAttackMove()
	{
		Integer power = getPower();

		return power != null && power > 0;
	}
}

