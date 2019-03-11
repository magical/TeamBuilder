package rec.games.pokemon.teambuilder.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SavedTeam.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
	public abstract SavedTeamDao savedTeamDao();

	private static AppDatabase INSTANCE;

	public static AppDatabase getDatabase(final Context context)
	{
		synchronized(AppDatabase.class)
		{
			if(INSTANCE == null)
			{
				INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "teams.db").build();
			}
			return INSTANCE;
		}
	}
}
