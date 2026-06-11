package web;

import java.util.ArrayList;
import java.util.Set;

import bean.Product;
import game.GameState;
import game.InventoryItem;

public class VendingMachinePage {

	public static String createAgeCheckPage() {
		StringBuilder html = new StringBuilder();

		appendPageStart(html, "年齢確認");
		html.append("<main class=\"age-panel\">");
		html.append("<h1>20歳以上ですか？</h1>");
		html.append("<div class=\"age-actions\">");
		html.append("<form method=\"post\" action=\"/age-check\">");
		html.append("<input type=\"hidden\" name=\"answer\" value=\"yes\">");
		html.append("<button class=\"age-button primary\" type=\"submit\">はい</button>");
		html.append("</form>");
		html.append("<form method=\"post\" action=\"/age-check\">");
		html.append("<input type=\"hidden\" name=\"answer\" value=\"no\">");
		html.append("<button class=\"age-button\" type=\"submit\">いいえ</button>");
		html.append("</form>");
		html.append("</div>");
		html.append("</main>");
		appendPageEnd(html);

		return html.toString();
	}

	public static String createAgeDeniedPage() {
		StringBuilder html = new StringBuilder();

		appendPageStart(html, "購入できません");
		html.append("<main class=\"age-panel\">");
		html.append("<h1>購入できません</h1>");
		html.append("</main>");
		appendPageEnd(html);

		return html.toString();
	}

	public static String createProductListPage(
			ArrayList<Product> products,
			Integer currentMoney,
			int insertedMoney,
			String message,
			Set<Integer> productIdsWithImage,
			ArrayList<String> purchasedProducts,
			GameState gameState,
			boolean showPachinkoResult,
			boolean showExploreResult,
			boolean showSmokeResult,
			boolean showPurchaseResult,
			Integer statusStartMoney,
			Integer statusStartNicotine) {
		StringBuilder html = new StringBuilder();

		appendPageStart(html, "ニコチン・サバイバル");
		html.append("<main class=\"game-shell\">");
		appendGameStatus(html, gameState, productIdsWithImage, statusStartMoney, statusStartNicotine);
		html.append("<section class=\"bottom-area\">");
		appendActionArea(html, gameState);
		appendGameScreen(html, gameState, message, showPachinkoResult, showExploreResult, showSmokeResult, showPurchaseResult,
				statusStartMoney, statusStartNicotine);
		html.append("<aside class=\"inventory-area\">");
		appendInventory(html, gameState, productIdsWithImage);
		html.append("</aside>");
		html.append("</section>");
		html.append("</main>");
		appendVendingModal(html, products, productIdsWithImage, gameState);
		appendPageEnd(html);

		return html.toString();
	}

	private static void appendVendingModal(
			StringBuilder html,
			ArrayList<Product> products,
			Set<Integer> productIdsWithImage,
			GameState gameState) {
		html.append("<div id=\"vendingModal\" class=\"modal-overlay\">");
		html.append("<section class=\"vending-modal\">");
		html.append("<div class=\"modal-header\">");
		html.append("<h2>自販機</h2>");
		html.append("<button class=\"close-button\" type=\"button\" onclick=\"closeVendingModal()\">閉じる</button>");
		html.append("</div>");
		html.append("<div class=\"product-grid\">");

		for (Product product : products) {
			boolean gameFinished = gameState.isGameFinished();
			boolean soldOut = product.getQuantity() == 0;
			boolean shortage = gameState.getMoney() < product.getPrice();

			if (soldOut) {
				html.append("<section class=\"product-card sold-out\">");
			} else {
				html.append("<section class=\"product-card\">");
			}

			html.append("<div class=\"image-box\">");

			if (productIdsWithImage.contains(product.getId())) {
				html.append("<img src=\"/images/product_").append(product.getId()).append(".png\" alt=\"")
						.append(escapeHtml(product.getName())).append("\">");
			} else {
				html.append("No Image");
			}

			html.append("</div>");
			html.append("<div class=\"body\">");
			html.append("<p class=\"product-name\">").append(escapeHtml(product.getName())).append("</p>");
			html.append("<div class=\"product-info\"><span class=\"label\">単価</span><span>")
					.append(product.getPrice()).append("円</span></div>");
			html.append("<div class=\"product-info\"><span class=\"label\">数量</span><span>")
					.append(product.getQuantity()).append("</span></div>");
			html.append("<div class=\"product-info\"><span class=\"label\">ニコチン回復</span><span>")
					.append(product.getNicotine()).append("</span></div>");

			if (gameFinished) {
				html.append("<span class=\"finished-label\">ゲーム終了</span>");
				html.append("<button class=\"purchase-button\" type=\"button\" disabled>購入できません</button>");
			} else if (soldOut) {
				html.append("<span class=\"sold-out-label\">売り切れ</span>");
				html.append("<button class=\"purchase-button\" type=\"button\" disabled>購入できません</button>");
			} else if (shortage) {
				html.append("<span class=\"shortage-label\">所持金不足</span>");
				html.append("<button class=\"purchase-button\" type=\"button\" disabled>購入できません</button>");
			} else {
				html.append("<form class=\"purchase-form\" method=\"post\" action=\"/purchase\">");
				appendAgeConfirmedInput(html);
				html.append("<input type=\"hidden\" name=\"productId\" value=\"").append(product.getId()).append("\">");
				html.append("<button class=\"purchase-button\" type=\"submit\">購入</button>");
				html.append("</form>");
			}

			html.append("</div>");
			html.append("</section>");
		}

		html.append("</div>");
		html.append("</section>");
		html.append("</div>");
	}

