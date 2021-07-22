package optmizations;

import java.util.List;

import model.CostMatrix;
import model.CustomerAdaptaded;
import model.Visita;
import utils.MatrizDistancia;

public class Optimizer {
	protected CostMatrix costMatrix;
	protected boolean changed;
	protected int size;
	
	protected void exchange(int a, int b, CustomerAdaptaded[] optimizedRoute){
		CustomerAdaptaded customerA = optimizedRoute[a];		
		optimizedRoute[a] = optimizedRoute[b];
		optimizedRoute[b] = customerA;
	}
	
	public CostMatrix getCostMatrix() {
		return costMatrix;
	}
	public void setCostMatrix(CostMatrix costMatrix) {
		this.costMatrix = costMatrix;
	}
	public boolean isChanged() {
		return changed;
	}
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}	
}
