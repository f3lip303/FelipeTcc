package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkRandom implements Network {

	Logger logger = LoggerFactory.getLogger(getClass().getName());

	// DECLARAÇÃO DE VARIÁVEIS

	private int nrClientes; // no trabalho do Paulo totalClientesMin = 28; totalClientesMax = 45;

	// relativas à malha

	private int numAreaX;
	private int numAreaY;
	private int raioInicial;
	private double densidade;
	private int quantidadeClientes; // quantidade de clientes na zona inicial
	private int[] alfa = new int[4];
	private double probabilidadeDeClienteEstarNaRota;
	private int velocidadeMedia;
	private int jornada;
	private double[] pontoPolarR;
	private double[] pontoPolarTeta;
	private int nrveiculos_aux;

	private double[] pontoCartesianoX; // todos os pontos do distrito
	private double[] pontoCartesianoY;
	private double pontoCMX; // ponto centro de massa de todas as rotas
	private double pontoCMY;
	private double pontoTransAuxX; // ponto de localização de transportadora auxiliar
	private double pontoTransAuxY;

	// para cada rota (ou distrito, uma vez que apenas uma rota atende a todos os
	// pontos selecionados daquele distrito)
	private int[] pontoCartesianoSelecionados; // vetor de 0 e 1 que informa se o ponto do distrito foi selecionados
	// private double[] pontoCartesianoSelecionadosX; // apenas os pontos do
	// distrito que foram selecionados
	// private double[] pontoCartesianoSelecionadosY;
	private int totalPontoCartesianoSelecionados;

	private String tipoRota;
	private int codSimulacao;

	private Route[][] rotas;
	private RouteRandomAuxiliar rotaAux;
	private int[] nrClientesZona;
	private double[] raio;
	double[] area;
	double[] th;
	double[] tu;
	double k2;
	DataSource dataSource = null;
	Connection con;

	UniformRealDistribution distRndZeroUm = new UniformRealDistribution(0, 1);

	/**
	 * Construtuor de uma Rede Randomica * @param tpRt : tipo de rota
	 * 
	 * @param nrX :
	 * @param nrY :
	 * @param jor
	 * @throws SQLException
	 */

	/**
	 * Construtuor de uma Rede Randomica
	 * 
	 * @param tpRt  tipo de rotas a serem geradas
	 * @param nrX   número de faixas
	 * @param nrY   número de zonas em cada faixa
	 * @param r     raio inicial (distância do depósito a primeira faixas)
	 * @param den   densidade de clientes por zonas
	 * @param qtdCl quantidade de clientes em uma zona na faixa inicial
	 * @param ang   angulos que delineam as zonas
	 * @param prob  probabilidade de cliente estar na rota
	 * @param vel   velocidade média no distrito
	 * @param jor
	 * @param nrAux número de veículos auxiliares a serem criados
	 * @throws SQLException
	 */
	public NetworkRandom(String tpRt, int nrX, int nrY, int r, double den, int qtdCl, int[] ang, double prob, int vel,
			int jor, int nrAux) throws SQLException {
		if (RouteFactory.isValid(tpRt)) {
			tipoRota = tpRt;
			numAreaX = nrX;
			numAreaY = nrY;
			raioInicial = r;
			densidade = den;
			quantidadeClientes = qtdCl; // quantidade de clientes na zona inicial
			alfa = ang;
			probabilidadeDeClienteEstarNaRota = prob;
			area = new double[numAreaX];
			rotas = new RouteRandom[numAreaX][numAreaY];

			raio = new double[numAreaX + 1];
			nrClientesZona = new int[numAreaX];
			th = new double[numAreaX];
			tu = new double[numAreaX];
			velocidadeMedia = vel;
			jornada = jor;
			nrveiculos_aux = nrAux;

			distRndZeroUm.reseedRandomGenerator(System.currentTimeMillis());

			nrClientesZona[0] = quantidadeClientes;
			raio[0] = raioInicial;
			area[0] = calculaArea(nrClientesZona[0], densidade);
			k2 = new NormalDistribution(1.32, 0.12).sample();
			th[0] = calculaTh(raio[0], k2, velocidadeMedia);
			tu[0] = jornada * 60 - th[0];

			for (int i = 1; i < numAreaX; i++) {
				raio[i] = calculaRaio(i, 2);
				th[i] = calculaTh(raio[i], k2, velocidadeMedia);
				tu[i] = jornada * 60 - th[i];
				nrClientesZona[i] = (int) (calculaNumeroClienteZona(nrClientesZona, i));
				area[i] = calculaArea(nrClientesZona[i], densidade);
			}
			raio[numAreaX] = calculaRaio(numAreaX, 2);

			// TESTE COM NÚMERO FIXO DE CLIENTES POR ZONA DO PAULO
			// Sem carga
			nrClientesZona[0] = 36;
			nrClientesZona[1] = 29;
			nrClientesZona[2] = 25;
			// Com carga
			nrClientesZona[0] = 47;
			nrClientesZona[1] = 34;
			nrClientesZona[2] = 29;
			pontoTransAuxX = 0;
			pontoTransAuxY = 0;

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

		} else {
			// logger.error("Este tipo de rede não existe");
		}
	}

	public NetworkRandom(String tpRt) throws SQLException {
		this(tpRt, 3, 3, 6, 0.75, 37, new int[] { 15, 30, 45, 60 }, 0.7, 30, 8, 0);
	}

	public double calcularK2(double variavel, double k, double mi) {
		boolean laco = true;
		double k2 = 0.0;
		double s = 0.0;
		while (laco) {
			for (int i = 1; i <= (int) k; i++) {
				s = s + distRndZeroUm.sample();
			}
			double auxA = Math.pow((12 / k), 1 / 2);
			double auxB = (s - (k / 2));
			k2 = (variavel * auxA * auxB + mi);
			if (k2 >= 1) {
				laco = false;
			}
			s = 0.0;
		}
		return k2;
	}

	public void criarRotas() throws SQLException {

		// System.out.println("clientes selecionados ajustado (X):
		// "+Arrays.toString(ptSel[0]));
		// BasicDataSource bds = (BasicDataSource) dataSource;
		// System.out.println("NumActive: " + bds.getNumActive());
		// System.out.println("NumIdle: " + bds.getNumIdle());

		// Criar roteiros regulares
		Route r;
		for (int i = 0; i < numAreaX; i++) {
			for (int j = 0; j < numAreaY; j++) {

				// System.out.println("número de clientes da faixa "+Integer.toString(i)+":
				// "+Integer.toString(nrClientesZona[i]));

				gerarPontos(i, j);
				// System.out.println("clientes gerados:"+Arrays.toString(pontoCartesianoX));

				int[] pt01 = gerarPontosSelecionados(i);
				// System.out.println("número de clientes selecionados:
				// "+Integer.toString(totalPontoCartesianoSelecionados));
				// System.out.println("clientes selecionados: "+Arrays.toString(pt01));

				double[][] ptSel = montarPontosCartesianosSelecionados(i, pt01);

				// System.out.println("zona: "+Integer.toString(i)+Integer.toString(j));
				// System.out.println("visitas selecionadas: " +
				// Integer.toString(totalPontoCartesianoSelecionados));
				// System.out.println("Tamanho da rota criada: " +
				// Integer.toString(ptSel[0].length));

				r = RouteFactory.getRoute(this, tipoRota, i, j, ptSel[0], ptSel[1]);

				this.rotas[i][j] = r;
			}
		}
	}

	public void gravaEstatisticasDaSimulacao(int ciclos) throws SQLException {

		if (con.isClosed()) {
			con = dataSource.getConnection();
		}

		// Descobrindo número médio de veículos regulares em cada ciclo
		int nrveiculos_reg = numAreaX * numAreaY;

		// Identifica o maior código de simulação
		codSimulacao = 0;
		String query = "select max(id) from simulacao";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			codSimulacao = rs.getInt(1);
		}
		rs.close();

		// Descobrindo número de veículos auxiliares em cada ciclo
		int nrveiculos_aux = 0;
		query = "select count(zonaj) from rotas where cods=" + codSimulacao + " and codc=1 and zonai=99999";
		// System.out.println(query);
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			nrveiculos_aux = rs.getInt(1);
		}
		rs.close();

		// Descobrindo distancia media viajada pelos veículos regulares em cada dia
		double distviajada_reg = 0;
		query = "select sum(distviajada) from rotas where cods=" + codSimulacao + " and zonai<>99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			distviajada_reg = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo distancia media viajada pelos veículos auxiliares em cada dia
		double distviajada_aux = 0;
		query = "select sum(distviajada) from rotas where cods=" + codSimulacao + " and zonai=99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			distviajada_aux = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo o tempo de ciclo médio utilizado pelos veículos regulares em cada
		// dia
		double tempociclo_reg = 0;
		query = "select sum(tempociclofornecimento) from rotas where cods=" + codSimulacao + " and zonai<>99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			tempociclo_reg = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo o tempo de ciclo médio utilizado pelos veículos auxiliares em
		// cada dia
		double tempociclo_aux = 0;
		query = "select sum(tempociclofornecimento) from rotas where cods=" + codSimulacao + " and zonai=99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			tempociclo_aux = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo o número de visitas médio esperado para veículos regulares em
		// cada dia
		double nrvisitasesperadas_reg = 0;
		query = "select sum(totvisitasplanejadas) from rotas where cods=" + codSimulacao + " and zonai<>99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			nrvisitasesperadas_reg = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo o número de visitas médio realizadas pelos veículos regulares em
		// cada dia
		double nrvisitasrealizadas_reg = 0;
		query = "select sum(totvisitasexecutadas) from rotas where cods=" + codSimulacao + " and zonai<>99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			nrvisitasrealizadas_reg = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo o número de visitas médio transferidas para veículos auxiliares
		// em cada dia
		// double nrvisitastranferidas_aux = nrvisitasesperadas_reg -
		// nrvisitasrealizadas_reg;

		// Descobrindo o número de visitas médio realizadas pelos veículos auxiliares em
		// cada dia
		double nrvisitasrealizadas_aux = 0;
		query = "select sum(totvisitasexecutadas) from rotas where cods=" + codSimulacao + " and zonai=99999";
		stmt = con.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			nrvisitasrealizadas_aux = rs.getDouble(1) / ciclos;
		}
		rs.close();

		// Descobrindo o percentual médio de visitas não realizadas
		double percvisitasnaoatendidas = 0;
		percvisitasnaoatendidas = (nrvisitasesperadas_reg - nrvisitasrealizadas_reg - nrvisitasrealizadas_aux)
				/ nrvisitasesperadas_reg;

		// Presistencia das estatísticas na tabela de simulação
		query = "UPDATE simulacao SET nrveiculos_reg=?, nrveiculos_aux=?, distviajada_reg=?, distviajada_aux=?, "
				+ "tempociclo_reg=?, tempociclo_aux=?, nrvisitasesperadas_reg=?, nrvisitasrealizadas_reg=?, "
				+ "nrvisitasrealizadas_aux=?, percvisitasnaoatendidas=? WHERE id=?;";
		PreparedStatement ps = con.prepareStatement(query);

		ps.setInt(1, nrveiculos_reg);
		ps.setInt(2, nrveiculos_aux);
		ps.setDouble(3, distviajada_reg);
		ps.setDouble(4, distviajada_aux);
		ps.setDouble(5, tempociclo_reg);
		ps.setDouble(6, tempociclo_aux);
		ps.setDouble(7, nrvisitasesperadas_reg);
		ps.setDouble(8, nrvisitasrealizadas_reg);
		// ps.setDouble(9, nrvisitastranferidas_aux);
		ps.setDouble(9, nrvisitasrealizadas_aux);
		ps.setDouble(10, percvisitasnaoatendidas);
		ps.setInt(11, codSimulacao);
		ps.addBatch();
		ps.executeBatch();

		con.close();

	}

	/**
	 * Efetua o Calculo de TH
	 * 
	 * @param raio
	 * @param k2
	 * @param velocidade
	 * @param indice
	 * @return
	 */
	public static double calculaTh(double raio, double k2, double velocidade) {
		double resultadoMedio;
		resultadoMedio = (2 * raio * 60 * k2) / velocidade;
		return resultadoMedio;
	}

	/**
	 * Efetua o Calculo de Tu
	 * 
	 * @param th
	 * @param h0
	 * @param indice
	 * @return
	 */
	public static double calculaTu(double th, double h0) {
		// System.out.println("th: "+(h0-th));
		return (h0 - th);
	}

	/**
	 * Faz a seleção dos pontos que serão visitados. No final é acrescido mais um
	 * ponto que será o retorno ao depósito.
	 */
	public int[] gerarPontosSelecionados(int i) {
		int c = 0;
		double nrRand;
		int[] ppR = new int[nrClientesZona[i]];
		for (int u = 0; u < nrClientesZona[i]; u++) {
			nrRand = distRndZeroUm.sample();
			if (nrRand <= probabilidadeDeClienteEstarNaRota) {
				ppR[u] = 1;
				c = c + 1;
			} else {
				ppR[u] = 0;
			}
		}
		totalPontoCartesianoSelecionados = c;
		return ppR;
	}

	/**
	 * Geração dos Pontos Cartesianos Selecionados
	 * 
	 * @param geral
	 * @param rota
	 * @param x
	 * @param y
	 */
	public double[][] montarPontosCartesianosSelecionados(int i, int[] vetorPontosSelecionados) {
		double[] pcsX = new double[totalPontoCartesianoSelecionados + 1]; // se deve pela necessidade de adicionar o
																			// ponto 0.0 no inicio dos pontos
																			// selecionados
		double[] pcsY = new double[totalPontoCartesianoSelecionados + 1];
		// int[] pcS = pontoCartesianoSelecionados; // não preciusaria ser
		// inicializado??
		double[][] ptSel = new double[2][totalPontoCartesianoSelecionados + 1];
		pcsX[0] = 0.0;
		pcsY[0] = 0.0;
		int l = 1;
		for (int u = 0; u < nrClientesZona[i]; u++) {
			if (vetorPontosSelecionados[u] == 1) {
				pcsX[l] = pontoCartesianoX[u];
				pcsY[l] = pontoCartesianoY[u];
				l = l + 1;
			}
		}

		// Preparação de array para retorno de pontos selecionados
		ptSel[0] = pcsX;
		ptSel[1] = pcsY;
		return ptSel;
	}

	/**
	 * Efetua o Calculo do número de clientes em cada zona
	 * 
	 * @param numeroClienteZona
	 * @param indice
	 * @return Número de clientes em casa zona
	 */
	public int calculaNumeroClienteZona(int[] numeroClienteZona, int indice) {
		double clientesZona = numeroClienteZona[indice - 1];
		double divisao = (tu[indice] / tu[indice - 1]);
		return (int) Math.round(clientesZona * divisao);

	}

	/**
	 * Efetua o Calculo do Raio
	 * 
	 * @param area
	 * @param alfa
	 * @param raio
	 * @param indice
	 * @param indiceAlfa
	 * @return
	 */
	public double calculaRaio(int indice, int indiceAlfa) {
		double auxA, auxB, auxC;
		auxA = (area[indice - 1] * 360);
		auxB = (alfa[indiceAlfa] - alfa[indiceAlfa - 1]);
		auxC = (raio[indice - 1] * raio[indice - 1]);
		return Math.sqrt(auxA / (Math.PI * auxB) + auxC);
	}

	/**
	 * Efetua o calculo da jornada de trabalho
	 * 
	 * @param horas
	 * @return retorna o valor da jornada de trabalho em minutos
	 */
	public double converteHoraParaMinuto(double horas) {
		return (horas * 60);
	}

	/**
	 * Efetua o Calculo da Area
	 * 
	 * @param numeroClienteZona
	 * @param densidade
	 * @param indice
	 * @return
	 */
	public double calculaArea(int numeroClienteZona, double densidade) {
		return (numeroClienteZona / densidade);
	}

	/**
	 * Calcular o valor de Sigma.
	 * 
	 * @param int indice, double ets
	 * @return Retorno um double com o valor de Sigma.
	 */
	public double calculaSigma(double indice, double ets) {
		return (indice * ets);
	}

	/**
	 * Gera os pontos cartesianos em x
	 * 
	 * @param ppr
	 * @param ppt
	 * @return Retorno o valor do ponto cartesiano X;
	 */
	public static double gerarPontoCartesianoX(double pontoPolarR, double pontoPolarTeta) {
		double valor = (pontoPolarR * Math.cos(pontoPolarTeta * Math.PI / 180));
		NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(7);
		valor = Double.parseDouble(nf.format(valor));
		return valor;
	}

	/**
	 * Gera os pontos cartesianos em Y
	 * 
	 * @param ppr
	 * @param ppt
	 * @return Retorno o valor do ponto cartesiano Y;
	 */
	public static double gerarPontoCartesianoY(double pontoPolarR, double pontoPolarTeta) {
		double valor = (pontoPolarR * Math.sin(pontoPolarTeta * Math.PI / 180));
		NumberFormat nf = DecimalFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(7);
		valor = Double.parseDouble(nf.format(valor));
		return valor;
	}

	/**
	 * Gera o Ponto Polar R
	 * 
	 * @param raio1
	 * @param raio2
	 * @return Retorna o valor do Ponto Polar R
	 */
	public double gerarPontoR(double raio1, double raio2) {
		return (raio1 + (raio2 - raio1) * distRndZeroUm.sample());
	}

	/**
	 * Gera o Ponto Polar Teta
	 * 
	 * @param raio1
	 * @param raio2
	 * @return Retorna o valor do Ponto Polar Teta
	 */
	public double gerarPontoTeta(double alfa1, double alfa2) {
		return (alfa1 + (alfa2 - alfa1) * distRndZeroUm.sample());
	}

	/**
	 * Faz a geração dos pontos Polares e Cartesianos
	 * 
	 * @param x     - eixo x das coordenadas
	 * @param y     - eixo y das coordenadas
	 * @param geral - Classe Geral
	 * @param rota  - Classe Rota
	 */
	public void gerarPontos(int x, int y) {
		// int nrClienteNaZona = nrClientesZona[x];
		double[] pcX = new double[nrClientesZona[x]];
		double[] pcY = new double[nrClientesZona[x]];
		double[] ppR = new double[nrClientesZona[x]];
		double[] ppT = new double[nrClientesZona[x]];

		for (int u = 0; u < nrClientesZona[x]; u++) {
			ppR[u] = gerarPontoR(raio[x], raio[x + 1]);
			ppT[u] = gerarPontoTeta(alfa[y], alfa[y + 1]);
		}

		for (int u = 0; u < nrClientesZona[x]; u++) {
			pcX[u] = gerarPontoCartesianoX(ppR[u], ppT[u]);
			pcY[u] = gerarPontoCartesianoY(ppR[u], ppT[u]);
		}

		pontoPolarR = ppR;
		pontoPolarTeta = ppT;
		pontoCartesianoX = pcX;
		pontoCartesianoY = pcY;

	}

	public void calculaCentroMassa() throws SQLException {
		pontoCMX = 0.0;
		pontoCMY = 0.0;
		List<Double> todosPontosX = new ArrayList<Double>();
		List<Double> todosPontosY = new ArrayList<Double>();

		for (int i = 0; i < numAreaX; i++) {
			for (int j = 0; j < numAreaY; j++) {
				double[] tempX = rotas[i][j].getPontoCartesianoSelecionadosX();
				double[] tempY = rotas[i][j].getPontoCartesianoSelecionadosY();
				// System.out.println(tempX.length);
				for (int t = 0; t < tempX.length; t++) {
					todosPontosX.add(tempX[t]);
					todosPontosY.add(tempY[t]);
				}
			}
		}
		pontoCMX = Utilidades.mean(todosPontosX);
		pontoCMY = Utilidades.mean(todosPontosY);

		// Criar rota auxiliar
		Double[] a = new Double[todosPontosX.size()];
		Double[] b = new Double[todosPontosY.size()];
		rotaAux = new RouteRandomAuxiliar(this, 99999, 0, ArrayUtils.toPrimitive(todosPontosX.toArray(a)),
				ArrayUtils.toPrimitive(todosPontosY.toArray(b)));
	}

	/**
	 * Calcula todos os centros de massa para todos os setores
	 * 
	 * @return centros[][][] dimensão 1 : determina as coordenadas X ou Y dimensão 2
	 *         : determina o eixo X dimensão 3 : determina o eixo Y
	 */
	public double[][][] calculaTodosCentrosMassa() {
		double[][][] centros = new double[2][numAreaX][numAreaY];
		pontoCMX = 0.0;
		pontoCMY = 0.0;

		for (int i = 0; i < numAreaX; i++) {
			for (int j = 0; j < numAreaY; j++) {
				List<Double> pontosX = new ArrayList<Double>();
				List<Double> pontosY = new ArrayList<Double>();
				for (int t = 0; t < rotas[i][j].getPontoCartesianoSelecionadosX().length; t++) {
					pontosX.add(rotas[i][j].getPontoCartesianoSelecionadosX()[t]);
					pontosY.add(rotas[i][j].getPontoCartesianoSelecionadosY()[t]);
				}
				centros[0][i][j] = Utilidades.mean(pontosX);
				centros[1][i][j] = Utilidades.mean(pontosY);
			}
		}

		return centros;
	}

	/**
	 * Calcula todos os centros para todos os setores
	 * 
	 * @return centros[][][] dimensão 1 : determina as coordenadas X ou Y dimensão 2
	 *         : determina o eixo X dimensão 3 : determina o eixo Y
	 */
	public double[][][] calculaTodosCentros() {
		double[][][] centros = new double[2][numAreaX][numAreaY];
		pontoCMX = 0.0;
		pontoCMY = 0.0;

		for (int i = 0; i < numAreaX; i++) {
			for (int j = 0; j < numAreaY; j++) {
				double d = raio[i] + (raio[i + 1] - raio[i]) / 2;
				double teta = alfa[i] + (alfa[i + 1] - alfa[i]) / 2;
				centros[0][i][j] = gerarPontoCartesianoX(d, teta);
				centros[1][i][j] = gerarPontoCartesianoY(d, teta);
			}
		}

		return centros;
	}

	public Double getPontoCMX() {
		return pontoCMX;
	}

	public Double getPontoCMY() {
		return pontoCMY;
	}

	public Route[][] getRotas() {
		// TODO Auto-generated method stub
		return rotas;
	}

	public int[] getNrClientesZona() {
		// TODO Auto-generated method stub
		return nrClientesZona;
	}

	public RouteRandomAuxiliar getRotaAux() {
		return rotaAux;
	}

	/**
	 * @return the codSimulacao
	 */
	public int getCodSimulacao() {
		return codSimulacao;
	}

	/**
	 * @return the pontoTransAuxX
	 */
	public double getPontoTransAuxX() {
		return pontoTransAuxX;
	}

	/**
	 * @return the pontoTransAuxY
	 */
	public double getPontoTransAuxY() {
		return pontoTransAuxY;
	}

}