	private static void appendPachinkoResultModal(StringBuilder html, GameState gameState, boolean showPachinkoResult) {
		if (!showPachinkoResult) {
			return;
		}

		String resultClass = getPachinkoResultClass(gameState.getMessage());
		String title = getPachinkoResultTitle(gameState.getMessage());
		String resultMessage = getPachinkoResultMessage(gameState.getMessage());

		html.append("<div id=\"pachinkoResultModal\" class=\"modal-overlay open\">");
		html.append("<section class=\"pachinko-result ").append(resultClass).append("\">");
		html.append("<h2>").append(escapeHtml(title)).append("</h2>");
		html.append("<p>").append(escapeHtml(resultMessage)).append("</p>");
		html.append("<button class=\"close-button\" type=\"button\" onclick=\"closePachinkoResultModal()\">閉じる</button>");
		html.append("</section>");
		html.append("</div>");
	}

	private static String getPachinkoResultClass(String message) {
		if (message == null) {
			return "pachinko-lose";
		}

		if (message.contains("お金が足りない")) {
			return "pachinko-disabled";
		}

		if (message.contains("3000円") || message.contains("激アツ")) {
			return "pachinko-hot";
		}

		if (message.contains("1000円")) {
			return "pachinko-jackpot";
		}

		if (message.contains("300円")) {
			return "pachinko-win";
		}

		return "pachinko-lose";
	}

	private static String getPachinkoResultTitle(String message) {
		String resultClass = getPachinkoResultClass(message);

		if ("pachinko-disabled".equals(resultClass)) {
			return "プレイ不可";
		}

		if ("pachinko-hot".equals(resultClass)) {
			return "激アツ！！！";
		}

		if ("pachinko-jackpot".equals(resultClass)) {
			return "大当たり！！";
		}

		if ("pachinko-win".equals(resultClass)) {
			return "当たり！";
		}

		return "ハズレ...";
	}

	private static String getPachinkoResultMessage(String message) {
		String resultClass = getPachinkoResultClass(message);

		if ("pachinko-disabled".equals(resultClass)) {
			return "お金が足りない";
		}

		if ("pachinko-hot".equals(resultClass)) {
			return "激アツ！！3000円儲かった！";
		}

		if ("pachinko-jackpot".equals(resultClass)) {
			return "大当たり！1000円儲かった！";
		}

		if ("pachinko-win".equals(resultClass)) {
			return "300円儲かった！";
		}

		return "100円失った";
	}

	private static String getPachinkoResultImage(String message) {
		String resultClass = getPachinkoResultClass(message);

		if ("pachinko-hot".equals(resultClass)) {
			return "get3000yen.png";
		}

		if ("pachinko-jackpot".equals(resultClass)) {
			return "get1000yen.png";
		}

		if ("pachinko-win".equals(resultClass)) {
			return "get300yen.png";
		}

		return "lost100yen.png";
	}

	private static void appendGameStatus(
			StringBuilder html,
			GameState gameState,
			Set<Integer> productIdsWithImage,
			Integer statusStartMoney,
			Integer statusStartNicotine) {
		int displayMoney = statusStartMoney != null ? statusStartMoney : gameState.getMoney();
		int displayNicotine = statusStartNicotine != null ? statusStartNicotine : gameState.getNicotine();

		html.append("<section class=\"game-status\">");
		html.append("<h1>ニコチン・サバイバル</h1>");
		html.append("<div class=\"status-grid\">");
		html.append("<div class=\"status-card\"><span class=\"status-label\">所持金</span><strong id=\"moneyStatus\">")
				.append(displayMoney).append("円</strong></div>");
		html.append("<div class=\"status-card nicotine-status\"><span class=\"status-label\">ニコチンメーター</span><strong id=\"nicotineStatus\">")
				.append(displayNicotine).append("/100</strong>");
		html.append("<div class=\"nicotine-bar\"><span id=\"nicotineBar\" style=\"width:")
				.append(displayNicotine).append("%;\"></span></div></div>");
		html.append("<div class=\"status-card\"><span class=\"status-label\">進行</span><strong>")
				.append(gameState.getDay()).append("日目 / ")
				.append(gameState.getActionCount()).append("回</strong></div>");
		html.append("</div>");
		if (gameState.isGameFinished()) {
			html.append("<p class=\"finished-message\">ゲーム終了</p>");
		}
		html.append("</section>");
	}

