package utils;

import java.sql.SQLException;

public abstract class RouteRandomDinamico extends RouteRandom {

	public RouteRandomDinamico(NetworkRandom net, int fxV, int fxH, double[] pontosX, double[] pontosY)
			throws SQLException {
		super(net, true, fxV, fxH, pontosX, pontosY);
	}

	/**
	 * Calcula valores relativos aos tempos de ciclo das visitas restantes
	 * (realização da visita e retorno ao depósito)
	 * 
	 * @param i                : visita atual
	 * @param tempoServico
	 * @param tempoAtendimento : tempo de atendimento na visita atual
	 * @return : [0] Tempo de deslocamento para a próxima visita [3] Tempo de
	 *         atendimento + tempo de deslocamento + tempo de atendimento no destino
	 *         [1] Tempo de retorno ao depósito [2] Tempo de atendimento + tempo de
	 *         deslocamento + tempo de atendimento no destino + Tempo de retorno ao
	 *         depósito
	 * 
	 */
	public double[] calculaTemposDeCiclo() {
		double[] ciclos = new double[pontoCartesianoSelecionadosX.length - visitaAtual - 2];
		int aux = 0;
		double tempoGastoSimulado = tempoGasto;
		double atendimentoOrigem = distTempoServico.sample();
		double deslocamento = calculaTempoPercurso(
				matrizDistanciaCorrigida[rota.get(visitaAtual + aux + 1)][rota.get(visitaAtual + aux + 2)]);
		double atendimentoDestino = distTempoServico.sample();
		double retornoDeposito = calculaTempoPercurso(matrizDistanciaCorrigida[rota.get(visitaAtual + aux + 2)][0]);
		ciclos[aux] = tempoGastoSimulado + atendimentoOrigem + deslocamento + atendimentoDestino + retornoDeposito;
		aux++;

		while ((visitaAtual + aux) < (pontoCartesianoSelecionadosX.length - 2)) {
			ciclos[aux] = ciclos[aux - 1] - atendimentoDestino - retornoDeposito;
			atendimentoOrigem = distTempoServico.sample();
			deslocamento = calculaTempoPercurso(
					matrizDistanciaCorrigida[rota.get(visitaAtual + aux + 1)][rota.get(visitaAtual + aux + 2)]);
			atendimentoDestino = distTempoServico.sample();
			retornoDeposito = calculaTempoPercurso(matrizDistanciaCorrigida[rota.get(visitaAtual + aux + 2)][0]);
			ciclos[aux] += atendimentoOrigem + deslocamento + atendimentoDestino + retornoDeposito;
			aux++;
		}
		return ciclos;
	}
}
