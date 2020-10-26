package optmizations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import utils.Utilidades;

public class HeuristicaMelhoria3opt implements HeuristicaMelhoria {

	List<Integer> nodes; // contem os vertices da rota
	List<Integer> nodesImproved; // contem os vertices da rota

	public List<Integer> solve(double[][] matrizDistancia, List<Integer> rota) {
		Integer[] rotaAr = solve(matrizDistancia, rota.toArray(new Integer[0]));
		return new ArrayList<Integer>(Arrays.asList(rotaAr));
	}

	/**
	 * Efetua a contrução da rota. Utiliza o método da inserção do mais distante
	 * como a Heurística construtiva.s
	 * 
	 * @param matrizDistancia
	 * @param numeroClientes
	 * @return
	 */
	public Integer[] solve(double[][] matrizDistancia, Integer[] rota) {
		// System.out.println(" - Vou melhorar (3opt)");
		nodes = Arrays.asList(rota);
		boolean parada = false;
		// Realizar combinações de 3 arcos
		while (!parada) {
			parada = true;
			for (int i = 1; i < nodes.size() - 2; i++) {
				for (int j = i + 1; j < nodes.size() - 2; j++) {
					for (int k = j + 1; k < nodes.size() - 2; k++) {
//				for (int j = 1; j < nodes.size()-2; j++) {
//					for (int k = 1; k < nodes.size()-2; k++) {
						// Verificar troca e atualizar nodesImproved
						int result = realizarTroca(i, j, k, matrizDistancia);
						if (result > 0) {
							parada = false;
						}
					}
				}
			}
		}
		return nodes.toArray(new Integer[0]);
	}

	/**
	 * Efetua o calculo para verificar o ganho obtido entre as trocas
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param z1
	 * @param z2
	 * @param w
	 * @return Retorna o valor do ganho e se houve o ganho
	 */
	public static double[] verificarGanho(int x1, int x2, int y1, int y2, int z1, int z2, double[][] w) {
		double peso, maximo;
		double ganho = 0;
		int escolha = 0;
		double[] resultado = new double[2];
		peso = w[x1][x2] + w[y1][y2] + w[z1][z2];
		maximo = peso - (w[y1][x1] + w[z1][x2] + w[z2][y2]);
		if (maximo > ganho) {
			ganho = maximo;
			escolha = 1;
		}
		maximo = peso - (w[x1][y2] + w[z1][x2] + w[y1][z2]);
		if (maximo > ganho) {
			ganho = maximo;
			escolha = 2;
		}
		resultado[0] = ganho;
		resultado[1] = escolha;
		return resultado;
	}

