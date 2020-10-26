package utils;

import java.sql.SQLException;
import java.util.List;

public class RouteRandomEstatico extends RouteRandom {

	public RouteRandomEstatico(NetworkRandom net, int fxV, int fxH, double[] pontosX, double[] pontosY)
			throws SQLException {
		super(net, true, fxV, fxH, pontosX, pontosY);
	}

	@Override
	public List<Integer> identificaTarefasParaTransferir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executaTransferencia(List<Integer> l) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removerDaRota(Double ptX) {
		// TODO Auto-generated method stub

	}

	@Override
	public void inserirNaRota(double ptX, double ptY) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public double estimarInserirNaRota(double ptX, double ptY) {
		// TODO Auto-generated method stub
		return 0;
	}

}
