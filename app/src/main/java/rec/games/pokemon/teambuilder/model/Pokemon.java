package rec.games.pokemon.teambuilder.model;

// Pokemon is a data class representing a pokemon
public abstract class Pokemon
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

