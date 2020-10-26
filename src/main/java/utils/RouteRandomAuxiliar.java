package utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



public class RouteRandomAuxiliar extends RouteRandom implements EstrategiaCooperacaoVeiculos{

	protected List<Double> pontosX = new ArrayList<Double>();
	protected List<Double> pontosY = new ArrayList<Double>();
	protected List<Double> pontosTempo = new ArrayList<Double>();
	protected List<Double> pontosTempoOrdenado;
	protected List<Double> pontosXOrdenado;
	protected List<Double> pontosYOrdenado;
	String queryRota;

	// pontosX está armazenado na váriavel pontoCartesianoSelecionadosX
	public RouteRandomAuxiliar(NetworkRandom net, int fxV, int fxH, double[] pontosX, double[] pontosY)
			throws SQLException {
		super(net, false, fxV, fxH, pontosX, pontosY);
		rota = new ArrayList<Integer>();
		adicionaTarefa(0.0, 0.0, 0.0); // inicio do depósito inicial
		adicionaTarefa(0.0, 0.0, 1000); // retorno ao depósito no fim da operação, o tempo alto é apenas para assegurar
										// que esta tarefa estará no fim da "rota"
	}

	/**
	 * Função que adiciona os pontos de origem e destino no roteiro auxiliar
	 * 
	 * @param ptx coordenada do ponto X
	 * @param pty coordenada do ponto Y
	 * @param ptp instante em que o ponto foi adicionado
	 */
	public void adicionaTarefa(double ptx, double pty, double ptp) {
		pontosX.add(ptx);
		pontosY.add(pty);
		pontosTempo.add(ptp);
		// System.out.println(pontosX.toString());
	}

	private void ordenaPontosRecebidosNoTempo() {
		pontosTempoOrdenado = new ArrayList<>();
		pontosXOrdenado = new ArrayList<>();
		pontosYOrdenado = new ArrayList<>();
		pontosTempoOrdenado.addAll(pontosTempo);
		Collections.sort(pontosTempoOrdenado);
		for (int k = 0; k < pontosTempoOrdenado.size(); k++) {
			int i = pontosTempo.indexOf(pontosTempoOrdenado.get(k));
			pontosXOrdenado.add(pontosX.get(i));
			pontosYOrdenado.add(pontosY.get(i));
		}
		System.out.println("Pontos Tempo Ordenado: " + pontosTempoOrdenado.toString());
		System.out.println("Pontos X Ordenado: " + pontosXOrdenado.toString());
		System.out.println("Pontos Y Ordenado: " + pontosYOrdenado.toString());
	}

	/*
	 * Método do roteiro auxiliar é executado após a realização das outras rotas
	 * 
	 * @param r : ciclo de execução
	 * 
	 */
	public void processamento(int r) throws SQLException {

		codCiclo = r;

		pontoCartesianoSelecionadosX = new double[pontosTempo.size()];
		pontoCartesianoSelecionadosY = new double[pontosTempo.size()];
		temposDeViagem = new double[pontosTempo.size() + 1]; // inclui retorno ao depósito
		temposDeServico = new double[pontosTempo.size() + 1]; // inclui retorno ao depósito
		velDeViagem = new double[pontosTempo.size() + 1]; // inclui retorno ao depósito

		iniciaOperacao(codCiclo);

		// Verifica se existem tarefas a serem realizadas
		if (pontosTempo.size() > 2) {

			// System.out.println("Pontos Tempo original: " + pontosTempo.toString());
			// System.out.println("Pontos Tempo ordenados: " +
			// pontosTempoOrdenado.toString());
			// System.out.println("Pontos X original: " + pontosX.toString());
			System.out.println("Pontos X ordenados: " + pontosXOrdenado.toString());

			atualizaRota();
			chegadaAoDistrito();
			atendimento();
			atualizaRota();
			while ((tempoGasto + estimaTempoParaProximaVisita() <= tempoDisponivel)
					&& (visitaAtual < rota.size() - 2)) {
				deslocamento();
				atendimento();
				atualizaRota();
			}
			retornoAoDeposito();

			gravaEstatisticasDaRota();

			System.out.println("Tempos de viagem: " + Arrays.toString(temposDeViagem));
			System.out.println("Tempos de serviço: " + Arrays.toString(temposDeServico));

			System.out.println("Tempo gasto pelo veículo auxiliar: " + Double.toString(tempoGasto));
			System.out.println(
					"Visitas delegadas ao veículo auxiliar: " + Integer.toString(pontosTempoOrdenado.size() - 2));
			System.out.println("Visitas realizadas: " + Integer.toString(visitaAtual));

			con.close();

		}

	}

	public void iniciaOperacao(int cc) throws SQLException {

		con = dataSource.getConnection();

		// Identifica o maior código de simulação
		String query = "select max(id) from simulacao";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			codSimulacao = rs.getInt(1);
		}
		rs.close();

		// Ordenação por ordem de ocorrência de acordo com pontosTempo
		ordenaPontosRecebidosNoTempo();

