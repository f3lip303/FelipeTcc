package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import optmizations.HeuristicaConstrucaoInsercaoMaisDistante;
import optmizations.HeuristicaMelhoria;
import optmizations.HeuristicaMelhoria3opt;





public class RouteRandom extends Route implements EstrategiaCooperacaoVeiculos{

	Logger logger = LoggerFactory.getLogger(getClass().getName());

	protected int tempoDisponivel; // Tempo disponível é 480 min (ou 8h)

	// relativas ao tempo de atendimento
	protected double meanServiceTime; // media de tempo de serviço para
										// normal
	protected double sdServiceTime; // desvio de tempo de serviço para
									// normal

	// parâmetros relativos a velocidade
	protected double probabilidadeTransitoSerNormal;
	protected double meanPrmVelNormal;
	protected double sdPrmVelNormal;
	protected double meanPrmVelCongestionamento;
	protected double sdPrmVelCongestionamento;

	// parâmetros de ajuste das distancias euclidianas, as aproximando de
	protected double meanPrmAdjustDistance;
	protected double sdPrmAdjustDistance;

	// parâmetros relativos à análise sequencial
	protected double probabilidadeInicial;
	protected double probabilidadeFinal;
	protected double sequencialAlfa;
	protected double sequencialBeta;

	// apenas os pontos do distrito que foram selecionados
	protected double[] pontoCartesianoSelecionadosX;
	protected double[] pontoCartesianoSelecionadosY;

	protected Integer[] rotaInicial; // criada através de alguma heurística de criação
	protected Integer[] rotaMelhorada; // criada através de alguma heurística de
										// melhoria
	protected List<Integer> rota; // rota a ser executada
	protected int indiceClienteAtendido;

	protected double[][] matrizDistancia; // criada aleatoriamente
	protected double[][] matrizDistanciaCorrigida; // ajustada aleatoriamente por uma normal com parametros
													// meanPrmAdjustDistance e sdPrmAdjustDistance

	/*
	 * ArrayList listaA = new ArrayList(); // listas utilizadas para definir que
	 * tarefas deverão ser substituidas ArrayList listaB = new ArrayList();
	 */
	protected UniformRealDistribution distRndZeroUm;
	protected LogNormalDistribution distAdjustDistance;
	protected LogNormalDistribution distTempoServico;
	protected LogNormalDistribution distVelocidadeNormal;
	protected LogNormalDistribution distVelocidadeCongestionamento;
	protected NormalDistribution distVelRestanteRoteiro;
	protected NormalDistribution distTempoServicoRestante;

	protected boolean transitoCongestionado = false;
	protected boolean construirRota;
	// protected int nrVisitas; // qtd total de visitas
	protected int visitaAtual = 0;
	protected int totVisitasPlanejadas = 0;
	protected double tempoGasto = 0.0;

	protected double distViajada = 0.0;

	protected double temposDeViagem[];
	protected double temposDeServico[];
	protected double velDeViagem[];
	protected int faixaV, faixaH, codSimulacao, codCiclo;

	// Gravação na tabela de simulação
	// Inicializa o banco de dados
	DataSource dataSource = null;
	Connection con;

	String queryAtendimento, queryDeslocamento;
	// PreparedStatement psAtendimento, psDeslocamento;
	// Statement stmtAtendimento, stmtDeslocamento;

	// Rede em que a rota foi criada
	NetworkRandom netMain;

