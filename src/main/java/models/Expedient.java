package models;

public class Expedient {	
	private double begin,
					end;
	
	public Expedient() {
		super();
	}

	public Expedient(double begin, double end) {
		super();
		this.begin = begin;
		this.end = end;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {		
		return new Expedient(getBegin(), getEnd());
	}
	
	@Override
	public String toString (){
		return "Begin: "+begin+"\nEnd: "+end;
	}
	
	public double getBegin() {
		return begin;
	}

	public void setBegin(double begin) {
		this.begin = begin;
	}

	public double getEnd() {
		return end;
	}

	public void setEnd(double end) {
		this.end = end;
	}
	
}
