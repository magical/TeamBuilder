package rec.games.pokemon.teambuilder.model;

public class DeferredPokemonTypeResource extends PokemonType
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
