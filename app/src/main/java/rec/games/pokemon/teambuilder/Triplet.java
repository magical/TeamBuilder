package rec.games.pokemon.teambuilder;

//Utility class that follows the same pattern as Android's Pair class
public class Triplet<T, U, V>
{
	public final T first;
	public final U second;
	public final V third;

	public Triplet(T first, U second, V third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof  Triplet))
			return false;

		Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) obj;

		return (first == triplet.first || first.equals(triplet.first))
			&& (second == triplet.second || second.equals(triplet.second))
			&& (third == triplet.third || third.equals(triplet.third));
	}

	@Override
	public int hashCode()
	{
		return (first == null? 0: first.hashCode())
			^ (second == null? 0: second.hashCode())
			^ (third == null? 0: third.hashCode());
	}

	public static <A, B, C> Triplet<A, B, C> create(A a, B b, C c)
	{
		return new Triplet<>(a, b, c);
	}
}