	/**
	 * Método construtor
	 * 
	 * @param constRota  : variável que indica a necessidade ou não de se construir
	 *                   uma rota inicial
	 * @param fx         : faixa
	 * @param pontosX    : vetor coordenadas X de pontos
	 * @param pontosY    : vetor coordenadas Y de pontos
	 * @param tpDisp     : tempo disponível pra realizar o roteiro
	 * @param meanST     : parametro media para função normal de tempo de serviço
	 * @param sdST       : parametro desvio padrão para função normal de tempo de
	 *                   serviço
	 * @param velNormal  : velocidade normal
	 * @param velLento   : velocidade de trânsito lento
	 * @param probNormal : probabilidade de trânsito ser lento
	 * @param meanAdjust : parametro media para função normal de ajuste das
	 *                   distâncias
	 * @param sdAdjust   : parametro desvio padrão para função normal de ajuste das
	 *                   distâncias
	 * @param asProbIni  : análise sequencial - probabilidade inicial
	 * @param asProbFin  : análise sequencial - probabilidade final
	 * @param asAlfa     : análise sequencial - parâmetro alfa
	 * @param asBeta     :análise sequencial - parâmetro beta
	 * @throws SQLException
	 */
	public RouteRandom(NetworkRandom net, boolean constRota, int fxV, int fxH, double[] pontosX, double[] pontosY,
			int tpDisp, double meanST, double sdST, int velNormal, double varVelNormal, int velLento,
			double varVelLento, double probNormal, double meanAdjust, double sdAdjust, double asProbIni,
			double asProbFin, double asAlfa, double asBeta) throws SQLException {

		super(1);
		netMain = net;

		construirRota = constRota;

		tempoDisponivel = tpDisp; // Tempo disponível é 480 min (ou 8h)

		// relativas ao tempo de atendimento
		meanServiceTime = meanST; // media de tempo de serviço para
									// normal
		sdServiceTime = sdST; // desvio de tempo de serviço para
								// normal

		// parâmetros relativos a velocidade
		probabilidadeTransitoSerNormal = probNormal;
		meanPrmVelNormal = velNormal;
		sdPrmVelNormal = varVelNormal;
		meanPrmVelCongestionamento = velLento;
		sdPrmVelCongestionamento = varVelLento;

		// parâmetros de ajuste das distancias euclidianas, as aproximando de
		// distâncias reais
		meanPrmAdjustDistance = meanAdjust;
		sdPrmAdjustDistance = sdAdjust;

		// parâmetros relativos à análise sequencial
		probabilidadeInicial = asProbIni;
		probabilidadeFinal = asProbFin;
		sequencialAlfa = asAlfa;
		sequencialBeta = asBeta;

		double a, b2;
		// Criação das distribuições de probabilidade para geração dos números
		// randomicos
		distRndZeroUm = new UniformRealDistribution(0, 1);
		distRndZeroUm.reseedRandomGenerator(System.currentTimeMillis());

		b2 = Math.log(Math.pow(sdPrmAdjustDistance, 2) / Math.pow(meanPrmAdjustDistance, 2) + 1);
		a = Math.log(meanPrmAdjustDistance) - b2 / 2;
		distAdjustDistance = new LogNormalDistribution(a, b2);
		distAdjustDistance.reseedRandomGenerator(System.currentTimeMillis());

		b2 = Math.log(Math.pow(sdServiceTime, 2) / Math.pow(meanServiceTime, 2) + 1);
		a = Math.log(meanServiceTime) - b2 / 2;
		distTempoServico = new LogNormalDistribution(a, b2);
		distTempoServico.reseedRandomGenerator(System.currentTimeMillis());

		b2 = Math.log(Math.pow(sdPrmVelNormal, 2) / Math.pow(meanPrmVelNormal, 2) + 1);
		a = Math.log(meanPrmVelNormal) - b2 / 2;
		distVelocidadeNormal = new LogNormalDistribution(a, b2);
		distVelocidadeNormal.reseedRandomGenerator(System.currentTimeMillis());

		b2 = Math.log(Math.pow(sdPrmVelCongestionamento, 2) / Math.pow(meanPrmVelCongestionamento, 2) + 1);
		a = Math.log(meanPrmVelCongestionamento) - b2 / 2;
		distVelocidadeCongestionamento = new LogNormalDistribution(a, b2);
		distVelocidadeCongestionamento.reseedRandomGenerator(System.currentTimeMillis());

		logger.trace("Criação de rota.");
		// nrVisitas = pontosX.length-1; // o primeiro ponto do roteiro é o depósito
		pontoCartesianoSelecionadosX = pontosX;
		pontoCartesianoSelecionadosY = pontosY;
		temposDeViagem = new double[pontosX.length + 2]; // inclui retorno ao depósito
		temposDeServico = new double[pontosX.length + 2]; // inclui retorno ao depósito
		velDeViagem = new double[pontosX.length + 2]; // inclui retorno ao depósito
		faixaV = fxV;
		faixaH = fxH;

		transitoCongestionado = verificaCondicaoDeCongestionamentoInicial();
		// System.out.println("Transito congestionado (" +
		// Integer.toString(faixaV)+Integer.toString(faixaH)+")? "+
		// Boolean.toString(transitoCongestionado));

		// System.out.println("pontos X "+Integer.toString(fxV)+Integer.toString(fxH)+":
		// "+Arrays.toString(pontoCartesianoSelecionadosX));
		// System.out.println("pontos Y "+Integer.toString(fxV)+Integer.toString(fxH)+":
		// "+Arrays.toString(pontoCartesianoSelecionadosY));

		matrizDistancia = Utilidades.definirMatrizDistancia(pontoCartesianoSelecionadosX, pontoCartesianoSelecionadosY);
		matrizDistanciaCorrigida = Utilidades.correcaoMatrizDistancia(matrizDistancia, distAdjustDistance);

		if (construirRota) {
			// rotaInicial = new int[nrVisitas+2]; // +1 para retornar ao depósito
			HeuristicaConstrucaoInsercaoMaisDistante hc = new HeuristicaConstrucaoInsercaoMaisDistante();
			rotaInicial = hc.solve(matrizDistanciaCorrigida);
			logger.trace("Rota construída");

			HeuristicaMelhoria hm = new HeuristicaMelhoria3opt();
			rotaMelhorada = hm.solve(matrizDistanciaCorrigida, rotaInicial);
			logger.trace("Rota melhorada");

			// System.out.println("Código da simulação : "+Integer.toString(codSimulacao));
			// System.out.println("Tamanho da rota 0:
			// "+Double.toString(Utilidades.calculaCustoRota(matrizDistanciaCorrigida,rota0)));
			// System.out.println("Tamanho da rota inicial:
			// "+Double.toString(Utilidades.calculaCustoRota(matrizDistanciaCorrigida,rotaInicial)));
			// System.out.println("Tamanho da rota melhorada:
			// "+Double.toString(Utilidades.calculaCustoRota(matrizDistanciaCorrigida,rotaMelhorada)));
			// System.out.println("pontos X "+Integer.toString(fxV)+Integer.toString(fxH)+":
			// "+Arrays.toString(pontoCartesianoSelecionadosX));
			// System.out.println("pontos Y "+Integer.toString(fxV)+Integer.toString(fxH)+":
			// "+Arrays.toString(pontoCartesianoSelecionadosY));
			// System.out.println("rota melhorada
			// "+Integer.toString(fxV)+Integer.toString(fxH)+":
			// "+Arrays.toString(rotaMelhorada));
			// System.out.println("Elementos selecionados: "+Arrays.toString(pontosX));

			rota = new ArrayList<Integer>(Arrays.asList(rotaMelhorada));

			// System.out.println("Tamanho da rota : "+Integer.toString(rota.size()));
			// System.out.println("Rota
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): "+rota.toString());
			// System.out.println("PontosX
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
			// "+Arrays.toString(pontoCartesianoSelecionadosX));
			// System.out.println("Nr de visitas : "+Integer.toString(nrVisitas));

		}

