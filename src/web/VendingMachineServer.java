package web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import bean.Product;
import dao.ProductDAO;
import game.GameState;
import game.GameService;

public class VendingMachineServer {

	private static final int PORT = 8080;
	private static final Path IMAGE_DIR = Path.of("images").toAbsolutePath().normalize();
	private static Integer currentMoney;
	private static int insertedMoney;
	private static String message;
	private static ArrayList<String> purchasedProducts = new ArrayList<String>();
	private static GameState gameState = new GameState();
	private static GameService gameService = new GameService();

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		server.createContext("/", VendingMachineServer::handleIndex);
		server.createContext("/age-check", VendingMachineServer::handleAgeCheck);
		server.createContext("/set-money", VendingMachineServer::handleSetMoney);
		server.createContext("/add-money", VendingMachineServer::handleAddMoney);
		server.createContext("/refund", VendingMachineServer::handleRefund);
		server.createContext("/purchase", VendingMachineServer::handlePurchase);
		server.createContext("/explore", VendingMachineServer::handleExplore);
		server.createContext("/images/", VendingMachineServer::handleImage);
		server.setExecutor(null);
		server.start();

		System.out.println("Web自動販売機を起動しました。");
		System.out.println("http://localhost:" + PORT + "/");
	}

	private static void handleIndex(HttpExchange exchange) throws IOException {
		if (!"/".equals(exchange.getRequestURI().getPath())) {
			sendTextResponse(exchange, 404, "Not Found");
			return;
		}

		// 年齢確認済み状態はサーバーに保存しないため、トップページは毎回確認画面にします。
		sendTextResponse(exchange, 200, VendingMachinePage.createAgeCheckPage());
	}

	private static void handleAgeCheck(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		String answer = getFormValue(requestBody, "answer");

		if ("yes".equals(answer)) {
			sendProductListResponse(exchange);
			return;
		}

		sendTextResponse(exchange, 200, VendingMachinePage.createAgeDeniedPage());
	}

	private static void handleSetMoney(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		String moneyText = getFormValue(requestBody, "money");

		try {
			int money = Integer.parseInt(moneyText);

			if (money >= 0) {
				currentMoney = money;
				message = "所持金を設定しました";
			}
		} catch (NumberFormatException e) {
			currentMoney = null;
		}

		sendProductListResponse(exchange);
	}

	private static void handleAddMoney(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		String amountText = getFormValue(requestBody, "amount");

		try {
			int amount = Integer.parseInt(amountText);

			if (amount > 0) {
				if (currentMoney == null) {
					message = "所持金を設定してください";
					sendProductListResponse(exchange);
					return;
				}

				if (currentMoney < amount) {
					message = "所持金が足りないため投入できません";
					sendProductListResponse(exchange);
					return;
				}

				currentMoney = currentMoney - amount;
				insertedMoney = insertedMoney + amount;
				message = amount + "円投入しました";
			}
		} catch (NumberFormatException e) {
			message = "投入金額が正しくありません";
		}

		sendProductListResponse(exchange);
	}

	private static void handleRefund(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		if (currentMoney == null) {
			currentMoney = insertedMoney;
		} else {
			currentMoney = currentMoney + insertedMoney;
		}

		insertedMoney = 0;
		message = "おつりを返却しました";
		sendProductListResponse(exchange);
	}

	private static void handlePurchase(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		String idText = getFormValue(requestBody, "productId");

		try {
			int productId = Integer.parseInt(idText);
			purchaseProduct(productId);
		} catch (NumberFormatException e) {
			message = "該当する商品がありません";
		}

		sendProductListResponse(exchange);
	}

	private static void handleExplore(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		ProductDAO dao = new ProductDAO();
		gameService.explore(gameState, dao.searchAllProducts());
		sendProductListResponse(exchange);
	}

	private static void purchaseProduct(int productId) {
		ProductDAO dao = new ProductDAO();
		Product product = dao.searchProduct(productId);

		if (product == null) {
			message = "該当する商品がありません";
			return;
		}

		if (product.getQuantity() == 0) {
			message = "売り切れです";
			return;
		}

		if (insertedMoney < product.getPrice()) {
			message = "投入金が足りません";
			return;
		}

		insertedMoney = insertedMoney - product.getPrice();
		product.setQuantity(product.getQuantity() - 1);
		dao.editProduct(product);
		purchasedProducts.add(product.getId() + "," + product.getName());
		message = "購入しました";
	}

	private static void handleImage(HttpExchange exchange) throws IOException {
		String requestPath = exchange.getRequestURI().getPath();
		String fileName = requestPath.substring("/images/".length());
		Path imagePath = IMAGE_DIR.resolve(fileName).normalize();

		if (!imagePath.startsWith(IMAGE_DIR) || !Files.isRegularFile(imagePath)) {
			sendTextResponse(exchange, 404, "Not Found");
			return;
		}

		byte[] responseBytes = Files.readAllBytes(imagePath);
		exchange.getResponseHeaders().set("Content-Type", getImageContentType(imagePath));
		exchange.sendResponseHeaders(200, responseBytes.length);

		try (OutputStream os = exchange.getResponseBody()) {
			os.write(responseBytes);
		}
	}

	private static String getImageContentType(Path imagePath) {
		String fileName = imagePath.getFileName().toString().toLowerCase();

		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}

		if (fileName.endsWith(".gif")) {
			return "image/gif";
		}

		return "image/png";
	}

	private static void sendTextResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
		byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

		exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		exchange.sendResponseHeaders(statusCode, responseBytes.length);

		try (OutputStream os = exchange.getResponseBody()) {
			os.write(responseBytes);
		}
	}

	private static String getFormValue(String requestBody, String key) {
		String[] params = requestBody.split("&");

		for (String param : params) {
			String[] keyValue = param.split("=", 2);

			if (keyValue.length == 2 && key.equals(urlDecode(keyValue[0]))) {
				return urlDecode(keyValue[1]);
			}
		}

		return "";
	}

	private static String urlDecode(String text) {
		return URLDecoder.decode(text, StandardCharsets.UTF_8);
	}

	private static boolean isAgeConfirmed(String requestBody) {
		// サーバー側で年齢確認済み状態を保持せず、商品画面のフォームから来たリクエストだけを許可します。
		return "yes".equals(getFormValue(requestBody, "ageConfirmed"));
	}

	private static void sendProductListResponse(HttpExchange exchange) throws IOException {
		ProductDAO dao = new ProductDAO();
		ArrayList<Product> products = dao.searchAllProducts();
		Set<Integer> productIdsWithImage = findProductIdsWithImage(products);
		String html = VendingMachinePage.createProductListPage(
				products,
				currentMoney,
				insertedMoney,
				message,
				productIdsWithImage,
				purchasedProducts,
				gameState);

		sendTextResponse(exchange, 200, html);
	}

	private static Set<Integer> findProductIdsWithImage(ArrayList<Product> products) {
		Set<Integer> productIdsWithImage = new HashSet<Integer>();

		for (Product product : products) {
			Path imagePath = IMAGE_DIR.resolve("product_" + product.getId() + ".png").normalize();

			if (imagePath.startsWith(IMAGE_DIR) && Files.isRegularFile(imagePath)) {
				productIdsWithImage.add(product.getId());
			}
		}

		return productIdsWithImage;
	}

	private static void redirectToIndex(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().set("Location", "/");
		exchange.sendResponseHeaders(303, -1);
		exchange.close();
	}
}
