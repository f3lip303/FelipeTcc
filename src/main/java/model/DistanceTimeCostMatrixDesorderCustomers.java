package model;

import utils.MatrizDistancia;

public class DistanceTimeCostMatrixDesorderCustomers extends CostMatrixDesorderCustomers {

	private double[][] times;
	private double speed;
	private ICostTypeAccess iCostTypeAccess;

	/**
	 * 
	 * @param customers
	 */
	public DistanceTimeCostMatrixDesorderCustomers(Visita[] customers) {
		super(customers);
		int qttCustomers = customers.length;
		this.setTimes(new double[qttCustomers][qttCustomers]);
	}

	/**
	 * @param customers
	 * @param times
	 * @param speed
	 */
	public DistanceTimeCostMatrixDesorderCustomers(Visita[] customers, double speed) {
		this(customers);
		this.setSpeed(speed);
	}

	/**
	 * This method generate the time matrix based on the distance matrix
	 * 
	 * @param costMatrix
	 * @param speed
	 */
	public DistanceTimeCostMatrixDesorderCustomers(MatrizDistancia costMatrix, double speed) {
		this(costMatrix.getCustomers(), speed);
		if (costMatrix.getEnumCostTypeCostMatrix() != null && costMatrix.getEnumCostTypeCostMatrix().isDistance()) {
			super.setMatrizDistancia(costMatrix.getMatrizDistancia());
			super.setEnumCostTypeCostMatrix(EnumCostTypeCostMatrix.DISTANCE_TIME);
			super.setSize(costMatrix.getSize());
			this.defineTimeCosts();
		} else
			throw new IllegalArgumentException(
					"The cost matrix param must be a cost matrix with a enum type cost matrix distance ");
	}

	/**
	 * 
	 * @param customers
	 * @param iCostTypeAccess
	 */
	public DistanceTimeCostMatrixDesorderCustomers(Visita[] customers, ICostTypeAccess iCostTypeAccess) {
		this(customers);
		this.setiCostTypeAccess(iCostTypeAccess);
	}

	/**
	 * 
	 * @param customers
	 * @param enumCostTypeCostMatrix
	 */
	public DistanceTimeCostMatrixDesorderCustomers(Visita[] customers,
			EnumCostTypeCostMatrix enumCostTypeCostMatrix) {
		this(customers);
		this.setEnumCostTypeCostMatrix(enumCostTypeCostMatrix);
	}

	public void defineTimeCosts() {
		if (this.getSpeed() > 0) {
			Visita[] customers = super.getCustomers();
			for (int i = customers.length - 1; i >= 0; i--) {
				for (int j = customers.length - 1; j >= 0; j--) {
					if (i != j)
						this.defineTimeCostByIndex(i, j);
				}
			}
		} else
			throw new IllegalStateException("Error: The speed is not setted, please set the speed.");
	}

	public void addTimeCost(Visita a, Visita b) {
		if (this.getSpeed() > 0) {
			this.defineTimeCostByCustomers(a, b);
		} else
			throw new IllegalStateException("Error: The speed is not setted, please set the speed.");
	}

