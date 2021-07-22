package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

/*import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;*/
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
//import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.math3.distribution.LogNormalDistribution;

import model.Visita;

public class Utilidades {

	// static BasicDataSource ds = new BasicDataSource();

	static DataSource ds;

	public static Integer[] inicializaArray(Integer[] myArray, Integer value) {
		for (int i = 0; i < myArray.length; i++) {
			myArray[i] = 0;
		}
		return myArray;
	}

	public static double calculaDistancia2Pontos(double xA, double xB, double yA, double yB) {
		double valorX = 0, valorY = 0;
		valorX = (xA - xB);
		valorY = (yA - yB);
		return Math.sqrt(Math.pow(valorX, 2) + Math.pow(valorY, 2));
	}

	public static double calculaCustoRota(double[][] matrix, List<Integer> rt) {
		double custo = 0;
		for (int i = 0; i < (rt.size() - 1); i++) {
			custo += matrix[rt.get(i)][rt.get(i + 1)];
		}
		return custo;
	}

	public static double calculaCustoRota(double[][] matrix, Integer[] rt) {
		double custo = 0;
		for (int i = 0; i < (rt.length - 1); i++) {
			custo += matrix[rt[i]][rt[i + 1]];
		}
		return custo;
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
		for (int i = 1; i < route.length - 1; i++) {
			rota[i] = recVisita(visitas, route[i]);

		}
		rota[route.length - 1] = visitas[0];
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

	public static double[][] definirMatrizDistancia(double[] ptX, double[] ptY) {
		// System.out.println("pontosX:"+Arrays.toString(pontoCartesianoSelecionadosX));
		// System.out.println("pontosY:"+Arrays.toString(pontoCartesianoSelecionadosY));
		int tam = ptX.length;
		double[][] valorT = new double[tam][tam];
		for (int u = 0; u < tam; u++) {
			for (int k = 0; k < tam; k++) {
				valorT[u][k] = Utilidades.calculaDistancia2Pontos(ptX[k], ptX[u], ptY[k], ptY[u]);
			}
		}
		return valorT;
	}

	public static double[][] correcaoMatrizDistancia(double[][] m, LogNormalDistribution distAdjustDistance) {
		int tam = m.length;
		// NormalDistribution normalDistribution = new
		// NormalDistribution(meanPrmAdjustDistance, sdPrmAdjustDistance);
		double[][] matrizDistanciaCor = new double[tam][tam];
		for (int i = 0; i < tam; i++) {
			for (int j = 0; j < tam; j++) {
				// double prmAdjust = distAdjustDistance.sample();
				matrizDistanciaCor[i][j] = (1 * m[i][j]);
				matrizDistanciaCor[j][i] = (1 * m[j][i]);
			}
		}
		return matrizDistanciaCor;
	}

	public static double[][] correcaoMatrizDistancia(double[][] m) {
		int tam = m.length;
		// NormalDistribution normalDistribution = new
		// NormalDistribution(meanPrmAdjustDistance, sdPrmAdjustDistance);
		double[][] matrizDistanciaCor = new double[tam][tam];
		for (int i = 0; i < tam; i++) {
			for (int j = 0; j < tam; j++) {
				// double prmAdjust = distAdjustDistance.sample();
				matrizDistanciaCor[i][j] = (1 * m[i][j]);
				matrizDistanciaCor[j][i] = (1 * m[j][i]);
			}
		}
		return matrizDistanciaCor;
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

	public static int inArray(double[] a, double valor) {
		int isThere = -1;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == valor) {
				isThere = i;
				break;
			}
		}
		return isThere;
	}

	public static double mean(List<Double> valores) {
		double acumulador = 0;
		for (Double valor : valores) {
			acumulador += valor;
		}
		return acumulador / valores.size();
	}

