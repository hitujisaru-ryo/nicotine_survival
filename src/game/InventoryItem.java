package game;

public class InventoryItem {

	private int productId;
	private String productName;
	private int remainingPieces;

	public InventoryItem(String productName, int remainingPieces) {
		this.productName = productName;
		this.remainingPieces = remainingPieces;
	}

	public InventoryItem(int productId, String productName, int remainingPieces) {
		this.productId = productId;
		this.productName = productName;
		this.remainingPieces = remainingPieces;
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
}