		// Inserir pontos no banco de dados
		queryRota = "INSERT INTO visitas(cods, codc, zonai, zonaj, ptx, pty) VALUES (?, ?, ?, ?, ?, ?);";
		PreparedStatement psRota = con.prepareStatement(queryRota);
		psRota.setInt(1, codSimulacao);
		psRota.setInt(2, codCiclo);
		psRota.setInt(3, faixaV);
		psRota.setInt(4, faixaH);
		for (int i = 0; i < pontosXOrdenado.size() - 1; i++) {
			psRota.setDouble(5, pontosXOrdenado.get(i));
			psRota.setDouble(6, pontosYOrdenado.get(i));
			psRota.addBatch();
		}
		psRota.executeBatch();
		System.out.println("Inserindo na tabela de visitas os pontos do roteiro auxiliar!!! Faixa: "
				+ Integer.toString(faixaV) + Integer.toString(faixaH) + " Simulacao: " + Integer.toString(codSimulacao)
				+ " Ciclo: " + Integer.toString(codCiclo));

		/*
		 * // inserção no banco do ponto inicial no banco de dados String queryInsercao
		 * =
		 * "INSERT INTO visitas(cods, codc, zonai, zonaj, ptx, pty) VALUES (?, ?, ?, ?, ?, ?);"
		 * ; PreparedStatement psInsercao; psInsercao =
		 * con.prepareStatement(queryInsercao); psInsercao.setInt(1, codSimulacao);
		 * psInsercao.setInt(2, codCiclo); psInsercao.setInt(3, faixaV);
		 * psInsercao.setInt(4, faixaH); psInsercao.setDouble(5, 0.0);
		 * psInsercao.setDouble(6, 0.0); psInsercao.execute();
		 * //psInsercao.executeBatch();
		 * 
		 * // Atualização da ordem da rota no banco de dados antes do início das
		 * operações
		 * 
		 * 
		 * 
		 * System.out.println("Inicia operação (" +
		 * Integer.toString(faixaV)+Integer.toString(faixaH)+")");
		 * 
		 * // Atualização da ordem da rota no banco de dados antes do início das
		 * operações String queryRota2 = "UPDATE visitasplanejadas SET ordemnarota=? "+
		 * "WHERE cods=? and codc=? and zonai=? and zonaj=? and ptx=? and pty=?";
		 * PreparedStatement psRota2 = con.prepareStatement(queryRota2);
		 * psRota2.setInt(2, codSimulacao); psRota2.setInt(3, codCiclo);
		 * psRota2.setInt(4, faixaV); psRota2.setInt(5, faixaH); for (int i=0;
		 * i<rota.size()-1; i++){ psRota2.setDouble(1, i); psRota2.setDouble(6,
		 * pontoCartesianoSelecionadosX[rota.get(i)]); psRota2.setDouble(7,
		 * pontoCartesianoSelecionadosY[rota.get(i)]); psRota2.addBatch(); }
		 * psRota2.executeBatch();
		 */

	}

	public void atualizaRota() {

		// o roteiro do veículo auxiliar inicia ao recebimento da primeira visita
		if (tempoGasto == 0) {
			tempoGasto = pontosTempoOrdenado.get(1);
			pontoCartesianoSelecionadosX[0] = pontosXOrdenado.get(0);
			pontoCartesianoSelecionadosY[0] = pontosYOrdenado.get(0);
			rota.add(0);
			pontoCartesianoSelecionadosX[1] = pontosXOrdenado.get(1);
			pontoCartesianoSelecionadosY[1] = pontosYOrdenado.get(1);
			rota.add(1);
			pontoCartesianoSelecionadosX[2] = pontosXOrdenado.get(0);
			pontoCartesianoSelecionadosY[2] = pontosYOrdenado.get(0);
			rota.add(0);
			matrizDistancia = Utilidades.definirMatrizDistancia(pontoCartesianoSelecionadosX,
					pontoCartesianoSelecionadosY);
			matrizDistanciaCorrigida = Utilidades.correcaoMatrizDistancia(matrizDistancia, distAdjustDistance);
			visitaAtual = 0;
		} else {

			// Descobrindo quantas tarefas foram recebidas até o momento tempoGasto
			int qtNewTarefas = 0;
			List<Integer> tarefasConhecidas = new ArrayList<>();
			while (pontosTempoOrdenado.get(qtNewTarefas) <= tempoGasto) {
				tarefasConhecidas.add(qtNewTarefas);
				qtNewTarefas++;
			}
			// Removendo as tarefas já incluídas em rota
			tarefasConhecidas.removeAll(rota);

			// Se existir 1 nova tarefa para ser executada não é necessário roteamento, a
			// rota é óbvia
			if (tarefasConhecidas.size() == 1) {
				pontoCartesianoSelecionadosX[tarefasConhecidas.get(0)] = pontosXOrdenado.get(tarefasConhecidas.get(0));
				pontoCartesianoSelecionadosY[tarefasConhecidas.get(0)] = pontosYOrdenado.get(tarefasConhecidas.get(0));
				rota.add(rota.size() - 1, tarefasConhecidas.get(0));
				matrizDistancia = Utilidades.definirMatrizDistancia(pontoCartesianoSelecionadosX,
						pontoCartesianoSelecionadosY);
				matrizDistanciaCorrigida = Utilidades.correcaoMatrizDistancia(matrizDistancia, distAdjustDistance);
			}
			// Se existirem 2 ou mais novas tarefas para ser executadas no período
			// tempoGasto
			else if (tarefasConhecidas.size() > 1) {
				// Determinando os pontos para o novo roteiro
				double[] ptX = new double[qtNewTarefas - visitaAtual];
				double[] ptY = new double[qtNewTarefas - visitaAtual];
				ptX[0] = pontosXOrdenado.get(rota.get(visitaAtual));
				ptY[0] = pontosYOrdenado.get(rota.get(visitaAtual));
				int i = 0;
				while (i < tarefasConhecidas.size()) {
					i++;
					ptX[i] = pontosXOrdenado.get(tarefasConhecidas.get(i - 1));
					ptY[i] = pontosYOrdenado.get(tarefasConhecidas.get(i - 1));
				}

				System.out.println("Pontos para rota X: " + Arrays.toString(ptX));
				// System.out.println("Pontos para rota Y: " + Arrays.toString(ptY));
				/*
				 * // Rota de genetico para o novo roteiro TSPProblem problem = new
				 * TSPProblem(ptX,ptY); CrossoverOperator crossoverOperator = new
				 * OrdenatedCrossover(); MutationOperator mutationOperator = new
				 * ShiftMutationOperator(); Selector reproductionSelector = new
				 * RouletteSelector(); Selector surviveSelector = new DeterministicSelector();
				 * GeneticAlgorithm ga = new GeneticAlgorithm(problem, crossoverOperator,
				 * mutationOperator, reproductionSelector, surviveSelector); ga.setMax_it(200);
				 * ga.setPopulationSize(50); ga.setMutationProbability(0.10);
				 * ga.setCrossoverProbability(0.75); OptimizationResult r = ga.run();
				 * 
				 * // Posso otimizar isso evitando que a rotina do genético seja chamada a cada
				 * vez que uma tarefa seja executada
				 * 
				 * // para a rota encontrada, procurar o valor do indice da visita após o ponto
				 * inicial 1 List<Integer> rotaGen = r.getBestSolution().getRota(); int seq = 0;
				 * seq = rotaGen.indexOf(1); if (seq!=rotaGen.size()-1){ seq =
				 * rotaGen.get(seq+1); } else { seq = rotaGen.get(seq-1); }
				 * rota.add(rota.size()-1,pontosXOrdenado.indexOf(ptX[seq-1]));
				 * pontoCartesianoSelecionadosX[pontosXOrdenado.indexOf(ptX[seq-1])] =
				 * ptX[seq-1]; pontoCartesianoSelecionadosY[pontosXOrdenado.indexOf(ptX[seq-1])]
				 * = ptY[seq-1]; matrizDistancia =
				 * Utilidades.definirMatrizDistancia(pontoCartesianoSelecionadosX,
				 * pontoCartesianoSelecionadosY); matrizDistanciaCorrigida =
				 * Utilidades.correcaoMatrizDistancia(matrizDistancia, distAdjustDistance);
				 * 
				 * //System.out.println("Rota atualizada: " + rota.toString());
				 * System.out.println("Resultado do algoritmo genético: " +
				 * r.getBestSolution().toString());
				 */

			}

			else if ((tarefasConhecidas.size() == 0) && (visitaAtual < pontosXOrdenado.size() - 2)) {
				// Se não houveram tarefas até o instante, inserir a próxima tarefa no roteiro
				// no instante em que esta ocorrer
				tempoGasto = pontosTempoOrdenado.get(visitaAtual + 1);
				pontoCartesianoSelecionadosX[visitaAtual + 1] = pontosXOrdenado.get(visitaAtual + 1);
				pontoCartesianoSelecionadosY[visitaAtual + 1] = pontosYOrdenado.get(visitaAtual + 1);
				rota.add(rota.size() - 1, visitaAtual + 1);
				matrizDistancia = Utilidades.definirMatrizDistancia(pontoCartesianoSelecionadosX,
						pontoCartesianoSelecionadosY);
				matrizDistanciaCorrigida = Utilidades.correcaoMatrizDistancia(matrizDistancia, distAdjustDistance);
			}

		}

		System.out.println("Rota atualizada: " + rota.toString());
		// atualizar pontos cartesianos selecionados

		// atualizar rota

	}

	@Override
	public List<Integer> identificaTarefasParaTransferir() {
		return null;
	}

	@Override
	public void executaTransferencia(List<Integer> tarefasParaTransferir) {

	}

	@Override
	public void inserirNaRota(double ptX, double ptY) {
		// TODO Auto-generated method stub

	}

	@Override
	public double estimarInserirNaRota(double ptX, double ptY) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void removerDaRota(Double ponto) {
		// TODO Auto-generated method stub

	}
}
