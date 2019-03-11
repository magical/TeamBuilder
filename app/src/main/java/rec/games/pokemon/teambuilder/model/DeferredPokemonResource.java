package rec.games.pokemon.teambuilder.model;

public class DeferredPokemonResource extends Pokemon
{
	protected String resourceName;
	protected String url;

	public DeferredPokemonResource(int id, String resourceName, String url)
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

	public String getUrl()
	{
		return url;
	}
}
