package optmizations;

import java.util.List;

public interface HeuristicaMelhoria {

	public Integer[] solve(double[][] matrizDistancia, Integer[] rotaInicial);
	public List<Integer> solve(double[][] matrizDistancia, List<Integer> rotaInicial);
}
