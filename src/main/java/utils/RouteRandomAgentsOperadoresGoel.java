package utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import optmizations.HeuristicaMelhoria;
import optmizations.HeuristicaMelhoria3opt;


public class RouteRandomAgentsOperadoresGoel extends RouteRandomDinamico {
	
	//TODO BANCO DE DADOS SERÀ O MONGODB
	PreparedStatement psRetirada, psInsercao;
	Statement stmtRetirada;
	String queryRetirada, queryInsercao, queryEventos, queryAtualizaSeq;

	public RouteRandomAgentsOperadoresGoel(NetworkRandom net, int fxV, int fxH, double[] pontosX, double[] pontosY)
			throws SQLException {
		super(net, fxV, fxH, pontosX, pontosY);

		// Preparação para criação de pontos
		queryInsercao = "INSERT INTO visitas(cods, codc, zonai, zonaj, ptx, pty)" + "VALUES (?, ?, ?, ?, ?, ?);";

		// Preparação para retirada do roteiro
		queryRetirada = "delete from visitas " + "WHERE cods=? and codc=? and zonai=? and zonaj=? and ptx=? and pty=?";

		queryEventos = "INSERT INTO eventos(cods, codc, zonai, zonaj, ptx, pty, instante, evento) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

		queryAtualizaSeq = "update visitas set ordemnarota=?"
				+ "WHERE cods=? and codc=? and zonai=? and zonaj=? and ptx=? and pty=?";
	}

	@Override
	public List<Integer> identificaTarefasParaTransferir() {
		List<Integer> tarefasParaTransferir = new ArrayList<Integer>();
		List<Integer> rotaEstimada = new ArrayList<Integer>();
		if ((visitaAtual + 1) < (rota.size() - 2)) {
			rotaEstimada = new ArrayList<Integer>(rota.subList(visitaAtual, rota.size()));
		}

		if (rotaEstimada.size() > 0) {
			// Calcula distancias ao centro de massa
			HashMap<Integer, Double> custos = calcOperadorRemocao(rota.subList(visitaAtual + 2, rota.size() - 1));
			// System.out.println(distCM);
			custos = Utilidades.sortHashMapByValues(custos);
			// System.out.println(distCM);

			// Verificar o valor esperado dos próximos tempos
			double tp = tempoGasto + estimaTempoRestanteRota(rotaEstimada);
			while ((tp > tempoDisponivel) && (custos.size() > 0)) {
				Integer ptMenorCusto = (Integer) custos.keySet().toArray()[0];
				// System.out.println(ptMaiProxCM);
				tarefasParaTransferir.add(ptMenorCusto);
				// remove ponto mais próximo do centro de massa
				custos.remove(ptMenorCusto);
				rotaEstimada.remove(ptMenorCusto);
				tp = tempoGasto + estimaTempoRestanteRota(rotaEstimada);
			}

			// Remover última visita que representa o retorno ao depósito
			// System.out.println("TAREFAS PARA TRANSFERIR");
			// System.out.println(tarefasParaTransferir);
			// tarefasParaTransferir.remove(tarefasParaTransferir.size());
			// System.out.println(tarefasParaTransferir);
		}
		return tarefasParaTransferir;
	}

	/**
	 * Método que implementa o operador de exclusão de Goel, que observa a redução
	 * da rota em eliminar-se uma visita
	 * 
	 * @param tarefasNaoRealizadas lista dos pontos que faltam ser visitados
	 * @return custos hashmap com lista de possíveis custos com a eliminação de cada
	 *         tarefa pendente
	 */
	public HashMap<Integer, Double> calcOperadorRemocao(List<Integer> tarefasNaoRealizadas) {
		double vlCusto;
		Integer[] a;
		HashMap<Integer, Double> custos = new HashMap<Integer, Double>();
		// Para cada tarefa não realizada
		for (Integer s : tarefasNaoRealizadas) {
			vlCusto = 0;
			List<Integer> rotaTemp = new ArrayList<Integer>(rota);
			rotaTemp.remove(rotaTemp.indexOf(s));
			vlCusto = Utilidades.calculaCustoRota(matrizDistanciaCorrigida, rotaTemp);
			custos.put(s, vlCusto);
		}
		return custos;
	}

