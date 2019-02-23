package rec.games.pokemon.teambuilder;

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
	public abstract String getName();
	public abstract PokemonType getType();
	public abstract Integer getPower();

	public boolean isAttackMove()
	{
		Integer power = getPower();

		return power != null && power.intValue() > 0;
	}
}

class DeferredPokemonMoveResource extends PokemonMove
{
	protected String name;
	protected String url;

	public DeferredPokemonMoveResource(int id, String name, String url)
	{
		super(id);

		this.name = name;
		this.url = url;
	}

	@Override
	public String getName()
	{
		return name;
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
	//for now use the resource name, but later we should use the locale specific names
	protected String name;
	//similar to the ones described in PokemonTypes
	protected HashMap<String, String> localeNames;
	protected PokemonType type;
	protected Integer power;

	public PokemonMoveResource(int id, String name, int power, PokemonType type)
	{
		super(id);

		this.name = name;
		this.power = Integer.valueOf(power);
		this.type = type;
	}


	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public PokemonType getType()
	{
		return type;
	}

	@Override
	public Integer getPower()
	{
		return power;
	}
}
