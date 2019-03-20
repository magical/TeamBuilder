package rec.games.pokemon.teambuilder.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

@Entity(tableName = "team_member_moves")
class SavedTeamMemberMoveEntity
{
	@ColumnInfo(name = "member_id")
	int memberId = 0;

	@ColumnInfo(name = "move_id")
	int moveId = 0;
}
