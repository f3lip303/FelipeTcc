package utils;

import java.util.List;

import model.Visita;

public class DistanceCalculator {
	public static double distance(Visita a, Visita b) {
		return Math.sqrt(Math.pow(calculateHorizontalLine(a, b), 2) + Math.pow(calculateVerticalLine(a, b), 2));
	}

	public static double distance(List<Visita> customers) {
		int customersSize = customers.size(), i1 = customersSize - 1;
		double distance = 0;
		for (int i = 0; i < i1; i++)
			for (int j = i + 1; j < customersSize; j++)
				distance += distance(customers.get(i), customers.get(j));
		return distance;
	}

	public static double calculateHorizontalLine(Visita a, Visita b) {
		double ax = a.getX(), bx = b.getX();
		return ax > bx ? ax - bx : bx - ax;
	}

	public static double calculateVerticalLine(Visita a, Visita b) {
		double ay = a.getY(), by = b.getY();
		return ay > by ? ay - by : by - ay;
	}

}
