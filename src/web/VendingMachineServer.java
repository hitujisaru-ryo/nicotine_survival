package web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import bean.Product;
import dao.ProductDAO;
import game.GameState;
import game.GameService;
import game.InventoryItem;

public class VendingMachineServer {

	private static final int PORT = 18081;
	private static final int THREAD_POOL_SIZE = 8;
	private static final int MAX_REQUEST_BODY_BYTES = 8192;
	private static final int MAX_REQUESTS_PER_MINUTE = 120;
	private static final Path IMAGE_DIR = Path.of("images").toAbsolutePath().normalize();
	private static final ZoneId GMT_ZONE = ZoneId.of("GMT");
	private static final ConcurrentHashMap<String, ArrayDeque<Long>> requestTimesByIp = new ConcurrentHashMap<String, ArrayDeque<Long>>();
	private static final ThreadLocal<Integer> currentStatusCode = ThreadLocal.withInitial(() -> 200);
	private static Integer currentMoney;
	private static int insertedMoney;
	private static String message;
	private static ArrayList<String> purchasedProducts = new ArrayList<String>();
	private static GameState gameState = new GameState();
	private static GameService gameService = new GameService();
	private static UserDataDAO userDataDAO = new UserDataDAO();
	private static ArrayList<Product> webProducts = new ArrayList<Product>();
	private static String currentAccountName;
	private static boolean resultSaved;
	private static boolean showPachinkoResult;
	private static boolean showExploreResult;
	private static boolean showSmokeResult;
	private static boolean showPurchaseResult;
	private static Integer statusStartMoney;
	private static Integer statusStartNicotine;
	private static ArrayList<InventoryItem> inventoryBeforeExplore;

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		createLoggedContext(server, "/", VendingMachineServer::handleIndex);
		createLoggedContext(server, "/account", VendingMachineServer::handleAccount);
		createLoggedContext(server, "/start-game", VendingMachineServer::handleStartGame);
		createLoggedContext(server, "/age-check", VendingMachineServer::handleAgeCheck);
		createLoggedContext(server, "/set-money", VendingMachineServer::handleSetMoney);
		createLoggedContext(server, "/add-money", VendingMachineServer::handleAddMoney);
		createLoggedContext(server, "/refund", VendingMachineServer::handleRefund);
		createLoggedContext(server, "/purchase", VendingMachineServer::handlePurchase);
		createLoggedContext(server, "/explore", VendingMachineServer::handleExplore);
		createLoggedContext(server, "/pachinko", VendingMachineServer::handlePachinko);
		createLoggedContext(server, "/smoke", VendingMachineServer::handleSmoke);
		createLoggedContext(server, "/reset", VendingMachineServer::handleReset);
		createLoggedContext(server, "/restart", VendingMachineServer::handleRestart);
		createLoggedContext(server, "/quit-smoking", VendingMachineServer::handleQuitSmoking);
		createLoggedContext(server, "/images/", VendingMachineServer::handleImage);
		server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
		reloadWebProducts();
		server.start();

