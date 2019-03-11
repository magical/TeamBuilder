package rec.games.pokemon.teambuilder.model;

//Data class representing pokemon types
public abstract class PokemonType
{
	protected int id;

	public PokemonType(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public boolean isDeferred()
	{
		return this instanceof DeferredPokemonTypeResource;
	}

	//subclasses may or may not have these. Or they could return different values
	public abstract String getName();

	public abstract Double getDamageMultiplier(PokemonType pokemonType);

	public boolean isLoaded()
	{
		return false;
	}
}

