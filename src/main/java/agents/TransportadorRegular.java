package agents;

import java.sql.SQLException;
import java.util.Arrays;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.NetworkRandom;
import utils.Utilidades;


public class TransportadorRegular extends Transportador{
	protected int nrVeiculosX;
	protected int nrVeiculosY;
	protected int nrVeiculosAux;
	protected int[][] matrizRotasRegularesConcluidas;
	protected NetworkRandom net;
	protected boolean existemRotasRegularesPendentes;

	protected void setup() {

		Object[] args = getArguments(); // leitura de parametros
		tipoSimulacao = args[0].toString();
		qtdRodadas = (int) args[1];
		nrVeiculosX = (int) args[2];
		nrVeiculosY = (int) args[3];
		nrVeiculosAux = (int) args[4];
		matrizRotasRegularesConcluidas = new int[nrVeiculosX + 1][nrVeiculosY + 1];
		existemRotasAuxiliaresPendentes = true;
		if (nrVeiculosAux == 0)
			existemRotasAuxiliaresPendentes = false;

		// Registry of buyer on DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("TransportadorRegular");
		sd.setName(getLocalName() + "-TransportadorRegular");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
			System.out.println("Agent " + getAID().getName() + " INICIADO.");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Criar rede randomica
		try {
			net = new NetworkRandom(tipoSimulacao);
			// net.criarRotas();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Criar rotas para veiculos regulares dos 9 distritos
		// System.out.println("AGENT " + getAID().getName() + ") - Start cicle:
		// "+Integer.toString(codCiclo));

		// System.out.println("Inicio da simulacao " + tipoSimulacao);
		if (tipoSimulacao.equals("estatica")) {
			addBehaviour(new CriarRotas());
			addBehaviour(new ReceberRotasFinalizadas());
		} else {
			addBehaviour(new CriarRotas());
			addBehaviour(new ReceberRotasFinalizadas());
			addBehaviour(new ReceberTarefaParaTransferir());
			addBehaviour(new AguardarAutorizacao());
		}

	}

	public class CriarRotas extends OneShotBehaviour {

		public void action() {

			// Descobrindo número de veículos auxiliares em cada ciclo

			/*
			 * int nrveiculos_aux = 0; try { DataSource dataSource =
			 * Utilidades.setupDataSource(); Connection con; con =
			 * dataSource.getConnection(); String query =
			 * "select count(zonaj) from rotas where cods="+Integer.toString(net.
			 * getCodSimulacao())+" and codc="+Integer.toString(codCiclo)+" and zonai=99999"
			 * ; //System.out.println(query); Statement stmt = con.createStatement();
			 * ResultSet rs = stmt.executeQuery(query); while(rs.next()){ nrveiculos_aux =
			 * rs.getInt(1); } rs.close(); } catch (SQLException e1) { // TODO
			 * Auto-generated catch block e1.printStackTrace(); }
			 * 
			 * if ((nrveiculos_aux != nrVeiculosAux)&&(nrveiculos_aux>0)){
			 * //myAgent.doDelete(); }
			 */

			myAgent.doWait(1000);

			codCiclo++;
			System.out.println("AGENT (" + getAID().getName() + ") - Start cicle: " + Integer.toString(codCiclo)
					+ " - Simulation: " + Integer.toString(net.getCodSimulacao()));

			try {
				net.criarRotas();

				// Comunicar ao transportador auxiliar o incício do ciclo
				ACLMessage msgAux = new ACLMessage(ACLMessage.INFORM);
				msgAux.setLanguage("AUX");
				msgAux.setProtocol("INICIO");
				msgAux.addReceiver(new AID("TransportadorAuxiliar", AID.ISLOCALNAME));
				msgAux.setContent(Integer.toString(codCiclo));
				send(msgAux);

				ACLMessage msg00x = new ACLMessage(ACLMessage.INFORM);
				msg00x.setLanguage("ROTAS");
				msg00x.addReceiver(new AID("VeiculoRegular(00)", AID.ISLOCALNAME));
				msg00x.setOntology("pontosx");
				msg00x.setContent(Arrays.toString((net.getRotas()[0][0]).getPontoCartesianoSelecionadosX()));
				send(msg00x);

				ACLMessage msg00y = new ACLMessage(ACLMessage.INFORM);
				msg00y.setLanguage("ROTAS");
				msg00y.addReceiver(new AID("VeiculoRegular(00)", AID.ISLOCALNAME));
				msg00y.setOntology("pontosy");
				msg00y.setContent(Arrays.toString(net.getRotas()[0][0].getPontoCartesianoSelecionadosY()));
				send(msg00y);

				ACLMessage msg01x = new ACLMessage(ACLMessage.INFORM);
				msg01x.setLanguage("ROTAS");
				msg01x.addReceiver(new AID("VeiculoRegular(01)", AID.ISLOCALNAME));
				msg01x.setOntology("pontosx");
				msg01x.setContent(Arrays.toString(net.getRotas()[0][1].getPontoCartesianoSelecionadosX()));
				send(msg01x);

				ACLMessage msg01y = new ACLMessage(ACLMessage.INFORM);
				msg01y.setLanguage("ROTAS");
				msg01y.addReceiver(new AID("VeiculoRegular(01)", AID.ISLOCALNAME));
				msg01y.setOntology("pontosy");
				msg01y.setContent(Arrays.toString(net.getRotas()[0][1].getPontoCartesianoSelecionadosY()));
				send(msg01y);

				ACLMessage msg02x = new ACLMessage(ACLMessage.INFORM);
				msg02x.setLanguage("ROTAS");
				msg02x.addReceiver(new AID("VeiculoRegular(02)", AID.ISLOCALNAME));
				msg02x.setOntology("pontosx");
				msg02x.setContent(Arrays.toString(net.getRotas()[0][2].getPontoCartesianoSelecionadosX()));
				send(msg02x);

				ACLMessage msg02y = new ACLMessage(ACLMessage.INFORM);
				msg02y.setLanguage("ROTAS");
				msg02y.addReceiver(new AID("VeiculoRegular(02)", AID.ISLOCALNAME));
				msg02y.setOntology("pontosy");
				msg02y.setContent(Arrays.toString(net.getRotas()[0][2].getPontoCartesianoSelecionadosY()));
				send(msg02y);

				ACLMessage msg10x = new ACLMessage(ACLMessage.INFORM);
				msg10x.setLanguage("ROTAS");
				msg10x.addReceiver(new AID("VeiculoRegular(10)", AID.ISLOCALNAME));
				msg10x.setOntology("pontosx");
				msg10x.setContent(Arrays.toString(net.getRotas()[1][0].getPontoCartesianoSelecionadosX()));
				send(msg10x);

				ACLMessage msg10y = new ACLMessage(ACLMessage.INFORM);
				msg10y.setLanguage("ROTAS");
				msg10y.addReceiver(new AID("VeiculoRegular(10)", AID.ISLOCALNAME));
				msg10y.setOntology("pontosy");
				msg10y.setContent(Arrays.toString(net.getRotas()[1][0].getPontoCartesianoSelecionadosY()));
				send(msg10y);

				ACLMessage msg11x = new ACLMessage(ACLMessage.INFORM);
				msg11x.setLanguage("ROTAS");
				msg11x.addReceiver(new AID("VeiculoRegular(11)", AID.ISLOCALNAME));
				msg11x.setOntology("pontosx");
				msg11x.setContent(Arrays.toString(net.getRotas()[1][1].getPontoCartesianoSelecionadosX()));
				send(msg11x);

				ACLMessage msg11y = new ACLMessage(ACLMessage.INFORM);
				msg11y.setLanguage("ROTAS");
				msg11y.addReceiver(new AID("VeiculoRegular(11)", AID.ISLOCALNAME));
				msg11y.setOntology("pontosy");
				msg11y.setContent(Arrays.toString(net.getRotas()[1][1].getPontoCartesianoSelecionadosY()));
				send(msg11y);

				ACLMessage msg12x = new ACLMessage(ACLMessage.INFORM);
				msg12x.setLanguage("ROTAS");
				msg12x.addReceiver(new AID("VeiculoRegular(12)", AID.ISLOCALNAME));
				msg12x.setOntology("pontosx");
				msg12x.setContent(Arrays.toString(net.getRotas()[1][2].getPontoCartesianoSelecionadosX()));
				send(msg12x);

				ACLMessage msg12y = new ACLMessage(ACLMessage.INFORM);
				msg12y.setLanguage("ROTAS");
				msg12y.addReceiver(new AID("VeiculoRegular(12)", AID.ISLOCALNAME));
				msg12y.setOntology("pontosy");
				msg12y.setContent(Arrays.toString(net.getRotas()[1][2].getPontoCartesianoSelecionadosY()));
				send(msg12y);

				ACLMessage msg20x = new ACLMessage(ACLMessage.INFORM);
				msg20x.setLanguage("ROTAS");
				msg20x.addReceiver(new AID("VeiculoRegular(20)", AID.ISLOCALNAME));
				msg20x.setOntology("pontosx");
				msg20x.setContent(Arrays.toString(net.getRotas()[2][0].getPontoCartesianoSelecionadosX()));
				send(msg20x);

				ACLMessage msg20y = new ACLMessage(ACLMessage.INFORM);
				msg20y.setLanguage("ROTAS");
				msg20y.addReceiver(new AID("VeiculoRegular(20)", AID.ISLOCALNAME));
				msg20y.setOntology("pontosy");
				msg20y.setContent(Arrays.toString(net.getRotas()[2][0].getPontoCartesianoSelecionadosY()));
				send(msg20y);

				ACLMessage msg21x = new ACLMessage(ACLMessage.INFORM);
				msg21x.setLanguage("ROTAS");
				msg21x.addReceiver(new AID("VeiculoRegular(21)", AID.ISLOCALNAME));
				msg21x.setOntology("pontosx");
				msg21x.setContent(Arrays.toString(net.getRotas()[2][1].getPontoCartesianoSelecionadosX()));
				send(msg21x);

				ACLMessage msg21y = new ACLMessage(ACLMessage.INFORM);
				msg21y.setLanguage("ROTAS");
				msg21y.addReceiver(new AID("VeiculoRegular(21)", AID.ISLOCALNAME));
				msg21y.setOntology("pontosy");
				msg21y.setContent(Arrays.toString(net.getRotas()[2][1].getPontoCartesianoSelecionadosY()));
				send(msg21y);

				ACLMessage msg22x = new ACLMessage(ACLMessage.INFORM);
				msg22x.setLanguage("ROTAS");
				msg22x.addReceiver(new AID("VeiculoRegular(22)", AID.ISLOCALNAME));
				msg22x.setOntology("pontosx");
				msg22x.setContent(Arrays.toString(net.getRotas()[2][2].getPontoCartesianoSelecionadosX()));
				send(msg22x);

				ACLMessage msg22y = new ACLMessage(ACLMessage.INFORM);
				msg22y.setLanguage("ROTAS");
				msg22y.addReceiver(new AID("VeiculoRegular(22)", AID.ISLOCALNAME));
				msg22y.setOntology("pontosy");
				msg22y.setContent(Arrays.toString(net.getRotas()[2][2].getPontoCartesianoSelecionadosY()));
				send(msg22y);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println("AGENT (" + getAID().getName() + ") - Mensagens de ciclo
			// de rotas enviadas");
			// myAgent.doWait();
		}
	}

	public class ReceberRotasFinalizadas extends CyclicBehaviour {

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchLanguage("ROTAS"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		public void action() {
			ACLMessage msg = myAgent.receive(template);

			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM)) {
				// Message received. Process it
				System.out.println("Mensagem final de: " + msg.getSender().getName());
				System.out
						.println("Matriz de rotas concluidas: " + Arrays.deepToString(matrizRotasRegularesConcluidas));
				String[] msgArray = msg.getContent().split(";");

				// Rota regular
				if (msg.getSender().getName().indexOf("VeiculoRegular") != -1) {
					matrizRotasRegularesConcluidas[Integer.parseInt(msgArray[0])][Integer.parseInt(msgArray[1])] = 1;
				}

				// Rota transportador auxiliar
				if (msg.getSender().getName().indexOf("TransportadorAuxiliar") != -1) {
					existemRotasAuxiliaresPendentes = false;
					System.out.println("Transp. Auxiliar Envio MSG de rotas terminadas");
				}

				// Verificando conteúdo da matriz de rotas regulares concluídas
				existemRotasRegularesPendentes = false;
				for (int i = 0; i <= nrVeiculosX; i++) {
					for (int j = 0; j <= nrVeiculosY; j++) {
						if (matrizRotasRegularesConcluidas[i][j] == 0) {
							existemRotasRegularesPendentes = true;
							// break;
						}
					}
				}

				// Se não houverem rotas pendentes, solicitar ao Sync início de ciclo
				if (!existemRotasRegularesPendentes && !existemRotasAuxiliaresPendentes) {
					System.out.println("Transp. Reg sem rotas pendentes ");
					matrizRotasRegularesConcluidas = new int[nrVeiculosX + 1][nrVeiculosY + 1];
					existemRotasAuxiliaresPendentes = true;
					if (nrVeiculosAux == 0)
						existemRotasAuxiliaresPendentes = false;

					SequentialBehaviour sb = new SequentialBehaviour();
					sb.addSubBehaviour(new EnviarAlertaFimCiclo());
					sb.addSubBehaviour(new PedirAutorizacao());
					addBehaviour(sb);
				}

				/*
				 * if (!existemRotasAuxiliaresPendentes && !existemRotasRegularesPendentes){
				 * //System.out.println("Transp. Aux avisado? "+avisoAux); addBehaviour(new
				 * PedirAutorizacao()); } else if (existemRotasAuxiliaresPendentes &&
				 * !existemRotasRegularesPendentes){ addBehaviour(new EnviarAlertaFimCiclo()); }
				 */
			} else {
				block();
			}
		}
	}

	public class ReceberTarefaParaTransferir extends CyclicBehaviour {

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchLanguage("ROTAS"),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM)) {
				System.out.println("AGENT " + getAID().getName() + ") - TASK TO TRANSFER");
			} else {
				block();
			}

		}

	}

	/**
	 * Implementação do mecanismo de sincronização "conservative sincronization" do
	 * Veiculo Regular, onde o agente solicita autorização para a realização de uma
	 * tarefa num determinado instante.
	 * 
	 * @author dmontier
	 *
	 */
	protected class PedirAutorizacao extends OneShotBehaviour {
		public void action() {
			ACLMessage msgPedido = new ACLMessage(ACLMessage.REQUEST);
			msgPedido.setLanguage("SYNC");
			msgPedido.addReceiver(new AID("SyncAgent", AID.ISLOCALNAME));
			msgPedido.setContent("99999");
			send(msgPedido);
		}
	}

	protected class EnviarAlertaFimCiclo extends OneShotBehaviour {
		public void action() {
			ACLMessage msgPedido = new ACLMessage(ACLMessage.INFORM);
			msgPedido.setLanguage("AUX");
			msgPedido.setProtocol("FIM");
			msgPedido.addReceiver(new AID("TransportadorAuxiliar", AID.ISLOCALNAME));
			msgPedido.setContent(Integer.toString(codCiclo));
			send(msgPedido);
		}
	}

	MessageTemplate templateAguardarAutorizacao = MessageTemplate.and(MessageTemplate.MatchLanguage("SYNC"),
			MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));

	/**
	 * Implementação do mecanismo de sincronização "conservative sincronization" do
	 * Veiculo Regular, onde o agente recebe a liberação para a realização de uma
	 * tarefa específica.
	 * 
	 * @author dmontier
	 *
	 */
	protected class AguardarAutorizacao extends CyclicBehaviour {
		// Double tempoAutorizado;
		public void action() {
			ACLMessage msg = myAgent.receive(templateAguardarAutorizacao);
			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM_REF)) {
				// codCiclo++;
				// System.out.println("AGENT " + getAID().getName() + " - Recebi mensagem");
				if (codCiclo < qtdRodadas) {
					matrizRotasRegularesConcluidas = new int[nrVeiculosX + 1][nrVeiculosY + 1];
					existemRotasAuxiliaresPendentes = true;
					if (nrVeiculosAux == 0)
						existemRotasAuxiliaresPendentes = false;
					addBehaviour(new CriarRotas());

				} else {
					try {
						net.gravaEstatisticasDaSimulacao(qtdRodadas);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("AGENT " + getAID().getName() + ") - FINISH SIMULATION");
					Utilidades.SendMailTLS("Simulação " + tipoSimulacao + " (" + Integer.toString(net.getCodSimulacao())
							+ ") terminada.");
				}
			} else {
				block();
			}
		}
	}
}
