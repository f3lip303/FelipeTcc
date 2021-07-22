package model;

import java.util.List;

import utils.DistanceCalculator;


public class CostMatrix {

	protected double[][] costs;
	protected CustomerAdaptaded[] customers;
	protected int size;
	private EnumCostTypeCostMatrix enumCostTypeCostMatrix;
	
	public CostMatrix(CustomerAdaptaded[] customers) {
		super();
		this.customers = customers;
		int qttCustomers = customers.length;		
		costs = new double[qttCustomers][qttCustomers];									
	}				
	
	private CustomerAdaptaded getCustomer(int id){
		int size = customers.length;
		for (int i = 0; i < size; i++) {
			CustomerAdaptaded c = customers[i];
			if (c.getId() == id)
				return c;
		}
		return null;
	}		
	
	public boolean isDistanceTimeCostMatrixDesorderCustomers(){
		return this instanceof DistanceTimeCostMatrixDesorderCustomers;
	}
	
	public void addCost(CustomerAdaptaded a, CustomerAdaptaded b, Double distance) {
		int ia = a.getId() - 1, 
			ib = b.getId() - 1;
		costs[ia][ib] = distance;
		costs[ib][ia] = distance;
		size++;
	}
	
	public void addPoints(CustomerAdaptaded a, CustomerAdaptaded b) {		
		addCost(a, b, DistanceCalculator.distance(a, b));		
	}
	
	public Double getCost(CustomerAdaptaded a, CustomerAdaptaded b){
		if (a == b)
			return 0d;			
		return costs[a.getId() - 1][b.getId() - 1];
	}
	
	public Double getCost(Rota route){
		if (route != null){
			int totalCustomers = route.getTotalCustomers();
			if (totalCustomers > 0)				
				return this.getCost(route.getCustomers());
			return 0d;
		}else
			throw new NullPointerException("Erro: The route is null.");
		
	}
	
	public Double getCost(int indexA, int indexB){
		if (indexA == indexB)
			return 0d;			
		return costs[indexA][indexB];
	}
	
	public Double getCostAmong(CustomerAdaptaded... customers){
		double totalCost = 0d;
		int size = customers.length - 1;
		for (int i = 0; i < size; i++){
			if (customers[i] == null || customers[i + 1] == null){
				break;
			}
			totalCost += getCost(customers[i], customers[i + 1]);
		}		
		return totalCost;
	}
	
	public Double getCost(Rota[] routes){
		if (routes == null)
			throw new NullPointerException("Routes array is null.");
		double totalCost = 0d;		
		for (int i = routes.length - 1; i >= 0; totalCost += routes[i--].getCost());		
		return totalCost;
	}
	
	public Double getCost(List<CustomerAdaptaded> customers){
		CustomerAdaptaded[] auxCustomers = new CustomerAdaptaded[customers.size()];
		auxCustomers = customers.toArray(auxCustomers);
		return getCostAmong(auxCustomers);
	}
	
	@Override
	public String toString(){
		String info = "\tDistances\nCustomers Distance\n";
		int lines_size = costs.length;
		for (int i = 0; i < lines_size; i++) {
			CustomerAdaptaded a = customers[i], 
					b;
			for (int j = i + 1; j < lines_size; info += a.getId()+" C"+(b = customers[j++]).getId()+" "+getCost (a, b)+"\n");				
		}
		return info;		
	}				
	
	public boolean isEmpty(){
		return size == 0; 
	}

	public double getCostAmong(CustomerAdaptaded start, CustomerAdaptaded[] customers, CustomerAdaptaded end) {		
		return this.getCost(start, customers[0]) + this.getCostAmong(customers) + this.getCost(customers[customers.length - 1], start);
	}

	/**
	 * @return the costs
	 */
	public double[][] getCosts() {
		return costs;
	}

	/**
	 * @param costs the costs to set
	 */
	public void setCosts(double[][] costs) {
		this.costs = costs;
	}

	/**
	 * @return the customers
	 */
	public CustomerAdaptaded[] getCustomers() {
		return customers;
	}

	/**
	 * @param customers the customers to set
	 */
	public void setCustomers(CustomerAdaptaded[] customers) {
		this.customers = customers;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the enumCostTypeCostMatrix
	 */
	public EnumCostTypeCostMatrix getEnumCostTypeCostMatrix() {
		return enumCostTypeCostMatrix;
	}

	/**
	 * @param enumCostTypeCostMatrix the enumCostTypeCostMatrix to set
	 */
	public void setEnumCostTypeCostMatrix(EnumCostTypeCostMatrix enumCostTypeCostMatrix) {	
		this.enumCostTypeCostMatrix = enumCostTypeCostMatrix;
	}			
	
}
