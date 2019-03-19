package rec.games.pokemon.teambuilder.model;

import android.arch.lifecycle.LiveData;

public class DeferredPokemonMoveResource extends PokemonMove
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
	public String getName()
	{
		return resourceName;
	}

	@Override
	public LiveData<PokemonType> getType()
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
