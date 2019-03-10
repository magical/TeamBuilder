package rec.games.pokemon.teambuilder.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SavedTeamDao
{
	@Insert
	void insertSavedTeam(SavedTeam team);

	@Delete
	void deleteSavedTeam(SavedTeam team);

	@Query("SELECT * from teams WHERE id = :id")
	LiveData<SavedTeam> getTeamById(int id);

	@Query("SELECT * from teams")
	LiveData<List<SavedTeam>> getAllTeams();
}
