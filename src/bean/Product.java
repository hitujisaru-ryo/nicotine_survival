package bean;

import java.io.Serializable;

public class Product implements Serializable {

	private int id;
	private String name;
	private int price;
	private int quantity;
	private int numberOfPieces;
	private int nicotine;

	public Product() {
	}

	public Product(int id, String name, int price, int quantity) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
	}

	public Product(int id, String name, int price, int quantity, int numberOfPieces, int nicotine) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.numberOfPieces = numberOfPieces;
		this.nicotine = nicotine;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getPrice() {
		return price;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setNumberOfPieces(int numberOfPieces) {
		this.numberOfPieces = numberOfPieces;
	}

	public int getNumberOfPieces() {
		return numberOfPieces;
	}

	public void setNicotine(int nicotine) {
		this.nicotine = nicotine;
	}

	public int getNicotine() {
		return nicotine;
	}
}
