package utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RouteRandomCentroMassa extends RouteRandomDinamico {

	double pontoCMX;
	double pontoCMY;
	//TODO BANCO DE DADOS SERÁ O MONGODB
	PreparedStatement psRetirada, psInsercao;
	Statement stmtRetirada;
	String queryRetirada, queryInsercao, queryEventos;
	double tempoParaDiferenciarTarefas = 0.00000000001;

	public RouteRandomCentroMassa(NetworkRandom net, int fxV, int fxH, double[] pontosX, double[] pontosY)
			throws SQLException {
		super(net, fxV, fxH, pontosX, pontosY);

		// Preparação para criação de pontos
		queryInsercao = "INSERT INTO visitas(cods, codc, zonai, zonaj, ptx, pty)" + "VALUES (?, ?, ?, ?, ?, ?);";

		// Preparação para retirada do roteiro
		// queryRetirada = "UPDATE visitas SET tpremovido=? WHERE cods=? and codc=? and
		// zonai=? and zonaj=? and ptx=? and pty=?";
		// Preparação para retirada do roteiro
		queryRetirada = "delete from visitas " + "WHERE cods=? and codc=? and zonai=? and zonaj=? and ptx=? and pty=?";

		queryEventos = "INSERT INTO eventos(cods, codc, zonai, zonaj, ptx, pty, instante, evento) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

	}

	public void setPontoCM(double vlX, double vlY) {
		this.pontoCMX = vlX;
		this.pontoCMY = vlY;
	}

	@Override
	public List<Integer> identificaTarefasParaTransferir() {

		List<Integer> tarefasParaTransferir = new ArrayList<Integer>();
		List<Integer> rotaEstimada = new ArrayList<Integer>();
		if ((visitaAtual + 1) < (rota.size() - 2)) {
			rotaEstimada = new ArrayList<Integer>(rota.subList(visitaAtual, rota.size()));
		}

		if (rotaEstimada.size() > 3) {
			// Calcula distancias ao centro de massa
			HashMap<Integer, Double> distCM = calcDistanciaAoCM(rota.subList(visitaAtual + 2, rota.size() - 1));
			System.out.println("Rota original: " + rota.toString());
			System.out.println("Visita atual: " + Integer.toString(visitaAtual));
			System.out.println(distCM);
			distCM = Utilidades.sortHashMapByValues(distCM);

			// Verificar o valor esperado dos próximos tempos
			double tp = tempoGasto + estimaTempoRestanteRota(rotaEstimada);
			while ((tp > tempoDisponivel) && (distCM.size() > 0)) {
				System.out.println(distCM);
				Integer ptMaiProxCM = (Integer) distCM.keySet().toArray()[0];
				System.out.println(ptMaiProxCM);
				tarefasParaTransferir.add(ptMaiProxCM);
				// remove ponto mais próximo do centro de massa
				distCM.remove(ptMaiProxCM);
				rotaEstimada.remove(ptMaiProxCM);
				tp = tempoGasto + estimaTempoRestanteRota(rotaEstimada);
			}
		}
		return tarefasParaTransferir;
	}

	@Override
	public void executaTransferencia(List<Integer> tarefasParaTransferir) throws SQLException {

		for (Integer tarefa : tarefasParaTransferir) {
			// Adiciona na rota auxiliar
			inserirNaRota(pontoCartesianoSelecionadosX[tarefa], pontoCartesianoSelecionadosY[tarefa]);
			// Remove da rota regular
			removerDaRota(tarefa);
		}
	}

	public HashMap<Integer, Double> calcDistanciaAoCM(List<Integer> tarefasNaoRealizadas) {
		HashMap<Integer, Double> distCM = new HashMap<Integer, Double>();
		for (Integer t : tarefasNaoRealizadas) {
			distCM.put(t, Utilidades.calculaDistancia2Pontos(pontoCartesianoSelecionadosX[t], pontoCMX,
					pontoCartesianoSelecionadosY[t], pontoCMY));
		}
		return distCM;
	}

	public void removerDaRota(Integer tarefa) throws SQLException {
		psRetirada = con.prepareStatement(queryRetirada);
		psRetirada.setInt(1, codSimulacao);
		psRetirada.setInt(2, codCiclo);
		psRetirada.setInt(3, faixaV);
		psRetirada.setInt(4, faixaH);
		psRetirada.setDouble(5, pontoCartesianoSelecionadosX[tarefa]);
		psRetirada.setDouble(6, pontoCartesianoSelecionadosY[tarefa]);
		psRetirada.execute();

		psRetirada = con.prepareStatement(queryEventos);
		psRetirada.setInt(1, codSimulacao);
		psRetirada.setInt(2, codCiclo);
		psRetirada.setInt(3, faixaV);
		psRetirada.setInt(4, faixaH);
		psRetirada.setDouble(5, pontoCartesianoSelecionadosX[tarefa]);
		psRetirada.setDouble(6, pontoCartesianoSelecionadosY[tarefa]);
		psRetirada.setDouble(7, tempoGasto);
		psRetirada.setString(8, "DELETE");
		psRetirada.addBatch();
		psRetirada.executeBatch();
	}

	@Override
	public void inserirNaRota(double ptX, double ptY) throws SQLException {
		// System.out.println("Tarefas tranferidas
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"):
		// "+Double.toString(pontoCartesianoSelecionadosX[tarefa]));
		netMain.getRotaAux().adicionaTarefa(ptX, ptY, tempoGasto + tempoParaDiferenciarTarefas);
		rota.remove(rota.indexOf(Utilidades.inArray(pontoCartesianoSelecionadosX, ptX)));
		tempoParaDiferenciarTarefas += 0.00000000001;
		// System.out.println(rota.toString());

		// rotina de inserção no banco de dados para informar a retirada da tarefa
		psInsercao = con.prepareStatement(queryInsercao);
		psInsercao.setInt(1, codSimulacao);
		psInsercao.setInt(2, codCiclo);
		psInsercao.setInt(3, faixaV);
		psInsercao.setInt(4, faixaH);
		psInsercao.setDouble(5, ptX);
		psInsercao.setDouble(6, ptY);
		psInsercao.execute();
	}

	@Override
	public void removerDaRota(Double ptX) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public double estimarInserirNaRota(double ptX, double ptY) {
		// TODO Auto-generated method stub
		return 0;
	}

}
