package rec.games.pokemon.teambuilder;

import java.util.HashMap;
import java.util.Set;

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

class DeferredPokemonTypeResource extends PokemonType
{
	protected String resourceName;
	protected String url;

	public DeferredPokemonTypeResource(int id, String resourceName, String url)
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
	//TODO: for now the name will be the resource name returned by the api
	protected String resourceName;
	//later we should use this for locale specific names
	//key: locale. value: locale specific name
	protected HashMap<String, String> localeNames;

	//key: PokemonType id. value: damageMultiplier against the key
	protected HashMap<Integer, Double> damageMultipliers;

	public PokemonTypeResource(int id, String resourceName, HashMap<Integer, Double> damageMultipliers)
	{
		super(id);

		this.resourceName = resourceName;
		this.damageMultipliers = damageMultipliers;
	}

	@Override
	public String getName()
	{
		return resourceName;
	}

	@Override
	public Double getDamageMultiplier(PokemonType pokemonType)
	{
		return damageMultipliers.get(pokemonType.getId());
	}

	@Override
	public boolean isLoaded()
	{
		return true;
	}

	static HashMap<Integer, Double> generateDamageMultipliers(PokeAPIUtils.TypeRelations damageRelations, Set<Integer> typeKeys)
	{
		HashMap<Integer, Double> damageMultipliers = new HashMap<>(typeKeys.size());

		//set the defaults
		for(int typeKey: typeKeys)
			damageMultipliers.put(typeKey, 1.0);

		//no damage
		for(PokeAPIUtils.NamedAPIResource typeResource: damageRelations.no_damage_to)
			damageMultipliers.put(PokeAPIUtils.getId(typeResource.url), 0.0);

		//half damage
		for(PokeAPIUtils.NamedAPIResource typeResource: damageRelations.half_damage_to)
			damageMultipliers.put(PokeAPIUtils.getId(typeResource.url), 0.5);

		//double damage
		for(PokeAPIUtils.NamedAPIResource typeResource: damageRelations.double_damage_to)
			damageMultipliers.put(PokeAPIUtils.getId(typeResource.url), 2.0);

		return damageMultipliers;
	}
}
