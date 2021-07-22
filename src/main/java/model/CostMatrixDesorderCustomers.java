package model;

import utils.DistanceCalculator;
import utils.MatrizDistancia;

public class CostMatrixDesorderCustomers extends CostMatrix {
	public CostMatrixDesorderCustomers(CustomerAdaptaded[] customers) {
		super(customers);
	}

	protected int getCustomerIndex(CustomerAdaptaded customerAdaptaded) {
		for (int i = customers.length - 1; i >= 0; i--) {
			if (customers[i].compareTo(customerAdaptaded) == 0)
				return i;
		}
		System.out.println("Customer " + customerAdaptaded.getId() + " not found");
		return -1;
	}

	@Override
	public void addCost(CustomerAdaptaded a, CustomerAdaptaded b, Double distance) {
		int ia = getCustomerIndex(a), ib = getCustomerIndex(b);
		costs[ia][ib] = distance;
		costs[ib][ia] = distance;
		size++;
	}

	@Override
	public void addPoints(CustomerAdaptaded a, CustomerAdaptaded b) {
		int ia = getCustomerIndex(a), ib = getCustomerIndex(b);
		double distance = DistanceCalculator.distance(a, b);
		costs[ia][ib] = distance;
		costs[ib][ia] = distance;
		size++;
	}

	@Override
	public Double getCost(CustomerAdaptaded a, CustomerAdaptaded b) {
		if (a == b)
			return 0d;
		return costs[getCustomerIndex(a)][getCustomerIndex(b)];
	}

}
