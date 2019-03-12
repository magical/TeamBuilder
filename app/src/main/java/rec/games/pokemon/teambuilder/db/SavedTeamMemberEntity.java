package rec.games.pokemon.teambuilder.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "team_members")
class SavedTeamMemberEntity
{
	@PrimaryKey(autoGenerate = true)
	public int id;

	@ColumnInfo(name = "team_id")
	public int teamId;

	@ColumnInfo(name = "pokemon_id")
	public int pokemonId;
}
