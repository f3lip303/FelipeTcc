package model;

public enum EnumCostTypeCostMatrix {
	TIME(1), DISTANCE(2), DISTANCE_TIME(3);

	private int type;

	private EnumCostTypeCostMatrix(int type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	public boolean isTime() {
		return this.getType() == this.TIME.getType();
	}

	public boolean isDistance() {
		return this.getType() == this.DISTANCE.getType();
	}

	public boolean isDistanceTime() {
		return this.getType() == this.DISTANCE_TIME.getType();
	}
}