	/**
	 * Efetua o calculo para verificar o ganho obtido entre as trocas
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param z1
	 * @param z2
	 * @param w
	 * @return Retorna o valor do ganho e se houve o ganho
	 */
	public int realizarTroca(int x, int y, int z, double[][] w) {
		double custoTemp;
		double custoOriginal = Utilidades.calculaCustoRota(w, nodes);
		double melhorCusto = custoOriginal;

		// operação varia de 0 a 7 com as trocas 2 a 2 (1,2,3) e com as trocas 3 a 3
		// (4,5,6,7)

		// Operação 04
		List<Integer> route4 = new ArrayList<Integer>();
		route4.add(nodes.get(0));
		route4.addAll(getTrecho(0, x, false));
		route4.add(nodes.get(x));
		route4.add(nodes.get(z));
		route4.addAll(getTrecho(z, y + 1, true));
		if (y + 1 != z)
			route4.add(nodes.get(y + 1));
		route4.add(nodes.get(x + 1));
		route4.addAll(getTrecho(x + 1, y, false));
		if (x + 1 != y)
			route4.add(nodes.get(y));
		route4.add(nodes.get(z + 1));
		route4.addAll(getTrecho(z + 1, 0, false));
		route4.add(nodes.get(nodes.size() - 1));
		custoTemp = Utilidades.calculaCustoRota(w, route4);
		if (custoTemp < melhorCusto) {
			nodes = new ArrayList<>(route4);
			melhorCusto = custoTemp;
			return 4;
		}

		// Operação 05
		List<Integer> route5 = new ArrayList<Integer>();
		route5.add(nodes.get(0));
		route5.addAll(getTrecho(0, x, false));
		route5.add(nodes.get(x));
		route5.add(nodes.get(y));
		route5.addAll(getTrecho(y, x + 1, true));
		if (x + 1 != y)
			route5.add(nodes.get(x + 1));
		route5.add(nodes.get(z));
		route5.addAll(getTrecho(z, y + 1, true));
		if (y + 1 != z)
			route5.add(nodes.get(y + 1));
		route5.add(nodes.get(z + 1));
		route5.addAll(getTrecho(z + 1, 0, false));
		route5.add(nodes.get(nodes.size() - 1));
		custoTemp = Utilidades.calculaCustoRota(w, route5);
		if (custoTemp < melhorCusto) {
			nodes = new ArrayList<>(route5);
			melhorCusto = custoTemp;
			return 5;
		}

		// Operação 06
		List<Integer> route6 = new ArrayList<Integer>();
		route6.add(nodes.get(0));
		route6.addAll(getTrecho(0, x, false));
		route6.add(nodes.get(x));
		route6.add(nodes.get(y + 1));
		route6.addAll(getTrecho(y + 1, z, false));
		if (y + 1 != z)
			route6.add(nodes.get(z));
		route6.add(nodes.get(y));
		route6.addAll(getTrecho(y, x + 1, true));
		if (x + 1 != y)
			route6.add(nodes.get(x + 1));
		route6.add(nodes.get(z + 1));
		route6.addAll(getTrecho(z + 1, 0, false));
		route6.add(nodes.get(nodes.size() - 1));
		custoTemp = Utilidades.calculaCustoRota(w, route6);
		if (custoTemp < melhorCusto) {
			nodes = new ArrayList<>(route6);
			melhorCusto = custoTemp;
			return 6;
		}

		// Operação 07
		List<Integer> route7 = new ArrayList<Integer>();
		route7.add(nodes.get(0));
		route7.addAll(getTrecho(0, x, false));
		route7.add(nodes.get(x));
		route7.add(nodes.get(y + 1));
		route7.addAll(getTrecho(y + 1, z, false));
		if (y + 1 != z)
			route7.add(nodes.get(z));
		route7.add(nodes.get(x + 1));
		route7.addAll(getTrecho(x + 1, y, false));
		if (x + 1 != y)
			route7.add(nodes.get(y));
		route7.add(nodes.get(z + 1));
		route7.addAll(getTrecho(z + 1, 0, false));
		route7.add(nodes.get(nodes.size() - 1));
		custoTemp = Utilidades.calculaCustoRota(w, route7);
		if (custoTemp < melhorCusto) {
			nodes = new ArrayList<>(route7);
			melhorCusto = custoTemp;
			return 7;
		}

		return 0;

	}

	private List<Integer> getTrecho(int begin, int end, boolean invert) {
		List<Integer> trecho = new ArrayList<Integer>();
		// Inicio e nos intermediarios da rota
		if (Math.abs(begin - end) > 2 && end != 0) {
			if (begin > end) {
				trecho.addAll(nodes.subList(end + 1, begin));
			} else {
				trecho.addAll(nodes.subList(begin + 1, end));
			}
			if (invert) {
				Collections.reverse(trecho);
			}
		} else if (Math.abs(begin - end) == 2 && end != 0) {
			if (begin > end) {
				trecho.add(nodes.get(begin - 1));
			} else {
				trecho.add(nodes.get(begin + 1));
			}
		}
		// Fim da rota
		if (begin < nodes.size() - 3 && end == 0) {
			trecho.addAll(nodes.subList(begin + 1, nodes.size() - 1));
			if (invert) {
				Collections.reverse(trecho);
			}
		} else if (begin == nodes.size() - 3 && end == 0) {
			trecho.add(nodes.get(begin + 1));
		}
		return trecho;
	}

}
