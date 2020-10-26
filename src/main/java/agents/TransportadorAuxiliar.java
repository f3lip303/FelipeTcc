package agents;

import java.sql.SQLException;
import java.util.Arrays;

import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.NetworkRandom;

public class TransportadorAuxiliar extends Transportador{

	protected int nrVeiculosAux;
	protected double centros[][][];
	protected AID jadeTopic, jadeTopic2;
	// protected int[] matrizRotasConcluidas;
	protected NetworkRandom net;
	protected int[] matrizRotasAuxiliaresConcluidas;

	protected void setup() {
		Object[] args = getArguments(); // leitura de parametros
		tipoSimulacao = args[0].toString();
		qtdRodadas = (int) args[1];
		nrVeiculosAux = (int) args[2];
		matrizRotasAuxiliaresConcluidas = new int[nrVeiculosAux];

		// matrizRotasConcluidas = new int[nrVeiculosAux];
		// Register on topics
		TopicManagementHelper topicHelper;
		try {
			topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			AID jadeTopic = topicHelper.createTopic("souTransportadorAuxiliar");
			topicHelper.register(jadeTopic);
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		// Criar rede randomica
		try {
			net = new NetworkRandom(tipoSimulacao);
			centros = net.calculaTodosCentros();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// System.out.println("AGENT " + getAID().getName() + ") - Start cicle:
		// "+Integer.toString(codCiclo));

		// System.out.println("Inicio da simulacao " + tipoSimulacao);
		// addBehaviour(new CriarRotas());
		addBehaviour(new ReceberAlertaCiclo());
		addBehaviour(new ReceberRotasFinalizadas());
		addBehaviour(new ReceberTarefaParaTransferir());
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
			/*
			 * ACLMessage msgPedido = new ACLMessage(ACLMessage.REQUEST);
			 * msgPedido.setLanguage("SYNC"); msgPedido.addReceiver(new AID("SyncAgent",
			 * AID.ISLOCALNAME)); msgPedido.setContent("88888"); send(msgPedido);
			 */
			ACLMessage msgPedido = new ACLMessage(ACLMessage.INFORM);
			msgPedido.setLanguage("ROTAS");
			msgPedido.addReceiver(new AID("TransportadorRegular", AID.ISLOCALNAME));
			msgPedido.setContent(Integer.toString(codCiclo));
			send(msgPedido);
		}
	}

	public class ReceberAlertaCiclo extends CyclicBehaviour {

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchLanguage("AUX"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM)
					&& (msg.getSender().getName().indexOf("TransportadorRegular") != -1)) {
				// Message received. Process it

				// Inicio do ciclo
				if (msg.getProtocol() == "INICIO") {
					codCiclo = Integer.parseInt(msg.getContent());
					System.out.println(
							"AGENT " + getAID().getName() + ") - Begin of cicle: " + Integer.toString(codCiclo));
					addBehaviour(new CriarRotas());
				}

				// Fim do ciclo
				if (msg.getProtocol() == "FIM") {
					System.out
							.println("AGENT " + getAID().getName() + ") - End of cicle: " + Integer.toString(codCiclo));
					TopicManagementHelper topicHelper;
					try {
						topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
						AID jadeTopic2 = topicHelper.createTopic("souVeiculoAuxiliar");

						ACLMessage msgAux = new ACLMessage(ACLMessage.INFORM);
						msgAux.setLanguage("AUX");
						msgAux.addReceiver(jadeTopic2);
						msgAux.setContent(Integer.toString(codCiclo));
						send(msgAux);

					} catch (ServiceException e) {
						e.printStackTrace();
					}

				}

				// addBehaviour(new CriarRotas());

				/*
				 * if (codCiclo>=1){ TopicManagementHelper topicHelper; try { topicHelper =
				 * (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME); AID
				 * jadeTopic2 = topicHelper.createTopic("souVeiculoAuxiliar");
				 * 
				 * ACLMessage msgAux = new ACLMessage(ACLMessage.INFORM);
				 * msgAux.setLanguage("AUX"); msgAux.addReceiver(jadeTopic2);
				 * msgAux.setContent(Integer.toString(codCiclo)); send(msgAux);
				 * 
				 * } catch (ServiceException e) { e.printStackTrace(); } }
				 */ } else {
				block();
			}
		}
	}

	public class ReceberRotasFinalizadas extends CyclicBehaviour {

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchLanguage("ROTAS"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		public void action() {
			ACLMessage msg = myAgent.receive(template);

			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM)
					&& (msg.getSender().getName().indexOf("VeiculoAuxiliar") != -1)) {
				// Message received. Process it
				System.out.println("XXXXXXXXXXXXXXXXXXXXXXX   Mensagem final de: " + msg.getSender().getName());
				String[] msgArray = msg.getContent().split(";");
				System.out.println("Mensagem de: " + msg.getSender().getName());

				matrizRotasAuxiliaresConcluidas[Integer.parseInt(msgArray[1])] = 1;

				// Verificando conteúdo da matriz de rotas auxiliares concluídas
				existemRotasAuxiliaresPendentes = false;
				for (int i = 0; i < nrVeiculosAux; i++) {
					if (matrizRotasAuxiliaresConcluidas[i] == 0) {
						existemRotasAuxiliaresPendentes = true;
						// break;
					}
				}

				if (!existemRotasAuxiliaresPendentes) {
					// System.out.println("Transp. Aux avisado? "+avisoAux);
					// addBehaviour(new AvisarFimDeCiclo());
					addBehaviour(new PedirAutorizacao());
					matrizRotasAuxiliaresConcluidas = new int[nrVeiculosAux];
				}

				/*
				 * 
				 * 
				 * 
				 * 
				 * if (!existemRotasRegularesPendentes && !avisoAux){ addBehaviour(new
				 * AvisarFimDeCiclo()); avisoAux = true;
				 * System.out.println("Transp. Aux avisado!!!"); }
				 */

				/*
				 * try { if ((!existemRotasPendentes)&&(codCiclo<qtdRodadas)){
				 * //System.out.println("valor de existem rotas pendentes: "+Boolean.toString(
				 * existemRotasPendentes)); matrizRotasConcluidas = new int[3][3];
				 * //myAgent.doWait(2000); // Caso não existam rotas pendentes inciar novo ciclo
				 * //Arrays.fill(matrizRotasConcluidas, 0); //doWait(1000);
				 * //System.out.println("AGENT " + getAID().getName() + ") : sent points");
				 * //addBehaviour(new CriarRotas()); addBehaviour(new PedirAutorizacao()); }
				 * else if ((!existemRotasPendentes)&&(codCiclo==qtdRodadas)) { // gravar
				 * informações da simulação //myAgent.doWait(5000);
				 * net.gravaEstatisticasDaSimulacao(qtdRodadas); System.out.println("AGENT " +
				 * getAID().getName() + ") - FINISH SIMULATION"); matrizRotasConcluidas = new
				 * int[3][3]; } } catch (SQLException e) { // TODO Auto-generated catch block
				 * System.out.println("AGENT " + getAID().getName() + ") : problem DB");
				 * e.printStackTrace(); }
				 */
			} else {
				block();
			}
		}
	}

	public class CriarRotas extends OneShotBehaviour {

		public void action() {

			// codCiclo++;
			System.out.println("AGENT (" + getAID().getName() + ") - Start cicle: " + Integer.toString(codCiclo)
					+ " - Simulation: " + Integer.toString(net.getCodSimulacao()));
			// myAgent.doWait(500);

			if (nrVeiculosAux == 1) {
				addBehaviour(new CriarRotas1());
			} else if (nrVeiculosAux == 2) {
				addBehaviour(new CriarRotas2());
			} else if (nrVeiculosAux == 3) {
				addBehaviour(new CriarRotas3());
			} else if (nrVeiculosAux == 4) {
				addBehaviour(new CriarRotas4());
			} else if (nrVeiculosAux == 5) {
				addBehaviour(new CriarRotas5());
			} else if (nrVeiculosAux == 6) {
				addBehaviour(new CriarRotas6());
			} else if (nrVeiculosAux == 7) {
				addBehaviour(new CriarRotas7());
			}
		}
	}

	public class CriarRotas1 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][1][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][1][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

		}
	}

	public class CriarRotas2 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][0][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][0][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

			double[] route99999_1x = { net.getPontoTransAuxX(), centros[0][2][1] };
			ACLMessage msg99999_1x = new ACLMessage(ACLMessage.INFORM);
			msg99999_1x.setLanguage("ROTAS");
			msg99999_1x.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1x.setOntology("pontosx");
			msg99999_1x.setContent(Arrays.toString(route99999_1x));
			send(msg99999_1x);

			double[] route99999_1y = { net.getPontoTransAuxY(), centros[1][2][1] };
			ACLMessage msg99999_1y = new ACLMessage(ACLMessage.INFORM);
			msg99999_1y.setLanguage("ROTAS");
			msg99999_1y.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1y.setOntology("pontosy");
			msg99999_1y.setContent(Arrays.toString(route99999_1y));
			send(msg99999_1y);
		}
	}

	public class CriarRotas3 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][2][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][2][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

			double[] route99999_1x = { net.getPontoTransAuxX(), centros[0][1][0] };
			ACLMessage msg99999_1x = new ACLMessage(ACLMessage.INFORM);
			msg99999_1x.setLanguage("ROTAS");
			msg99999_1x.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1x.setOntology("pontosx");
			msg99999_1x.setContent(Arrays.toString(route99999_1x));
			send(msg99999_1x);

			double[] route99999_1y = { net.getPontoTransAuxY(), centros[1][1][0] };
			ACLMessage msg99999_1y = new ACLMessage(ACLMessage.INFORM);
			msg99999_1y.setLanguage("ROTAS");
			msg99999_1y.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1y.setOntology("pontosy");
			msg99999_1y.setContent(Arrays.toString(route99999_1y));
			send(msg99999_1y);

			double[] route99999_2x = { net.getPontoTransAuxX(), centros[0][1][2] };
			ACLMessage msg99999_2x = new ACLMessage(ACLMessage.INFORM);
			msg99999_2x.setLanguage("ROTAS");
			msg99999_2x.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2x.setOntology("pontosx");
			msg99999_2x.setContent(Arrays.toString(route99999_2x));
			send(msg99999_2x);

			double[] route99999_2y = { net.getPontoTransAuxY(), centros[1][1][2] };
			ACLMessage msg99999_2y = new ACLMessage(ACLMessage.INFORM);
			msg99999_2y.setLanguage("ROTAS");
			msg99999_2y.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2y.setOntology("pontosy");
			msg99999_2y.setContent(Arrays.toString(route99999_2y));
			send(msg99999_2y);
		}
	}

	public class CriarRotas4 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][0][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][0][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

			double[] route99999_1x = { net.getPontoTransAuxX(), centros[0][1][0] };
			ACLMessage msg99999_1x = new ACLMessage(ACLMessage.INFORM);
			msg99999_1x.setLanguage("ROTAS");
			msg99999_1x.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1x.setOntology("pontosx");
			msg99999_1x.setContent(Arrays.toString(route99999_1x));
			send(msg99999_1x);

			double[] route99999_1y = { net.getPontoTransAuxY(), centros[1][1][0] };
			ACLMessage msg99999_1y = new ACLMessage(ACLMessage.INFORM);
			msg99999_1y.setLanguage("ROTAS");
			msg99999_1y.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1y.setOntology("pontosy");
			msg99999_1y.setContent(Arrays.toString(route99999_1y));
			send(msg99999_1y);

			double[] route99999_2x = { net.getPontoTransAuxX(), centros[0][1][2] };
			ACLMessage msg99999_2x = new ACLMessage(ACLMessage.INFORM);
			msg99999_2x.setLanguage("ROTAS");
			msg99999_2x.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2x.setOntology("pontosx");
			msg99999_2x.setContent(Arrays.toString(route99999_2x));
			send(msg99999_2x);

			double[] route99999_2y = { net.getPontoTransAuxY(), centros[1][1][2] };
			ACLMessage msg99999_2y = new ACLMessage(ACLMessage.INFORM);
			msg99999_2y.setLanguage("ROTAS");
			msg99999_2y.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2y.setOntology("pontosy");
			msg99999_2y.setContent(Arrays.toString(route99999_2y));
			send(msg99999_2y);

			double[] route99999_3x = { net.getPontoTransAuxX(), centros[0][2][1] };
			ACLMessage msg99999_3x = new ACLMessage(ACLMessage.INFORM);
			msg99999_3x.setLanguage("ROTAS");
			msg99999_3x.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3x.setOntology("pontosx");
			msg99999_3x.setContent(Arrays.toString(route99999_3x));
			send(msg99999_3x);

			double[] route99999_3y = { net.getPontoTransAuxY(), centros[1][2][1] };
			ACLMessage msg99999_3y = new ACLMessage(ACLMessage.INFORM);
			msg99999_3y.setLanguage("ROTAS");
			msg99999_3y.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3y.setOntology("pontosy");
			msg99999_3y.setContent(Arrays.toString(route99999_3y));
			send(msg99999_3y);

		}
	}

	public class CriarRotas5 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][0][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][0][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

			double[] route99999_1x = { net.getPontoTransAuxX(), centros[0][1][0] };
			ACLMessage msg99999_1x = new ACLMessage(ACLMessage.INFORM);
			msg99999_1x.setLanguage("ROTAS");
			msg99999_1x.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1x.setOntology("pontosx");
			msg99999_1x.setContent(Arrays.toString(route99999_1x));
			send(msg99999_1x);

			double[] route99999_1y = { net.getPontoTransAuxY(), centros[1][1][0] };
			ACLMessage msg99999_1y = new ACLMessage(ACLMessage.INFORM);
			msg99999_1y.setLanguage("ROTAS");
			msg99999_1y.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1y.setOntology("pontosy");
			msg99999_1y.setContent(Arrays.toString(route99999_1y));
			send(msg99999_1y);

			double[] route99999_2x = { net.getPontoTransAuxX(), centros[0][1][2] };
			ACLMessage msg99999_2x = new ACLMessage(ACLMessage.INFORM);
			msg99999_2x.setLanguage("ROTAS");
			msg99999_2x.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2x.setOntology("pontosx");
			msg99999_2x.setContent(Arrays.toString(route99999_2x));
			send(msg99999_2x);

			double[] route99999_2y = { net.getPontoTransAuxY(), centros[1][1][2] };
			ACLMessage msg99999_2y = new ACLMessage(ACLMessage.INFORM);
			msg99999_2y.setLanguage("ROTAS");
			msg99999_2y.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2y.setOntology("pontosy");
			msg99999_2y.setContent(Arrays.toString(route99999_2y));
			send(msg99999_2y);

			double[] route99999_3x = { net.getPontoTransAuxX(), centros[0][2][1] };
			ACLMessage msg99999_3x = new ACLMessage(ACLMessage.INFORM);
			msg99999_3x.setLanguage("ROTAS");
			msg99999_3x.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3x.setOntology("pontosx");
			msg99999_3x.setContent(Arrays.toString(route99999_3x));
			send(msg99999_3x);

			double[] route99999_3y = { net.getPontoTransAuxY(), centros[1][2][1] };
			ACLMessage msg99999_3y = new ACLMessage(ACLMessage.INFORM);
			msg99999_3y.setLanguage("ROTAS");
			msg99999_3y.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3y.setOntology("pontosy");
			msg99999_3y.setContent(Arrays.toString(route99999_3y));
			send(msg99999_3y);

			double[] route99999_4x = { net.getPontoTransAuxX(), centros[0][1][1] };
			ACLMessage msg99999_4x = new ACLMessage(ACLMessage.INFORM);
			msg99999_4x.setLanguage("ROTAS");
			msg99999_4x.addReceiver(new AID("VeiculoAuxiliar(99999_4)", AID.ISLOCALNAME));
			msg99999_4x.setOntology("pontosx");
			msg99999_4x.setContent(Arrays.toString(route99999_4x));
			send(msg99999_4x);

			double[] route99999_4y = { net.getPontoTransAuxY(), centros[1][1][1] };
			ACLMessage msg99999_4y = new ACLMessage(ACLMessage.INFORM);
			msg99999_4y.setLanguage("ROTAS");
			msg99999_4y.addReceiver(new AID("VeiculoAuxiliar(99999_4)", AID.ISLOCALNAME));
			msg99999_4y.setOntology("pontosy");
			msg99999_4y.setContent(Arrays.toString(route99999_4y));
			send(msg99999_4y);
		}
	}

	public class CriarRotas6 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][0][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][0][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

			double[] route99999_1x = { net.getPontoTransAuxX(), centros[0][1][0] };
			ACLMessage msg99999_1x = new ACLMessage(ACLMessage.INFORM);
			msg99999_1x.setLanguage("ROTAS");
			msg99999_1x.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1x.setOntology("pontosx");
			msg99999_1x.setContent(Arrays.toString(route99999_1x));
			send(msg99999_1x);

			double[] route99999_1y = { net.getPontoTransAuxY(), centros[1][1][0] };
			ACLMessage msg99999_1y = new ACLMessage(ACLMessage.INFORM);
			msg99999_1y.setLanguage("ROTAS");
			msg99999_1y.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1y.setOntology("pontosy");
			msg99999_1y.setContent(Arrays.toString(route99999_1y));
			send(msg99999_1y);

			double[] route99999_2x = { net.getPontoTransAuxX(), centros[0][1][2] };
			ACLMessage msg99999_2x = new ACLMessage(ACLMessage.INFORM);
			msg99999_2x.setLanguage("ROTAS");
			msg99999_2x.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2x.setOntology("pontosx");
			msg99999_2x.setContent(Arrays.toString(route99999_2x));
			send(msg99999_2x);

			double[] route99999_2y = { net.getPontoTransAuxY(), centros[1][1][2] };
			ACLMessage msg99999_2y = new ACLMessage(ACLMessage.INFORM);
			msg99999_2y.setLanguage("ROTAS");
			msg99999_2y.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2y.setOntology("pontosy");
			msg99999_2y.setContent(Arrays.toString(route99999_2y));
			send(msg99999_2y);

			double[] route99999_3x = { net.getPontoTransAuxX(), centros[0][2][1] };
			ACLMessage msg99999_3x = new ACLMessage(ACLMessage.INFORM);
			msg99999_3x.setLanguage("ROTAS");
			msg99999_3x.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3x.setOntology("pontosx");
			msg99999_3x.setContent(Arrays.toString(route99999_3x));
			send(msg99999_3x);

			double[] route99999_3y = { net.getPontoTransAuxY(), centros[1][2][1] };
			ACLMessage msg99999_3y = new ACLMessage(ACLMessage.INFORM);
			msg99999_3y.setLanguage("ROTAS");
			msg99999_3y.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3y.setOntology("pontosy");
			msg99999_3y.setContent(Arrays.toString(route99999_3y));
			send(msg99999_3y);

			double[] route99999_4x = { net.getPontoTransAuxX(), centros[0][0][0] };
			ACLMessage msg99999_4x = new ACLMessage(ACLMessage.INFORM);
			msg99999_4x.setLanguage("ROTAS");
			msg99999_4x.addReceiver(new AID("VeiculoAuxiliar(99999_4)", AID.ISLOCALNAME));
			msg99999_4x.setOntology("pontosx");
			msg99999_4x.setContent(Arrays.toString(route99999_4x));
			send(msg99999_4x);

			double[] route99999_4y = { net.getPontoTransAuxY(), centros[1][0][0] };
			ACLMessage msg99999_4y = new ACLMessage(ACLMessage.INFORM);
			msg99999_4y.setLanguage("ROTAS");
			msg99999_4y.addReceiver(new AID("VeiculoAuxiliar(99999_4)", AID.ISLOCALNAME));
			msg99999_4y.setOntology("pontosy");
			msg99999_4y.setContent(Arrays.toString(route99999_4y));
			send(msg99999_4y);

			double[] route99999_5x = { net.getPontoTransAuxX(), centros[0][2][2] };
			ACLMessage msg99999_5x = new ACLMessage(ACLMessage.INFORM);
			msg99999_5x.setLanguage("ROTAS");
			msg99999_5x.addReceiver(new AID("VeiculoAuxiliar(99999_5)", AID.ISLOCALNAME));
			msg99999_5x.setOntology("pontosx");
			msg99999_5x.setContent(Arrays.toString(route99999_5x));
			send(msg99999_5x);

			double[] route99999_5y = { net.getPontoTransAuxY(), centros[1][2][2] };
			ACLMessage msg99999_5y = new ACLMessage(ACLMessage.INFORM);
			msg99999_5y.setLanguage("ROTAS");
			msg99999_5y.addReceiver(new AID("VeiculoAuxiliar(99999_5)", AID.ISLOCALNAME));
			msg99999_5y.setOntology("pontosy");
			msg99999_5y.setContent(Arrays.toString(route99999_5y));
			send(msg99999_5y);

		}
	}

	public class CriarRotas7 extends OneShotBehaviour {

		public void action() {
			// System.out.println("centros: "+Arrays.deepToString(centros));
			// Comunicar aos agentes veiculos sobre as rotas
			double[] route99999_0x = { net.getPontoTransAuxX(), centros[0][0][1] };
			ACLMessage msg99999_0x = new ACLMessage(ACLMessage.INFORM);
			msg99999_0x.setLanguage("ROTAS");
			msg99999_0x.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0x.setOntology("pontosx");
			msg99999_0x.setContent(Arrays.toString(route99999_0x));
			send(msg99999_0x);

			double[] route99999_0y = { net.getPontoTransAuxY(), centros[1][0][1] };
			ACLMessage msg99999_0y = new ACLMessage(ACLMessage.INFORM);
			msg99999_0y.setLanguage("ROTAS");
			msg99999_0y.addReceiver(new AID("VeiculoAuxiliar(99999_0)", AID.ISLOCALNAME));
			msg99999_0y.setOntology("pontosy");
			msg99999_0y.setContent(Arrays.toString(route99999_0y));
			send(msg99999_0y);

			double[] route99999_1x = { net.getPontoTransAuxX(), centros[0][1][0] };
			ACLMessage msg99999_1x = new ACLMessage(ACLMessage.INFORM);
			msg99999_1x.setLanguage("ROTAS");
			msg99999_1x.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1x.setOntology("pontosx");
			msg99999_1x.setContent(Arrays.toString(route99999_1x));
			send(msg99999_1x);

			double[] route99999_1y = { net.getPontoTransAuxY(), centros[1][1][0] };
			ACLMessage msg99999_1y = new ACLMessage(ACLMessage.INFORM);
			msg99999_1y.setLanguage("ROTAS");
			msg99999_1y.addReceiver(new AID("VeiculoAuxiliar(99999_1)", AID.ISLOCALNAME));
			msg99999_1y.setOntology("pontosy");
			msg99999_1y.setContent(Arrays.toString(route99999_1y));
			send(msg99999_1y);

			double[] route99999_2x = { net.getPontoTransAuxX(), centros[0][1][2] };
			ACLMessage msg99999_2x = new ACLMessage(ACLMessage.INFORM);
			msg99999_2x.setLanguage("ROTAS");
			msg99999_2x.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2x.setOntology("pontosx");
			msg99999_2x.setContent(Arrays.toString(route99999_2x));
			send(msg99999_2x);

			double[] route99999_2y = { net.getPontoTransAuxY(), centros[1][1][2] };
			ACLMessage msg99999_2y = new ACLMessage(ACLMessage.INFORM);
			msg99999_2y.setLanguage("ROTAS");
			msg99999_2y.addReceiver(new AID("VeiculoAuxiliar(99999_2)", AID.ISLOCALNAME));
			msg99999_2y.setOntology("pontosy");
			msg99999_2y.setContent(Arrays.toString(route99999_2y));
			send(msg99999_2y);

			double[] route99999_3x = { net.getPontoTransAuxX(), centros[0][2][1] };
			ACLMessage msg99999_3x = new ACLMessage(ACLMessage.INFORM);
			msg99999_3x.setLanguage("ROTAS");
			msg99999_3x.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3x.setOntology("pontosx");
			msg99999_3x.setContent(Arrays.toString(route99999_3x));
			send(msg99999_3x);

			double[] route99999_3y = { net.getPontoTransAuxY(), centros[1][2][1] };
			ACLMessage msg99999_3y = new ACLMessage(ACLMessage.INFORM);
			msg99999_3y.setLanguage("ROTAS");
			msg99999_3y.addReceiver(new AID("VeiculoAuxiliar(99999_3)", AID.ISLOCALNAME));
			msg99999_3y.setOntology("pontosy");
			msg99999_3y.setContent(Arrays.toString(route99999_3y));
			send(msg99999_3y);

			double[] route99999_4x = { net.getPontoTransAuxX(), centros[0][0][0] };
			ACLMessage msg99999_4x = new ACLMessage(ACLMessage.INFORM);
			msg99999_4x.setLanguage("ROTAS");
			msg99999_4x.addReceiver(new AID("VeiculoAuxiliar(99999_4)", AID.ISLOCALNAME));
			msg99999_4x.setOntology("pontosx");
			msg99999_4x.setContent(Arrays.toString(route99999_4x));
			send(msg99999_4x);

			double[] route99999_4y = { net.getPontoTransAuxY(), centros[1][0][0] };
			ACLMessage msg99999_4y = new ACLMessage(ACLMessage.INFORM);
			msg99999_4y.setLanguage("ROTAS");
			msg99999_4y.addReceiver(new AID("VeiculoAuxiliar(99999_4)", AID.ISLOCALNAME));
			msg99999_4y.setOntology("pontosy");
			msg99999_4y.setContent(Arrays.toString(route99999_4y));
			send(msg99999_4y);

			double[] route99999_5x = { net.getPontoTransAuxX(), centros[0][2][2] };
			ACLMessage msg99999_5x = new ACLMessage(ACLMessage.INFORM);
			msg99999_5x.setLanguage("ROTAS");
			msg99999_5x.addReceiver(new AID("VeiculoAuxiliar(99999_5)", AID.ISLOCALNAME));
			msg99999_5x.setOntology("pontosx");
			msg99999_5x.setContent(Arrays.toString(route99999_5x));
			send(msg99999_5x);

			double[] route99999_5y = { net.getPontoTransAuxY(), centros[1][2][2] };
			ACLMessage msg99999_5y = new ACLMessage(ACLMessage.INFORM);
			msg99999_5y.setLanguage("ROTAS");
			msg99999_5y.addReceiver(new AID("VeiculoAuxiliar(99999_5)", AID.ISLOCALNAME));
			msg99999_5y.setOntology("pontosy");
			msg99999_5y.setContent(Arrays.toString(route99999_5y));
			send(msg99999_5y);

			double[] route99999_6x = { net.getPontoTransAuxX(), centros[0][1][1] };
			ACLMessage msg99999_6x = new ACLMessage(ACLMessage.INFORM);
			msg99999_6x.setLanguage("ROTAS");
			msg99999_6x.addReceiver(new AID("VeiculoAuxiliar(99999_6)", AID.ISLOCALNAME));
			msg99999_6x.setOntology("pontosx");
			msg99999_6x.setContent(Arrays.toString(route99999_6x));
			send(msg99999_6x);

			double[] route99999_6y = { net.getPontoTransAuxY(), centros[1][1][1] };
			ACLMessage msg99999_6y = new ACLMessage(ACLMessage.INFORM);
			msg99999_6y.setLanguage("ROTAS");
			msg99999_6y.addReceiver(new AID("VeiculoAuxiliar(99999_6)", AID.ISLOCALNAME));
			msg99999_6y.setOntology("pontosy");
			msg99999_6y.setContent(Arrays.toString(route99999_6y));
			send(msg99999_6y);

		}
	}

	/*
	 * MessageTemplate templateAguardarAutorizacao = MessageTemplate.and(
	 * MessageTemplate.MatchLanguage("SYNC"),
	 * MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF) );
	 * 
	 *//**
		 * Implementação do mecanismo de sincronização "conservative sincronization" do
		 * Veiculo Regular, onde o agente recebe a liberação para a realização de uma
		 * tarefa específica.
		 * 
		 * @author dmontier
		 *
		 *//*
			 * protected class AguardarAutorizacao extends CyclicBehaviour { //Double
			 * tempoAutorizado; public void action() { ACLMessage msg =
			 * myAgent.receive(templateAguardarAutorizacao); if ((msg !=
			 * null)&&(msg.getPerformative()==ACLMessage.INFORM_REF)){
			 * //System.out.println("AGENT " + getAID().getName() + " - Recebi mensagem");
			 * if (codCiclo<qtdRodadas){ matrizRotasRegularesConcluidas = new
			 * int[nrVeiculosX+1][nrVeiculosY+1]; existemRotasAuxiliaresPendentes = true;
			 * addBehaviour(new CriarRotas());
			 * 
			 * } else { try { net.gravaEstatisticasDaSimulacao(qtdRodadas); } catch
			 * (SQLException e) { // TODO Auto-generated catch block e.printStackTrace(); }
			 * System.out.println("AGENT " + getAID().getName() + ") - FINISH SIMULATION");
			 * } } else { block(); } } }
			 */
}
