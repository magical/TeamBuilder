package rec.games.pokemon.teambuilder.Model;

import okhttp3.Callback;
import okhttp3.Request;

class PriorityNetworkRequest implements Comparable<PriorityNetworkRequest>
{
	final NetworkUtils.NetworkPriority priority;
	final Request request;
	final NetworkUtils.OnCallStart callStart;
	final Callback callback;

	PriorityNetworkRequest(NetworkUtils.NetworkPriority priority, Request request, NetworkUtils.OnCallStart callStart, Callback callback)
	{
		this.priority = priority;
		this.request = request;
		this.callStart = callStart;
		this.callback = callback;
	}

	//are natural order is determined by their priorities
	@Override
	public int compareTo(PriorityNetworkRequest other)
	{
		return priority.compareTo(other.priority);
	}
}