	private static void appendGameScreen(
			StringBuilder html,
			GameState gameState,
			String message,
			boolean showPachinkoResult,
			boolean showExploreResult,
			boolean showSmokeResult,
			boolean showPurchaseResult,
			Integer statusStartMoney,
			Integer statusStartNicotine) {
		html.append("<section class=\"game-screen\">");
		html.append("<div class=\"screen-frame\">");
		html.append("<div class=\"pixel-window\">");
		if (showExploreResult && !gameState.isGameFinished()) {
			html.append("<img id=\"gameImage\" class=\"game-image\" src=\"/images/search.png\" alt=\"探索中\">");
		} else if (showPachinkoResult && !gameState.isGameFinished()) {
			html.append("<img id=\"gameImage\" class=\"game-image\" src=\"/images/goPatinko.png\" alt=\"パチンコへ行く\">");
		} else if (showSmokeResult && !gameState.isGameFinished()) {
			html.append("<img id=\"gameImage\" class=\"game-image\" src=\"/images/smoking.png\" alt=\"喫煙中\">");
		} else if (gameState.isDayAdvanced()) {
			html.append("<img id=\"gameImage\" class=\"game-image\" src=\"/images/goHome.png\" alt=\"帰宅中\">");
		} else if (gameState.isGameOver()) {
			html.append("<img class=\"game-image\" src=\"/images/gameOver.png\" alt=\"ゲームオーバー\">");
		} else {
			html.append("<img id=\"gameImage\" class=\"game-image\" src=\"/images/start.png\" alt=\"開始画面\">");
		}
		html.append("</div>");

		if (showExploreResult && !gameState.isGameFinished()) {
			html.append("<p id=\"screenText\" class=\"screen-text\">何かないかな...</p>");
		} else if (showPachinkoResult && !gameState.isGameFinished()) {
			html.append("<p id=\"screenText\" class=\"screen-text\">働きに行くか！</p>");
		} else if (showSmokeResult && !gameState.isGameFinished()) {
			html.append("<p id=\"screenText\" class=\"screen-text\">")
					.append(escapeHtml(getSmokeStartText(gameState.getMessage()))).append("</p>");
		} else if (gameState.isDayAdvanced()) {
			html.append("<p id=\"screenText\" class=\"screen-text\">")
					.append(gameState.getSurvivedDay()).append("日耐えた</p>");
		} else if (showPurchaseResult && !gameState.isGameFinished()) {
			html.append("<p id=\"screenText\" class=\"screen-text\">").append(escapeHtml(message)).append("</p>");
		} else if (gameState.isGameOver()) {
			html.append("<p class=\"screen-text\">").append(escapeHtml(gameState.getMessage())).append("</p>");
		} else {
			html.append("<p id=\"screenText\" class=\"screen-text\"></p>");
		}

		if (message != null && !message.isEmpty()
				&& !gameState.isDayAdvanced()
				&& !showPurchaseResult
				&& !isPurchaseMessage(message)) {
			html.append("<p class=\"message\">").append(escapeHtml(message)).append("</p>");
		}

		html.append("</div>");
		html.append("</section>");

		if (showExploreResult && !gameState.isGameFinished()) {
			html.append("<script>");
			html.append("window.exploreResultImage='/images/").append(getExploreResultImage(gameState.getMessage())).append("';");
			html.append("window.exploreResultText='").append(escapeJs(getExploreResultText(gameState.getMessage()))).append("';");
			html.append("</script>");
		}

		if (showPachinkoResult && !gameState.isGameFinished()) {
			html.append("<script>");
			html.append("window.pachinkoResultImage='/images/").append(getPachinkoResultImage(gameState.getMessage())).append("';");
			html.append("window.pachinkoResultText='").append(escapeJs(getPachinkoResultMessage(gameState.getMessage()))).append("';");
			html.append("</script>");
		}

		if (!gameState.isDayAdvanced() && showSmokeResult && !gameState.isGameFinished()) {
			html.append("<script>");
			html.append("window.smokeResultImage='/images/smile.png';");
			html.append("window.smokeResultText='").append(escapeJs("きもちィｨｨ")).append("';");
			html.append("</script>");
		}

		if (!gameState.isDayAdvanced() && showPurchaseResult && !gameState.isGameFinished()) {
			html.append("<script>");
			html.append("window.purchaseResultText='").append(escapeJs(message)).append("';");
			html.append("</script>");
		}

		if (gameState.isDayAdvanced()) {
			html.append("<script>");
			html.append("window.dayTransitionText='").append(gameState.getSurvivedDay()).append("日耐えた';");
			html.append("</script>");
		}

		if (statusStartMoney != null || statusStartNicotine != null) {
			html.append("<script>");
			html.append("window.finalMoney=").append(gameState.getMoney()).append(";");
			html.append("window.finalNicotine=").append(gameState.getNicotine()).append(";");
			html.append("window.statusUpdateDelay=")
					.append(getStatusUpdateDelay(gameState, showPachinkoResult, showExploreResult, showSmokeResult, showPurchaseResult))
					.append(";");
			html.append("</script>");
		}
	}

