package optmizations;

import java.util.List;

import model.DistanceTimeCostMatrixDesorderCustomers;
import model.Visita;
import utils.MatrizDistancia;

public class Opt2 extends RouteOptimizer{
	
	public Opt2(MatrizDistancia distanceMatrix, Visita[] route) {
		super(distanceMatrix, route);
	}

	@Override
	public Visita[] optimize(Visita[] route) {
		int i1 = size - 3, i2 = size - 2;
		MatrizDistancia costMatrix = this.getCostMatrix();
		if (costMatrix.isDistanceTimeCostMatrixDesorderCustomers()) {
			DistanceTimeCostMatrixDesorderCustomers distanceTimeCostMatrixDesorderCustomers = (DistanceTimeCostMatrixDesorderCustomers) costMatrix;
			for (int i = 1; i < i1; i++) {
				for (int j = 1; j < i2; j++) {
					if (i != j) {
						// System.out.println("Item "+i+"."+j+" of 2-OPT");
						Visita[] currentRoute = this.exchange2Opt(i, j, route);
						if (distanceTimeCostMatrixDesorderCustomers.getTimeCostAmong(
								currentRoute) < distanceTimeCostMatrixDesorderCustomers.getTimeCostAmong(route)) {
							route = currentRoute;
						}

					}
				}
			}
		} else {
			for (int i = 1; i < i1; i++) {
				for (int j = 1; j < i2; j++) {
					if (i != j) {
						// System.out.println("Item "+i+"."+j+" of 2-OPT");
						Visita[] currentRoute = this.exchange2Opt(i, j, route);
						if (costMatrix.getCostAmong(currentRoute) < this.getCostMatrix().getCostAmong(route)) {
							route = currentRoute;
						}

					}
				}
			}
		}
		return route;
	}

	public Visita[] exchange2Opt(int a, int b, Visita[] optimizedRoute) {
		Visita[] route = optimizedRoute.clone();
		super.exchange(a, b, route);
		// inverter subroteiro
		if ((b - a) > 2) {
			int begin, end;
			if ((a + 1) < b) {
				begin = a + 1;
				end = b;
			} else {
				begin = b;
				end = a + 1;
			}
			int limit = (end - begin) / 2;
			for (int i = 0; i < limit; exchange(begin + i, end - i++ - 1, route))
				;
		}
		return route;
	}

}
