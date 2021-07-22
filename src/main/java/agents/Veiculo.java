package agents;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import agents.Veiculo.EstadosVeiculos;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.util.Logger;
import utils.RouteFactory;
import utils.RouteRandom;
import utils.Utilidades;

public class Veiculo extends Agent {

	public enum EstadosVeiculos {
		STATE_DEPOSITO, STATE_DESLOCAMENTO_IDA, STATE_DESLOCAMENTO_ENTRE, STATE_DESLOCAMENTO_VOLTA, STATE_ATENDIMENTO,
		STATE_FINALIZADO, STATE_AGUARDANDO, STATE_NEGOCIANDO, STATE_COMUNICACAO
	}

	protected EstadosVeiculos estAtual = EstadosVeiculos.STATE_DEPOSITO;
	protected EstadosVeiculos estAnterior;
	protected Logger myLogger = Logger.getMyLogger(getClass().getName());
	protected Long timeFactor = (long) 1000; // para cada minuto na simulacao, dar delay de 1000 milisegundos (1
												// segundo)
	protected String tipoSimulacao;
	protected int codCiclo = 0;
	protected int contLeiloesAbertos = 0;
	protected int contPropostasAbertas = 0;
	protected int faixaV, faixaH, qtdRodadas;
	// apenas os pontos do distrito que foram selecionados
	protected double[] pontoCartesianoSelecionadosX = null;
	protected double[] pontoCartesianoSelecionadosY = null;
	protected RouteRandom r;
	protected List<Object> agentesVeiculosAtivos = new ArrayList<Object>();
	// private SequentialBehaviour Operacao,Operacao2;
	MessageTemplate mTemplateCfp = MessageTemplate.and(
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
			MessageTemplate.MatchPerformative(ACLMessage.CFP));
	double[] ptInicialX; // pontos iniciais X
	double[] ptInicialY;
	protected boolean participoDeLeilao;

	protected void takeDown() {
		// Exit from DF
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("AGENT " + getAID().getName() + " killed.");
	}

	protected class LeiloarTarefa extends ContractNetInitiator {
		private double ptX, ptY, tempo;
		// private Veiculo v;