	private static int getStatusUpdateDelay(
			GameState gameState,
			boolean showPachinkoResult,
			boolean showExploreResult,
			boolean showSmokeResult,
			boolean showPurchaseResult) {
		if (gameState.isDayAdvanced() && (showExploreResult || showPachinkoResult)) {
			return 5000;
		}

		if (showExploreResult || showPachinkoResult || showSmokeResult) {
			return 2000;
		}

		if (showPurchaseResult) {
			return 1000;
		}

		return 0;
	}

	private static String getSmokeStartText(String message) {
		if (message == null || !message.contains("を")) {
			return "たばこを吸った";
		}

		String productName = message.substring(0, message.indexOf("を"));
		return productName + "を吸った";
	}

	private static boolean isPurchaseMessage(String message) {
		return message != null && message.endsWith("を購入した");
	}

	private static String getExploreResultImage(String message) {
		if (message == null) {
			return "start.png";
		}

		if (message.contains("100円")) {
			return "get100yen.png";
		}

		if (message.contains("吸い殻")) {
			return "getSikemoku.png";
		}

		if (message.contains("未開封の")) {
			return "getTabacco.png";
		}

		return "start.png";
	}

	private static String getExploreResultText(String message) {
		if (message == null) {
			return "";
		}

		if (message.contains("100円")) {
			return "100円を拾った";
		}

		if (message.contains("吸い殻")) {
			return "シケモクを拾った";
		}

		if (message.contains("未開封の")) {
			String productName = message.replace("未開封の", "").replace("を見つけた", "");
			return productName + "を拾った";
		}

		return "";
	}

	private static String getNicotineStatusText(GameState gameState) {
		if (gameState.isGameOver()) {
			return "ニコチン切れ...";
		}

		if (gameState.isGameClear()) {
			return "生存成功";
		}

		if (gameState.isNicotineShortage()) {
			return "ニコチン切れ...";
		}

		return "正常";
	}

	private static void appendActionArea(StringBuilder html, GameState gameState) {
		html.append("<aside class=\"action-area\">");
		html.append("<h2>アクション</h2>");
		html.append("<div class=\"game-actions\">");
		html.append("<form class=\"explore-form\" method=\"post\" action=\"/explore\">");
		appendAgeConfirmedInput(html);
		if (gameState.isGameFinished()) {
			html.append("<button class=\"explore-button\" type=\"button\" disabled>探索する</button>");
		} else {
			html.append("<button class=\"explore-button\" type=\"submit\">探索する</button>");
		}
		html.append("</form>");
		html.append("<form class=\"pachinko-form\" method=\"post\" action=\"/pachinko\">");
		appendAgeConfirmedInput(html);
		if (gameState.isGameFinished()) {
			html.append("<button class=\"pachinko-button\" type=\"button\" disabled>パチンコ</button>");
		} else {
			html.append("<button class=\"pachinko-button\" type=\"submit\">パチンコ</button>");
		}
		html.append("</form>");
		html.append("</div>");
		if (gameState.isGameFinished()) {
			html.append("<button class=\"vending-button\" type=\"button\" disabled>自販機</button>");
		} else {
			html.append("<button class=\"vending-button\" type=\"button\" onclick=\"openVendingModal()\">自販機</button>");
		}
		html.append("<form class=\"quit-form\" method=\"post\" action=\"/quit-smoking\">");
		appendAgeConfirmedInput(html);
		html.append("<button class=\"quit-button\" type=\"submit\">禁煙する</button>");
		html.append("</form>");
		html.append("</aside>");
	}

	private static void appendInventory(StringBuilder html, GameState gameState, Set<Integer> productIdsWithImage) {
		html.append("<div class=\"inventory-box\">");
		html.append("<h2>持ち物</h2>");

		if (gameState.getInventory().isEmpty()) {
			html.append("<p class=\"inventory-empty\">持ち物はありません</p>");
		} else {
			html.append("<ul class=\"inventory-list\">");

			for (InventoryItem item : gameState.getInventory()) {
				html.append("<li class=\"inventory-item\">");
				html.append("<span class=\"inventory-thumb\">");

				if (productIdsWithImage.contains(item.getProductId())) {
					html.append("<img src=\"/images/product_").append(item.getProductId()).append(".png\" alt=\"")
							.append(escapeHtml(item.getProductName())).append("\">");
				} else {
					html.append("No Image");
				}

				html.append("</span>");
				html.append("<span class=\"inventory-detail\">").append(escapeHtml(item.getProductName()))
						.append(" / 残り").append(item.getRemainingPieces()).append("本</span>");
				html.append("<form class=\"smoke-form\" method=\"post\" action=\"/smoke\">");
				appendAgeConfirmedInput(html);
				html.append("<input type=\"hidden\" name=\"productId\" value=\"").append(item.getProductId()).append("\">");
				if (gameState.isGameFinished()) {
					html.append("<button class=\"smoke-button\" type=\"button\" disabled>吸う</button>");
				} else {
					html.append("<button class=\"smoke-button\" type=\"submit\">吸う</button>");
				}
				html.append("</form>");
				html.append("</li>");
			}

			html.append("</ul>");
		}

		html.append("</div>");
	}

