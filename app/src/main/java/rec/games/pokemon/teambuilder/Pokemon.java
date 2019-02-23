package rec.games.pokemon.teambuilder;

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

	//subclasses may or may not have these. Or they could return different values
	public abstract String getName();
}

class DeferredPokemonResource extends Pokemon
{
	protected String name;
	protected String url;

	public DeferredPokemonResource(int id, String name, String url)
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

	public String getUrl()
	{
		return url;
	}
}

class PokemonResource extends Pokemon
{
	//later down the road we could use name as the pokemon-species name or the pokemon-form name
	//rather than just using it as the resource name that comes from the PokeAPI
	protected String name;

	protected PokemonType type1;
	//if type2 is not null, then this pokemon possess a second type
	protected PokemonType type2;

	//in java, object arrays default array value is null
	//so if a move in this array is not null, then this pokemon is using that move
	protected PokemonMove[] moves = new PokemonMove[4];

	//TODO: maybe also store a reference to the sprite's here as well

	public PokemonResource(int id, String name)
	{
		super(id);

		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}
}
