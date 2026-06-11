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
}
