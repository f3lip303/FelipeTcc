package optmizations;

import model.Visita;
import utils.MatrizDistancia;

public abstract class RouteOptimizer extends Optimizer {
	protected Visita[] optimizedRoute, routeToOptimize;

	public RouteOptimizer(MatrizDistancia costMatrix, Visita[] route) {
		super.setCostMatrix(costMatrix);
		super.setSize(route.length);
		this.setRouteToOptimize(route.clone());
	}

	public Visita[] optimize() {
		Visita[] routeToOptimize = this.getRouteToOptimize();
		MatrizDistancia costMatrix = this.getCostMatrix();
		if (routeToOptimize != null)
			if (costMatrix != null) {
				this.setOptimizedRoute(this.optimize(routeToOptimize.clone()));
				return optimizedRoute;
			} else
				throw new RuntimeException("Erro: The cost matrix was not defined. There's no cost matrix to use.");
		throw new RuntimeException("Erro: The route was not defined. There's no route to optimize.");

	}

	protected abstract Visita[] optimize(Visita[] route);

	public Visita[] getRouteToOptimize() {
		return routeToOptimize;
	}

	public void setRouteToOptimize(Visita[] routeToOptimize) {
		this.routeToOptimize = routeToOptimize;
	}

	public Visita[] getOptimizedRoute() {
		return optimizedRoute;
	}

	/**
	 * @param optimizedRoute the optimizedRoute to set
	 */
	public void setOptimizedRoute(Visita[] optimizedRoute) {
		this.optimizedRoute = optimizedRoute;
	}
}