package rec.games.pokemon.teambuilder.model;

import okhttp3.Request;

class PriorityNetworkRequest implements Comparable<PriorityNetworkRequest>
{
	final NetworkPriority priority;
	final Request request;
	final PriorityCallback callback;

	PriorityNetworkRequest(NetworkPriority priority, Request request, PriorityCallback callback)
	{
		this.priority = priority;
		this.request = request;
		this.callback = callback;
	}

	//are natural order is determined by their priorities
	@Override
	public int compareTo(PriorityNetworkRequest other)
	{
		return priority.compareTo(other.priority);
	}
}
