package optmizations;

import model.CostMatrix;
import model.CustomerAdaptaded;
import model.Visita;
import utils.MatrizDistancia;

public abstract class RouteOptimizer extends Optimizer {
	protected CustomerAdaptaded[] optimizedRoute, routeToOptimize;

	public RouteOptimizer(CostMatrix costMatrix, CustomerAdaptaded[] route) {
		super.setCostMatrix(costMatrix);
		super.setSize(route.length);
		this.setRouteToOptimize(route.clone());
	}

	public CustomerAdaptaded[] optimize() {
		CustomerAdaptaded[] routeToOptimize = this.getRouteToOptimize();
		CostMatrix costMatrix = this.getCostMatrix();
		if (routeToOptimize != null)
			if (costMatrix != null) {
				this.setOptimizedRoute(this.optimize(routeToOptimize.clone()));
				return optimizedRoute;
			} else
				throw new RuntimeException("Erro: The cost matrix was not defined. There's no cost matrix to use.");
		throw new RuntimeException("Erro: The route was not defined. There's no route to optimize.");

	}

	protected abstract CustomerAdaptaded[] optimize(CustomerAdaptaded[] route);

	public CustomerAdaptaded[] getRouteToOptimize() {
		return routeToOptimize;
	}

	public void setRouteToOptimize(CustomerAdaptaded[] routeToOptimize) {
		this.routeToOptimize = routeToOptimize;
	}

	public CustomerAdaptaded[] getOptimizedRoute() {
		return optimizedRoute;
	}

	/**
	 * @param optimizedRoute the optimizedRoute to set
	 */
	public void setOptimizedRoute(CustomerAdaptaded[] optimizedRoute) {
		this.optimizedRoute = optimizedRoute;
	}
}