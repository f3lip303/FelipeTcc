package model;


public class TimeCostTypeAccess implements ICostTypeAccess{
	private DistanceTimeCostMatrixDesorderCustomers distanceTimeCostMatrixDesorderCustomers;

	public TimeCostTypeAccess(DistanceTimeCostMatrixDesorderCustomers distanceTimeCostMatrixDesorderCustomers) {
		this.setDistanceTimeCostMatrixDesorderCustomers(distanceTimeCostMatrixDesorderCustomers);
	}

	
	/**
	 * @return the distanceTimeCostMatrixDesorderCustomers
	 */
	public DistanceTimeCostMatrixDesorderCustomers getDistanceTimeCostMatrixDesorderCustomers() {
		return distanceTimeCostMatrixDesorderCustomers;
	}

	/**
	 * @param distanceTimeCostMatrixDesorderCustomers the
	 *                                                distanceTimeCostMatrixDesorderCustomers
	 *                                                to set
	 */
	public void setDistanceTimeCostMatrixDesorderCustomers(
			DistanceTimeCostMatrixDesorderCustomers distanceTimeCostMatrixDesorderCustomers) {
		this.distanceTimeCostMatrixDesorderCustomers = distanceTimeCostMatrixDesorderCustomers;
	}


	public void addCost(Visita a, Visita b, Double cost) {
		this.getDistanceTimeCostMatrixDesorderCustomers().addTimeCost(a, b, cost);
		
	}


	public Double getCost(Visita a, Visita b) {
		return this.getDistanceTimeCostMatrixDesorderCustomers().getTimeCost(a, b);
	}


	public double getCostAmong(Visita start, Visita[] customers, Visita end) {
		// TODO Auto-generated method stub
		return this.getDistanceTimeCostMatrixDesorderCustomers().getTimeCostAmong(start, customers, end);
	}


	public Double getCostAmong(Visita... customers) {
		// TODO Auto-generated method stub
		return this.getDistanceTimeCostMatrixDesorderCustomers().getTimeCostAmong(customers);
	}
	

}
