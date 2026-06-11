package game;

public class InventoryItem {

	private int productId;
	private String productName;
	private int remainingPieces;
	private int nicotine;

	public InventoryItem(String productName, int remainingPieces) {
		this.productName = productName;
		this.remainingPieces = remainingPieces;
	}

	public InventoryItem(int productId, String productName, int remainingPieces) {
		this.productId = productId;
		this.productName = productName;
		this.remainingPieces = remainingPieces;
	}

	public InventoryItem(int productId, String productName, int remainingPieces, int nicotine) {
		this.productId = productId;
		this.productName = productName;
		this.remainingPieces = remainingPieces;
		this.nicotine = nicotine;
	}

	public int getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public int getRemainingPieces() {
		return remainingPieces;
	}

	public int getNicotine() {
		return nicotine;
	}

	public void addRemainingPieces(int pieces) {
		remainingPieces += pieces;
	}

	public void decreaseRemainingPieces(int pieces) {
		remainingPieces -= pieces;

		if (remainingPieces < 0) {
			remainingPieces = 0;
		}
	}
}
