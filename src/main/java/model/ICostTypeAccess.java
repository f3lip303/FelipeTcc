package model;


public interface ICostTypeAccess {
	public void addCost(CustomerAdaptaded a, CustomerAdaptaded b, Double cost);

	public Double getCost(CustomerAdaptaded a, CustomerAdaptaded b);

	public double getCostAmong(CustomerAdaptaded start, CustomerAdaptaded[] customers, CustomerAdaptaded end);

	public Double getCostAmong(CustomerAdaptaded... customers);
}
