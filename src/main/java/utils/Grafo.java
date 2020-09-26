package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Visita;

public class Grafo {

	public final int numerosDeNos;
	public List grafo = new ArrayList<Visita>();

	public Grafo(int numerosDeNos) {
		super();
		this.numerosDeNos = numerosDeNos;
		try {
			setGrafo(criarMatriz(numerosDeNos, 0));
		} catch (Exception ex) {
			if (ex.getMessage() == null)
				System.out.println("Ocorreu um erro de " + ex + " no construtor");
			else
				System.out.println(ex.getMessage());
		}
	}

	public List criarMatriz(int tamanho, int valorPadrao) throws Exception {
		if (tamanho <= 1) {
			throw new Exception("O tamanho deve ser manhor que 1");
		}
		try {
			for (int i = 0; i < tamanho; i++) {
				Random random = new Random();
				int x = random.nextInt(100 - 0) + 0;
				int y = random.nextInt(100 - 0) + 0;
				Visita visita = new Visita(x, y);

				// Teste
				visita.setCodigoVisita(valorPadrao);
				valorPadrao++;
				// Teste

				grafo.add(visita);
			}

		} catch (Exception e) {
			if (e.getMessage() == null)
				System.out.println("Ocorreu um erro de " + e + " em criaMatriz");
			else
				System.out.println("Erro ao criar a matriz");
		}
		return grafo;
	}
	
	public void printListaClientes() {
		for (int i = 0; i < grafo.size(); i++) {
			System.out.println(grafo.get(i));
		}
	}

	public List getGrafo() {
		return grafo;
	}

	public void setGrafo(List grafo) {
		this.grafo = grafo;
	}

	public int getNumerosDeNos() {
		return numerosDeNos;
	}

}