	private static void appendControlPanel(
			StringBuilder html,
			Integer currentMoney,
			int insertedMoney,
			ArrayList<String> purchasedProducts) {
		html.append("<aside class=\"control-panel\">");
		html.append("<h2>操作パネル</h2>");
		html.append("<div class=\"direct-money-box\">");
		html.append("<p class=\"panel-label\">所持金を直接設定</p>");
		html.append("<form class=\"money-form\" method=\"post\" action=\"/set-money\">");
		appendAgeConfirmedInput(html);
		html.append("<input id=\"money\" name=\"money\" type=\"number\" min=\"0\" step=\"1\" placeholder=\"金額を入力\">");
		html.append("<button type=\"submit\">設定</button>");
		html.append("</form>");
		html.append("<p class=\"current-money\">現在の所持金：");
		appendMoneyText(html, currentMoney);
		html.append("</p>");
		html.append("</div>");
		html.append("<div class=\"coin-grid\">");
		appendMoneyButton(html, 1000);
		appendMoneyButton(html, 500);
		appendMoneyButton(html, 100);
		appendMoneyButton(html, 50);
		appendMoneyButton(html, 10);
		html.append("</div>");
		html.append("<form class=\"refund-form\" method=\"post\" action=\"/refund\">");
		appendAgeConfirmedInput(html);
		html.append("<button class=\"refund-button\" type=\"submit\">おつり</button>");
		html.append("</form>");
		html.append("<p class=\"panel-label\">投入金合計</p>");
		html.append("<p class=\"money-display inserted-money\">").append(insertedMoney).append("円</p>");
		html.append("<div class=\"purchased-box\">");
		html.append("<p class=\"panel-label\">購入済み</p>");

		if (purchasedProducts.isEmpty()) {
			html.append("<p class=\"purchased-empty\">まだありません</p>");
		} else {
			html.append("<ul class=\"purchased-list\">");

			for (String purchasedProduct : purchasedProducts) {
				appendPurchasedProduct(html, purchasedProduct);
			}

			html.append("</ul>");
		}

		html.append("</div>");
		html.append("</aside>");
	}

	private static void appendPurchasedProduct(StringBuilder html, String purchasedProduct) {
		String[] data = purchasedProduct.split(",", 2);
		int productId = Integer.parseInt(data[0]);
		String productName = data.length == 2 ? data[1] : "";

		html.append("<li class=\"purchased-item\">");
		html.append("<span class=\"purchased-thumb\">");

		if (productId > 0) {
			html.append("<img src=\"/images/product_").append(productId).append(".png\" alt=\"")
					.append(escapeHtml(productName)).append("\">");
		} else {
			html.append("No Image");
		}

		html.append("</span>");
		html.append("<span>").append(escapeHtml(productName)).append("</span>");
		html.append("</li>");
	}

	private static void appendMoneyText(StringBuilder html, Integer money) {
		if (money == null) {
			html.append("未設定");
		} else {
			html.append(money).append("円");
		}
	}

	private static void appendMoneyButton(StringBuilder html, int amount) {
		html.append("<form method=\"post\" action=\"/add-money\">");
		appendAgeConfirmedInput(html);
		html.append("<input type=\"hidden\" name=\"amount\" value=\"").append(amount).append("\">");
		html.append("<button class=\"coin-button\" type=\"submit\">").append(amount).append("円</button>");
		html.append("</form>");
	}

	private static void appendAgeConfirmedInput(StringBuilder html) {
		// 年齢確認済み状態をサーバーに保存しないため、商品画面から送るフォームにだけ印を付けます。
		html.append("<input type=\"hidden\" name=\"ageConfirmed\" value=\"yes\">");
	}

