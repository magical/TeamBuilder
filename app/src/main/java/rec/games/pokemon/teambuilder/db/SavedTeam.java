package rec.games.pokemon.teambuilder.db;

import android.arch.persistence.room.Relation;

import java.util.List;

public class SavedTeam {
	public int id;
	public String name;

	@Relation(parentColumn = "id", entityColumn = "team_id",
		entity = SavedTeamMemberEntity.class, projection = {"pokemon_id"})
	public List<Integer> memberIds;
}
