package utils;

import java.sql.SQLException;
import java.util.List;

public interface EstrategiaCooperacaoVeiculos {

	List<Integer> identificaTarefasParaTransferir();

	void executaTransferencia(List<Integer> l) throws SQLException;

	void removerDaRota(Double ptX) throws SQLException;

	void inserirNaRota(double ptX, double ptY) throws SQLException;

	double estimarInserirNaRota(double ptX, double ptY);
}
