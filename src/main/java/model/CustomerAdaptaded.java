package model;

import models.Expedient;

public class CustomerAdaptaded implements Comparable<CustomerAdaptaded>{
	private int id;
	private String name;
	private double x, y,
					poundsDemand,
					downTime;
	private int visitsLimit;
	private double arrivalHour,
					exitHour;
	private double currentKilometer;
	private boolean isCD;
	private Expedient expedient;
	
	
	/**
	 * @param id
	 * @param name
	 * @param x
	 * @param y
	 * @param poundsDemand
	 * @param downTime
	 * @param visitsLimit
	 * @param arrivalHour
	 * @param exitHour
	 * @param currentKilometer
	 */
	public CustomerAdaptaded(int id, String name, double x, double y, double poundsDemand, double downTime,
			int visitsLimit, double arrivalHour, double exitHour, double currentKilometer) {
		super();
		this.id = id;
		this.name = name;
		this.x = x;
		this.y = y;
		this.poundsDemand = poundsDemand;
		this.downTime = downTime;
		this.visitsLimit = visitsLimit;
		this.arrivalHour = arrivalHour;
		this.exitHour = exitHour;
		this.currentKilometer = currentKilometer;
	}
	/**
	 * @param id
	 * @param name
	 * @param x
	 * @param y
	 */
	public CustomerAdaptaded(int id, String name, double x, double y) {
		super();
		this.id = id;
		this.name = name;
		this.x = x;
		this.y = y;
	}	
	/**
	 * @param name
	 * @param x
	 * @param y
	 */
	public CustomerAdaptaded(String name, double x, double y) {
		super();
		this.name = name;
		this.x = x;
		this.y = y;
	}	
	/**
	 * @param id
	 * @param name
	 * @param x
	 */
	public CustomerAdaptaded(int id, String name, double x) {
		super();
		this.id = id;
		this.name = name;
		this.x = x;
	}
	/**
	 * @param id
	 * @param x
	 * @param y
	 */
	public CustomerAdaptaded(int id, double x, double y) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
	}
	/**
	 * @param id
	 * @param name
	 */
	public CustomerAdaptaded(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}	
	/**
	 * @param x
	 * @param y
	 */
	public CustomerAdaptaded(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	/**
	 * @param name
	 */
	public CustomerAdaptaded(String name) {
		super();
		this.name = name;
	}
	/**
	 * @param id
	 */
	public CustomerAdaptaded(int id) {
		super();
		this.id = id;
	}
	/**
	 * 
	 */
	public CustomerAdaptaded() {
		super();
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}	
	/**
	 * @param y the y to set
	 * @param x the x to set
	 */
	public void setXY(double x, double y) {
		this.y = y;
		this.x = x;
	}
	
	public String getXY() {
		return "("+x+", "+y+")";
	}
	
	@Override
	public String toString(){		
		return (this.isCD() ? "CD:\n" : "")+"ID: "+id+"\n"+
				"Name: "+name+"\n"+
				"X: "+x+" Y: "+y+"\n"+
				"Pounds demand: "+this.getPoundsDemand()+"\n"+
				"Down time: "+this.getDownTime()+"\n"+
				"Arrival hour: "+this.getArrivalHour()+"\n"+
				"Exit hour: "+this.getExitHour()+"\n"+
				"Current kilometer: "+this.getCurrentKilometer();
	}
	
	/**
	 * @return the poundsByKilometer
	 */
	public double getPoundsDemand() {
		return poundsDemand;
	}
	/**
	 * @param poundsByKilometer the poundsByKilometer to set
	 */
	public void setPoundsDemand(double poundsDemand) {
		this.poundsDemand = poundsDemand;
	}
	/**
	 * @return the downTime
	 */
	public double getDownTime() {
		return downTime;
	}
	/**
	 * @param downTime the downTime to set
	 */
	public void setDownTime(double downTime) {
		this.downTime = downTime;
	}
	/**
	 * @return the visitsLimit
	 */
	public int getVisitsLimit() {
		return visitsLimit;
	}
	/**
	 * @param visitsLimit the visitsLimit to set
	 */
	public void setVisitsLimit(int visitsLimit) {
		this.visitsLimit = visitsLimit;
	}
	
	@Override
	public Object clone(){
		CustomerAdaptaded newCustomer = new CustomerAdaptaded(this.getId(), this.getName(), this.getX(), this.getY(), this.getPoundsDemand(), this.getDownTime(), this.getVisitsLimit(), this.getArrivalHour(), this.getExitHour(), this.getCurrentKilometer());
		newCustomer.setExpedient(this.getExpedient());
		return newCustomer;		
	}
	
	public int compareTo(CustomerAdaptaded arg0) {		
		int id = arg0.getId();
		if (this.id < id)
			return -1;
		return this.id > id ? 1 : 0;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;		
		if (obj == null || getClass() != obj.getClass())
			return false;						
		return this.compareTo((CustomerAdaptaded) obj) == 0;
	}
	/**
	 * @return the arrivalHour
	 */
	public double getArrivalHour() {
		return arrivalHour;
	}
	/**
	 * @param arrivalHour the arrivalHour to set
	 */
	public void setArrivalHour(double arrivalHour) {
		this.arrivalHour = arrivalHour;
	}
	/**
	 * @return the exitHour
	 */
	public double getExitHour() {
		return exitHour;
	}
	/**
	 * @param exitHour the exitHour to set
	 */
	public void setExitHour(double exitHour) {
		this.exitHour = exitHour;
	}
	/**
	 * @return the currentKilometer
	 */
	public double getCurrentKilometer() {
		return currentKilometer;
	}
	/**
	 * @param currentKilometer the currentKilometer to set
	 */
	public void setCurrentKilometer(double currentKilometer) {
		this.currentKilometer = currentKilometer;
	}
	
	public boolean isCD() {
		return isCD;
	}
	
	public void setCD(boolean isCD) {
		this.isCD = isCD;
	}
	/**
	 * @return the expedient
	 */
	public Expedient getExpedient() {
		return expedient;
	}
	/**
	 * @param expedient the expedient to set
	 */
	public void setExpedient(Expedient expedient) {
		this.expedient = expedient;
	}				
	
}