		public LeiloarTarefa(Agent a, ACLMessage m, double myPtX, double myPtY, double myTempo) {
			super(a, m);
			ptX = myPtX;
			ptY = myPtY;
			tempo = myTempo;
			System.out.println("--> AUCTION: STARTING " + a.getName() + " pointX: " + Double.toString(myPtX));
			contLeiloesAbertos++;
			System.out.println("Leiloes abertos " + a.getName() + ": " + Integer.toString(contLeiloesAbertos));
			if (estAtual != EstadosVeiculos.STATE_NEGOCIANDO) {
				estAnterior = estAtual;
				estAtual = EstadosVeiculos.STATE_NEGOCIANDO;
				// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
			}

		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {

			// Evaluate proposals.
			double bestProposal = Double.POSITIVE_INFINITY;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					double proposal = Double.parseDouble(msg.getContent());
					/*
					 * System.out.println("Preço avaliado: "+Double.toString(proposal));
					 * System.out.println("Responses.size: "+Integer.toString(responses.size()));
					 * System.out.println("acceptances.size: "+Integer.toString(acceptances.size()))
					 * ;
					 * 
					 * System.out.println("CFP.envidas: "+getDataStore().get(ALL_CFPS_KEY));
					 * System.out.println("Responses.key: "+getDataStore().get(ALL_RESPONSES_KEY));
					 * System.out.println("acceptances.key: "+getDataStore().get(ALL_ACCEPTANCES_KEY
					 * ));
					 */ if (proposal < bestProposal) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
					}
				}
			}
			// Accept the proposal of the best proposer
			if (accept != null) {
				System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				accept.setContent(Double.toString(ptX) + " " + Double.toString(ptY));
			} else {
				System.out.println("--> AUCTION: WITHOUT CANDIDATES");
				// agentesVeiculosAtivos.clear();
				// addBehaviour(new PedirAutorizacao());
				// r.removerDaRota(ptX);
				// removeBehaviour(this);
				contLeiloesAbertos--;
				System.out
						.println("Leiloes abertos " + myAgent.getName() + ": " + Integer.toString(contLeiloesAbertos));
				if (contLeiloesAbertos == 0) {
					estAtual = estAnterior;
					// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
					addBehaviour(new PedirAutorizacao());
				}
			}
		}

		protected void handleInform(ACLMessage inform) {
			System.out.println("--> AUCTION: FINISHING AUCTION");
			// Tarefa atribuída com sucesso
			// double price = Double.parseDouble(inform.getContent());
			// System.out.println("Negociation finished. "+inform.getContent());

			try {
				// TODO Arrumar
				r.removerDaRota(ptX);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// estAtual = estAnterior;
			contLeiloesAbertos--;
			System.out.println("Leiloes abertos " + myAgent.getName() + ": " + Integer.toString(contLeiloesAbertos));
			if (contLeiloesAbertos == 0) {
				// TODO Arrumar
				/*
				 * if (r.getVisitaAtual() < r.getRota().size() - 2) { estAtual = estAnterior; }
				 * else { estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA; participoDeLeilao
				 * = false; }
				 */

				// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
				addBehaviour(new PedirAutorizacao());
			}
		}

		@Override
		protected void handleFailure(ACLMessage failure) {
			System.out.println("--> AUCTION: FAILED");
			// estAtual = estAnterior;
			// addBehaviour(new PedirAutorizacao());
			super.handleFailure(failure);
			contLeiloesAbertos--;
			System.out.println("Leiloes abertos " + myAgent.getName() + ": " + Integer.toString(contLeiloesAbertos));
			if (contLeiloesAbertos == 0) {
				estAtual = estAnterior;
				// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
				addBehaviour(new PedirAutorizacao());
			}
		}
	}

	protected class ParticiparLeilaoTarefa extends ContractNetResponder {

		public ParticiparLeilaoTarefa(Agent a, MessageTemplate mt) {
			super(a, mt);

		}

		@Override
		protected ACLMessage handleCfp(ACLMessage cfp) {
			ACLMessage reply = cfp.createReply();
			double[] point = Utilidades.fromString(cfp.getContent());
			// Verificar se há tempo disponível para a rota estimada

			if ((participoDeLeilao)
					&& (r.getTempoGasto()
							+ r.estimaTempoRestanteRota(r.getRota().subList(r.getVisitaAtual(), r.getRota().size())) < r
									.getTempoDisponivel())
					&& (estAtual != EstadosVeiculos.STATE_COMUNICACAO) && (estAtual != EstadosVeiculos.STATE_FINALIZADO)
					&& (estAtual != EstadosVeiculos.STATE_DESLOCAMENTO_VOLTA)) {
				// Preparar uma proposta atraves do genético
				double gastoComTarefa = r.estimarInserirNaRota(point[0], point[1]);
				reply.setContent(Double.toString(gastoComTarefa));
				reply.setPerformative(ACLMessage.PROPOSE);
				System.out.println("--> AUCTION: SEND PROPOSE (" + myAgent.getLocalName() + ") "
						+ Double.toString(gastoComTarefa));

				contPropostasAbertas++;
				// System.out.println("Proposta aberta " +a.getName()+ ":
				// "+Integer.toString(contLeiloesAbertos));
				if (estAtual != EstadosVeiculos.STATE_NEGOCIANDO) {
					estAnterior = estAtual;
					// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
				}

			} else {
				// Não preparar proposta e recusar cfp
				reply.setPerformative(ACLMessage.REFUSE);
				System.out.println("--> AUCTION: SEND REFUSE");
			}
			return reply;
		}

		@Override
		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
				throws FailureException {
			// System.out.println("--> AUCTION: SUPIMPS, VEHICLE
			// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+") WIN:
			// "+propose.getContent());
			double[] point = Utilidades.fromString(accept.getContent());
			// System.out.println("Route before insertion (" + getAID().getName() + "):
			// "+r.getRota().subList(r.getVisitaAtual(), r.getRota().size()).toString());
			try {
				r.inserirNaRota(point[0], point[1]);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			contPropostasAbertas--;
			if (contPropostasAbertas == 0) {
				estAtual = estAnterior;
				// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
				addBehaviour(new PedirAutorizacao());
			}

			// addBehaviour(new PedirAutorizacao());

			// System.out.println("Route after insertion (" + getAID().getName() + "):
			// "+r.getRota().subList(r.getVisitaAtual(), r.getRota().size()).toString());
			ACLMessage reply = accept.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			reply.setContent("Done");
			return reply;
		}

		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			// System.out.println("--> AUCTION: PROPOSAL REJECTED");
			contPropostasAbertas--;
			if (contPropostasAbertas == 0) {
				estAtual = estAnterior;
				// estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_ENTRE;
				addBehaviour(new PedirAutorizacao());
			}

		}
	}

	protected class PedirAutorizacao extends OneShotBehaviour {
		public void action() {
			ACLMessage msgPedido = new ACLMessage(ACLMessage.REQUEST);
			msgPedido.setLanguage("SYNC");
			msgPedido.addReceiver(new AID("SyncAgent", AID.ISLOCALNAME));
			if (estAtual != EstadosVeiculos.STATE_FINALIZADO) {
				// TODO Arrumar
				msgPedido.setContent(Double.toString(r.getTempoGasto()));
			} else {
				msgPedido.setContent("999999");
			}
			send(msgPedido);
		}
	}

	protected class Deslocamento extends OneShotBehaviour {
		public void action() {
			try {
				r.deslocamento();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			estAtual = EstadosVeiculos.STATE_ATENDIMENTO;
			addBehaviour(new PedirAutorizacao());

			// myAgent.doWait((long)
			// r.getTemposDeViagem()[r.getVisitaAtual()]*timeFactor+1);
			// System.out.println("AGENT " + getAID().getName() + ") - waiting for:
			// "+Long.toString((long)
			// r.getTemposDeViagem()[r.getVisitaAtual()]*timeFactor+1));
		}
	}

	protected class IniciarOperacao extends CyclicBehaviour {

		// Comportamento que recebe os pontos e que prepara o início da operação
		MessageTemplate templateIniciarOperacao = MessageTemplate.and(MessageTemplate.MatchLanguage("ROTAS"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		public void action() {
			ACLMessage msg = myAgent.receive(templateIniciarOperacao);
			// System.out.println("AGENT " + getAID().getName() + " - Recebi mensagem");
			if ((msg != null) && (msg.getPerformative() == ACLMessage.INFORM)) {
				// Message received. Process it
				// codCiclo++;
				String var = msg.getOntology();
				String cont = msg.getContent();
				if (var == "pontosx") {
					pontoCartesianoSelecionadosX = Utilidades.fromString(cont);
					ptInicialX = pontoCartesianoSelecionadosX;
					// System.out.println("msg-pontosX"+cont);
				} else if (var == "pontosy") {
					pontoCartesianoSelecionadosY = Utilidades.fromString(cont);
					ptInicialY = pontoCartesianoSelecionadosY;
					// System.out.println("msg-pontosY"+cont);
				}

				// Criar a rota e iniciar a operação
				if (pontoCartesianoSelecionadosX != null && pontoCartesianoSelecionadosY != null) {
					// System.out.println("AGENT " + getAID().getName() + ") : recebi a mensagem de
					// "+var);
					// System.out.println("AGENT " + getAID().getName() + ") : recebi pontos"+);
					try {
						// System.out.println("AGENT " + getAID().getName() + " pontosX" +
						// Arrays.toString(pontoCartesianoSelecionadosX));
						// System.out.println("AGENT " + getAID().getName() + " pontosY" +
						// Arrays.toString(pontoCartesianoSelecionadosY));

						// Update informação se veiculos regulares vão participar inicialmente dos
						// leilões
						if (faixaV != 99999)
							participoDeLeilao = true;
						// Update informação se veiculos auxiliares vão participar inicialmente dos
						// leilões
						else
							participoDeLeilao = true;

						contLeiloesAbertos = 0;
						codCiclo++;
						System.out.println("AGENT " + getAID().getName() + ") - ciclo: " + Integer.toString(codCiclo));

						r = (RouteRandom) RouteFactory.getRoute(null, tipoSimulacao, faixaV, faixaH,
								pontoCartesianoSelecionadosX, pontoCartesianoSelecionadosY);
						// Neste ponto, eu poderia já inicializar um leilão para que os veículos
						// auxiliares podessem receber as suas demandas?
						r.iniciaOperacao(codCiclo);
						estAtual = EstadosVeiculos.STATE_DESLOCAMENTO_IDA;
						addBehaviour(new IdaAoDistrito());
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						// System.out.println("AGENT " + getAID().getName() + ") - params
						// ("+Integer.toString(faixaV)+Integer.toString(faixaH)+"): problem DB");
						e.printStackTrace();
					}
					// block();
					catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				block();
			}
		}
	}

	/**
	 * Comportamento do Veiculo Regular deslocar-se ao distrito
	 * 
	 * @author dmontier
	 *
	 */
	protected class IdaAoDistrito extends OneShotBehaviour {
		public void action() {
			try {
				r.chegadaAoDistrito();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			estAtual = EstadosVeiculos.STATE_ATENDIMENTO;
			addBehaviour(new PedirAutorizacao());
			// myAgent.doWait();
			// myAgent.doWait((long)
			// r.getTemposDeViagem()[r.getVisitaAtual()]*timeFactor+1);
			// System.out.println("AGENT " + getAID().getName() + ") - waiting for:
			// "+Long.toString((long)
			// r.getTemposDeViagem()[r.getVisitaAtual()]*timeFactor+timeFactor));
		}
	}

	/**
	 * Comportamento do Veiculo Regular retornar ao depósito
	 * 
	 * @author dmontier
	 *
	 */
	protected class RetornoAoDeposito extends OneShotBehaviour {
		public void action() {
			try {
				r.retornoAoDeposito();
				r.gravaEstatisticasDaRota();
				pontoCartesianoSelecionadosX = null;
				pontoCartesianoSelecionadosY = null;
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			estAtual = EstadosVeiculos.STATE_COMUNICACAO;
			addBehaviour(new PedirAutorizacao());
			// System.out.println("AGENT " + getAID().getName() + ") - waiting for:
			// "+Long.toString((long)
			// r.getTemposDeViagem()[r.getVisitaAtual()]*timeFactor+1));
		}
	}

}
