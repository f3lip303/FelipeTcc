package utils;

import java.util.ArrayList;
import java.util.List;

import model.Ponto;


public class Route implements Comparable<Route>{

	private int day;
	private int visits;
	List<Ponto> pointsToVisit = new ArrayList<Ponto>();
	private double totalTime = 0.0;
	
	public Route(int day){
		this.setDay(day);
	}
	
	public Route(int day, int visits) {
		this.setDay(day);
		this.visits = visits;
	}
	
	public void add(Ponto newPt){
		pointsToVisit.add(newPt);
	}
	
	public List<Ponto> getPointsToVisit() {
		return pointsToVisit;
	}

	public void setPointsToVisit(List<Ponto> pointsToVisit) {
		this.pointsToVisit = pointsToVisit;
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String toString(){
		return pointsToVisit.toString();
	}
	
	public double[] getPontoCartesianoSelecionadosY(){
		double[] aux = new double[pointsToVisit.size()];
		int i=0;
		for(Ponto p : pointsToVisit) {
			aux[i++] = p.getY();
		}
		return aux;
	}
	
	public double[] getPontoCartesianoSelecionadosX(){
		double[] aux = new double[pointsToVisit.size()];
		int i=0;
		for(Ponto p : pointsToVisit) {
			aux[i++] = p.getX();
		}
		return aux;
	}
	

	public int compareTo(Route arg0) {
		if (this.pointsToVisit.size() > arg0.pointsToVisit.size()) return -1;
		else if (this.pointsToVisit.size() < arg0.pointsToVisit.size()) return 1;
		else if (this.totalTime > arg0.totalTime) return -1;
		else if (this.totalTime < arg0.totalTime) return 1;
		else return 0;
	}
}
