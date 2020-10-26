package tccfelipe.felipetcc;

import java.util.Random;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import model.Visita;
import optmizations.HeuristicaConstrucaoInsercaoMaisDistante;
import optmizations.Opt2;
import utils.Grafo;
import utils.MatrizDistancia;
import utils.Utilidades;

/**
 * Hello world!
 * 
 * @author felipe de lima
 */
public class App {
	public static void main(String[] args) throws Exception {
		
		MongoClient mongoClient = new MongoClient("localhost",27017);
		MongoDatabase database = mongoClient.getDatabase("TCC");
		MongoCollection<Document> colection = database.getCollection("Visitas");
		MongoCursor cursor = colection.find().iterator();
		
		try {
			 while(cursor.hasNext()) {
	                System.out.println(cursor.next().toString());
			 }
		} finally {
			cursor.close();
		}
		
		/*System.out.println("Hello World!");
		Grafo matriz = new Grafo(20);
		//matriz.printListaClientes();

		Visita[] visitas = Utilidades.copyGrafo(matriz.grafo);

		MatrizDistancia mtrizDist = new MatrizDistancia(matriz.getGrafo().size(), visitas);
		//mtrizDist.printMatriz(mtrizDist.getMatrizDistancia());

		HeuristicaConstrucaoInsercaoMaisDistante hcis = new HeuristicaConstrucaoInsercaoMaisDistante();
		Integer[] saida = hcis.solve(mtrizDist.getMatrizDistancia());

		for (int i = 0; i < saida.length; i++) {
			System.out.println("ponto número = " + i + "é igual a = " + saida[i]);

		}
		System.out.println("--------- Teste Do 2-OPT");
		System.out.println(" ");
		
		Opt2 opt2 = new Opt2(mtrizDist, visitas);
		Visita[] teste = Utilidades.route(saida, visitas);
		Visita[] saida2 = opt2.optimize(teste);
		for (int i = 0; i < saida.length; i++) {
			System.out.println("ponto número = " + i + "é igual a = " + saida2[i].getCodigoVisita());

		}
		/*
			 * Random random = new Random(); for (int i = 0; i < 10; i++) { int x =
			 * random.nextInt(100-0+1)+0; System.out.println(x); }
			 */

	}
}
