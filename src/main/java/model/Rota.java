package model;

public class Rota {

	private int codigoSimulacao;
	private int codigoCiclo;
	private int codigoSecao;
	private int codigoDistrito;
	private double distanciaViajada;
	private double tempoCiclo;
	private int totalVisitasPlan;
	private int totalVisitasExec;

	public int getCodigoSimulacao() {
		return codigoSimulacao;
	}

	public void setCodigoSimulacao(int codigoSimulacao) {
		this.codigoSimulacao = codigoSimulacao;
	}

	public int getCodigoCiclo() {
		return codigoCiclo;
	}

	public void setCodigoCiclo(int codigoCiclo) {
		this.codigoCiclo = codigoCiclo;
	}

	public int getCodigoSecao() {
		return codigoSecao;
	}

	public void setCodigoSecao(int codigoSecao) {
		this.codigoSecao = codigoSecao;
	}

	public int getCodigoDistrito() {
		return codigoDistrito;
	}

	public void setCodigoDistrito(int codigoDistrito) {
		this.codigoDistrito = codigoDistrito;
	}

	public double getDistanciaViajada() {
		return distanciaViajada;
	}

	public void setDistanciaViajada(double distanciaViajada) {
		this.distanciaViajada = distanciaViajada;
	}

	public double getTempoCiclo() {
		return tempoCiclo;
	}

	public void setTempoCiclo(double tempoCiclo) {
		this.tempoCiclo = tempoCiclo;
	}

	public int getTotalVisitasPlan() {
		return totalVisitasPlan;
	}

	public void setTotalVisitasPlan(int totalVisitasPlan) {
		this.totalVisitasPlan = totalVisitasPlan;
	}

	public int getTotalVisitasExec() {
		return totalVisitasExec;
	}

	public void setTotalVisitasExec(int totalVisitasExec) {
		this.totalVisitasExec = totalVisitasExec;
	}
}
