package agents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import utils.Utilidades;

public class Simulador extends Agent{

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	private Long timeFactor = (long) 1000; // para cada minuto na simulacao, dar delay de 1000 milisegundos (1 segundo)
	private Integer qtdRodadas;
	private String tipoSimulacao;

	protected void setup() {

		Object[] args = getArguments(); // leitura de parametros
		tipoSimulacao = args[0].toString();
		qtdRodadas = Integer.parseInt(args[1].toString());

		// Registro do agente simulador no DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Simulador");
		sd.setName(getLocalName() + "-Simulador");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Gravação na tabela de simulação
		try {
			//TODO DATA SOURCE DO MONGO
			DataSource dataSource = Utilidades.setupDataSource();
			Connection con = dataSource.getConnection();
			String query = "INSERT INTO simulacao (qtciclos,tipo) VALUES (?,?)";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setInt(1, qtdRodadas);
			ps.setString(2, tipoSimulacao);
			ps.addBatch();
			ps.executeBatch();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Agent " + getAID().getName() + " INICIADO.");
		System.out.println("Simulation '" + tipoSimulacao + "' started");
		if (tipoSimulacao.equals("estatica")) {
			addBehaviour(new IniciarAgentesRegulares(0));
		} else if (tipoSimulacao.equals("dinamica_agentes_dist")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesRegulares(0));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_equil")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesRegulares(0));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesRegulares(0));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_1")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_1")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(1));
			sb.addSubBehaviour(new IniciarAgentesRegulares(1));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_2")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_2")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(2));
			sb.addSubBehaviour(new IniciarAgentesRegulares(2));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_3")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_3")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(3));
			sb.addSubBehaviour(new IniciarAgentesRegulares(3));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_4")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_4")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(4));
			sb.addSubBehaviour(new IniciarAgentesRegulares(4));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_5")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_5")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(5));
			sb.addSubBehaviour(new IniciarAgentesRegulares(5));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_6")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_6")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(6));
			sb.addSubBehaviour(new IniciarAgentesRegulares(6));
			addBehaviour(sb);
		} else if (tipoSimulacao.equals("dinamica_agentes_ult_aux_7")
				|| tipoSimulacao.equals("dinamica_agentes_equil_aux_7")) {
			SequentialBehaviour sb = new SequentialBehaviour();
			sb.addSubBehaviour(new IniciarAgenteSync());
			sb.addSubBehaviour(new IniciarAgentesAuxiliares(7));
			sb.addSubBehaviour(new IniciarAgentesRegulares(7));
			addBehaviour(sb);
		}
	}

	protected void takeDown() {
		// Exit from DF
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Agent " + getAID().getName() + " killed.");
	}

	public class IniciarAgenteSync extends OneShotBehaviour {

		public void action() {

			AgentContainer aContainer = getContainerController();
			AgentController aController;
			try {
				aController = aContainer.createNewAgent("SyncAgent", "logdyn.agents.SyncAgent", null);
				aController.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public class IniciarAgentesRegulares extends OneShotBehaviour {

		int nrVeiculosAux;

		public IniciarAgentesRegulares(int v) {
			this.nrVeiculosAux = v;
		}

		public void action() {

			Object[] argumentos = new Object[5];
			argumentos[0] = tipoSimulacao;
			argumentos[1] = qtdRodadas;
			argumentos[2] = 2;
			argumentos[3] = 2;
			argumentos[4] = nrVeiculosAux;

			Object[] argumentos00 = new Object[3];
			Object[] argumentos01 = new Object[3];
			Object[] argumentos02 = new Object[3];
			Object[] argumentos10 = new Object[3];
			Object[] argumentos11 = new Object[3];
			Object[] argumentos12 = new Object[3];
			Object[] argumentos20 = new Object[3];
			Object[] argumentos21 = new Object[3];
			Object[] argumentos22 = new Object[3];
			argumentos00[0] = tipoSimulacao;
			argumentos01[0] = tipoSimulacao;
			argumentos02[0] = tipoSimulacao;
			argumentos10[0] = tipoSimulacao;
			argumentos11[0] = tipoSimulacao;
			argumentos12[0] = tipoSimulacao;
			argumentos20[0] = tipoSimulacao;
			argumentos21[0] = tipoSimulacao;
			argumentos22[0] = tipoSimulacao;

			AgentContainer aContainer = getContainerController();
			AgentController aController;
			try {
				argumentos00[1] = 0;
				argumentos00[2] = 0;
				aController = aContainer.createNewAgent("VeiculoRegular(00)", "logdyn.agents.VeiculoRegular",
						argumentos00);
				aController.start();
				argumentos01[1] = 0;
				argumentos01[2] = 1;
				aController = aContainer.createNewAgent("VeiculoRegular(01)", "logdyn.agents.VeiculoRegular",
						argumentos01);
				aController.start();
				argumentos02[1] = 0;
				argumentos02[2] = 2;
				aController = aContainer.createNewAgent("VeiculoRegular(02)", "logdyn.agents.VeiculoRegular",
						argumentos02);
				aController.start();
				argumentos10[1] = 1;
				argumentos10[2] = 0;
				aController = aContainer.createNewAgent("VeiculoRegular(10)", "logdyn.agents.VeiculoRegular",
						argumentos10);
				aController.start();
				argumentos11[1] = 1;
				argumentos11[2] = 1;
				aController = aContainer.createNewAgent("VeiculoRegular(11)", "logdyn.agents.VeiculoRegular",
						argumentos11);
				aController.start();
				argumentos12[1] = 1;
				argumentos12[2] = 2;
				aController = aContainer.createNewAgent("VeiculoRegular(12)", "logdyn.agents.VeiculoRegular",
						argumentos12);
				aController.start();
				argumentos20[1] = 2;
				argumentos20[2] = 0;
				aController = aContainer.createNewAgent("VeiculoRegular(20)", "logdyn.agents.VeiculoRegular",
						argumentos20);
				aController.start();
				argumentos21[1] = 2;
				argumentos21[2] = 1;
				aController = aContainer.createNewAgent("VeiculoRegular(21)", "logdyn.agents.VeiculoRegular",
						argumentos21);
				aController.start();
				argumentos22[1] = 2;
				argumentos22[2] = 2;
				aController = aContainer.createNewAgent("VeiculoRegular(22)", "logdyn.agents.VeiculoRegular",
						argumentos22);
				aController.start();
				aController = aContainer.createNewAgent("TransportadorRegular", "logdyn.agents.TransportadorRegular",
						argumentos);
				aController.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	public class IniciarAgentesAuxiliares extends OneShotBehaviour {

		int nrVeiculosAux;

		public IniciarAgentesAuxiliares(int v) {
			this.nrVeiculosAux = v;
		}

		public void action() {

			// agente transportador
			Object[] argumentos = new Object[3];
			argumentos[0] = tipoSimulacao;
			argumentos[1] = qtdRodadas;
			argumentos[2] = nrVeiculosAux;
			AgentContainer aContainer = getContainerController();
			AgentController aController;
			try {
				aController = aContainer.createNewAgent("TransportadorAuxiliar", "logdyn.agents.TransportadorAuxiliar",
						argumentos);
				aController.start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}

			// agentes veiculos
			try {
				if (nrVeiculosAux >= 1) {
					Object[] argumentos99999_0 = new Object[3];
					argumentos99999_0[0] = tipoSimulacao;
					argumentos99999_0[1] = 99999;
					argumentos99999_0[2] = 0;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_0)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_0);
					aController.start();
				}
				if (nrVeiculosAux >= 2) {
					Object[] argumentos99999_1 = new Object[3];
					argumentos99999_1[0] = tipoSimulacao;
					argumentos99999_1[1] = 99999;
					argumentos99999_1[2] = 1;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_1)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_1);
					aController.start();
				}
				if (nrVeiculosAux >= 3) {
					Object[] argumentos99999_2 = new Object[3];
					argumentos99999_2[0] = tipoSimulacao;
					argumentos99999_2[1] = 99999;
					argumentos99999_2[2] = 2;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_2)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_2);
					aController.start();
				}
				if (nrVeiculosAux >= 4) {
					Object[] argumentos99999_3 = new Object[3];
					argumentos99999_3[0] = tipoSimulacao;
					argumentos99999_3[1] = 99999;
					argumentos99999_3[2] = 3;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_3)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_3);
					aController.start();
				}
				if (nrVeiculosAux >= 5) {
					Object[] argumentos99999_4 = new Object[3];
					argumentos99999_4[0] = tipoSimulacao;
					argumentos99999_4[1] = 99999;
					argumentos99999_4[2] = 4;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_4)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_4);
					aController.start();
				}
				if (nrVeiculosAux >= 6) {
					Object[] argumentos99999_5 = new Object[3];
					argumentos99999_5[0] = tipoSimulacao;
					argumentos99999_5[1] = 99999;
					argumentos99999_5[2] = 5;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_5)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_5);
					aController.start();
				}
				if (nrVeiculosAux >= 7) {
					Object[] argumentos99999_6 = new Object[3];
					argumentos99999_6[0] = tipoSimulacao;
					argumentos99999_6[1] = 99999;
					argumentos99999_6[2] = 6;
					aController = aContainer.createNewAgent("VeiculoAuxiliar(99999_6)", "logdyn.agents.VeiculoAuxiliar",
							argumentos99999_6);
					aController.start();
				}
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}
}
