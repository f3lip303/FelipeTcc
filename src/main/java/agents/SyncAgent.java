package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Utilidades;

public class SyncAgent extends Agent {

	HashMap<String, Double> listaEventos;
	HashMap<String, Double> listaEventosCopia;

	boolean processar = false;
	int contVeiculos, nrVeiculosInicio;

	protected void setup() {

		System.out.println("AGENT " + getLocalName() + " started.");
		listaEventos = new HashMap<String, Double>();

		addBehaviour(new RecebeEvento());
		// addBehaviour(new EsvaziaLista(this,200));
	}

	// Comportamento que adiciona eventos numa lista de eventos
	private class RecebeEvento extends CyclicBehaviour {

		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchLanguage("SYNC"),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

		@Override
		public void action() {

			ACLMessage msg = myAgent.receive(template);
			if ((msg != null) && (msg.getPerformative() == ACLMessage.REQUEST)) {
				// System.out.println("TOKEN RECEBIDO: contador de veiculos:
				// "+Integer.toString(contVeiculos)+" lista de eventos:
				// "+Integer.toString(listaEventos.size())+"
				// sender:"+msg.getSender().getName());
				// Atualiza número de veículos registrados
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd2 = new ServiceDescription();
				sd2.setType("VeiculoRegular");
				ServiceDescription sd3 = new ServiceDescription();
				sd3.setType("VeiculoAuxiliar");
				try {
					template.addServices(sd2);
					DFAgentDescription[] result = DFService.search(myAgent, template);
					contVeiculos = result.length;
					// System.out.println(myAgent.getLocalName()+": Ehhhh, nao morri. Encontrei
					// "+result.length+ " agentes.");
					template.clearAllServices();
					template.addServices(sd3);
					result = DFService.search(myAgent, template);
					contVeiculos += result.length;
					// System.out.println(myAgent.getLocalName()+": Ehhhh, nao morri. Encontrei
					// "+result.length+ " agentes.");
					// System.out.println(": Ok, temos: "+Integer.toString(contVeiculos)+ "
					// veiculos.");
				} catch (FIPAException fe) {
					System.out.println(myAgent.getLocalName() + ": Hiii, morri.");
					fe.printStackTrace();
				}

				// Atualizar lista
				listaEventos.put(msg.getSender().getLocalName(), Double.valueOf(msg.getContent()));
				// Ordenar lista
				listaEventos = Utilidades.sortHashMapByValues2(listaEventos);
				// System.out.println(">>> LISTA de eventos ordenada: " +
				// listaEventos.toString());

				// Verificar número de eventos cadastrados na lista
				if (listaEventos.size() >= contVeiculos) {
					processar = true;
					nrVeiculosInicio = contVeiculos;
				}

				// Capturar erro e parar operação para verificar log
				if (nrVeiculosInicio != contVeiculos && nrVeiculosInicio != 0) {
					myAgent.doDelete();
				}

				// dispara evento mais recente
				if (processar && listaEventos.size() >= nrVeiculosInicio) {
					addBehaviour(new AutorizaEvento());
				} else {
					// System.out.println("contador de veiculos: "+Integer.toString(contVeiculos)+"
					// lista de eventos: "+Integer.toString(listaEventos.size())+"
					// sender:"+msg.getSender().getName());
					// System.out.println("Lista eventos: "+listaEventos.toString());
				}
			} else {
				block();
			}
		}
	}

	// Comportamento que autoriza um agente a realizar uma determinada tarefa
	private class AutorizaEvento extends OneShotBehaviour {

		double tempo;
		String agente;

		@Override
		public void action() {
			if (listaEventos.size() > 0) {
				// Envio da mensagem para o agente com o evento registrado mais cedo
				List<String> mapKeys = new ArrayList<String>(listaEventos.keySet());
				List<Double> mapValues = new ArrayList<Double>(listaEventos.values());
				agente = mapKeys.get(0);
				tempo = mapValues.get(0);

				listaEventos.remove(mapKeys.get(0));
				// System.out.println("Lista eventos: "+listaEventos.toString());
				ACLMessage msgAutorizacao = new ACLMessage(ACLMessage.INFORM_REF);
				msgAutorizacao.setLanguage("SYNC");
				msgAutorizacao.addReceiver(new AID(agente, AID.ISLOCALNAME));
				msgAutorizacao.setContent(Double.toString(tempo));
				send(msgAutorizacao);

				// Processar mensagem do transportador e limpar todas as autorizações
				if ((tempo == 99999) && (listaEventos.size() > 10)) {
					// System.out.println("Lista eventos: "+listaEventos.toString());
					listaEventos.clear();
					processar = false;
				}
				// System.out.println(myAgent.getLocalName()+": Evento autorizado para:
				// "+mapKeys.get(0));
			}
		}
	}

