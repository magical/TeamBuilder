package rec.games.pokemon.teambuilder.Model;

import android.support.annotation.Nullable;

public interface CollectionObserver<T>
{
	void onItemChanged(@Nullable T t, int index);
}
