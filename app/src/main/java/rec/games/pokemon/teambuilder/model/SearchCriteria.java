package rec.games.pokemon.teambuilder.model;

import android.support.annotation.Nullable;

public interface SearchCriteria<E>
{
	boolean match(@Nullable E e);
}
