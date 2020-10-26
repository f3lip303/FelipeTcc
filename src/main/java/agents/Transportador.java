package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class Transportador extends Agent {

	protected Long timeFactor = (long) 1000; // para cada minuto na simulacao, dar delay de 1000 milisegundos (1
												// segundo)
	protected String tipoSimulacao;
	protected int codCiclo = 0;
	protected int qtdRodadas;
	protected boolean existemRotasAuxiliaresPendentes;

	protected void takeDown() {
		// Exit from DF
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Agent " + getAID().getName() + " killed.");
	}
}
