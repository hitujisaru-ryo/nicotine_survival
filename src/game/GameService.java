package game;

import java.util.ArrayList;
import java.util.Random;

import bean.Product;

public class GameService {

	private Random random = new Random();

	public void explore(GameState gameState, ArrayList<Product> products) {
		gameState.advanceAction();

		if (gameState.isGameClear()) {
			gameState.setMessage("給料日が来た！ゲームクリア！");
			return;
		}

		int roll = random.nextInt(100);

		if (gameState.isNicotineShortage()) {
			exploreWithNicotineShortage(gameState, products, roll);
		} else {
			exploreNormally(gameState, products, roll);
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
				product.getNumberOfPieces()));
		gameState.setMessage("未開封の" + product.getName() + "を見つけた");
	}
}
