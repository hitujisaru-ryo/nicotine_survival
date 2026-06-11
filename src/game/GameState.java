package game;

import java.util.ArrayList;

import bean.Product;

public class GameState {

	private static final String INITIAL_MESSAGE = "ニコチン・サバイバル開始";

	private int money = 0;
	private int nicotine = 100;
	private int day = 1;
	private int actionCount = 0;
	private boolean gameOver = false;
	private boolean dayAdvanced = false;
	private int survivedDay = 0;
	private String message = INITIAL_MESSAGE;
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

	public boolean isDayAdvanced() {
		return dayAdvanced;
	}

	public int getSurvivedDay() {
		return survivedDay;
	}

	public void clearDayAdvanced() {
		dayAdvanced = false;
		survivedDay = 0;
	}

	public ArrayList<InventoryItem> getInventory() {
		return inventory;
	}

	public void addMoney(int amount) {
		money += amount;
	}

	public void subtractMoney(int amount) {
		money -= amount;

		if (money < 0) {
			money = 0;
		}
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
		if (isGameFinished()) {
			return;
		}

		dayAdvanced = false;
		survivedDay = 0;
		actionCount++;
		decreaseNicotine(10);

		if (nicotine == 0) {
			gameOver = true;
			message = "ニコチン切れで動けなくなった...";
			return;
		}

		if (actionCount >= 20) {
			survivedDay = day;
			day++;
			actionCount = 0;
			dayAdvanced = true;
		}

		if (isGameClear()) {
			message = "給料日が来た！5日間生き延びた！";
		}
	}

	public boolean isNicotineShortage() {
		return getNicotine() <= 20;
	}

	public boolean isGameClear() {
		return !gameOver && day >= 6;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public boolean isGameFinished() {
		return isGameClear() || isGameOver();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void addInventoryItem(InventoryItem item) {
		inventory.add(item);
	}

	public void addProductToInventory(Product product) {
		for (InventoryItem item : inventory) {
			if (item.getProductId() == product.getId()) {
				item.addRemainingPieces(product.getNumberOfPieces());
				return;
			}
		}

		inventory.add(new InventoryItem(
				product.getId(),
				product.getName(),
				product.getNumberOfPieces(),
				product.getNicotine()));
	}

	public void reset() {
		money = 0;
		nicotine = 100;
		day = 1;
		actionCount = 0;
		gameOver = false;
		dayAdvanced = false;
		survivedDay = 0;
		message = INITIAL_MESSAGE;
		inventory.clear();
	}

	public void quitSmoking() {
		gameOver = true;
		message = "ゲームオーバー";
	}
}
