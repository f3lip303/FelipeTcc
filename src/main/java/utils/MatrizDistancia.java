package utils;

import java.util.List;

import model.DistanceTimeCostMatrixDesorderCustomers;
import model.EnumCostTypeCostMatrix;
import model.Visita;

public class MatrizDistancia {

	protected double[][] matrizDistancia;
	protected Visita[] customers;
	protected int size;
	private EnumCostTypeCostMatrix enumCostTypeCostMatrix; 

	public MatrizDistancia(Visita[] customers) {
		customers = customers;
	}

	public MatrizDistancia(int numVisitas, Visita[] customers) {
		this.matrizDistancia = calcularMatriz(numVisitas, customers);
		this.customers = customers;
		//enumCostTypeCostMatrix.setType(2);

	}

	public boolean isDistanceTimeCostMatrixDesorderCustomers() {
		return this instanceof DistanceTimeCostMatrixDesorderCustomers;
	}

	private double distance(Visita a, Visita b) {
		return Math.sqrt(Math.pow(calculateHorizontalLine(a, b), 2) + Math.pow(calculateVerticalLine(a, b), 2));
	}

	public double[][] calcularMatriz(int numVisitas, Visita[] customers) {

		double[][] matrizDistancia = new double[numVisitas][numVisitas];
		for (int i = 0; i < matrizDistancia.length; i++) {
			for (int j = 0; j < matrizDistancia.length; j++) {
				if (i == j) {
					matrizDistancia[i][j] = 9999;

				} else {
					matrizDistancia[i][j] = distance(customers[i], customers[j]);
				}
			}
		}
		return matrizDistancia;
	}

	public void addCost(Visita a, Visita b, Double distance) {
		int ia = a.getCodigoVisita() - 1, ib = b.getCodigoVisita() - 1;
		matrizDistancia[ia][ib] = distance;
		matrizDistancia[ib][ia] = distance;
		size++;
	}

	public void addPoints(Visita a, Visita b) {
		addCost(a, b, DistanceCalculator.distance(a, b));
	}

	public Double getCostAmong(Visita... customers) {
		double totalCost = 0d;
		int size = customers.length - 1;
		for (int i = 0; i < size; i++) {
			if (customers[i] == null || customers[i + 1] == null) {
				break;
			}
			totalCost += getMatrizDistancia(customers[i], customers[i + 1]);
		}
		return totalCost;
	}

	public double getCostAmong(Visita start, Visita[] customers, Visita end) {
		return this.getMatrizDistancia(start, customers[0]) + this.getCostAmong(customers)
				+ this.getMatrizDistancia(customers[customers.length - 1], start);
	}

	public Double getMatrizDistancia(Visita a, Visita b) {
		if (a == b)
			return 0d;
		//return matrizDistancia[a.getCodigoVisita() - 1][b.getCodigoVisita() - 1];
		return matrizDistancia[a.getCodigoVisita()][b.getCodigoVisita()];
	}

	private double calculateHorizontalLine(Visita a, Visita b) {
		double ax = a.getX(), bx = b.getX();
		return ax > bx ? ax - bx : bx - ax;
	}

	private double calculateVerticalLine(Visita a, Visita b) {
		double ay = a.getY(), by = b.getY();
		return ay > by ? ay - by : by - ay;
	}

	public void printMatriz(double matriz[][]) throws Exception {

		if (matriz == null)
			throw new Exception("a matriz e nula");

		if (matriz[0] == null)
			throw new Exception("a matriz nao foi inicializada");

		int tamanho = matriz.length;

		for (int i = 0; i < tamanho; i++) {
			for (int j = 0; j < tamanho; j++)
				System.out.printf("[" + matriz[i][j] + "] ");
			System.out.printf("\n");
		}

	}

	public double[][] getMatrizDistancia() {
		return matrizDistancia;
	}

	public void setMatrizDistancia(double[][] matrizDistancia) {
		this.matrizDistancia = matrizDistancia;
	}

	public Visita[] getCustomers() {
		return customers;
	}

	public void setCustomers(Visita[] customers) {
		this.customers = customers;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public EnumCostTypeCostMatrix getEnumCostTypeCostMatrix() {
		return enumCostTypeCostMatrix;
	}

	public void setEnumCostTypeCostMatrix(EnumCostTypeCostMatrix enumCostTypeCostMatrix) {
		this.enumCostTypeCostMatrix = enumCostTypeCostMatrix;
	}

}