		// Salvar rota no banco de dados
		/*
		 * if (con.isClosed()){ con = dataSource.getConnection(); }
		 */

	}

	public RouteRandom(NetworkRandom net, boolean constRota, int fxV, int fxH, double[] pontosX, double[] pontosY)
			throws SQLException {
		this(net, constRota, fxV, fxH, pontosX, pontosY, 480, 12, 6, 30, 7.5, 20, 5, 0.6, 1.32, 0.0915, 0.0, 0.7, 0.005,
				0.005);
	}

	/**
	 * Rotina que dispara a realização da rota
	 * 
	 * @throws SQLException
	 */
	public void processamento(int r) throws SQLException {

		iniciaOperacao(r);

		// chegada ao distrito
		chegadaAoDistrito();
		atendimento();

		// se houver tempo para realizar a próxima visita, atender ao próximo cliente e
		// retornar ao depósito, então pode realizar a visita
		while ((tempoGasto + estimaTempoParaProximaVisita() <= tempoDisponivel) && (visitaAtual < rota.size() - 2)) {
			deslocamento();
			atendimento();
		}

		retornoAoDeposito();

		gravaEstatisticasDaRota();

	}

	public void iniciaOperacao(int cc) throws SQLException {

		con = dataSource.getConnection();

		// Identifica o maior código de simulação
		codSimulacao = 0;
		String query = "select max(id) from simulacao";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			codSimulacao = rs.getInt(1);
		}
		rs.close();

		codCiclo = cc;

		// Preparação para criação de pontos
		query = "INSERT INTO visitas(cods, codc, zonai, zonaj, ptx, pty, ordemnarota)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setInt(1, codSimulacao);
		ps.setInt(2, codCiclo);
		ps.setInt(3, faixaV);
		ps.setInt(4, faixaH);

		query = "INSERT INTO visitasplanejadas(cods, codc, zonai, zonaj, ptx, pty, ordemnarota)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?);";
		PreparedStatement ps2 = con.prepareStatement(query);
		ps2.setInt(1, codSimulacao);
		ps2.setInt(2, codCiclo);
		ps2.setInt(3, faixaV);
		ps2.setInt(4, faixaH);

		for (int aux = 0; aux < (pontoCartesianoSelecionadosX.length); aux++) {
			ps.setDouble(5, pontoCartesianoSelecionadosX[rota.get(aux)]);
			ps.setDouble(6, pontoCartesianoSelecionadosY[rota.get(aux)]);
			ps.setDouble(7, aux);
			ps.addBatch();
			ps2.setDouble(5, pontoCartesianoSelecionadosX[rota.get(aux)]);
			ps2.setDouble(6, pontoCartesianoSelecionadosY[rota.get(aux)]);
			ps2.setDouble(7, aux);
			ps2.addBatch();
		}
		ps.executeBatch();
		ps2.executeBatch();

		// Identificação do total de visitas planejadas
		query = "SELECT max(ordemnarota) from visitasplanejadas WHERE cods=" + codSimulacao + " and codc=" + codCiclo
				+ " and zonai=" + faixaV + " and zonaj=" + faixaH;
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			totVisitasPlanejadas = rs.getInt(1);
		}
		rs.close();

	}

	public void chegadaAoDistrito() throws SQLException {
		// Realiza o deslocamento para o primeiro cliente
		double tempoDeslocamento = calculaTempoNormal(matrizDistanciaCorrigida[rota.get(0)][rota.get(1)]);
		tempoGasto += tempoDeslocamento;
		distViajada += matrizDistanciaCorrigida[rota.get(0)][rota.get(1)];

		// Preparação para deslocamento
		queryDeslocamento = "UPDATE visitas SET tpdeslocamento=? "
				+ "WHERE cods=? and codc=? and zonai=? and zonaj=? and ptx=? and pty=?";

		PreparedStatement psDeslocamento = con.prepareStatement(queryDeslocamento);
		psDeslocamento.setDouble(1, tempoGasto);
		psDeslocamento.setInt(2, codSimulacao);
		psDeslocamento.setInt(3, codCiclo);
		psDeslocamento.setInt(4, faixaV);
		psDeslocamento.setInt(5, faixaH);
		psDeslocamento.setDouble(6, pontoCartesianoSelecionadosX[rota.get(visitaAtual)]);
		psDeslocamento.setDouble(7, pontoCartesianoSelecionadosY[rota.get(visitaAtual)]);
		psDeslocamento.addBatch();
		psDeslocamento.execute();

		System.out.println("Chegada ao distrito (" + Integer.toString(faixaV) + Integer.toString(faixaH) + ")");
		// visitaAtual++;

	}

	/**
	 * Rotina que modela o deslocamento do veículo para realizar a próxima tarefa
	 * 
	 * @throws SQLException
	 */
	public void deslocamento() throws SQLException {

		// Realiza o deslocamento para o próximo cliente
		double tempoDeslocamento = calculaTempoPercurso(
				matrizDistanciaCorrigida[rota.get(visitaAtual)][rota.get(visitaAtual + 1)]);
		tempoGasto += tempoDeslocamento;
		distViajada += matrizDistanciaCorrigida[rota.get(visitaAtual)][rota.get(visitaAtual + 1)];

		PreparedStatement psDeslocamento = con.prepareStatement(queryDeslocamento);
		psDeslocamento.setDouble(1, tempoGasto);
		psDeslocamento.setInt(2, codSimulacao);
		psDeslocamento.setInt(3, codCiclo);
		psDeslocamento.setInt(4, faixaV);
		psDeslocamento.setInt(5, faixaH);
		psDeslocamento.setDouble(6, pontoCartesianoSelecionadosX[rota.get(visitaAtual)]);
		psDeslocamento.setDouble(7, pontoCartesianoSelecionadosY[rota.get(visitaAtual)]);
		psDeslocamento.addBatch();
		psDeslocamento.executeBatch();

		// System.out.println("Deslocamento (" +
		// Integer.toString(faixaV)+Integer.toString(faixaH)+") - Visita atual:
		// "+Integer.toString(visitaAtual));

	}

	/*
	 * Rotina que modela a realização da tarefa
	 * 
	 * @see logdyn.utils.Route#atendimento()
	 */
	public void atendimento() throws SQLException {

		visitaAtual++;

		// Atualização de variáveis no início do atendimento

		temposDeServico[visitaAtual] = distTempoServico.sample();
		tempoGasto += temposDeServico[visitaAtual];

		// System.out.println(Double.toString(tempoGasto));

		// Realizar teste SPRT analiseSequencial(double[][], int[], int, int, int,
		// double, double, double)
		if (!transitoCongestionado) {
			int resultadoTeste = testaCapacidadeRealizarRota();
			// System.out.println("Resultado SPRT = " + Integer.toString(resultadoTeste));
			if (resultadoTeste == 1) { // Se houve congestionamento detectado
				transitoCongestionado = true;
				// System.out.println("Encontrei congestionamento em (" +
				// Integer.toString(faixaV)+Integer.toString(faixaH)+") na visita
				// "+Integer.toString(visitaAtual));

				// Inserir na tabela de eventos
				String queryEventos = "INSERT INTO eventos(cods, codc, zonai, zonaj, ptx, pty, instante, evento) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
				PreparedStatement psEvento = con.prepareStatement(queryEventos);
				psEvento.setInt(1, codSimulacao);
				psEvento.setInt(2, codCiclo);
				psEvento.setInt(3, faixaV);
				psEvento.setInt(4, faixaH);
				psEvento.setDouble(5, pontoCartesianoSelecionadosX[rota.get(visitaAtual)]);
				psEvento.setDouble(6, pontoCartesianoSelecionadosY[rota.get(visitaAtual)]);
				psEvento.setDouble(7, tempoGasto);
				psEvento.setString(8, "JAM");
				psEvento.addBatch();
				psEvento.executeBatch();
			}
		}

		// Preparação para atendimento
		queryAtendimento = "UPDATE visitas SET tpinicioatendimento=?, tpfimatendimento=? "
				+ "WHERE cods=? and codc=? and zonai=? and zonaj=? and ptx=? and pty=?";

		PreparedStatement psAtendimento = con.prepareStatement(queryAtendimento);
		psAtendimento.setDouble(1, tempoGasto - temposDeServico[visitaAtual]);
		psAtendimento.setDouble(2, tempoGasto);
		psAtendimento.setInt(3, codSimulacao);
		psAtendimento.setInt(4, codCiclo);
		psAtendimento.setInt(5, faixaV);
		psAtendimento.setInt(6, faixaH);
		psAtendimento.setDouble(7, pontoCartesianoSelecionadosX[rota.get(visitaAtual)]);
		psAtendimento.setDouble(8, pontoCartesianoSelecionadosY[rota.get(visitaAtual)]);
		psAtendimento.addBatch();
		psAtendimento.executeBatch();

		// Identificar tarefas a serem transferidas (o máximo de visitas quer podem ser
		// transferidas são as visitas restantes)
		List<Integer> tarefasParaTransferir = new ArrayList<Integer>();
		if (identificaTarefasParaTransferir() != null) {
			tarefasParaTransferir.addAll(identificaTarefasParaTransferir());
		}

		// Atualizar a variável rota (que represeanta a rota atual a ser realizada)
		// System.out.println("Tarefas para tranferir:
		// "+tarefasParaTransferir.toString());
		if (tarefasParaTransferir.size() > 0) {
			// nrVisitas = nrVisitas - tarefasParaTransferir.size();
			// System.out.println("Rota
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+") - Tasks to tranf:
			// "+tarefasParaTransferir.toString()+" - Tempo em que foi detectado:
			// "+Double.toString(tempoGasto));
			// Transferir tarefas com problemas
			executaTransferencia(tarefasParaTransferir);
		}

		System.out.println("Atendimento (" + Integer.toString(faixaV) + "_" + Integer.toString(faixaH)
				+ ") - Visita atual: " + Integer.toString(visitaAtual) + "/" + Integer.toString(rota.size() - 2)
				+ " - Tempo Gasto: " + Double.toString(tempoGasto));

	}

	public void retornoAoDeposito() throws SQLException {
		// Tempo de retorno ao depósito
		// visitaAtual++;
		if (con.isClosed()) {
			con = dataSource.getConnection();
		}

		tempoGasto += calculaTempoNormal(matrizDistanciaCorrigida[rota.get(visitaAtual)][0]);
		distViajada += matrizDistanciaCorrigida[rota.get(visitaAtual)][0];

		PreparedStatement psDeslocamento = con.prepareStatement(queryDeslocamento);
		psDeslocamento.setDouble(1, tempoGasto);
		psDeslocamento.setInt(2, codSimulacao);
		psDeslocamento.setInt(3, codCiclo);
		psDeslocamento.setInt(4, faixaV);
		psDeslocamento.setInt(5, faixaH);
		psDeslocamento.setDouble(6, pontoCartesianoSelecionadosX[rota.get(visitaAtual)]);
		psDeslocamento.setDouble(7, pontoCartesianoSelecionadosY[rota.get(visitaAtual)]);
		psDeslocamento.addBatch();
		psDeslocamento.executeBatch();

		System.out.println("Retorno ao deposito (" + Integer.toString(faixaV) + Integer.toString(faixaH)
				+ ") - Tempo Gasto: " + Double.toString(tempoGasto));
	}

	/*
	 * @see logdyn.utils.Route#testaCapacidadeRealizarRota(int)
	 */
	//TODO VERIFICAR SE VOU FAZER ESTE TESTE
	public int testaCapacidadeRealizarRota() {
		// Esta implementação utiliza o método SPRT
		// return Utilidades.analiseSequencial(matrizDistanciaCorrigida, rota,
		// velocidadeTransitoNormal, velocidadeTransitoLento, velocidadeAtual,
		// matrizDistanciaCorrigida[rota[i]][rota[i+1]], sequencialAlfa,
		// sequencialBeta);
		return Utilidades.analiseSequencialAgentes(matrizDistanciaCorrigida, rota, meanPrmVelNormal, sdPrmVelNormal,
				meanPrmVelCongestionamento, sdPrmVelCongestionamento, visitaAtual, velDeViagem, sequencialAlfa,
				sequencialBeta);
	}

	/**
	 * Efetua o calculo do gasto entre dois pontos.
	 * 
	 * @param matrizDistancia
	 * @param rota
	 * @param resultado
	 * @param indice
	 * @param totalClientes
	 * @return
	 */
	public double resultadoGasto(double[][] matrizDistancia, int[] rota, int resultado, int indice) {
		double elementoMatriz = valorElementoMatriz(matrizDistancia, rota, indice, indice + 1);
		return calculaTempoPercurso(elementoMatriz);
	}

	public double valorElementoMatriz(double[][] matrizDistancia, int[] rota, int indiceA, int indiceB) {
		return matrizDistancia[rota[indiceA]][rota[indiceB]];
	}

	/**
	 * Efetua o resulado Gasto entre o depÃ³sito e o ponto passado como parÃ¢metro
	 * 
	 * @param matrizDistancia
	 * @param rota
	 * @param resultado
	 * @param indice
	 * @param totalClientes
	 * @return
	 */
	public double resultadoGastoFinal(double[][] matrizDistancia, int[] rota, int resultado, int indice) {
		double elementoMatriz;
		elementoMatriz = valorElementoMatriz(matrizDistancia, rota, 0, indice);
		return calculaTempoPercurso(elementoMatriz);
	}

	/**
	 * É verificada a possibilidade de inicialmente o transito já estar
	 * congestionado return true: se a via está congestionada false: se a via não
	 * está congestionada
	 */
	public boolean verificaCondicaoDeCongestionamentoInicial() {
		if (distRndZeroUm.sample() > probabilidadeTransitoSerNormal) {
			return true;
		}
		return false;
	}

	public void gravaEstatisticasDaRota() throws SQLException {

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

	/**
	 * Calculo do tempo de viagem dentro da rota para uma determinada distância a
	 * ser percorrida
	 * 
	 * @param valorElementoMatriz : distancia percorrida em km
	 * @return minutos para realizar o trecho valorElementoMatriz
	 */
	public double calculaTempoPercurso(double valorElementoMatriz) {
		double velDeslocamento = 0;
		if (transitoCongestionado) {
			// Se houver detectado congestionamento, todas as amostras são do tipo
			// congestionamento
			velDeslocamento = distVelocidadeCongestionamento.sample();
		} else {
			// Se não houver congestionamento, as amostras são avaliadas se serão de
			// trânsito livre ou congestionado
			if (distRndZeroUm.sample() > probabilidadeTransitoSerNormal) {
				velDeslocamento = distVelocidadeCongestionamento.sample();
			} else {
				velDeslocamento = distVelocidadeNormal.sample();
			}
		}
		velDeViagem[visitaAtual] = velDeslocamento;
		temposDeViagem[visitaAtual] = 60 * valorElementoMatriz / velDeslocamento;
		return temposDeViagem[visitaAtual];
	}

	/**
	 * Calculo do tempo de viagem dentro da rota para uma determinada distância a
	 * ser percorrida
	 * 
	 * @param valorElementoMatriz : distancia percorrida em km
	 * @return minutos para realizar o trecho valorElementoMatriz
	 */
	public double calculaTempoLento(double valorElementoMatriz) {
		double velDeslocamento = 0;
		velDeslocamento = distVelocidadeCongestionamento.sample();
		velDeViagem[visitaAtual] = velDeslocamento;
		temposDeViagem[visitaAtual] = 60 * valorElementoMatriz / velDeslocamento;
		return temposDeViagem[visitaAtual];
	}

	/**
	 * Calculo do tempo de viagem dentro da rota para uma determinada distância a
	 * ser percorrida
	 * 
	 * @param valorElementoMatriz : distancia percorrida em km
	 * @return minutos para realizar o trecho valorElementoMatriz
	 */
	public double calculaTempoNormal(double valorElementoMatriz) {
		double velDeslocamento = 0;
		velDeslocamento = distVelocidadeNormal.sample();
		velDeViagem[visitaAtual] = velDeslocamento;
		temposDeViagem[visitaAtual] = 60 * valorElementoMatriz / velDeslocamento;
		return temposDeViagem[visitaAtual];
	}

	/**
	 * Calculo do tempo estimado de viagem de uma determinada rota
	 * 
	 * @param myRoute : rota restante a ser percorrida
	 * @return minutos para realizar a rota myRoute
	 */
	public double estimaTempoRestanteRota(List<Integer> myRoute) {

		// cáculo da distancia restante a ser percorrida
		double distRestante = 0;
		for (int i = 0; i < myRoute.size() - 1; i++) {
			distRestante += matrizDistanciaCorrigida[myRoute.get(i)][myRoute.get(i + 1)];
		}
		// distRestante += matrizDistanciaCorrigida[myRoute.size()-2][0];

		// cáculo do valor esperado para velocidade dos transportes restantes segundo
		// normal
		int restanteVisitas = myRoute.size();
		double tpTrasnp, tpAtend;
		if (transitoCongestionado) {
			distVelRestanteRoteiro = new NormalDistribution(meanPrmVelCongestionamento,
					Math.pow(sdPrmVelCongestionamento, 2) / restanteVisitas);
		} else {
			distVelRestanteRoteiro = new NormalDistribution(meanPrmVelNormal,
					Math.pow(sdPrmVelNormal, 2) / restanteVisitas);
		}
		tpTrasnp = distRestante / distVelRestanteRoteiro.sample();

		// cáculo do valor esperado para atendimentos restantes
		distTempoServicoRestante = new NormalDistribution(meanServiceTime,
				Math.pow(sdServiceTime, 2) / restanteVisitas);
		tpAtend = restanteVisitas * distTempoServicoRestante.sample();

		return tpTrasnp + tpAtend;
	}

	/**
	 * Calcula uma estimativa de tempos de deslocamento, atendimento e retorno ao
	 * depósito estimadoao próximo cliente
	 * 
	 * @return
	 */
	public double estimaTempoParaProximaVisita() {
		if (visitaAtual < rota.size() - 1) {
			double tempoRetornoEstimado = 0.0, distancia = 0.0, tempoDeslocamentoEstimado = 0.0;
			distancia = matrizDistanciaCorrigida[rota.get(visitaAtual)][rota.get(visitaAtual + 1)];
			tempoDeslocamentoEstimado = calculaTempoPercurso(distancia);
			double tempoServicoEstimado = distTempoServico.sample();
			distancia = matrizDistanciaCorrigida[rota.get(visitaAtual + 1)][0];
			tempoRetornoEstimado = calculaTempoPercurso(distancia);
			return tempoDeslocamentoEstimado + tempoServicoEstimado + tempoRetornoEstimado;

		} else {
			return 1000000;
		}
	}

	public double[] getPontoCartesianoSelecionadosX() {
		return pontoCartesianoSelecionadosX;
	}

	public double[] getPontoCartesianoSelecionadosY() {
		return pontoCartesianoSelecionadosY;
	}

	public void setCodCiclo(int codCiclo) {
		this.codCiclo = codCiclo;
	}

	/**
	 * @return the visitaAtual
	 */
	public int getVisitaAtual() {
		return visitaAtual;
	}

	public void setVisitaAtual(int visitaAtual) {
		this.visitaAtual = visitaAtual;
	}

	/**
	 * @return the rota
	 */
	public List<Integer> getRota() {
		return rota;
	}

	/**
	 * @return the temposDeViagem
	 */
	public double[] getTemposDeViagem() {
		return temposDeViagem;
	}

	/**
	 * @return the temposDeServico
	 */
	public double[] getTemposDeServico() {
		return temposDeServico;
	}

	/**
	 * @return the tempoDisponivel
	 */
	public int getTempoDisponivel() {
		return tempoDisponivel;
	}

	/**
	 * @return the tempoGasto
	 */
	public double getTempoGasto() {
		return tempoGasto;
	}

	/**
	 * @return the totVisitasPlanejadas
	 */
	public int getTotVisitasPlanejadas() {
		return totVisitasPlanejadas;
	}

	/**
	 * @param totVisitasPlanejadas the totVisitasPlanejadas to set
	 */
	public void setTotVisitasPlanejadas(int totVisitasPlanejadas) {
		this.totVisitasPlanejadas = totVisitasPlanejadas;
	}

	@Override
	public List<Integer> identificaTarefasParaTransferir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executaTransferencia(List<Integer> l) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removerDaRota(Double ptX) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inserirNaRota(double ptX, double ptY) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double estimarInserirNaRota(double ptX, double ptY) {
		// TODO Auto-generated method stub
		return 0;
	}
}
