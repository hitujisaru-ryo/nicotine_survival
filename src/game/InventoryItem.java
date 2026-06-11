package game;

public class InventoryItem {

	private String productName;
	private int remainingPieces;

	public InventoryItem(String productName, int remainingPieces) {
		this.productName = productName;
		this.remainingPieces = remainingPieces;
	}

	public String getProductName() {
		return productName;
	}

	public int getRemainingPieces() {
		return remainingPieces;
	}
}
