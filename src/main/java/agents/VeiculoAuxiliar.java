package agents;

import java.sql.SQLException;

import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class VeiculoAuxiliar extends Veiculo {

	protected void setup() {
		// MessageTemplate.MatchReplyByDate(new Date(System.currentTimeMillis() +
		// 1000));
		Object[] args = getArguments(); // leitura de parametros
		tipoSimulacao = args[0].toString();
		faixaV = (int) args[1];
		faixaH = (int) args[2];

		// System.out.println("AGENT - with params
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+") - "+
		// getAID().getName() + "): created");
		System.out.println("AGENT " + getAID().getName() + ": created");

		// Registry of regular vehicle on DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("VeiculoAuxiliar");
		sd.setName(getLocalName() + "-VeiculoAuxiliar");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Register on topics
		TopicManagementHelper topicHelper;
		try {
			topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			AID jadeTopic = topicHelper.createTopic("souVeiculoAuxiliar");
			topicHelper.register(jadeTopic);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		addBehaviour(new IniciarOperacao());
		addBehaviour(new AguardarAutorizacao());
		addBehaviour(new ParticiparLeilaoTarefa(this, mTemplateCfp));
		addBehaviour(new ReceberAlertaFimCiclo());

	}

	/**
	 * Comportamento do Veiculo Regular atender a um cliente
	 * 
	 * @author dmontier
	 *
	 */
	private class Atendimento extends OneShotBehaviour {
		public void action() {
			try {
				if (r.getVisitaAtual() < r.getRota().size() - 2) {
					r.atendimento();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if ((r.getTempoGasto() + r.estimaTempoParaProximaVisita() <= r.getTempoDisponivel())
					&& (r.getVisitaAtual() < r.getRota().size() - 2)) {
				estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
			} else {
				if (r.getTempoGasto() + r.estimaTempoParaProximaVisita() <= r.getTempoDisponivel()) {
					estAtual = EstadosVeiculos.STATE_AGUARDANDO;
				} else {
					estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA;
					participoDeLeilao = false;
					/*
					 * // Deregister on topics souVeiculo e participoEmLeilao TopicManagementHelper
					 * topicHelper; try { topicHelper = (TopicManagementHelper)
					 * getHelper(TopicManagementHelper.SERVICE_NAME); AID jadeTopic =
					 * topicHelper.createTopic("participoEmLeilao");
					 * topicHelper.deregister(jadeTopic); } catch (ServiceException e) { // TODO
					 * Auto-generated catch block e.printStackTrace(); }
					 */
				}

			}
			addBehaviour(new PedirAutorizacao());
		}
	}

	/*	*//**
			 * Comportamento que comunica ao transportador sobre o termino de uma rota
			 * 
			 * @author dmontier
			 *
			 *//*
				 * protected class ComunicaTransportador extends OneShotBehaviour{ public void
				 * action(){ // Comunicar ao transportador que a rota foi realizada ACLMessage
				 * msgTermino = new ACLMessage(ACLMessage.INFORM);
				 * msgTermino.setLanguage("ROTAS"); msgTermino.addReceiver(new
				 * AID("TransportadorAuxiliar", AID.ISLOCALNAME));
				 * msgTermino.setContent(Integer.toString(faixaV)+";"+Integer.toString(faixaH));
				 * send(msgTermino); myAgent.doWait(); } }
				 */

	public class ReceberAlertaFimCiclo extends CyclicBehaviour {

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchLanguage("AUX"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM)
					&& (msg.getSender().getName().indexOf("TransportadorAuxiliar") != -1)) {

				// novoCiclo = ;
				System.out.println("AGENT " + getAID().getName() + ") - Alert end of cicle: "
						+ Integer.toString(Integer.parseInt(msg.getContent())));

				if (!(estAtual == EstadosVeiculos.STATE_FINALIZADO || estAtual == EstadosVeiculos.STATE_COMUNICACAO)) {

					estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA;
					addBehaviour(new PedirAutorizacao());

					/*
					 * SequentialBehaviour sb = new SequentialBehaviour(); sb.addSubBehaviour(new
					 * RetornoAoDepositoAbrupto()); sb.addSubBehaviour(new
					 * ComunicaTransportadorAbrupto()); sb.addSubBehaviour(new
					 * IniciarCiclo(novoCiclo)); sb.addSubBehaviour(new PedirAutorizacao());
					 * addBehaviour(sb);
					 */
				} /*
					 * else if (estAtual==EstadosVeiculos.STATE_COMUNICACAO){
					 * 
					 * SequentialBehaviour sb = new SequentialBehaviour(); sb.addSubBehaviour(new
					 * ComunicaTransportadorAbrupto()); sb.addSubBehaviour(new
					 * IniciarCiclo(novoCiclo)); sb.addSubBehaviour(new PedirAutorizacao());
					 * addBehaviour(sb);
					 * 
					 * } else if (estAtual==EstadosVeiculos.STATE_FINALIZADO){
					 * 
					 * SequentialBehaviour sb = new SequentialBehaviour(); sb.addSubBehaviour(new
					 * IniciarCiclo(novoCiclo)); sb.addSubBehaviour(new PedirAutorizacao());
					 * addBehaviour(sb); }
					 */

			} else {
				block();
			}

		}
	}

	/**
	 * Comportamento do Veiculo Auxiliar que ao final do ciclo de veículos regulares
	 * avisa aos auxiliares para retornar
	 * 
	 * @author dmontier
	 *
	 */
	/*
	 * protected class RetornoAoDepositoAbrupto extends OneShotBehaviour{ public
	 * void action(){ try { r.retornoAoDeposito(); r.gravaEstatisticasDaRota();
	 * //pontoCartesianoSelecionadosX = null; //pontoCartesianoSelecionadosY = null;
	 * } catch (SQLException e) { e.printStackTrace(); }
	 * 
	 * } }
	 */

	/**
	 * Comportamento que comunica ao transportador sobre o termino de uma rota
	 * 
	 * @author dmontier
	 *
	 */
	/*
	 * protected class ComunicaTransportadorAbrupto extends OneShotBehaviour{ public
	 * void action(){ System.out.println("Agent "+myAgent.getName()+
	 * "comunica transportador!!!"); // Comunicar ao transportador que a rota foi
	 * realizada ACLMessage msgTermino = new ACLMessage(ACLMessage.INFORM);
	 * msgTermino.setLanguage("ROTAS"); msgTermino.addReceiver(new
	 * AID("TransportadorRegular", AID.ISLOCALNAME));
	 * msgTermino.setContent(Integer.toString(faixaV)+";"+Integer.toString(faixaH));
	 * send(msgTermino); //myAgent.doWait();
	 * 
	 * } }
	 */

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
			// System.out.println("AGENT " + getAID().getName() + " - Recebi mensagem -
			// Status: "+estAtual.toString());
			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM_REF)) {
				// System.out.println("AGENT " + getAID().getName() + ") - Recebi autorizacao");
				// tempoAutorizado = Double.valueOf(msg.getContent());
				if (estAtual.equals(EstadosVeiculos.STATE_DESLOCAMENTO_IDA)) {
					addBehaviour(new IdaAoDistrito());
				} else if (estAtual.equals(EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE)) {
					addBehaviour(new Deslocamento());
				} else if (estAtual.equals(EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA)) {
					addBehaviour(new RetornoAoDeposito());
				} else if (estAtual.equals(EstadosVeiculos.STATE_ATENDIMENTO)) {
					addBehaviour(new Atendimento());
				} else if (estAtual.equals(EstadosVeiculos.STATE_COMUNICACAO)) {
					addBehaviour(new ComunicaTransportador());
				} else if (estAtual.equals(EstadosVeiculos.STATE_AGUARDANDO)) {
					estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
					addBehaviour(new Deslocamento());
				} else if (estAtual.equals(EstadosVeiculos.STATE_FINALIZADO)) {
					// myAgent.doWait();

				} else if (estAtual.equals(EstadosVeiculos.STATE_NEGOCIANDO)) {
					// myAgent.doWait();
				} else {
					block();
				}
			} else {
				block();
			}
		}
	}

	/**
	 * Comportamento que comunica ao transportador sobre o termino de uma rota
	 * 
	 * @author dmontier
	 *
	 */
	protected class ComunicaTransportador extends OneShotBehaviour {
		public void action() {
			System.out.println("Agent " + myAgent.getName() + " comunica transportador!!!");
			// Comunicar ao transportador que a rota foi realizada
			ACLMessage msgTermino = new ACLMessage(ACLMessage.INFORM);
			msgTermino.setLanguage("ROTAS");
			msgTermino.addReceiver(new AID("TransportadorAuxiliar", AID.ISLOCALNAME));
			msgTermino.setContent(Integer.toString(faixaV) + ";" + Integer.toString(faixaH));
			send(msgTermino);
			// myAgent.doWait();

			estAtual = EstadosVeiculos.STATE_FINALIZADO;
			addBehaviour(new PedirAutorizacao());

		}
	}
}
