package rec.games.pokemon.teambuilder.model;

import java.util.ArrayList;
import java.util.List;

public class Team
{
	public final static String TEAM_ID = Team.class.getName();

	public List<TeamMember> members = new ArrayList<>();
}
