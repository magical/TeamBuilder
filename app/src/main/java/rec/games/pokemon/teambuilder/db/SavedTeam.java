package rec.games.pokemon.teambuilder.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Relation;

import java.util.List;

@Entity(tableName = "teams")
public class SavedTeam
{
	@PrimaryKey(autoGenerate = true)
	public int id;

	@Relation(parentColumn = "id", entityColumn = "team_id",
		entity = SavedTeamMember.class, projection = {"pokemon_id"})
	public List<Integer> memberIds;
}

@Entity(tableName = "team_members")
class SavedTeamMember
{
	@PrimaryKey(autoGenerate = true)
	public int id;

	@ColumnInfo(name = "team_id")
	public int teamId;

	@ColumnInfo(name = "pokemon_id")
	public int pokemonId;
}