		System.out.println("Web自動販売機を起動しました。");
		System.out.println("http://localhost:" + PORT + "/");
	}

	private static void createLoggedContext(HttpServer server, String path, HttpHandler handler) {
		server.createContext(path, exchange -> {
			long startedAt = System.currentTimeMillis();
			String clientIp = getClientIp(exchange);
			currentStatusCode.set(200);

			if (!isRequestAllowed(clientIp)) {
				sendTextResponse(exchange, 429, "Too Many Requests");
				logAccess(exchange, clientIp, 429, System.currentTimeMillis() - startedAt);
				currentStatusCode.remove();
				return;
			}

			try {
				handler.handle(exchange);
				logAccess(exchange, clientIp, currentStatusCode.get(), System.currentTimeMillis() - startedAt);
			} catch (IOException e) {
				logAccess(exchange, clientIp, 500, System.currentTimeMillis() - startedAt);
				throw e;
			} finally {
				currentStatusCode.remove();
			}
		});
	}

	private static boolean isRequestAllowed(String clientIp) {
		long now = System.currentTimeMillis();
		long windowStart = now - 60000;
		ArrayDeque<Long> requestTimes = requestTimesByIp.computeIfAbsent(clientIp, key -> new ArrayDeque<Long>());

		synchronized (requestTimes) {
			while (!requestTimes.isEmpty() && requestTimes.peekFirst() < windowStart) {
				requestTimes.removeFirst();
			}

			if (requestTimes.size() >= MAX_REQUESTS_PER_MINUTE) {
				return false;
			}

			requestTimes.addLast(now);
			return true;
		}
	}

	private static String readRequestBody(HttpExchange exchange) throws IOException {
		String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");

		if (contentLength != null && !contentLength.isBlank()) {
			try {
				if (Long.parseLong(contentLength) > MAX_REQUEST_BODY_BYTES) {
					sendTextResponse(exchange, 413, "Request Entity Too Large");
					return null;
				}
			} catch (NumberFormatException e) {
				sendTextResponse(exchange, 400, "Bad Request");
				return null;
			}
		}

		byte[] bodyBytes = exchange.getRequestBody().readNBytes(MAX_REQUEST_BODY_BYTES + 1);

		if (bodyBytes.length > MAX_REQUEST_BODY_BYTES) {
			sendTextResponse(exchange, 413, "Request Entity Too Large");
			return null;
		}

		return new String(bodyBytes, StandardCharsets.UTF_8);
	}

	private static String getClientIp(HttpExchange exchange) {
		String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",", 2)[0].trim();
		}

		return exchange.getRemoteAddress().getAddress().getHostAddress();
	}

	private static void logAccess(HttpExchange exchange, String clientIp, int statusCode, long elapsedMillis) {
		System.out.println(clientIp + " " + exchange.getRequestMethod() + " "
				+ exchange.getRequestURI().getPath() + " " + statusCode + " " + elapsedMillis + "ms");
	}

	private static void handleIndex(HttpExchange exchange) throws IOException {
		if (!"/".equals(exchange.getRequestURI().getPath())) {
			sendTextResponse(exchange, 404, "Not Found");
			return;
		}

		sendTextResponse(exchange, 200, VendingMachinePage.createRankingPage(userDataDAO.findAll()));
	}

	private static void handleAccount(HttpExchange exchange) throws IOException {
		if (!"/account".equals(exchange.getRequestURI().getPath())) {
			sendTextResponse(exchange, 404, "Not Found");
			return;
		}

		sendTextResponse(exchange, 200, VendingMachinePage.createAccountPage(null));
	}

	private static void handleStartGame(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}
		String accountName = getFormValue(requestBody, "accountName").trim();

		if (accountName.isEmpty()) {
			sendTextResponse(exchange, 200, VendingMachinePage.createAccountPage("名前を入力してください"));
			return;
		}

		currentAccountName = accountName;
		startNewGame();
		sendTextResponse(exchange, 200, VendingMachinePage.createAgeCheckPage());
	}

	private static void handleAgeCheck(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}
		String answer = getFormValue(requestBody, "answer");

		if ("yes".equals(answer)) {
			if (currentAccountName == null || currentAccountName.isBlank()) {
				redirectToIndex(exchange);
				return;
			}

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

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

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

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

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

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

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

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		String idText = getFormValue(requestBody, "productId");

		try {
			int productId = Integer.parseInt(idText);
			rememberStatusBeforeAction();
			showPurchaseResult = purchaseProduct(productId);
			if (!showPurchaseResult) {
				clearStatusBeforeAction();
			}
		} catch (NumberFormatException e) {
			message = "該当する商品がありません";
			showPurchaseResult = false;
			clearStatusBeforeAction();
		}

		sendProductListResponse(exchange);
	}

	private static void handleExplore(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		ProductDAO dao = new ProductDAO();
		rememberStatusBeforeAction();
		rememberInventoryBeforeExplore();
		gameService.explore(gameState, dao.searchAllProducts());
		showExploreResult = true;
		saveResultIfGameFinished();
		sendProductListResponse(exchange);
	}

	private static void handlePachinko(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		rememberStatusBeforeAction();
		gameService.pachinko(gameState);
		showPachinkoResult = true;
		saveResultIfGameFinished();
		sendProductListResponse(exchange);
	}

	private static void handleSmoke(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		String idText = getFormValue(requestBody, "productId");

		try {
			int productId = Integer.parseInt(idText);
			rememberStatusBeforeAction();
			gameService.smoke(gameState, productId);
			showSmokeResult = true;
			saveResultIfGameFinished();
		} catch (NumberFormatException e) {
			gameState.setMessage("吸えるたばこがありません");
			clearStatusBeforeAction();
		}

		sendProductListResponse(exchange);
	}

	private static void handleReset(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		gameState.reset();
		message = null;
		purchasedProducts.clear();
		reloadWebProducts();
		resultSaved = false;
		sendProductListResponse(exchange);
	}

	private static void handleQuitSmoking(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		gameState.quitSmoking();
		message = null;
		saveResultIfGameFinished();
		sendProductListResponse(exchange);
	}

	private static void handleRestart(HttpExchange exchange) throws IOException {
		if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
			redirectToIndex(exchange);
			return;
		}

		String requestBody = readRequestBody(exchange);
		if (requestBody == null) {
			return;
		}

		if (!isAgeConfirmed(requestBody)) {
			redirectToIndex(exchange);
			return;
		}

		if (currentAccountName == null || currentAccountName.isBlank()) {
			redirectToIndex(exchange);
			return;
		}

		startNewGame();
		sendProductListResponse(exchange);
	}

	private static boolean purchaseProduct(int productId) {
		if (gameState.isGameFinished()) {
			message = "ゲーム終了しています";
			return false;
		}

		Product product = findWebProduct(productId);

		if (product == null) {
			message = "該当する商品がありません";
			return false;
		}

		if (product.getQuantity() == 0) {
			message = "売り切れです";
			return false;
		}

		if (gameState.getMoney() < product.getPrice()) {
			message = "所持金不足";
			return false;
		}

		gameState.subtractMoney(product.getPrice());
		product.setQuantity(product.getQuantity() - 1);
		gameState.addProductToInventory(product);
		purchasedProducts.add(product.getId() + "," + product.getName());
		message = product.getName() + "を購入した";
		return true;
	}

	private static void handleImage(HttpExchange exchange) throws IOException {
		String requestPath = exchange.getRequestURI().getPath();
		String fileName = requestPath.substring("/images/".length());
		Path imagePath = IMAGE_DIR.resolve(fileName).normalize();

		if (!imagePath.startsWith(IMAGE_DIR) || !Files.isRegularFile(imagePath)) {
			sendTextResponse(exchange, 404, "Not Found");
			return;
		}

		long imageSize = Files.size(imagePath);
		FileTime lastModifiedTime = Files.getLastModifiedTime(imagePath);
		String etag = createImageEtag(imagePath, imageSize, lastModifiedTime);
		String lastModified = formatHttpDate(lastModifiedTime.toMillis());

		exchange.getResponseHeaders().set("Content-Type", getImageContentType(imagePath));
		exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
		exchange.getResponseHeaders().set("ETag", etag);
		exchange.getResponseHeaders().set("Last-Modified", lastModified);

		if (isImageCacheValid(exchange, etag, lastModifiedTime)) {
			currentStatusCode.set(304);
			exchange.sendResponseHeaders(304, -1);
			exchange.close();
			return;
		}

		currentStatusCode.set(200);
		exchange.sendResponseHeaders(200, imageSize);

		try (OutputStream os = exchange.getResponseBody()) {
			Files.copy(imagePath, os);
		}
	}

	private static String createImageEtag(Path imagePath, long imageSize, FileTime lastModifiedTime) {
		return "\"" + imagePath.getFileName() + "-" + imageSize + "-" + lastModifiedTime.toMillis() + "\"";
	}

	private static boolean isImageCacheValid(HttpExchange exchange, String etag, FileTime lastModifiedTime) {
		String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");

		if (etag.equals(ifNoneMatch)) {
			return true;
		}

		String ifModifiedSince = exchange.getRequestHeaders().getFirst("If-Modified-Since");

		if (ifModifiedSince == null || ifModifiedSince.isBlank()) {
			return false;
		}

		try {
			Instant requestedTime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(ifModifiedSince, Instant::from);
			long requestedSeconds = requestedTime.getEpochSecond();
			long imageSeconds = lastModifiedTime.toInstant().getEpochSecond();
			return imageSeconds <= requestedSeconds;
		} catch (RuntimeException e) {
			return false;
		}
	}

	private static String formatHttpDate(long epochMillis) {
		return DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(epochMillis).atZone(GMT_ZONE));
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
		currentStatusCode.set(statusCode);
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
		ensureWebProductsLoaded();
		Set<Integer> productIdsWithImage = findProductIdsWithImage(webProducts);
		String html = VendingMachinePage.createProductListPage(
				webProducts,
				currentMoney,
				insertedMoney,
				message,
				productIdsWithImage,
				purchasedProducts,
				gameState,
				showPachinkoResult,
				showExploreResult,
				showSmokeResult,
				showPurchaseResult,
				statusStartMoney,
				statusStartNicotine,
				inventoryBeforeExplore);
		showPachinkoResult = false;
		showExploreResult = false;
		showSmokeResult = false;
		showPurchaseResult = false;
		clearStatusBeforeAction();
		clearInventoryBeforeExplore();
		gameState.clearDayAdvanced();

		sendTextResponse(exchange, 200, html);
	}

	private static void rememberStatusBeforeAction() {
		statusStartMoney = gameState.getMoney();
		statusStartNicotine = gameState.getNicotine();
	}

	private static void clearStatusBeforeAction() {
		statusStartMoney = null;
		statusStartNicotine = null;
	}

	private static void startNewGame() {
		currentMoney = null;
		insertedMoney = 0;
		message = null;
		purchasedProducts.clear();
		gameState.reset();
		reloadWebProducts();
		resultSaved = false;
		showPachinkoResult = false;
		showExploreResult = false;
		showSmokeResult = false;
		showPurchaseResult = false;
		clearStatusBeforeAction();
		clearInventoryBeforeExplore();
	}

	private static void saveResultIfGameFinished() {
		if (resultSaved || !gameState.isGameFinished()) {
			return;
		}

		userDataDAO.addRecord(currentAccountName, gameState.getDay(), gameState.getActionCount(), getClearTitle());
		resultSaved = true;
	}

	private static String getClearTitle() {
		if (!gameState.isGameClear()) {
			return null;
		}

		if (gameState.getMoney() >= 5000) {
			return "伝説のヤニカス";
		}

		if (gameState.getMoney() >= 3000) {
			return "ベテランヤニカス";
		}

		return "見習いヤニカス";
	}

	private static void ensureWebProductsLoaded() {
		if (webProducts.isEmpty()) {
			reloadWebProducts();
		}
	}

	private static void reloadWebProducts() {
		ProductDAO dao = new ProductDAO();
		webProducts = dao.searchAllProducts();
	}

	private static Product findWebProduct(int productId) {
		ensureWebProductsLoaded();

		for (Product product : webProducts) {
			if (product.getId() == productId) {
				return product;
			}
		}

		return null;
	}

	private static void rememberInventoryBeforeExplore() {
		inventoryBeforeExplore = new ArrayList<InventoryItem>();

		for (InventoryItem item : gameState.getInventory()) {
			inventoryBeforeExplore.add(new InventoryItem(
					item.getProductId(),
					item.getProductName(),
					item.getRemainingPieces(),
					item.getNicotine()));
		}
	}

	private static void clearInventoryBeforeExplore() {
		inventoryBeforeExplore = null;
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
		currentStatusCode.set(303);
		exchange.sendResponseHeaders(303, -1);
		exchange.close();
	}
}
