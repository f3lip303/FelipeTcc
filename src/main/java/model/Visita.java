package model;


public class Visita {

	private int codigoVisita;
	private int x;
	private int y;

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

	@Override
	public String toString() {
				return  "Codigo Visita = "+ getCodigoVisita() + "X = " + getX() + "Y = " + getY();
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
