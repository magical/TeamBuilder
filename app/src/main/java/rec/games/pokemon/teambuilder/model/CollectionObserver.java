package rec.games.pokemon.teambuilder.model;

import android.support.annotation.Nullable;

public interface CollectionObserver<T>
{
	void onItemChanged(@Nullable T t, int index);
}