	/*
	 * private class EsvaziaLista extends TickerBehaviour{
	 * 
	 * public EsvaziaLista(Agent a, long period) { super(a, period); // TODO
	 * Auto-generated constructor stub }
	 * 
	 * @Override protected void onTick() {
	 * //System.out.println(myAgent.getLocalName()+": Tick...");
	 * 
	 * if (listaEventos.size()>0){ if (listaEventos.equals(listaEventosCopia)){
	 * processar = false; addBehaviour(new AutorizaEvento()); } else {
	 * listaEventosCopia = new HashMap<String,Double>(listaEventos); } } else {
	 * myAgent.doWait(); }
	 * 
	 * // Capturar erro e parar operação para verificar log if
	 * (contVeiculos!=nrVeiculosInicio && processar){ myAgent.doDelete(); }
	 * 
	 * // if (listaEventos.size()>0){ // if
	 * (listaEventos.equals(listaEventosCopia)){ // for (int i=0;
	 * i<listaEventos.size(); i++){ // addBehaviour(new AutorizaEvento()); //
	 * myAgent.doWait(500); // } // } // } }
	 * 
	 * }
	 */
	/*
	 * // Comportamento que adiciona eventos numa lista de eventos private class
	 * RecebeEvento extends CyclicBehaviour{
	 * 
	 * 
	 * MessageTemplate template = MessageTemplate.and(
	 * MessageTemplate.MatchLanguage("SYNC"),
	 * MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
	 * 
	 * @Override public void action() {
	 * 
	 * ACLMessage msg = myAgent.receive(template); if ((msg !=
	 * null)&&(msg.getPerformative()==ACLMessage.REQUEST)){ // Atualizar lista
	 * listaEventos.put(msg.getSender().getLocalName(),
	 * Double.valueOf(msg.getContent())); listaEventos =
	 * Utilidades.sortHashMapByValues2(listaEventos); } else { block(); } } }
	 * 
	 * private class EsvaziaLista extends TickerBehaviour{
	 * 
	 * public EsvaziaLista(Agent a, long period) { super(a, period); // TODO
	 * Auto-generated constructor stub }
	 * 
	 * @Override protected void onTick() { // Atualiza número de veículos
	 * registrados DFAgentDescription template = new DFAgentDescription();
	 * ServiceDescription sd2 = new ServiceDescription();
	 * sd2.setType("VeiculoRegular"); ServiceDescription sd3 = new
	 * ServiceDescription(); sd3.setType("VeiculoAuxiliar"); try {
	 * template.addServices(sd2); DFAgentDescription[] result =
	 * DFService.search(myAgent, template); contVeiculos = result.length;
	 * //System.out.println(myAgent.getLocalName()+": Ehhhh, nao morri. Encontrei "
	 * +result.length+ " agentes."); template.clearAllServices();
	 * template.addServices(sd3); result = DFService.search(myAgent, template);
	 * contVeiculos += result.length;
	 * //System.out.println(myAgent.getLocalName()+": Ehhhh, nao morri. Encontrei "
	 * +result.length+ " agentes.");
	 * //System.out.println(": Ok, temos: "+Integer.toString(contVeiculos)+
	 * " veiculos."); } catch (FIPAException fe) {
	 * System.out.println(myAgent.getLocalName()+": Hiii, morri.");
	 * fe.printStackTrace(); }
	 * 
	 * // Capturar erro e parar operação para verificar log if
	 * (nrVeiculosInicio==0){ nrVeiculosInicio = contVeiculos; } if
	 * (contVeiculos!=nrVeiculosInicio){ myAgent.doDelete(); }
	 * 
	 * //System.out.println(myAgent.getLocalName()+": Tick..."); if
	 * (listaEventos.size()>0){ SequentialBehaviour sb = new SequentialBehaviour();
	 * for (int i=0; i<listaEventos.size(); i++){ sb.addSubBehaviour(new
	 * AutorizaEvento()); } addBehaviour(sb); listaEventos.clear(); } else {
	 * myAgent.doWait(); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * // Comportamento que autoriza um agente a realizar uma determinada tarefa
	 * private class AutorizaEvento extends OneShotBehaviour {
	 * 
	 * @Override public void action() { if (listaEventos.size()>0){ // Envio da
	 * mensagem para o agente com o evento registrado mais cedo List<String> mapKeys
	 * = new ArrayList<String>(listaEventos.keySet()); List<Double> mapValues = new
	 * ArrayList<Double>(listaEventos.values()); tempoGlobal = mapValues.get(0);
	 * System.out.println("Lista eventos: "+listaEventos.toString()); ACLMessage
	 * msgAutorizacao = new ACLMessage(ACLMessage.AGREE);
	 * msgAutorizacao.setLanguage("SYNC"); msgAutorizacao.addReceiver(new
	 * AID(mapKeys.get(0), AID.ISLOCALNAME));
	 * msgAutorizacao.setContent(Double.toString(tempoGlobal));
	 * send(msgAutorizacao);
	 * //System.out.println(myAgent.getLocalName()+": Evento autorizado para: "
	 * +mapKeys.get(0)); listaEventos.remove(mapKeys.get(0)); } else { block(); } }
	 * }
	 */
}
