package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;

import java.util.HashMap;

//Data class representing pokemon moves
abstract class PokemonMove
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

	//subclasses may or may not have these. Or they could return different values
	public abstract boolean isLoaded();
	public abstract String getName();
	public abstract PokemonType getType();
	public abstract Integer getPower();

	public boolean isAttackMove()
	{
		Integer power = getPower();

		return power != null && power > 0;
	}
}

class DeferredPokemonMoveResource extends PokemonMove
{
	protected String resourceName;
	protected String url;

	public DeferredPokemonMoveResource(int id, String resourceName, String url)
	{
		super(id);

		this.resourceName = resourceName;
		this.url = url;
	}

	@Override
	public boolean isLoaded()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return resourceName;
	}

	@Override
	public PokemonType getType()
	{
		return null;
	}

	//since we have no idea if it is an attacking or a support move yet, it's value is neither (or null)
	@Override
	public Integer getPower()
	{
		return null;
	}

	public String getUrl()
	{
		return url;
	}
}

class PokemonMoveResource extends PokemonMove
{
	//TODO: for now use the resource name, but later we should use the locale specific names
	protected String resourceName;
	//similar to the ones described in PokemonTypes
	protected HashMap<String, String> localeNames;
	protected LiveData<PokemonType> type;
	protected Integer power;

	public PokemonMoveResource(int id, String resourceName, int power, LiveData<PokemonType> type)
	{
		super(id);

		this.resourceName = resourceName;
		this.power = power;
		this.type = type;
	}

	@Override
	public boolean isLoaded()
	{
		return true;
	}

	@Override
	public String getName()
	{
		return resourceName;
	}

	@Override
	public PokemonType getType()
	{
		return type.getValue();
	}

	@Override
	public Integer getPower()
	{
		return power;
	}
}