	private void defineTimeCostByCustomers(Visita a, Visita b) {
		int ia = super.getCustomerIndex(a), ib = super.getCustomerIndex(b);
		if (ia >= 0 && ib >= 0) {
			double timeCostAToB = this.getDistanceCost(a, b) / speed, timeCostBToA = this.getDistanceCost(b, a) / speed;
			this.getTimes()[ia][ib] = timeCostAToB;
			this.getTimes()[ib][ia] = timeCostBToA;
		} else
			throw new IndexOutOfBoundsException("Error: There's nos exists customer A or B.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String info = "";
		int i2 = matrizDistancia.length, i1 = i2 - 1;
		System.out.println("Customers | Distance | Time");
		for (int i = 0; i < i1; i++) {
			for (int j = 0; j < i2; j++) {
				if (i != j) {
					System.out.println(this.getCustomers()[i].getCodigoVisita() + " " + this.getCustomers()[j].getCodigoVisita() + " | "
							+ this.getDistanceCost(this.getCustomers()[i], this.getCustomers()[j]) + " | "
							+ this.getTimeCost(this.getCustomers()[i], this.getCustomers()[j]));
				}
			}
		}
		return info;
	}

	private void defineTimeCostByIndex(int ia, int ib) {
		double timeCostAToB = this.getDistanceCostByIndex(ia, ib) / this.getSpeed(),
				timeCostBToA = this.getDistanceCostByIndex(ib, ia) / this.getSpeed();
		this.getTimes()[ia][ib] = timeCostAToB;
		this.getTimes()[ib][ia] = timeCostBToA;
	}

	public void addTimeCost(Visita a, Visita b, Double timeCost) {
		int ia = super.getCustomerIndex(a), ib = super.getCustomerIndex(b);
		this.getTimes()[ia][ib] = timeCost;
		this.getTimes()[ib][ia] = timeCost;
		super.setSize(super.getSize() + 1);
	}

	public void addDistanceCost(Visita a, Visita b, Double distance) {
		super.addCost(a, b, distance);
	}

	public Double getTimeCost(Visita a, Visita b) {
		if (a == b)
			return 0d;
		return times[getCustomerIndex(a)][getCustomerIndex(b)];
	}

	public Double getDistanceCost(Visita a, Visita b) {
		return super.getMatrizDistancia(a, b);
	}

	private Double getDistanceCostByIndex(int ia, int ib) {
		return super.getMatrizDistancia()[ia][ib];
	}

	@Override
	public double getCostAmong(Visita start, Visita[] customers, Visita end) {
		return this.getiCostTypeAccess().getCostAmong(start, customers, end);
	}

	@Override
	public Double getCostAmong(Visita... customers) {
		return this.getiCostTypeAccess().getCostAmong(customers);
	}

	public double getDistanceCostAmong(Visita start, Visita[] customers, Visita end) {
		return super.getCostAmong(start, customers, end);
	}

	public Double getDistanceCostAmong(Visita... customers) {
		return super.getCostAmong(customers);
	}

	public double getTimeCostAmong(Visita start, Visita[] customers, Visita end) {
		int limit = customers.length - 1;
		if (limit >= 0)
			return this.getTimeCost(start, customers[limit]) + this.getTimeCostAmong(customers)
					+ this.getTimeCost(customers[limit], end);
		return this.getTimeCost(start, end);
	}

	public Double getTimeCostAmong(Visita... customers) {
		double totalTimeCost = 0;
		int size = customers.length;
		if (size > 0)
			for (int i = 0; i < size - 1; totalTimeCost += this.getTimeCost(customers[i], customers[++i]))
				;
		return totalTimeCost;
	}

	/**
	 * @return the times
	 */
	public double[][] getTimes() {
		return times;
	}

	/**
	 * @param times the times to set
	 */
	public void setTimes(double[][] times) {
		this.times = times;
	}

	@Override
	public void addCost(Visita a, Visita b, Double cost) {
		this.getiCostTypeAccess().addCost(a, b, cost);
	}

	@Override
	public Double getMatrizDistancia(Visita a, Visita b) {
		return this.getiCostTypeAccess().getCost(a, b);
	}

	/**
	 * @return the speed in KM/H
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed in KM/H to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * @return the iCostTypeAccess
	 */
	public ICostTypeAccess getiCostTypeAccess() {
		return iCostTypeAccess;
	}

	/**
	 * @param iCostTypeAccess the iCostTypeAccess to set
	 */
	public void setiCostTypeAccess(ICostTypeAccess iCostTypeAccess) {
		this.iCostTypeAccess = iCostTypeAccess;
	}

	/**
	 * @param enumCostTypeCostMatrix the enumCostTypeCostMatrix to set
	 */
	@Override
	public void setEnumCostTypeCostMatrix(EnumCostTypeCostMatrix enumCostTypeCostMatrix) {
		if (enumCostTypeCostMatrix != null) {
			if (enumCostTypeCostMatrix.isTime())
				this.setiCostTypeAccess(new TimeCostTypeAccess(this));
			super.setEnumCostTypeCostMatrix(enumCostTypeCostMatrix);
		}
	}
}