	public static LinkedHashMap<String, Double> sortHashMapByValues2(HashMap<String, Double> passedMap) {
		List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		Collections.sort(mapKeys);
		List<Double> mapValues = new ArrayList<Double>(passedMap.values());
		Collections.sort(mapValues);

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = (Double) val;

				if (comp1.equals(comp2)) {
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((String) key, (Double) val);
					break;
				}

			}

		}
		return sortedMap;
	}

	public static LinkedHashMap<Integer, Double> sortHashMapByValues(HashMap<Integer, Double> passedMap) {
		List<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
		Collections.sort(mapKeys);
		List<Double> mapValues = new ArrayList<Double>(passedMap.values());
		Collections.sort(mapValues);

		LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = (Double) val;

				if (comp1.equals(comp2)) {
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put((Integer) key, (Double) val);
					break;
				}

			}

		}
		return sortedMap;
	}

	/**
	 * Efetua o calculo da Análise Sequencial - sequential probability ratio test
	 * (SPRT) Verifica se o veículo é capaz de atender a próxima visita e retornar
	 * ao depósito
	 * 
	 * @param n               : nr de clientes +1
	 * @param matrizDistancia : matriz de distância
	 * @param rota            : rota de visitas
	 * @param vz0             : velocidade livre
	 * @param vz1             : velocidade transito congestionado
	 * @param alfa            : erro do tipo 1
	 * @param beta            : erro do tipo 2
	 * @return Retorna vetor com resultado do teste de hipótese: [0] indice em que
	 *         verificou-se condição ou indice final; [1] 0 (H0) ou 1 (H1) ou 2
	 *         (final de roteiro) representar H0 ou H1
	 */
	public static int analiseSequencialAgentes(double[][] matrizDistancia, List<Integer> rotaAtual, double vz0,
			double vz0sd, double vz1, double vz1sd, int seqVisitaAtual, double[] velObserv, double alfa, double beta) {

		double tot1, tot2, auxA, auxB;
		double zi, a0, a1, b20, b21;
		int sig;

		auxA = 0;
		auxB = 0;
		sig = -1;
		// i = 0;
		tot1 = 0;
		tot2 = 0;

		b20 = Math.log((vz0sd * vz0sd) / (vz0 * vz0) + 1);
		a0 = Math.log(vz0) - b20 / 2;
		auxA = Math.log((1 - beta) / alfa);
		// auxA = Math.log(auxA);

		b21 = Math.log((vz1sd * vz1sd) / (vz1 * vz1) + 1);
		a1 = Math.log(vz1) - b21 / 2;
		auxB = Math.log(beta / (1 - alfa));
		// auxB = Math.log(auxB);
		// System.out.println("Velocidades observadas: "+Arrays.toString(velObserv));

		for (int i = 0; i < seqVisitaAtual; i++) {
			// Para cada visita a ser realizada no roteiro
			tot1 += Math.pow(Math.log(velObserv[i]) - a0, 2) / b20;
			tot2 += Math.pow(Math.log(velObserv[i]) - a1, 2) / b21;
		}

		zi = seqVisitaAtual * Math.log(Math.sqrt(b20) / Math.sqrt(b21)) + tot1 / 2 - tot2 / 2;
		// condição de normalidade
		if (zi <= auxB) {
			sig = 0;
		}
		// condição de congestionamento
		else if (zi >= auxA) {
			sig = 1;
		}
		// nenhuma constatação
		else {
			sig = 2;
		}

		return sig;
	}

	/*
	 * public static void SendMailTLS(String msg) {
	 * 
	 * // Capturando o nome do PC String hostname = "Unknown"; try { InetAddress
	 * addr; addr = InetAddress.getLocalHost(); hostname = addr.getHostName(); }
	 * catch (UnknownHostException ex) {
	 * System.out.println("Hostname can not be resolved"); }
	 * 
	 * // Envio do email
	 * 
	 * final String username = "f3lip303@.ufc.br"; final String password =
	 * "182pcasdftk";
	 * 
	 * Properties props = new Properties(); props.put("mail.smtp.auth", "true");
	 * props.put("mail.smtp.starttls.enable", "true"); props.put("mail.smtp.host",
	 * "smtp.gmail.com"); props.put("mail.smtp.port", "587");
	 * 
	 * Session session = Session.getInstance(props, new javax.mail.Authenticator() {
	 * protected PasswordAuthentication getPasswordAuthentication() { return new
	 * PasswordAuthentication(username, password); } });
	 * 
	 * try {
	 * 
	 * Message message = new MimeMessage(session); message.setFrom(new
	 * InternetAddress(username)); message.setRecipients(Message.RecipientType.TO,
	 * InternetAddress.parse(username));
	 * message.setSubject("Resultados da Simulação em " + hostname);
	 * message.setText(msg + "\n\n Concluído no computador de " + hostname);
	 * Transport.send(message);
	 * 
	 * System.out.println("---> Email enviado!");
	 * 
	 * } catch (MessagingException e) { throw new RuntimeException(e); } }
	 */

	public static double[] fromString(String string) {
		String[] strings = string.replace("[", "").replace("]", "").replace(",", "").split(" ");
		double[] result = new double[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Double.parseDouble(strings[i]);
		}
		return result;
	}

	/*
	 * public static DataSource setupDataSource() {
	 * 
	 * ds = (DataSource) new
	 * DriverManagerConnectionFactory("jdbc:postgresql://localhost:5432/TccFelipe",
	 * "felipe", "felipe");
	 * 
	 * ds.setDriverClassName("org.postgresql.Driver"); ds.setUsername("felipe");
	 * ds.setPassword("felipe");
	 * ds.setUrl("jdbc:postgresql://localhost:5432/TccFelipe"); ds.setMinIdle(15);
	 * // ds.setMaxActive(300); // ds.setRemoveAbandoned(true);
	 * ds.setRemoveAbandonedTimeout(30000);
	 * 
	 * 
	 * return ds; }
	 */

	public static Connection getConnection() throws SQLException, ClassNotFoundException {

		// Class.forName("org.postgresql.Driver");
		try {

			/*
			 * try { Class.forName("org.postgresql.Driver"); } catch (ClassNotFoundException
			 * e) { e.printStackTrace(); }
			 */
			//Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/TccFelipe", "felipe",
					"felipe");
			return con;

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
