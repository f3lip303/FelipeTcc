package test;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.bson.Document;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.opencsv.CSVWriter;

import model.CustomerAdaptaded;
import utils.MatrizDistancia;
import utils.Network;
import utils.NetworkRandom;
import utils.RouteFactory;
import utils.RouteRandom;
import utils.TSPReader;
import utils.Utilidades;

public class TestRouteRandom {

	public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {

		
		RouteRandom rota;
		// RouteRandom rotaMelhorada;
		Connection con2 = Utilidades.getConnection();

		NetworkRandom net = new NetworkRandom("dinamica_centroMassa");
		
		int testCodigo = net.getCodSimulacao();
		Gson gson = new Gson();

		Utilidades util = new Utilidades();


		net.criarRotas();

		rota = (RouteRandom) net.getRotas()[0][0];
		List<RouteRandom> rotaList = new ArrayList<RouteRandom>();
		for (int i = 0; i < net.getRotas().length; i++) {
			for (int j = 0; j < net.getRotas()[i].length; j++) {
				rotaList.add((RouteRandom) net.getRotas()[i][j]);
			}
		}
		// rotaMelhorada = net.getRotas().;

		double[][] matriz = util.definirMatrizDistancia(rota.getPontoCartesianoSelecionadosX(),
				rota.getPontoCartesianoSelecionadosY());

		double primeiraRota = util.calculaCustoRota(matriz, rota.getRotaInicial());

		double rotaMelhorada = util.calculaCustoRota(matriz, rota.getRotaMelhorada());

		//DataSource dataSource = Utilidades.setupDataSource();
		//Connection con = dataSource.getConnection();

		PreparedStatement ps = null;

		/*
		 * String query =
		 * "INSERT INTO rotas(cods, codc, zonai, zonaj, totvisitasplanejadas, totvisitasexecutadas, tempociclofornecimento, distviajada ) "
		 * + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
		 * 
		 * ps = con.prepareStatement(query); ps.setInt(1, 1); ps.setInt(2, 1);
		 * ps.setInt(3, 15); ps.setInt(4, 30); ps.setInt(5,
		 * rota.getTotVisitasPlanejadas()); // subtrai-se o ponto de origem que é um
		 * depósito e não uma visita ps.setInt(6, rota.getVisitaAtual());
		 * ps.setDouble(7, rota.getTempoGasto()); ps.setDouble(8, 24); ps.addBatch();
		 * ps.executeBatch();
		 * 
		 * con.close();
		 */

		//DataSource dataSource2 = Utilidades.setupDataSource();
		//Connection con2 = Utilidades.getConnection();

		String query2 = "INSERT INTO public.rota(pontocartesianoselecionadosy, pontocartesianoselecionadosx, rota, faixah, faixav) "
				+ "VALUES (?, ?, ?,? ,?);";
		ps = con2.prepareStatement(query2);
		ps.setString(1, Arrays.toString(rota.getPontoCartesianoSelecionadosY()));
		ps.setString(2, Arrays.toString(rota.getPontoCartesianoSelecionadosX()));
		ps.setString(3, rota.getRota().toString());
		ps.setLong(4, rota.getFaixaH());
		ps.setLong(5, rota.getFaixaV());
		ps.addBatch();
		ps.executeBatch();
		con2.close();

		String[] cabecalho = { "pontocartesianoselecionadosy", "pontocartesianoselecionadosx", "faixah",
				"faixav" };

		Writer writer =  new FileWriter ( " output2.csv ");
		CSVWriter csvWriter = new CSVWriter(writer);
		List<String[]> linhas = new ArrayList<String[]>();
		//percorrer array de pontos para o csv sair corretamente.
		linhas.add(new String[] { Arrays.toString(rota.getPontoCartesianoSelecionadosY())});
		linhas.add(new String[] { Arrays.toString(rota.getPontoCartesianoSelecionadosX())});
		linhas.add(new String[] { Integer.toString(rota.getFaixaH())});
		linhas.add(new String[] { Integer.toString(rota.getFaixaV())});

		
		csvWriter.writeNext(cabecalho);
		csvWriter.writeAll(linhas);


		csvWriter.flush();
		writer.close();

		System.out.println(primeiraRota);
		System.out.println(rotaMelhorada);

		// Utilizado para parar e Debugar
		int i = 0;
		while (i <= 10) {
			i = i + 1;
		}

		/*
		 * MongoClient mongoClient = new MongoClient("localhost",27017); MongoDatabase
		 * database = mongoClient.getDatabase("TCC"); MongoCollection<Document>
		 * collection = database.getCollection("Visitas"); MongoCursor cursor =
		 * collection.find().iterator();
		 * 
		 * try { while(cursor.hasNext()) { System.out.println(cursor.next().toString());
		 * } } finally { cursor.close(); }
		 * 
		 * collection = database.getCollection("network");
		 * 
		 * //Document document = (Document) gson.toJson(net);
		 * 
		 * collection.insertOne(new Document("Network", gson.toJson(net)));
		 */

		/*
		 * TSPReader tspReader = new
		 * TSPReader("/home/felipelima/eclipse-workspace/felipetcc/data/burma14.tsp");
		 * 
		 * System.out.println(tspReader.readDistances().toString());
		 * System.out.println("-------------------"); CustomerAdaptaded[] customers =
		 * tspReader.getCustomers();
		 * 
		 * List<Double> x = new ArrayList<Double>(); List<Double> y = new
		 * ArrayList<Double>();
		 * 
		 * for (CustomerAdaptaded customer : customers) { x.add(customer.getX());
		 * y.add(customer.getY()); } double[] xs = converterTeste(x); double[] ys =
		 * converterTeste(y); // * PROVAVEL MENTE, TEREI QUE PASSAR A INSTÂNCIA QUE SERÁ
		 * UTILIZADA NESTE MOMENTO, // * PARA PREENCHER AAS VARIAVEIS
		 * [pontoCartesianoSelecionadosX, pontoCartesianoSelecionadosY] rota =
		 * (RouteRandom) RouteFactory.getRoute(null, "dinamica_centroMassa", 15, 15, xs,
		 * ys); //Neste ponto, eu poderia já inicializar um leilão para que os veículos
		 * auxiliares podessem receber as suas demandas?
		 */

	}

	private static double[] converterTeste(List<Double> list) {

		double[] target = new double[list.size()];
		for (int i = 0; i < target.length; i++) {
			target[i] = list.get(i).doubleValue(); // java 1.4 style
			// or:
			target[i] = list.get(i); // java 1.5+ style (outboxing)
		}

		return target;

	}

}
