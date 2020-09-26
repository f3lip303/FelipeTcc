package utils;

import java.util.List;

import model.Visita;

public class Utilidades {

	public static Integer[] inicializaArray(Integer[] myArray, Integer value) {
		for (int i = 0; i < myArray.length; i++) {
			myArray[i] = 0;
		}
		return myArray;
	}

	public static Visita[] copyGrafo(List<Visita> grafo) {
		Visita[] visitas = new Visita[grafo.size()];
		for (int i = 0; i < grafo.size(); i++) {
			visitas[i] = grafo.get(i);
		}
		return visitas;

	}

	public static Visita[] route(Integer[] route, Visita[] visitas) {
		Visita[] rota = new Visita[route.length];
		rota[0] = visitas[0];
		for (int i = 1; i < route.length-1; i++) {
			rota[i] = recVisita(visitas, route[i]);

		}
		rota[route.length-1] = visitas[0];
		return rota;

	}

	private static Visita recVisita(Visita[] visitas, Integer codVisita) {
		for (int i = 0; i < visitas.length; i++) {
			if (visitas[i].getCodigoVisita() == codVisita) {
				return visitas[i];
			}

		}
		return null;

	}

	/**
	 * Função que verifica se um valor double está num array a
	 * 
	 * @param a     : array de double
	 * @param valor : valor a ser pesquisado
	 * @return o índice onde o valor foi encontrado ou -1 caso não tenha sido
	 *         encontrado nenhum valor
	 */
	public static int inArray(Integer[] a, double valor) {
		int isThere = -1;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == valor) {
				isThere = i;
				break;
			}
		}
		return isThere;
	}
}
