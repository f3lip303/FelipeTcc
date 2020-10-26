package utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import optmizations.HeuristicaMelhoria;
import optmizations.HeuristicaMelhoria3opt;


public class RouteRandomAgentsOperadoresGoelEquilibrio extends RouteRandomAgentsOperadoresGoel {

	public RouteRandomAgentsOperadoresGoelEquilibrio(NetworkRandom net, int fxV, int fxH, double[] pontosX,
			double[] pontosY) throws SQLException {
		super(net, fxV, fxH, pontosX, pontosY);

	}

	/**
	 * Função que estima a inserção de um ponto na rota através de Genetetic
	 * Algoritms
	 * 
	 * @param rotaMomento   rota no momento da estimativa
	 * @param visitaMomento visita no momento da estimativa
	 * @param ptX           coordenada X do ponto inserido
	 * @param ptY           coordenada Y do ponto inserido
	 * @return O acréscimo na distância entre os pontos pesquisados
	 */
	public double estimarInserirNaRota(double ptX, double ptY) {
		int tamanhoAntesDaInsercao = pontoCartesianoSelecionadosX.length;
		double custoEstimado = 0;
		double[] pontosXtemp = new double[tamanhoAntesDaInsercao + 1];
		double[] pontosYtemp = new double[tamanhoAntesDaInsercao + 1];
		double[][] matrizDistanciaTemp = new double[tamanhoAntesDaInsercao + 1][tamanhoAntesDaInsercao + 1];
		double[][] matrizDistanciaCorrigidaTemp = new double[tamanhoAntesDaInsercao + 1][tamanhoAntesDaInsercao + 1];
		List<Integer> rotaTemp = new ArrayList<Integer>(rota);
		List<Integer> solAjustada = new ArrayList<Integer>();

		System.arraycopy(pontoCartesianoSelecionadosX, 0, pontosXtemp, 0, tamanhoAntesDaInsercao);
		System.arraycopy(pontoCartesianoSelecionadosY, 0, pontosYtemp, 0, tamanhoAntesDaInsercao);
		pontosXtemp[tamanhoAntesDaInsercao] = ptX;
		pontosYtemp[tamanhoAntesDaInsercao] = ptY;

		// copiando valores já existentes da matriz de distancias conhecida, para evitar
		// que os mesmos pontos de
		// matrizDistanciaCorrigidaTemp tenham valores diferentes de
		// matrizDistanciaCorrigida
		matrizDistanciaTemp = Utilidades.definirMatrizDistancia(pontosXtemp, pontosYtemp);
		matrizDistanciaCorrigidaTemp = Utilidades.correcaoMatrizDistancia(matrizDistanciaTemp, distAdjustDistance);

		for (int i = 0; i < matrizDistanciaCorrigida.length; i++) {
			for (int j = 0; j < matrizDistanciaCorrigida.length; j++) {
				matrizDistanciaCorrigidaTemp[i][j] = matrizDistanciaCorrigida[i][j];
			}
		}
		/*
		 * if (faixaH==2 && faixaV==2){
		 * System.out.println("E Matriz ("+Integer.toString(faixaV)+Integer.toString(
		 * faixaH)+"): " + Arrays.deepToString(matrizDistanciaCorrigida));
		 * System.out.println("E Matriz Temp ("+Integer.toString(faixaV)+Integer.
		 * toString(faixaH)+"): " + Arrays.deepToString(matrizDistanciaCorrigidaTemp));
		 * }
		 */

		// double custoEstimadoRotaExistente =
		// Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp,
		// rotaTemp.subList(visitaAtual, rotaTemp.size()));
		double custoEstimadoRotaExistente = Utilidades.calculaCustoRota(matrizDistanciaCorrigida, rotaTemp);

		// Removendo o retorno ao deposito da rota temporaria
		rotaTemp.remove(rotaTemp.size() - 1);
		// Adicionando o novo ponto a rota temporaria
		rotaTemp.add(rotaTemp.size());
		// Adicionando o retorno ao deposito da rota temporaria
		rotaTemp.add(0);

		// double custoEstimadoNovaRota =
		// Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp,
		// rotaTemp.subList(visitaAtual, rotaTemp.size()));
		// System.out.println("visita atual
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
		// "+Integer.toString(visitaAtual));
		// System.out.println("dimensao matriz antes
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
		// "+Integer.toString(matrizDistanciaCorrigida.length));
		// System.out.println("dimensao matriz depois
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
		// "+Integer.toString(matrizDistanciaCorrigidaTemp.length));
		// System.out.println("rotaTemp
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
		// "+rotaTemp.toString());

		double custoEstimadoNovaRota = Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp, rotaTemp);

