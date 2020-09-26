package model;


public interface ICostTypeAccess {
	public void addCost(Visita a, Visita b, Double cost);

	public Double getCost(Visita a, Visita b);

	public double getCostAmong(Visita start, Visita[] customers, Visita end);

	public Double getCostAmong(Visita... customers);
}
