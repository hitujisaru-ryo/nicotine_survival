package game;

import java.util.ArrayList;

public class GameState {

	private int money = 0;
	private int nicotine = 100;
	private int day = 1;
	private int actionCount = 0;
	private String message = "ゲーム開始";
	private ArrayList<InventoryItem> inventory = new ArrayList<InventoryItem>();

	public int getMoney() {
		return money;
	}

	public int getNicotine() {
		if (nicotine < 0) {
			return 0;
		}

		if (nicotine > 100) {
			return 100;
		}

		return nicotine;
	}

	public int getDay() {
		return day;
	}

	public int getActionCount() {
		return actionCount;
	}

	public String getMessage() {
		return message;
	}

	public ArrayList<InventoryItem> getInventory() {
		return inventory;
	}

	public void addMoney(int amount) {
		money += amount;
	}

	public void increaseNicotine(int amount) {
		nicotine += amount;

		if (nicotine > 100) {
			nicotine = 100;
		}
	}

	public void decreaseNicotine(int amount) {
		nicotine -= amount;

		if (nicotine < 0) {
			nicotine = 0;
		}
	}

	public void advanceAction() {
		actionCount++;
		decreaseNicotine(5);

		if (actionCount >= 20) {
			day++;
			actionCount = 0;
		}
	}

	public boolean isNicotineShortage() {
		return getNicotine() <= 10;
	}

	public boolean isGameClear() {
		return day >= 6;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void addInventoryItem(InventoryItem item) {
		inventory.add(item);
	}
}
