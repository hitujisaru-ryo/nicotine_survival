package game;

import java.util.ArrayList;
import java.util.Random;

import bean.Product;

public class GameService {

	private Random random = new Random();

	public void explore(GameState gameState, ArrayList<Product> products) {
		if (gameState.isGameFinished()) {
			return;
		}

		gameState.advanceAction();

		if (gameState.isGameFinished()) {
			return;
		}

		int roll = random.nextInt(100);

		if (gameState.isNicotineShortage()) {
			exploreWithNicotineShortage(gameState, products, roll);
		} else {
			exploreNormally(gameState, products, roll);
		}
	}

	public void pachinko(GameState gameState) {
		if (gameState.isGameFinished()) {
			return;
		}

		if (gameState.getMoney() < 100) {
			gameState.setMessage("お金が足りない");
			return;
		}

		gameState.advanceAction();

		if (gameState.isGameFinished()) {
			return;
		}

		int roll = random.nextInt(100);

		if (gameState.isNicotineShortage()) {
			pachinkoWithNicotineShortage(gameState, roll);
		} else {
			pachinkoNormally(gameState, roll);
		}
	}

	public void smoke(GameState gameState, int productId) {
		if (gameState.isGameFinished()) {
			return;
		}

		InventoryItem targetItem = null;

		for (InventoryItem item : gameState.getInventory()) {
			if (item.getProductId() == productId) {
				targetItem = item;
				break;
			}
		}

		if (targetItem == null || targetItem.getRemainingPieces() <= 0) {
			gameState.setMessage("吸えるたばこがありません");
			return;
		}

		int smokedPieces = Math.min(5, targetItem.getRemainingPieces());
		targetItem.decreaseRemainingPieces(smokedPieces);
		gameState.increaseNicotine(targetItem.getNicotine());
		gameState.advanceAction();
		boolean empty = targetItem.getRemainingPieces() == 0;

		if (empty) {
			gameState.getInventory().remove(targetItem);
		}

		if (gameState.isGameFinished()) {
			return;
		}

		if (empty) {
			gameState.setMessage(targetItem.getProductName() + "を" + smokedPieces + "本吸った。箱が空になった");
		} else {
			gameState.setMessage(targetItem.getProductName() + "を" + smokedPieces + "本吸った。ニコチンが回復した");
		}
	}

	private void pachinkoNormally(GameState gameState, int roll) {
		if (roll < 50) {
			gameState.subtractMoney(100);
			gameState.setMessage("パチンコで負けた。100円失った");
		} else if (roll < 90) {
			gameState.addMoney(300);
			gameState.setMessage("パチンコで勝った！300円増えた");
		} else if (roll < 97) {
			gameState.addMoney(1000);
			gameState.setMessage("パチンコで大当たり！1000円増えた");
		} else {
			gameState.addMoney(3000);
			gameState.setMessage("激アツ！3000円増えた");
		}
	}

	private void pachinkoWithNicotineShortage(GameState gameState, int roll) {
		if (roll < 75) {
			gameState.subtractMoney(100);
			gameState.setMessage("ニコチン切れ...パチンコで負けた。100円失った");
		} else if (roll < 95) {
			gameState.addMoney(300);
			gameState.setMessage("ニコチン切れ...それでもパチンコで勝った！300円増えた");
		} else if (roll < 99) {
			gameState.addMoney(1000);
			gameState.setMessage("ニコチン切れ...パチンコで大当たり！1000円増えた");
		} else {
			gameState.addMoney(3000);
			gameState.setMessage("ニコチン切れでも激アツ！3000円増えた");
		}
	}

	private void exploreNormally(GameState gameState, ArrayList<Product> products, int roll) {
		if (roll < 20) {
			gameState.setMessage("何も見つからなかった");
		} else if (roll < 70) {
			gameState.addMoney(100);
			gameState.setMessage("100円を見つけた");
		} else if (roll < 95) {
			gameState.increaseNicotine(5);
			gameState.setMessage("吸い殻を見つけた。ニコチンが少し回復した");
		} else {
			addRandomProductToInventory(gameState, products);
		}
	}

	private void exploreWithNicotineShortage(GameState gameState, ArrayList<Product> products, int roll) {
		if (roll < 40) {
			gameState.setMessage("ニコチン切れ...何も見つからなかった");
		} else if (roll < 75) {
			gameState.addMoney(100);
			gameState.setMessage("ニコチン切れ...それでも100円を見つけた");
		} else if (roll < 95) {
			gameState.increaseNicotine(5);
			gameState.setMessage("吸い殻を見つけた。ニコチンが少し回復した");
		} else {
			addRandomProductToInventory(gameState, products);
		}
	}

	private void addRandomProductToInventory(GameState gameState, ArrayList<Product> products) {
		if (products.isEmpty()) {
			gameState.setMessage("未開封を探したが、何も見つからなかった");
			return;
		}

		Product product = products.get(random.nextInt(products.size()));
		gameState.addInventoryItem(new InventoryItem(
				product.getId(),
				product.getName(),
				product.getNumberOfPieces(),
				product.getNicotine()));
		gameState.setMessage("未開封の" + product.getName() + "を見つけた");
	}
}
