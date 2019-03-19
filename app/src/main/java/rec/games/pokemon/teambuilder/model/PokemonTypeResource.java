package rec.games.pokemon.teambuilder.model;

import java.util.HashMap;
import java.util.Set;

public class PokemonTypeResource extends PokemonType
{
	//TODO: for now the name will be the resource name returned by the api
	protected String resourceName;
	//later we should use this for locale specific names
	//key: locale. value: locale specific name
	protected HashMap<String, String> localeNames;

	//key: PokemonType id. value: damageMultiplier against the key
	protected HashMap<Integer, Double> damageMultipliers;

	public PokemonTypeResource(int id, String resourceName, HashMap<Integer, Double> damageMultipliers, HashMap<String, String> localeNames)
	{
		super(id);

		this.resourceName = resourceName;
		this.damageMultipliers = damageMultipliers;
		this.localeNames = localeNames;
	}

	public static HashMap<Integer, Double> generateDamageMultipliers(PokeAPIUtils.TypeRelations damageRelations, Set<Integer> typeKeys)
	{
		HashMap<Integer, Double> damageMultipliers = new HashMap<>(typeKeys.size());

		//set the defaults
		for(int typeKey : typeKeys)
			damageMultipliers.put(typeKey, 1.0);

		//no damage
		for(PokeAPIUtils.NamedAPIResource typeResource : damageRelations.no_damage_to)
			damageMultipliers.put(PokeAPIUtils.getId(typeResource.url), 0.0);

		//half damage
		for(PokeAPIUtils.NamedAPIResource typeResource : damageRelations.half_damage_to)
			damageMultipliers.put(PokeAPIUtils.getId(typeResource.url), 0.5);

		//double damage
		for(PokeAPIUtils.NamedAPIResource typeResource : damageRelations.double_damage_to)
			damageMultipliers.put(PokeAPIUtils.getId(typeResource.url), 2.0);

		return damageMultipliers;
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

	public String getLocaleName(String locale)
	{
		return localeNames.get(locale);
	}
}