		// Se visitaAtual é última do roteiro atual, então basta definir a rota como
		// rotaTemp, caso contrário, otimizar com genético
		List<Integer> rotaEstimada = new ArrayList<Integer>();
		if (visitaAtual == tamanhoAntesDaInsercao - 1) {
			rotaEstimada.addAll(rotaTemp);
		} else {

			// Inserção da rota já realizada
			rotaEstimada = rotaTemp.subList(0, visitaAtual);
			// System.out.println("Rota realizada até o momento
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
			// "+rotaEstimada.toString());

			/*
			 * // Montagem dos pontos restantes que precisam ser visitados double[]
			 * ptXrestantes = new double[tamanhoAntesDaInsercao-visitaAtual+2]; double[]
			 * ptYrestantes = new double[tamanhoAntesDaInsercao-visitaAtual+2]; for(int
			 * i=0;i<(tamanhoAntesDaInsercao-visitaAtual+1);i++){ ptXrestantes[i] =
			 * pontosXtemp[rotaTemp.get(visitaAtual+i)]; ptYrestantes[i] =
			 * pontosXtemp[rotaTemp.get(visitaAtual+i)]; }
			 * 
			 * //ptYrestantes[tamanhoAntesDaInsercao-visitaAtual]=;
			 * 
			 * System.out.println("E pontoCartesianoSelecionadosX ("+Integer.toString(faixaV
			 * )+Integer.toString(faixaH)+"): "+Arrays.toString(pontoCartesianoSelecionadosX
			 * )); System.out.println("E pontosXtemp  ("+Integer.toString(faixaV)+Integer.
			 * toString(faixaH)+"): "+Arrays.toString(pontosXtemp));
			 * System.out.println("E rota temp  ("+Integer.toString(faixaV)+Integer.toString
			 * (faixaH)+"): "+rotaTemp.toString());
			 * System.out.println("E visita atual ("+Integer.toString(faixaV)+Integer.
			 * toString(faixaH)+"): "+Integer.toString(visitaAtual));
			 * System.out.println("E ptXrestantes ("+Integer.toString(faixaV)+Integer.
			 * toString(faixaH)+"): "+Arrays.toString(ptXrestantes));
			 * System.out.println("PONTO X NOVO ("+Integer.toString(faixaV)+Integer.toString
			 * (faixaH)+"): "+Double.toString(ptX));
			 */
			// Rota de genetico para o novo roteiro
			/*
			 * TSPProblem problem = new
			 * TSPProblemWithExtremesFixed(ptXrestantes,ptYrestantes); CrossoverOperator
			 * crossoverOperator = new OrdenatedWithExtremesFixedCrossover();
			 * MutationOperator mutationOperator = new
			 * ShiftWithExtremesFixedMutationOperator(); Selector reproductionSelector = new
			 * RouletteSelector(); Selector surviveSelector = new DeterministicSelector();
			 * GeneticAlgorithm ga = new GeneticAlgorithm(problem, crossoverOperator,
			 * mutationOperator, reproductionSelector, surviveSelector); ga.setMax_it(1000);
			 * ga.setPopulationSize(100); ga.setMutationProbability(0.10);
			 * ga.setCrossoverProbability(0.75); OptimizationResult r = ga.run(); //
			 * Inserção da rota restante List<Integer> sol = r.getBestSolution().getRota();
			 * 
			 * // atualizar indices restantes para indices do vetor selecionado original for
			 * (Integer i : sol){ solAjustada.add(Utilidades.inArray(pontosXtemp,
			 * ptXrestantes[i-1])); } rotaEstimada.addAll(solAjustada);
			 * 
			 */

			// Rota 3opt para o novo roteiro
			HeuristicaMelhoria hm = new HeuristicaMelhoria3opt();
			List<Integer> rotaH = hm.solve(matrizDistanciaCorrigidaTemp,
					rotaTemp.subList(visitaAtual, rotaTemp.size()));
			rotaEstimada.addAll(rotaH);

			// custoEstimadoNovaRota =
			// Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp, solAjustada);
			custoEstimadoNovaRota = Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp, rotaEstimada);

		}

		// Calcular custo da rota estimada
		/*
		 * System.out.println("E pontoCartesianoSelecionadosX ("+Integer.toString(faixaV
		 * )+Integer.toString(faixaH)+"): " +
		 * Arrays.toString(pontoCartesianoSelecionadosX));
		 * System.out.println("E pontosXtemp ("+Integer.toString(faixaV)+Integer.
		 * toString(faixaH)+"): " + Arrays.toString(pontosXtemp));
		 * System.out.println("E rota original  ("+Integer.toString(faixaV)+Integer.
		 * toString(faixaH)+"): "+rota.toString());
		 * System.out.println("E visita atual  ("+Integer.toString(faixaV)+Integer.
		 * toString(faixaH)+"): "+Integer.toString(visitaAtual));
		 * System.out.println("E Rota sublist ("+Integer.toString(faixaV)+Integer.
		 * toString(faixaH)+"): "+rota.subList(visitaAtual, rota.size()).toString());
		 * System.out.println("E Rota estimada sublist ("+Integer.toString(faixaV)+
		 * Integer.toString(faixaH)+"): "+rotaEstimada.subList(visitaAtual,
		 * rotaEstimada.size()).toString());
		 * System.out.println("E Rota solAjustada ("+Integer.toString(faixaV)+Integer.
		 * toString(faixaH)+"): "+solAjustada.toString());
		 */
		return custoEstimadoNovaRota - custoEstimadoRotaExistente;
	}

}
