package model;

import utils.DistanceCalculator;
import utils.MatrizDistancia;

public class CostMatrixDesorderCustomers extends MatrizDistancia {
	public CostMatrixDesorderCustomers(Visita[] customers) {
		super(customers);
	}

	protected int getCustomerIndex(Visita customer) {
		for (int i = customers.length - 1; i >= 0; i--) {
			if (customers[i].compareTo(customer) == 0)
				return i;
		}
		System.out.println("Customer " + customer.getCodigoVisita() + " not found");
		return -1;
	}

	@Override
	public void addCost(Visita a, Visita b, Double distance) {
		int ia = getCustomerIndex(a), ib = getCustomerIndex(b);
		matrizDistancia[ia][ib] = distance;
		matrizDistancia[ib][ia] = distance;
		size++;
	}

	@Override
	public void addPoints(Visita a, Visita b) {
		int ia = getCustomerIndex(a), ib = getCustomerIndex(b);
		double distance = DistanceCalculator.distance(a, b);
		matrizDistancia[ia][ib] = distance;
		matrizDistancia[ib][ia] = distance;
		size++;
	}

	@Override
	public Double getMatrizDistancia(Visita a, Visita b) {
		if (a == b)
			return 0d;
		return matrizDistancia[getCustomerIndex(a)][getCustomerIndex(b)];
	}

}