	@Override
	public void executaTransferencia(List<Integer> tarefasParaTransferir) {
	}

	@Override
	public void removerDaRota(Double ptX) throws SQLException {

		int tarefa = Utilidades.inArray(pontoCartesianoSelecionadosX, ptX);
		// int tarefa2 = tarefa + 1;

		if (tarefa > 0) {
			if (con.isClosed()) {
				con = dataSource.getConnection();
			}
			psRetirada = con.prepareStatement(queryRetirada);
			psRetirada.setInt(1, codSimulacao);
			psRetirada.setInt(2, codCiclo);
			psRetirada.setInt(3, faixaV);
			psRetirada.setInt(4, faixaH);
			psRetirada.setDouble(5, pontoCartesianoSelecionadosX[tarefa]);
			psRetirada.setDouble(6, pontoCartesianoSelecionadosY[tarefa]);
			psRetirada.addBatch();
			psRetirada.executeBatch();

			psRetirada = con.prepareStatement(queryEventos);
			psRetirada.setInt(1, codSimulacao);
			psRetirada.setInt(2, codCiclo);
			psRetirada.setInt(3, faixaV);
			psRetirada.setInt(4, faixaH);
			psRetirada.setDouble(5, pontoCartesianoSelecionadosX[tarefa]);
			psRetirada.setDouble(6, pontoCartesianoSelecionadosY[tarefa]);
			psRetirada.setDouble(7, tempoGasto);
			psRetirada.setString(8, "DELETE");
			psRetirada.addBatch();
			psRetirada.executeBatch();

			// System.out.println("Rota antes da retirada: "+rota.toString());
			// System.out.println("Tarefas tranferidas
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Double.toString(pontoCartesianoSelecionadosX[tarefa]));
			// System.out.println("PontosX original
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Arrays.toString(pontoCartesianoSelecionadosX));

			// nrVisitas--;
			// remover pontos de pontoCartesianoSelecionadosX e pontoCartesianoSelecionadosY
			double[] pontosXtemp = new double[pontoCartesianoSelecionadosX.length - 1];
			double[] pontosYtemp = new double[pontoCartesianoSelecionadosY.length - 1];
			double[] temposDeViagemTemp = new double[pontoCartesianoSelecionadosX.length + 1]; // inclui retorno ao
																								// depósito
			double[] temposDeServicoTemp = new double[pontoCartesianoSelecionadosX.length + 1]; // inclui retorno ao
																								// depósito
			double[] velDeViagemTemp = new double[pontoCartesianoSelecionadosX.length + 1]; // inclui retorno ao
																							// depósito
			double[][] matrizDistanciaTemp = new double[pontoCartesianoSelecionadosX.length][pontoCartesianoSelecionadosX.length];
			double[][] matrizDistanciaCorrigidaTemp = new double[pontoCartesianoSelecionadosX.length][pontoCartesianoSelecionadosX.length];