	private static void appendPageStart(StringBuilder html, String title) {
		html.append("<!DOCTYPE html>");
		html.append("<html lang=\"ja\">");
		html.append("<head>");
		html.append("<meta charset=\"UTF-8\">");
		html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		html.append("<title>").append(escapeHtml(title)).append("</title>");
		html.append("<style>");
		html.append("html,body{width:100%;height:100%;margin:0;}");
		html.append("body{font-family:Arial,'Meiryo',sans-serif;background:#111827;color:#111827;overflow:hidden;}");
		html.append("h1{font-size:28px;margin:0 0 24px;}");
		html.append("h2{font-size:20px;margin:0 0 16px;}");
		html.append(".game-shell{height:100vh;display:grid;grid-template-rows:30vh 70vh;gap:0;padding:16px;box-sizing:border-box;}");
		html.append(".game-status{background:#172033;color:#f9fafb;border:3px solid #374151;border-radius:10px;padding:16px;box-sizing:border-box;overflow:hidden;}");
		html.append(".game-status h1{margin:0 0 14px;}");
		html.append(".status-grid{display:grid;grid-template-columns:1fr 2fr 1fr;gap:12px;}");
		html.append(".status-card{background:#0f172a;border:1px solid #374151;border-radius:8px;padding:12px;min-height:72px;}");
		html.append(".status-label{display:block;color:#9ca3af;font-size:13px;margin-bottom:4px;}");
		html.append(".status-card strong{font-size:24px;}");
		html.append(".nicotine-bar{height:10px;background:#374151;border-radius:999px;margin-top:8px;overflow:hidden;}");
		html.append(".nicotine-bar span{display:block;height:100%;background:#22c55e;}");
		html.append(".finished-message{display:inline-block;margin:14px 0 0;padding:6px 10px;background:#dc2626;color:#fff;border-radius:6px;font-weight:bold;}");
		html.append(".quit-form{margin-top:auto;}");
		html.append(".quit-button{padding:10px 18px;border:1px solid #dc2626;border-radius:6px;background:#dc2626;color:#fff;font-size:16px;font-weight:bold;cursor:pointer;}");
		html.append(".game-message{margin:14px 0 0;padding:10px;background:#facc15;color:#111827;border-radius:6px;font-weight:bold;}");
		html.append(".bottom-area{display:grid;grid-template-columns:240px minmax(0,1fr) 310px;gap:16px;min-height:0;padding-top:16px;box-sizing:border-box;}");
		html.append(".action-area,.inventory-area{background:#1f2937;border:3px solid #374151;border-radius:10px;padding:16px;color:#f9fafb;box-sizing:border-box;min-height:0;overflow:auto;}");
		html.append(".game-screen{background:#1f2937;border:3px solid #374151;border-radius:10px;padding:16px;color:#f9fafb;box-sizing:border-box;min-height:0;overflow:hidden;}");
		html.append(".action-area{display:flex;flex-direction:column;}");
		html.append(".action-help{margin:0 0 14px;color:#d1d5db;font-size:13px;line-height:1.6;}");
		html.append(".game-actions{display:grid;gap:10px;margin:14px 0;}");
		html.append(".explore-form,.pachinko-form{margin:0;}");
		html.append(".explore-button,.pachinko-button,.vending-button,.quit-button{width:100%;padding:12px 14px;border-radius:6px;font-size:16px;font-weight:bold;cursor:pointer;}");
		html.append(".explore-button{border:1px solid #22c55e;background:#22c55e;color:#052e16;}");
		html.append(".pachinko-button{border:1px solid #f97316;background:#f97316;color:#431407;}");
		html.append(".vending-button{border:1px solid #38bdf8;background:#38bdf8;color:#082f49;}");
		html.append(".explore-button:disabled,.pachinko-button:disabled,.vending-button:disabled,.smoke-button:disabled{border-color:#6b7280;background:#6b7280;color:#d1d5db;cursor:not-allowed;}");
		html.append(".screen-frame{height:100%;display:flex;flex-direction:column;background:#0f172a;border:2px solid #4b5563;border-radius:8px;padding:18px;box-sizing:border-box;}");
		html.append(".pixel-window{height:calc(100% - 76px);min-height:220px;background:#020617;border:4px solid #111827;display:flex;align-items:center;justify-content:center;margin-bottom:12px;box-sizing:border-box;overflow:hidden;}");
		html.append(".pixel-placeholder{width:220px;height:150px;border:2px dashed #64748b;color:#64748b;display:flex;align-items:center;justify-content:center;font-weight:bold;letter-spacing:2px;}");
		html.append(".game-image{max-width:100%;max-height:100%;object-fit:contain;}");
		html.append(".screen-frame h2{margin:0 0 10px;color:#f9fafb;}");
		html.append(".screen-text{margin:0;padding:12px;background:#020617;color:#f9fafb;border:1px solid #374151;border-radius:6px;font-weight:bold;line-height:1.4;min-height:20px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}");
		html.append(".inventory-box{margin:0;}");
		html.append(".inventory-box h2{color:#f9fafb;margin-bottom:10px;}");
		html.append(".inventory-empty{margin:0;color:#9ca3af;}");
		html.append(".inventory-list{display:grid;gap:10px;margin:0;padding:0;list-style:none;}");
		html.append(".inventory-item{display:flex;align-items:center;gap:8px;background:#1f2937;border:1px solid #374151;border-radius:8px;padding:8px;}");
		html.append(".inventory-detail{flex:1;}");
		html.append(".inventory-thumb{width:42px;height:42px;background:#eef2f6;border:1px solid #9ca3af;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:9px;color:#6b7280;overflow:hidden;}");
		html.append(".inventory-thumb img{width:100%;height:100%;object-fit:contain;display:block;}");
		html.append(".smoke-form{margin:0;}");
		html.append(".smoke-button{padding:7px 10px;border:1px solid #eab308;border-radius:6px;background:#eab308;color:#422006;font-weight:bold;cursor:pointer;}");
		html.append(".modal-overlay{display:none;position:fixed;inset:0;background:rgba(0,0,0,.72);z-index:10;align-items:center;justify-content:center;padding:24px;}");
		html.append(".modal-overlay.open{display:flex;}");
		html.append(".vending-modal{width:min(1040px,96vw);max-height:88vh;overflow:auto;background:#1f2937;border:3px solid #9ca3af;border-radius:10px;padding:16px;color:#f9fafb;}");
		html.append(".modal-header{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:14px;}");
		html.append(".modal-header h2{margin:0;color:#fff;}");
		html.append(".close-button{padding:8px 14px;border:1px solid #d1d5db;border-radius:6px;background:#f9fafb;color:#111827;font-weight:bold;cursor:pointer;}");
		html.append(".pachinko-result{width:360px;border-radius:10px;padding:24px;text-align:center;color:#111827;border:4px solid #d1d5db;}");
		html.append(".pachinko-result h2{margin:0 0 12px;font-size:30px;}");
		html.append(".pachinko-result p{margin:0 0 18px;font-size:20px;font-weight:bold;}");
		html.append(".pachinko-lose{background:#e5e7eb;border-color:#9ca3af;}");
		html.append(".pachinko-win{background:#dcfce7;border-color:#22c55e;}");
		html.append(".pachinko-jackpot{background:#fef3c7;border-color:#f59e0b;}");
		html.append(".pachinko-hot{background:#fee2e2;border-color:#ef4444;}");
		html.append(".pachinko-disabled{background:#f3f4f6;border-color:#6b7280;}");
		html.append(".age-panel{max-width:420px;background:#fff;border:1px solid #d0d7de;border-radius:8px;padding:24px;}");
		html.append(".age-actions{display:flex;gap:12px;}");
		html.append(".age-button{padding:10px 18px;border:1px solid #8c959f;border-radius:6px;background:#fff;color:#24292f;font-size:15px;cursor:pointer;}");
		html.append(".age-button.primary{border-color:#1f6feb;background:#1f6feb;color:#fff;}");
		html.append(".control-panel{position:sticky;top:18px;background:#d7dde5;border:6px solid #111827;border-radius:12px;padding:18px;box-shadow:inset 0 0 0 2px #f8fafc;}");
		html.append(".control-panel h2{text-align:center;background:#111827;color:#fff;border-radius:6px;padding:10px;margin-bottom:16px;}");
		html.append(".panel-label{margin:0 0 8px;color:#374151;font-weight:bold;}");
		html.append(".money-display{margin:0 0 16px;padding:16px;background:#07140c;color:#39ff88;border:3px inset #111827;border-radius:6px;font-size:34px;font-weight:bold;text-align:right;}");
		html.append(".wallet-display{margin:0 0 16px;padding:10px 12px;background:#f8fafc;border:2px solid #6b7280;border-radius:6px;font-size:22px;font-weight:bold;text-align:right;}");
		html.append(".coin-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:14px;}");
		html.append(".coin-button{width:100%;min-height:54px;border:2px solid #374151;border-radius:8px;background:#f8fafc;color:#111827;font-size:20px;font-weight:bold;cursor:pointer;}");
		html.append(".coin-button:hover{background:#e5e7eb;}");
		html.append(".refund-form{margin:0 0 16px;}");
		html.append(".refund-button{width:100%;min-height:48px;border:2px solid #991b1b;border-radius:8px;background:#dc2626;color:#fff;font-size:18px;font-weight:bold;cursor:pointer;}");
		html.append(".direct-money-box{border-top:2px solid #9ca3af;padding-top:14px;}");
		html.append(".purchased-box{border-top:2px solid #9ca3af;margin-top:16px;padding-top:14px;}");
		html.append(".purchased-empty{margin:0;color:#6b7280;}");
		html.append(".purchased-list{margin:0;padding:0;max-height:180px;overflow:auto;list-style:none;}");
		html.append(".purchased-item{display:flex;align-items:center;gap:8px;margin-bottom:8px;}");
		html.append(".purchased-thumb{width:34px;height:34px;background:#eef2f6;border:1px solid #9ca3af;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:9px;color:#6b7280;overflow:hidden;}");
		html.append(".purchased-thumb img{width:100%;height:100%;object-fit:contain;display:block;}");
		html.append(".money-panel{background:#fff;border:1px solid #d0d7de;border-radius:8px;padding:16px;margin:0 0 22px;max-width:520px;}");
		html.append(".money-form{display:flex;gap:10px;align-items:center;flex-wrap:wrap;margin:0 0 12px;}");
		html.append(".money-form input{width:150px;padding:9px 10px;border:1px solid #6b7280;border-radius:6px;font-size:15px;}");
		html.append(".money-form button{padding:8px 14px;border:1px solid #1f6feb;border-radius:6px;background:#1f6feb;color:#fff;font-size:15px;cursor:pointer;}");
		html.append(".current-money{margin:0;font-weight:bold;}");
		html.append(".message{margin:0 0 16px;padding:12px 14px;border:1px solid #bf8700;border-radius:8px;background:#fff8c5;color:#111827;}");
		html.append(".product-grid{display:grid;grid-template-columns:repeat(3,minmax(150px,1fr));gap:14px;}");
		html.append(".product-card{background:#fff;border:1px solid #d0d7de;border-radius:8px;overflow:hidden;min-height:340px;display:flex;flex-direction:column;color:#111827;}");
		html.append(".product-card.sold-out{background:#e5e7eb;opacity:.7;}");
		html.append(".image-box{height:148px;background:#eef2f6;display:flex;align-items:center;justify-content:center;color:#6b7280;font-size:14px;}");
		html.append(".image-box img{width:100%;height:100%;object-fit:contain;display:block;}");
		html.append(".product-card .body{padding:10px 12px 12px;display:flex;flex:1;flex-direction:column;}");
		html.append(".product-name{font-size:15px;font-weight:bold;margin:0 0 8px;line-height:1.35;min-height:40px;}");
		html.append(".product-info{display:flex;justify-content:space-between;margin-top:6px;font-size:13px;}");
		html.append(".label{color:#57606a;}");
		html.append(".sold-out-label{display:inline-block;margin-top:10px;padding:4px 8px;border-radius:999px;background:#cf222e;color:#fff;font-size:13px;font-weight:bold;}");
		html.append(".shortage-label{display:inline-block;margin-top:10px;padding:4px 8px;border-radius:999px;background:#bf8700;color:#fff;font-size:13px;font-weight:bold;}");
		html.append(".finished-label{display:inline-block;margin-top:10px;padding:4px 8px;border-radius:999px;background:#4b5563;color:#fff;font-size:13px;font-weight:bold;}");
		html.append(".purchase-form{margin-top:auto;padding-top:12px;}");
		html.append(".purchase-button{width:100%;padding:10px 12px;border:1px solid #16a34a;border-radius:6px;background:#16a34a;color:#fff;font-size:15px;font-weight:bold;cursor:pointer;}");
		html.append(".purchase-button:disabled{border-color:#8c959f;background:#8c959f;cursor:not-allowed;}");
		html.append("@media(max-width:1180px){.bottom-area{grid-template-columns:210px minmax(0,1fr) 280px;}.product-grid{grid-template-columns:repeat(2,minmax(150px,1fr));}}");
		html.append("</style>");
		html.append("</head>");
		html.append("<body>");
	}

