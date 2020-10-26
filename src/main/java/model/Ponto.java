package model;

public class Ponto {

	long id;

	protected double x, y, serviceTime, peso;

	String nome;

	public Ponto(long id, double x, double y, double st, double ps) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.serviceTime = st;
		this.nome = "";
		this.peso = ps;
	}

	public double getPeso() {
		return peso;
	}

	public void setPeso(double peso) {
		this.peso = peso;
	}

	public Ponto(long id, double x, double y, String nm) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.nome = nm;
		this.serviceTime = 0.0;
	}

	public Ponto(long id, double x, double y, double st) {
		this(id, x, y, st, 1);
	}

	public long getId() {
		return id;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public String toString() {
		return "Models.Ponto[ x=" + x + ",  y=" + y + " ]";
	}

	public double[] getPoint() {
		double[] point = { this.x, this.y };
		return point;
	}

	Long getIdLong() {
		Long idl = new Long(id);
		return idl;
	}

	public String getNome() {
		return nome;
	}

	public double getServiceTime() {
		return serviceTime;
	}

}