			System.arraycopy(temposDeViagem, 0, temposDeViagemTemp, 0, pontoCartesianoSelecionadosX.length);
			System.arraycopy(temposDeServico, 0, temposDeServicoTemp, 0, pontoCartesianoSelecionadosX.length);
			System.arraycopy(velDeViagem, 0, velDeViagemTemp, 0, pontoCartesianoSelecionadosX.length);
			System.arraycopy(pontoCartesianoSelecionadosX, 0, pontosXtemp, 0, tarefa);
			// System.out.println("PontosX parte 1
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Arrays.toString(pontosXtemp));
			System.arraycopy(pontoCartesianoSelecionadosY, 0, pontosYtemp, 0, tarefa);
			System.arraycopy(pontoCartesianoSelecionadosX, tarefa + 1, pontosXtemp, tarefa,
					pontoCartesianoSelecionadosX.length - tarefa - 1);
			// System.out.println("PontosX parte 2
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Arrays.toString(pontosXtemp));
			System.arraycopy(pontoCartesianoSelecionadosY, tarefa + 1, pontosYtemp, tarefa,
					pontoCartesianoSelecionadosY.length - tarefa - 1);

			pontoCartesianoSelecionadosX = new double[pontoCartesianoSelecionadosX.length - 1];
			pontoCartesianoSelecionadosY = new double[pontoCartesianoSelecionadosY.length - 1];
			// System.out.println("PontosX redefinido
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Arrays.toString(pontoCartesianoSelecionadosX));
			temposDeViagem = new double[pontoCartesianoSelecionadosX.length + 2];
			temposDeServico = new double[pontoCartesianoSelecionadosX.length + 2];
			velDeViagem = new double[pontoCartesianoSelecionadosX.length + 2];
			System.arraycopy(pontosXtemp, 0, pontoCartesianoSelecionadosX, 0, pontosXtemp.length);
			System.arraycopy(pontosYtemp, 0, pontoCartesianoSelecionadosY, 0, pontosYtemp.length);
			System.arraycopy(temposDeViagemTemp, 0, temposDeViagem, 0, pontoCartesianoSelecionadosX.length + 1);
			System.arraycopy(temposDeServicoTemp, 0, temposDeServico, 0, pontoCartesianoSelecionadosX.length + 1);
			System.arraycopy(velDeViagemTemp, 0, velDeViagem, 0, pontoCartesianoSelecionadosX.length + 1);

			// System.out.println(ptX);
			// System.out.println("PontosX alterado
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Arrays.toString(pontoCartesianoSelecionadosX));
			// System.out.println("PontosY alterado
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
			// Arrays.toString(pontoCartesianoSelecionadosY));
			matrizDistanciaTemp = matrizDistancia;
			matrizDistanciaCorrigidaTemp = matrizDistanciaCorrigida;
			matrizDistancia = new double[pontoCartesianoSelecionadosX.length][pontoCartesianoSelecionadosX.length];
			matrizDistanciaCorrigida = new double[pontoCartesianoSelecionadosX.length][pontoCartesianoSelecionadosX.length];
			int m, n;
			for (int i = 0; i < matrizDistanciaCorrigida.length; i++) {
				for (int j = 0; j < matrizDistanciaCorrigida.length; j++) {
					if (i >= tarefa) {
						m = i + 1;
					} else {
						m = i;
					}
					if (j >= tarefa) {
						n = j + 1;
					} else {
						n = j;
					}
					matrizDistancia[i][j] = matrizDistanciaTemp[m][n];
					matrizDistanciaCorrigida[i][j] = matrizDistanciaCorrigidaTemp[m][n];
				}
			}
			/*
			 * matrizDistancia = new
			 * double[pontoCartesianoSelecionadosX.length-1][pontoCartesianoSelecionadosX.
			 * length-1]; matrizDistanciaCorrigida = new
			 * double[pontoCartesianoSelecionadosX.length-1][pontoCartesianoSelecionadosX.
			 * length-1]; matrizDistancia = matrizDistanciaTemp; matrizDistanciaCorrigida =
			 * matrizDistanciaCorrigidaTemp;
			 */

			// atualizando os indices de rota
			rota.remove(rota.indexOf(tarefa));
			// System.out.println("Rota após a retirada: "+rota.toString());
			for (int i = 0; i < rota.size(); i++) {
				if (rota.get(i) > tarefa) {
					rota.set(i, rota.get(i) - 1);
				}
			}

			// System.out.println("Rota após a atualização: "+rota.toString());
		}
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
		double custoEstimadoRotaExistente, custoEstimadoNovaRota;
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
		// double custoEstimadoRotaExistente =
		// Utilidades.calculaCustoRota(matrizDistanciaCorrigida, rotaTemp);

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

		// Se visitaAtual é última do roteiro atual, então basta definir a rota como
		// rotaTemp, caso contrário, otimizar com genético
		List<Integer> rotaEstimada = new ArrayList<Integer>();
		if (visitaAtual == tamanhoAntesDaInsercao - 1) {
			rotaEstimada.addAll(rotaTemp);
			custoEstimadoRotaExistente = Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp,
					rota.subList(visitaAtual, rota.size()));
			custoEstimadoNovaRota = Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp,
					rotaTemp.subList(visitaAtual, rotaTemp.size()));
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

			custoEstimadoRotaExistente = Utilidades.calculaCustoRota(matrizDistanciaCorrigida,
					rota.subList(visitaAtual, rota.size()));
			custoEstimadoNovaRota = Utilidades.calculaCustoRota(matrizDistanciaCorrigidaTemp, rotaH);

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

	public void inserirNaRota(double ptX, double ptY) throws SQLException {

		int tamanhoAntesDaInsercao = pontoCartesianoSelecionadosX.length;
		// System.out.println("Rota Atual: "+rota.toString());
		// System.out.println("Visita atual: "+Integer.toString(visitaAtual));
		// System.out.println("Pontos X
		// selecionados:"+Arrays.toString(pontoCartesianoSelecionadosX));
		// System.out.println("Ponto X para adicionar: "+ptX);

		// Criação de arrays temporários
		double[] pontosXtemp = new double[tamanhoAntesDaInsercao + 1];
		double[] pontosYtemp = new double[tamanhoAntesDaInsercao + 1];
		double[] temposDeViagemTemp = new double[tamanhoAntesDaInsercao + 2]; // inclui retorno ao depósito
		double[] temposDeServicoTemp = new double[tamanhoAntesDaInsercao + 2]; // inclui retorno ao depósito
		double[] velDeViagemTemp = new double[tamanhoAntesDaInsercao + 2]; // inclui retorno ao depósito
		System.arraycopy(pontoCartesianoSelecionadosX, 0, pontosXtemp, 0, tamanhoAntesDaInsercao);
		System.arraycopy(pontoCartesianoSelecionadosY, 0, pontosYtemp, 0, tamanhoAntesDaInsercao);
		System.arraycopy(temposDeViagem, 0, temposDeViagemTemp, 0, tamanhoAntesDaInsercao + 1);
		System.arraycopy(temposDeServico, 0, temposDeServicoTemp, 0, tamanhoAntesDaInsercao + 1);
		System.arraycopy(velDeViagem, 0, velDeViagemTemp, 0, tamanhoAntesDaInsercao + 1);
		pontosXtemp[tamanhoAntesDaInsercao] = ptX;
		pontosYtemp[tamanhoAntesDaInsercao] = ptY;

		// atualização de tamanho dos vetores originais
		pontoCartesianoSelecionadosX = new double[tamanhoAntesDaInsercao + 1];
		pontoCartesianoSelecionadosY = new double[tamanhoAntesDaInsercao + 1];
		temposDeViagem = new double[tamanhoAntesDaInsercao + 2];
		temposDeServico = new double[tamanhoAntesDaInsercao + 2];
		velDeViagem = new double[tamanhoAntesDaInsercao + 2];
		double[][] matrizDistanciaTemp = new double[tamanhoAntesDaInsercao + 1][tamanhoAntesDaInsercao + 1];
		double[][] matrizDistanciaCorrigidaTemp = new double[tamanhoAntesDaInsercao + 1][tamanhoAntesDaInsercao + 1];
		System.arraycopy(pontosXtemp, 0, pontoCartesianoSelecionadosX, 0, tamanhoAntesDaInsercao + 1);
		System.arraycopy(pontosYtemp, 0, pontoCartesianoSelecionadosY, 0, tamanhoAntesDaInsercao + 1);
		System.arraycopy(temposDeViagemTemp, 0, temposDeViagem, 0, tamanhoAntesDaInsercao + 1);
		System.arraycopy(temposDeServicoTemp, 0, temposDeServico, 0, tamanhoAntesDaInsercao + 1);
		System.arraycopy(velDeViagemTemp, 0, velDeViagem, 0, tamanhoAntesDaInsercao + 1);

		// atualização da matriz de distâncias
		matrizDistanciaTemp = matrizDistancia;
		//TODO não seira o contrário?
		matrizDistanciaCorrigidaTemp = matrizDistanciaCorrigida;
		matrizDistancia = Utilidades.definirMatrizDistancia(pontoCartesianoSelecionadosX, pontoCartesianoSelecionadosY);
		//TODO não seira o contrário?
		matrizDistanciaCorrigida = Utilidades.correcaoMatrizDistancia(matrizDistancia, distAdjustDistance);
		for (int i = 0; i < matrizDistanciaCorrigidaTemp.length; i++) {
			for (int j = 0; j < matrizDistanciaCorrigidaTemp.length; j++) {
				matrizDistanciaCorrigida[i][j] = matrizDistanciaCorrigidaTemp[i][j];
			}
		}

		// System.out.println("Rota Atual
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): "+rota.toString());

		// Removendo o retorno ao deposito da rota temporaria
		int aux = rota.get(rota.size() - 1);
		rota.remove(rota.size() - 1);
		// Adicionando o novo ponto a rota temporaria
		rota.add(tamanhoAntesDaInsercao);
		// Adicionando o retorno ao deposito da rota temporaria
		rota.add(aux);
		// System.out.println("Rota com ponto adicional antes do genetico de
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): "+rota.toString());
		// nrVisitas++;

		// Se visitaAtual não é última do roteiro atual, então otimizar
		if (visitaAtual < tamanhoAntesDaInsercao - 1) {

			// Criação de rota melhorada
			List<Integer> rotaMelhorada = new ArrayList<Integer>();
			// Inserção da rota já realizada
			rotaMelhorada = rota.subList(0, visitaAtual);

			/*
			 * // Montagem dos pontos restantes que precisam ser visitados, com o último e
			 * com o retorno ao depósito double[] ptXrestantes = new
			 * double[tamanhoAntesDaInsercao-visitaAtual+2]; double[] ptYrestantes = new
			 * double[tamanhoAntesDaInsercao-visitaAtual+2]; for(int
			 * i=0;i<(tamanhoAntesDaInsercao-visitaAtual+1);i++){ ptXrestantes[i] =
			 * pontosXtemp[rota.get(visitaAtual+i)]; ptYrestantes[i] =
			 * pontosXtemp[rota.get(visitaAtual+i)]; }
			 * //System.out.println("Vetor ptXrestantes ("+Integer.toString(faixaV)+Integer.
			 * toString(faixaH)+"): "+Arrays.toString(ptXrestantes));
			 * //System.out.println("PONTO X NOVO ("+Integer.toString(faixaV)+Integer.
			 * toString(faixaH)+"): "+Double.toString(ptX));
			 * 
			 * // Rota de genetico para o novo roteiro TSPProblem problem = new
			 * TSPProblemWithExtremesFixed(ptXrestantes,ptYrestantes); CrossoverOperator
			 * crossoverOperator = new OrdenatedWithExtremesFixedCrossover();
			 * MutationOperator mutationOperator = new
			 * ShiftWithExtremesFixedMutationOperator(); Selector reproductionSelector = new
			 * RouletteSelector(); Selector surviveSelector = new DeterministicSelector();
			 * GeneticAlgorithm ga = new GeneticAlgorithm(problem, crossoverOperator,
			 * mutationOperator, reproductionSelector, surviveSelector); ga.setMax_it(1000);
			 * ga.setPopulationSize(100); //ga.setPopulationSize(25);
			 * //ga.setMutationProbability(0.10); ga.setMutationProbability(0.20);
			 * //ga.setCrossoverProbability(0.75); ga.setCrossoverProbability(0.90);
			 * OptimizationResult r = ga.run();
			 * 
			 * // Inserção da rota restante List<Integer> sol =
			 * r.getBestSolution().getRota();
			 * //System.out.println("Rota GA  ("+Integer.toString(faixaV)+Integer.toString(
			 * faixaH)+"): "+sol.toString());
			 * 
			 * // atualizar indices restantes para indices do vetor selecionado original,
			 * isto acontece porque a rotina do genetico retorna os indices de ptXrestantes
			 * e não de pontoCartesianoSelecionadosX List<Integer> solAjustada = new
			 * ArrayList<Integer>(); for (Integer i : sol){
			 * solAjustada.add(Utilidades.inArray(pontoCartesianoSelecionadosX,
			 * ptXrestantes[i-1])); } rotaMelhorada.addAll(solAjustada);
			 * 
			 * System.out.println("Rota GA  ("+Integer.toString(faixaV)+Integer.toString(
			 * faixaH)+"): "+solAjustada.toString());
			 * System.out.println("Rota GA cost ("+Integer.toString(faixaV)+Integer.toString
			 * (faixaH)+"): "+Double.toString(Utilidades.calculaCustoRota(
			 * matrizDistanciaCorrigida, solAjustada)));
			 * 
			 */

			// Rota 3opt para o novo roteiro
			HeuristicaMelhoria hm = new HeuristicaMelhoria3opt();
			List<Integer> rotaH = hm.solve(matrizDistanciaCorrigida, rota.subList(visitaAtual, rota.size()));
			rotaMelhorada.addAll(rotaH);

			// System.out.println("Rota Heuristica Composta
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): "+rotaH.toString());
			// System.out.println("Rota Heuristica Composta cost
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
			// "+Double.toString(Utilidades.calculaCustoRota(matrizDistanciaCorrigida,
			// rotaH)));

			rota = new ArrayList<Integer>(rotaMelhorada);
			System.out.println("Rota Heuristica (" + Integer.toString(faixaV) + Integer.toString(faixaH) + "): "
					+ rota.toString());
		}

		// Atualizações do banco de dados

		// Verificar rota resultante
		if (con.isClosed()) {
			con = dataSource.getConnection();
		}

		// Inserir na tabela de visitas
		psInsercao = con.prepareStatement(queryInsercao);
		psInsercao.setInt(1, codSimulacao);
		psInsercao.setInt(2, codCiclo);
		psInsercao.setInt(3, faixaV);
		psInsercao.setInt(4, faixaH);
		psInsercao.setDouble(5, ptX);
		psInsercao.setDouble(6, ptY);
		psInsercao.addBatch();
		psInsercao.executeBatch();

		// Inserir na tabela de eventos
		psInsercao = con.prepareStatement(queryEventos);
		psInsercao.setInt(1, codSimulacao);
		psInsercao.setInt(2, codCiclo);
		psInsercao.setInt(3, faixaV);
		psInsercao.setInt(4, faixaH);
		psInsercao.setDouble(5, ptX);
		psInsercao.setDouble(6, ptY);
		psInsercao.setDouble(7, tempoGasto);
		psInsercao.setString(8, "INSERT");
		psInsercao.addBatch();
		psInsercao.executeBatch();

		// Atualizar a sequencia da rota das visitas
		// System.out.println("PontosX alterado
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): " +
		// Arrays.toString(pontoCartesianoSelecionadosX));
		psInsercao = con.prepareStatement(queryAtualizaSeq);
		for (int i = 1; i < pontoCartesianoSelecionadosX.length; i++) {
			psInsercao.setInt(1, rota.indexOf(i));
			psInsercao.setInt(2, codSimulacao);
			psInsercao.setInt(3, codCiclo);
			psInsercao.setInt(4, faixaV);
			psInsercao.setInt(5, faixaH);
			psInsercao.setDouble(6, pontoCartesianoSelecionadosX[i]);
			psInsercao.setDouble(7, pontoCartesianoSelecionadosY[i]);
			psInsercao.addBatch();
			psInsercao.executeBatch();
		}
	}

	public void gravaEstatisticasDaRota() throws SQLException {

		// se a simulação for deste tipo, basea-se em agentes, e se a rota for auxiliar,
		// deve ser decrementado as variavei contadoras abaixo
		if (faixaV == 99999) {
			visitaAtual--;
			totVisitasPlanejadas--;
		}

		// Gravação das estatisitcas da rota
		PreparedStatement ps = null;
		String query = "INSERT INTO rotas(cods, codc, zonai, zonaj, totvisitasplanejadas, totvisitasexecutadas, tempociclofornecimento, distviajada ) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

		ps = con.prepareStatement(query);
		ps.setInt(1, codSimulacao);
		ps.setInt(2, codCiclo);
		ps.setInt(3, faixaV);
		ps.setInt(4, faixaH);
		ps.setInt(5, totVisitasPlanejadas); // subtrai-se o ponto de origem que é um depósito e não uma visita
		ps.setInt(6, visitaAtual);
		ps.setDouble(7, tempoGasto);
		ps.setDouble(8, distViajada);
		ps.addBatch();
		ps.executeBatch();

		con.close();

	}
}
