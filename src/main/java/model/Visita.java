package model;

public class Visita {

	private int codigoVisita;
	private int x;
	private int y;
	private int codigoCiclo;
	private int codigoSecao;
	private int codigoDistrito;
	private int ordemNaRota;
	private double tempoInicioAtendimento;
	private double tempoFimAtendimento;
	private double tempoDeslocamentoProximaVisita;
	
	/* Construtor padrão */
	public Visita() {
		super();
	}

	/* Construtor com params x e y */
	public Visita(int x, int y) {
		super();
		this.setX(x);
		this.setY(y);
	}

	public int getCodigoVisita() {
		return codigoVisita;
	}

	public void setCodigoVisita(int codigoVisita) {
		this.codigoVisita = codigoVisita;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
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

	public int getOrdemNaRota() {
		return ordemNaRota;
	}

	public void setOrdemNaRota(int ordemNaRota) {
		this.ordemNaRota = ordemNaRota;
	}

	public double getTempoInicioAtendimento() {
		return tempoInicioAtendimento;
	}

	public void setTempoInicioAtendimento(double tempoInicioAtendimento) {
		this.tempoInicioAtendimento = tempoInicioAtendimento;
	}

	public double getTempoFimAtendimento() {
		return tempoFimAtendimento;
	}

	public void setTempoFimAtendimento(double tempoFimAtendimento) {
		this.tempoFimAtendimento = tempoFimAtendimento;
	}


	public double getTempoDeslocamentoProximaVisita() {
		return tempoDeslocamentoProximaVisita;
	}

	public void setTempoDeslocamentoProximaVisita(double tempoDeslocamentoProximaVisita) {
		this.tempoDeslocamentoProximaVisita = tempoDeslocamentoProximaVisita;
	}

	@Override
	public String toString() {
		return "Codigo Visita = " + getCodigoVisita() + "X = " + getX() + "Y = " + getY();
	}
	/*
	 * @Override public String toString(){ return (this.isCD() ? "CD:\n" :
	 * "")+"ID: "+id+"\n"+ "Name: "+name+"\n"+ "X: "+x+" Y: "+y+"\n"+
	 * "Pounds demand: "+this.getPoundsDemand()+"\n"+
	 * "Down time: "+this.getDownTime()+"\n"+
	 * "Arrival hour: "+this.getArrivalHour()+"\n"+
	 * "Exit hour: "+this.getExitHour()+"\n"+
	 * "Current kilometer: "+this.getCurrentKilometer(); }
	 */

	public int compareTo(Visita arg0) {
		int id = arg0.codigoVisita;
		if (this.getCodigoVisita() < id)
			return -1;
		return this.codigoVisita > id ? 1 : 0;
	}
}
