package rec.games.pokemon.teambuilder.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public abstract class SavedTeamDao
{
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	public abstract long insertSavedTeamEntity(SavedTeamEntity t);

	@Delete
	public abstract void deleteSavedTeamEntity(SavedTeamEntity t);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	public abstract long insertSavedTeamMemberEntity(SavedTeamMemberEntity m);

	@Delete
	public abstract void deleteSavedTeamMember(SavedTeamMemberEntity m);

	@Query("SELECT EXISTS (SELECT 1 FROM team_members WHERE team_id = :teamId AND pokemon_id = :pokemonId)")
	public abstract boolean isPokemonInTeamSync(int teamId, int pokemonId);

	@Query("SELECT EXISTS (SELECT 1 FROM team_members WHERE team_id = :teamId AND pokemon_id = :pokemonId)")
	public abstract LiveData<Boolean> isPokemonInTeam(int teamId, int pokemonId);

	@Transaction
	@Query("SELECT * from teams WHERE id = :id")
	public abstract LiveData<SavedTeam> getTeamById(int id);

	@Transaction
	@Query("SELECT * from teams")
	public abstract LiveData<List<SavedTeam>> getAllTeams();

	@Query("DELETE FROM team_members WHERE team_id = :teamId AND pokemon_id = :pokemonId")
	public abstract void removePokemonFromTeamById(int teamId, int pokemonId);


	@Query("DELETE FROM team_members WHERE team_id = :teamId")
	public abstract void removeAllPokemonFromTeam(int teamId);

	@Transaction
	public void createSavedTeam(SavedTeam savedTeam) {
		SavedTeamEntity savedTeamEntity = new SavedTeamEntity();
		savedTeamEntity.id = savedTeam.id;
		savedTeamEntity.name = savedTeam.name;
		long teamId = insertSavedTeamEntity(savedTeamEntity);
		if (savedTeam.memberIds != null)
		{
			for(int pokemonId : savedTeam.memberIds)
			{
				SavedTeamMemberEntity tm = new SavedTeamMemberEntity();
				tm.id = 0;
				tm.teamId = (int) teamId;
				tm.pokemonId = pokemonId;
				insertSavedTeamMemberEntity(tm);
			}
		}
	}

	public void deleteSavedTeam(SavedTeam savedTeam) {
		if (savedTeam.id == 0) {
			return;
		}
		SavedTeamEntity savedTeamEntity = new SavedTeamEntity();
		savedTeamEntity.id = savedTeam.id;
		savedTeamEntity.name = savedTeam.name;
		removeAllPokemonFromTeam(savedTeam.id);
		deleteSavedTeamEntity(savedTeamEntity);

	}
}
