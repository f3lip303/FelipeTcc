package model;

public class Evento {

	private int codigoSimulacao;
	private int codigoCiclo;
	private int codigoSecao;
	private int codigoDistrito;
	private double duracao;
	private double x;
	private double y;
	private String tipoEvento;
	private double instante;

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

	public double getDuracao() {
		return duracao;
	}

	public void setDuracao(double duracao) {
		this.duracao = duracao;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getTipoEvento() {
		return tipoEvento;
	}

	public void setTipoEvento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}

	public double getInstante() {
		return instante;
	}

	public void setInstante(double instante) {
		this.instante = instante;
	}

}
