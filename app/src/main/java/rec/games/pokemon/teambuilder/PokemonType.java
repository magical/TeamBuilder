package rec.games.pokemon.teambuilder;

import java.util.HashMap;

//Data class representing pokemon types
abstract class PokemonType
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

	//subclasses may or may not have these. Or they could return different values
	public abstract String getName();
	public abstract Double getDamageMultiplier(PokemonType pokemonType);
}

class DeferredPokemonTypeResource extends PokemonType
{
	protected String name;
	protected String url;

	public DeferredPokemonTypeResource(int id, String name, String url)
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

	//since we lack the data to infer damage multipliers, we can't determine a damage multiplier. So null it is
	@Override
	public Double getDamageMultiplier(PokemonType pokemonType)
	{
		return null;
	}

	public String getUrl()
	{
		return url;
	}
}

class PokemonTypeResource extends PokemonType
{
	//for now the name will be the resource name returned by the api
	protected String name;
	//later we should use this for locale specific names
	//key: locale. value: locale specific name
	protected HashMap<String, String> localeNames;

	//key: PokemonType id. value: damageMultiplier against the key
	protected HashMap<Integer, Double> damageMultipliers;

	public PokemonTypeResource(int id, String name, HashMap<Integer, Double> damageMultipliers)
	{
		super(id);

		this.name = name;
		this.damageMultipliers = damageMultipliers;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Double getDamageMultiplier(PokemonType pokemonType)
	{
		return damageMultipliers.get(pokemonType.getId());
	}
}