	private static void appendPageEnd(StringBuilder html) {
		html.append("<script>");
		html.append("function openVendingModal(){var image=document.getElementById('gameImage');var text=document.getElementById('screenText');if(image){image.src='/images/goToBuy.png';}if(text){text.textContent='自販機発見';}setTimeout(function(){document.getElementById('vendingModal').classList.add('open');if(image){image.src='/images/start.png';}if(text){text.textContent='';}},1000);}");
		html.append("function closeVendingModal(){document.getElementById('vendingModal').classList.remove('open');}");
		html.append("function closePachinkoResultModal(){document.getElementById('pachinkoResultModal').classList.remove('open');}");
		html.append("if(window.exploreResultImage){setTimeout(function(){document.getElementById('gameImage').src=window.exploreResultImage;document.getElementById('screenText').textContent=window.exploreResultText;},1000);if(!window.dayTransitionText){setTimeout(function(){document.getElementById('gameImage').src='/images/start.png';document.getElementById('screenText').textContent='';},2000);}}");
		html.append("if(window.pachinkoResultImage){setTimeout(function(){document.getElementById('gameImage').src=window.pachinkoResultImage;document.getElementById('screenText').textContent=window.pachinkoResultText;},1000);if(!window.dayTransitionText){setTimeout(function(){document.getElementById('gameImage').src='/images/start.png';document.getElementById('screenText').textContent='';},2000);}}");
		html.append("if(window.dayTransitionText){var dayDelay=(window.exploreResultImage||window.pachinkoResultImage)?2000:0;setTimeout(function(){document.getElementById('gameImage').src='/images/goHome.png';document.getElementById('screenText').textContent=window.dayTransitionText;},dayDelay);setTimeout(function(){document.getElementById('gameImage').src='/images/sleep.png';document.getElementById('screenText').textContent=window.dayTransitionText;},dayDelay+1000);setTimeout(function(){document.getElementById('gameImage').src='/images/wakeUp.png';document.getElementById('screenText').textContent=window.dayTransitionText;},dayDelay+2000);setTimeout(function(){document.getElementById('gameImage').src='/images/start.png';document.getElementById('screenText').textContent='';},dayDelay+3000);}");
		html.append("if(window.smokeResultImage){setTimeout(function(){document.getElementById('gameImage').src=window.smokeResultImage;document.getElementById('screenText').textContent=window.smokeResultText;},1000);setTimeout(function(){document.getElementById('gameImage').src='/images/start.png';document.getElementById('screenText').textContent='';},2000);}");
		html.append("if(window.purchaseResultText){setTimeout(function(){document.getElementById('screenText').textContent='';},1000);}");
		html.append("if(window.statusUpdateDelay!==undefined){setTimeout(function(){var money=document.getElementById('moneyStatus');var nicotine=document.getElementById('nicotineStatus');var bar=document.getElementById('nicotineBar');if(money){money.textContent=window.finalMoney+'円';}if(nicotine){nicotine.textContent=window.finalNicotine+'/100';}if(bar){bar.style.width=window.finalNicotine+'%';}},window.statusUpdateDelay);}");
		html.append("</script>");
		html.append("</body>");
		html.append("</html>");
	}

	private static String escapeHtml(String text) {
		if (text == null) {
			return "";
		}

		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private static String escapeJs(String text) {
		if (text == null) {
			return "";
		}

		return text.replace("\\", "\\\\")
				.replace("'", "\\'")
				.replace("\r", "")
				.replace("\n", "");
	}
}
