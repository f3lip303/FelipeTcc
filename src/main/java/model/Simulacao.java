package model;

public class Simulacao {

	private int codigoSimulacao;
	private int numCiclos;
	private int numVeiculosProprios;
	private int numVeiculosAuxiliares;
	private double distanciaMediaViajadaVeicProp;
	private double distanciaMediaViajadaVeicAux;
	private double tempoMedioCiclosVeicProp;
	private double tempoMedioCiclosVeicAux;
	private int numMedioVisitasEstimadasVeicProp;
	private int numMedioVisitasRealizadasVeicProp;
	private int numMedioVisitasRealizadasVeicAux;
	private double percentualMediaVisitasNaoAtendidas;
	private double tempoMedioOciosoVeicAux;

	public int getCodigoSimulacao() {
		return codigoSimulacao;
	}

	public void setCodigoSimulacao(int codigoSimulacao) {
		this.codigoSimulacao = codigoSimulacao;
	}

	public int getNumCiclos() {
		return numCiclos;
	}

	public void setNumCiclos(int numCiclos) {
		this.numCiclos = numCiclos;
	}

	public int getNumVeiculosProprios() {
		return numVeiculosProprios;
	}

	public void setNumVeiculosProprios(int numVeiculosProprios) {
		this.numVeiculosProprios = numVeiculosProprios;
	}

	public int getNumVeiculosAuxiliares() {
		return numVeiculosAuxiliares;
	}

	public void setNumVeiculosAuxiliares(int numVeiculosAuxiliares) {
		this.numVeiculosAuxiliares = numVeiculosAuxiliares;
	}

	public double getDistanciaMediaViajadaVeicProp() {
		return distanciaMediaViajadaVeicProp;
	}

	public void setDistanciaMediaViajadaVeicProp(double distanciaMediaViajadaVeicProp) {
		this.distanciaMediaViajadaVeicProp = distanciaMediaViajadaVeicProp;
	}

	public double getDistanciaMediaViajadaVeicAux() {
		return distanciaMediaViajadaVeicAux;
	}

	public void setDistanciaMediaViajadaVeicAux(double distanciaMediaViajadaVeicAux) {
		this.distanciaMediaViajadaVeicAux = distanciaMediaViajadaVeicAux;
	}

	public double getTempoMedioCiclosVeicProp() {
		return tempoMedioCiclosVeicProp;
	}

	public void setTempoMedioCiclosVeicProp(double tempoMedioCiclosVeicProp) {
		this.tempoMedioCiclosVeicProp = tempoMedioCiclosVeicProp;
	}

	public double getTempoMedioCiclosVeicAux() {
		return tempoMedioCiclosVeicAux;
	}

	public void setTempoMedioCiclosVeicAux(double tempoMedioCiclosVeicAux) {
		this.tempoMedioCiclosVeicAux = tempoMedioCiclosVeicAux;
	}

	public int getNumMedioVisitasEstimadasVeicProp() {
		return numMedioVisitasEstimadasVeicProp;
	}

	public void setNumMedioVisitasEstimadasVeicProp(int numMedioVisitasEstimadasVeicProp) {
		this.numMedioVisitasEstimadasVeicProp = numMedioVisitasEstimadasVeicProp;
	}

	public int getNumMedioVisitasRealizadasVeicProp() {
		return numMedioVisitasRealizadasVeicProp;
	}

	public void setNumMedioVisitasRealizadasVeicProp(int numMedioVisitasRealizadasVeicProp) {
		this.numMedioVisitasRealizadasVeicProp = numMedioVisitasRealizadasVeicProp;
	}

	public int getNumMedioVisitasRealizadasVeicAux() {
		return numMedioVisitasRealizadasVeicAux;
	}

	public void setNumMedioVisitasRealizadasVeicAux(int numMedioVisitasRealizadasVeicAux) {
		this.numMedioVisitasRealizadasVeicAux = numMedioVisitasRealizadasVeicAux;
	}

	public double getPercentualMediaVisitasNaoAtendidas() {
		return percentualMediaVisitasNaoAtendidas;
	}

	public void setPercentualMediaVisitasNaoAtendidas(double percentualMediaVisitasNaoAtendidas) {
		this.percentualMediaVisitasNaoAtendidas = percentualMediaVisitasNaoAtendidas;
	}

	public double getTempoMedioOciosoVeicAux() {
		return tempoMedioOciosoVeicAux;
	}

	public void setTempoMedioOciosoVeicAux(double tempoMedioOciosoVeicAux) {
		this.tempoMedioOciosoVeicAux = tempoMedioOciosoVeicAux;
	}

}
