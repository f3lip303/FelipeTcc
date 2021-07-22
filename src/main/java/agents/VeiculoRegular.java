package agents;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import agents.Veiculo.EstadosVeiculos;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class VeiculoRegular extends Veiculo {

	protected List<Integer> tarefasParaTransferir = null;
	protected int rodada = 0;
	protected double tempoEspera = 0;

	protected void setup() {
		System.out.println("** * * ** ***** *INICIANDO SETUP VEÍCULO REGULAR *** * * ** * * * *");

		Object[] args = getArguments(); // leitura de parametros
		tipoSimulacao = args[0].toString();
		faixaV = Integer.valueOf(args[1].toString());
		faixaH = Integer.valueOf(args[2].toString());

		// MessageTemplate.MatchReplyByDate(new Date(System.currentTimeMillis() +
		// 1000));

		// System.out.println("AGENT - with params
		// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+") - "+
		// getAID().getName() + "): created");

		// Registry of regular vehicle on DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("VeiculoRegular");
		sd.setName(getLocalName() + "-VeiculoRegular");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Update the list of agentesVeiculosAtivos para participar de leilões
		agentesVeiculosAtivos.clear();
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd2 = new ServiceDescription();
		DFAgentDescription[] result;
		// System.out.println(this.getLocalName()+": Vou procurar agentes do tipo
		// veiculo ta. ");
		try {

			sd2.setType("VeiculoRegular");
			template.addServices(sd2);
			result = DFService.search(this, template);
			for (int i = 0; i < result.length; ++i) {
				if (result[i].getName() != this.getAID()) { // se veículo é diferente de veículo que criou a mensagem
					agentesVeiculosAtivos.add(result[i].getName());
				}
			}

			sd2.setType("VeiculoAuxiliar");
			template.addServices(sd2);
			result = DFService.search(this, template);
			// System.out.println(this.getLocalName()+": Ehhhh, nao morri. Encontrei
			// "+result.length+ " agentes.");
			for (int i = 0; i < result.length; ++i) {
				if (result[i].getName() != this.getAID()) { // se veículo é diferente de veículo que criou a mensagem
					agentesVeiculosAtivos.add(result[i].getName());
				}
			}
		} catch (FIPAException fe) {
			System.out.println(this.getLocalName() + ": Hiii, morri.");
			fe.printStackTrace();
		}

		if (this.tipoSimulacao.equals("distri_carga")) {
			addBehaviour(new IniciarOperacao());
			addBehaviour(new ParticiparLeilaoTarefa(this, mTemplateCfp));
			addBehaviour(new RealizarLeilao());
			addBehaviour(new AguardarAutorizacao());

		} else {
			addBehaviour(new IniciarOperacao());
			addBehaviour(new ParticiparLeilaoTarefa(this, mTemplateCfp));
			addBehaviour(new AguardarAutorizacao());
			// addBehaviour(new verificarFim());

		}
	}

	/**
	 * Comportamento do Veiculo Regular atender a um cliente
	 * 
	 *
	 */
	private class Atendimento extends OneShotBehaviour {
		public void action() {
			// participoDeLeilao = false;
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
				tarefasParaTransferir = new ArrayList<Integer>(r.identificaTarefasParaTransferir());
			} else {
				estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA;
				participoDeLeilao = false;
				// Deregister on topics souVeiculo e participoEmLeilao
				/*
				 * TopicManagementHelper topicHelper; try { topicHelper =
				 * (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME); AID
				 * jadeTopic = topicHelper.createTopic("participoEmLeilao");
				 * topicHelper.deregister(jadeTopic); } catch (ServiceException e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); }
				 */
			}

			// Atualizar a variável rota (que represeanta a rota atual a ser realizada)
			// System.out.println("Tarefas para tranferir:
			// "+tarefasParaTransferir.toString());
			if (tarefasParaTransferir != null)
				if (tarefasParaTransferir.size() > 0) {
					// estAnterior = estAtual;
					// estAtual = EstadosVeiculos.STATE_NEGOCIANDO;
					// addBehaviour(new PedirAutorizacao());
					addBehaviour(new RealizarLeilao());
				}

			addBehaviour(new PedirAutorizacao());

			// myAgent.doWait();

			// myAgent.doWait((long)
			// r.getTemposDeServico()[r.getVisitaAtual()]*timeFactor+1);
			// System.out.println("AGENT " + getAID().getName() + ") - waiting for:
			// "+Long.toString((long)
			// r.getTemposDeServico()[r.getVisitaAtual()]*1)+timeFactor);
		}
	}

	/**
	 * Comportamento que comunica ao transportador sobre o termino de uma rota
	 * 
	 *
	 */
	protected class ComunicaTransportador extends OneShotBehaviour {
		public void action() {
			System.out.println("Agent " + myAgent.getName() + "comunica transportador Que terminei a rota!!!");
			// Comunicar ao transportador que a rota foi realizada
			ACLMessage msgTermino = new ACLMessage(ACLMessage.INFORM);
			msgTermino.setLanguage("ROTAS");
			msgTermino.addReceiver(new AID("TransportadorRegular", AID.ISLOCALNAME));
			msgTermino.setContent(Integer.toString(faixaV) + ";" + Integer.toString(faixaH));
			send(msgTermino);
			// myAgent.doWait();

			estAtual = EstadosVeiculos.STATE_FINALIZADO;

			addBehaviour(new PedirAutorizacao());

		}

	}

	/**
	 * Comportamento do Veiculo Regular que realiza o leilão das tarefas
	 * identificadas
	 * 
	 *
	 */
	private class RealizarLeilao extends OneShotBehaviour {
		public void action() {

			participoDeLeilao = false;

			List<Double> pontosParaRemover = new ArrayList<Double>();
			// nrVisitas = nrVisitas - tarefasParaTransferir.size();
			// System.out.println("Agent
			// (veiculo"+Integer.toString(faixaV)+Integer.toString(faixaH)+") - Tasks to
			// tranf: "+tarefasParaTransferir.toString()+" - Tempo em que foi detectado:
			// "+Double.toString(r.getTempoGasto()));

			// Deregister on topics souVeiculo e participoEmLeilao
			/*
			 * TopicManagementHelper topicHelper = null; AID jadeTopic = null; try {
			 * topicHelper = (TopicManagementHelper)
			 * getHelper(TopicManagementHelper.SERVICE_NAME); jadeTopic =
			 * topicHelper.createTopic("participoEmLeilao");
			 * topicHelper.deregister(jadeTopic);
			 * //System.out.println("Agent (veiculo"+Integer.toString(faixaV)+Integer.
			 * toString(faixaH)+") - UNREGISTERED"); } catch (ServiceException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 */

			// Transferir tarefas selecionadas
			// SequentialBehaviour leiloes = new SequentialBehaviour();

			for (Integer tarefa : tarefasParaTransferir) {
				pontosParaRemover.add(r.getPontoCartesianoSelecionadosX()[tarefa]);

				// Criar leilão para esta tarefa
				// System.out.println("Leilao
				// (veiculo"+Integer.toString(faixaV)+Integer.toString(faixaH)+") da tarefa:
				// "+Integer.toString(tarefa));
				// System.out.println("Qtd veiculos ativos:
				// "+Integer.toString(agentesVeiculosAtivos.size()));

				if (agentesVeiculosAtivos.size() > 0) {
					// Fill the CFP message using DF agent
					ACLMessage msg = new ACLMessage(ACLMessage.CFP);
					msg.setContent(Double.toString(r.getPontoCartesianoSelecionadosX()[tarefa]) + " "
							+ Double.toString(r.getPontoCartesianoSelecionadosY()[tarefa]));
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					for (int i = 0; i < agentesVeiculosAtivos.size(); ++i) {
						if (!myAgent.getAID().equals(agentesVeiculosAtivos.get(i))) {
							msg.addReceiver((AID) agentesVeiculosAtivos.get(i));
						}
					}
					// Fill the CFP message using topics, CONTRACTNETINITIATOR NÃO É COMPATÍVEL COM
					// TOPICS
					// msg.addReceiver(jadeTopic);
					// leiloes.addSubBehaviour(new LeiloarTarefa(myAgent, msg,
					// r.getPontoCartesianoSelecionadosX()[tarefa],
					// r.getPontoCartesianoSelecionadosY()[tarefa], r.getTempoGasto()));
					addBehaviour(new LeiloarTarefa(myAgent, msg, r.getPontoCartesianoSelecionadosX()[tarefa],
							r.getPontoCartesianoSelecionadosY()[tarefa], r.getTempoGasto()));
				}

			}

			// Remover tarefas do roteiro
			/*
			 * for (Double pontoX : pontosParaRemover){ // Remove da rota regular try {
			 * r.removerDaRota(pontoX); } catch (SQLException e) { // TODO Auto-generated
			 * catch block e.printStackTrace(); } }
			 */

			// addBehaviour(leiloes);
			tarefasParaTransferir.clear();
		}
	}

	MessageTemplate templateAguardarAutorizacao = MessageTemplate.and(MessageTemplate.MatchLanguage("SYNC"),
			MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));

	/**
	 * Implementação do mecanismo de sincronização "conservative sincronization" do
	 * Veiculo Regular, onde o agente recebe a liberação para a realização de uma
	 * tarefa específica.
	 * 
	 *
	 */
	protected class AguardarAutorizacao extends CyclicBehaviour {
		// Double tempoAutorizado;
		public void action() {
			ACLMessage msg = myAgent.receive(templateAguardarAutorizacao);
			// System.out.println("AGENT " + getAID().getName() + " - Recebi mensagem");
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
				} else if (estAtual.equals(EstadosVeiculos.STATE_FINALIZADO)) {
					myAgent.doWait();
				} else if (estAtual.equals(EstadosVeiculos.STATE_NEGOCIANDO)) {
					// myAgent.doWait();
				} else {
					// block();
					if (rodada != 0 && r != null) {
						if (r.getTempoGasto() + r.estimaTempoParaProximaVisita() + tempoEspera >= r
								.getTempoDisponivel()) {
							estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA;
							// addBehaviour(new ComunicaTransportador());
							tempoEspera = tempoEspera + 20;
							block();
						} else {
							rodada++;
							block();

						}

					} else {
						rodada++;
						block();

					}

				}
			} else {
				System.out.println("ESTOU NO BLOCK DO SEM MENSAGEM ");
				tempoEspera = tempoEspera + 20;
				estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
				block();

			}
		}
	}

	protected class verificarFim extends CyclicBehaviour {
		public void action() {

			if (r != null) {
				if ((r.getTempoGasto() + r.estimaTempoParaProximaVisita() <= r.getTempoDisponivel())) {

				} else {
					System.out.println("Fim de Simulaçao chamado");
					estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA;
					participoDeLeilao = false;
				}
			}
		}
	}

}
