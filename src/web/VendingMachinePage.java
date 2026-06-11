package web;

import java.util.ArrayList;
import java.util.Set;

import bean.Product;

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
			ArrayList<String> purchasedProducts) {
		StringBuilder html = new StringBuilder();

		appendPageStart(html, "Web自動販売機");
		html.append("<h1>Web自動販売機</h1>");
		html.append("<main class=\"main-layout\">");
		html.append("<section class=\"products-area\">");
		html.append("<h2>商品一覧</h2>");

		if (message != null && !message.isEmpty()) {
			html.append("<p class=\"message\">").append(escapeHtml(message)).append("</p>");
		}

		html.append("<div class=\"product-grid\">");

		for (Product product : products) {
			boolean soldOut = product.getQuantity() == 0;
			boolean shortage = insertedMoney < product.getPrice();

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

			if (soldOut) {
				html.append("<span class=\"sold-out-label\">売り切れ</span>");
				html.append("<button class=\"purchase-button\" type=\"button\" disabled>購入できません</button>");
			} else if (shortage) {
				html.append("<span class=\"shortage-label\">投入金不足</span>");
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
		appendControlPanel(html, currentMoney, insertedMoney, purchasedProducts);
		html.append("</main>");
		appendPageEnd(html);

		return html.toString();
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
		html.append("body{font-family:Arial,'Meiryo',sans-serif;margin:24px;background:#4b5563;color:#222;}");
		html.append("h1{font-size:28px;margin:0 0 24px;}");
		html.append("h2{font-size:20px;margin:0 0 16px;}");
		html.append(".main-layout{display:grid;grid-template-columns:minmax(0,1fr) 320px;gap:22px;align-items:start;}");
		html.append(".products-area{background:#1f2937;border:6px solid #111827;border-radius:10px;padding:18px;}");
		html.append(".products-area h2{color:#fff;}");
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
		html.append(".message{max-width:520px;margin:0 0 22px;padding:12px 14px;border:1px solid #bf8700;border-radius:8px;background:#fff8c5;}");
		html.append(".product-grid{display:grid;grid-template-columns:repeat(5,minmax(128px,1fr));gap:12px;}");
		html.append(".product-card{background:#fff;border:1px solid #d0d7de;border-radius:8px;overflow:hidden;min-height:316px;display:flex;flex-direction:column;}");
		html.append(".product-card.sold-out{background:#e5e7eb;opacity:.7;}");
		html.append(".image-box{height:148px;background:#eef2f6;display:flex;align-items:center;justify-content:center;color:#6b7280;font-size:14px;}");
		html.append(".image-box img{width:100%;height:100%;object-fit:contain;display:block;}");
		html.append(".product-card .body{padding:10px 12px 12px;display:flex;flex:1;flex-direction:column;}");
		html.append(".product-name{font-size:15px;font-weight:bold;margin:0 0 8px;line-height:1.35;min-height:40px;}");
		html.append(".product-info{display:flex;justify-content:space-between;margin-top:6px;font-size:13px;}");
		html.append(".label{color:#57606a;}");
		html.append(".sold-out-label{display:inline-block;margin-top:10px;padding:4px 8px;border-radius:999px;background:#cf222e;color:#fff;font-size:13px;font-weight:bold;}");
		html.append(".shortage-label{display:inline-block;margin-top:10px;padding:4px 8px;border-radius:999px;background:#bf8700;color:#fff;font-size:13px;font-weight:bold;}");
		html.append(".purchase-form{margin-top:auto;padding-top:12px;}");
		html.append(".purchase-button{width:100%;padding:10px 12px;border:1px solid #16a34a;border-radius:6px;background:#16a34a;color:#fff;font-size:15px;font-weight:bold;cursor:pointer;}");
		html.append(".purchase-button:disabled{border-color:#8c959f;background:#8c959f;cursor:not-allowed;}");
		html.append("@media(max-width:1180px){.product-grid{grid-template-columns:repeat(4,minmax(128px,1fr));}}");
		html.append("@media(max-width:980px){.main-layout{grid-template-columns:1fr;}.control-panel{position:static;}.product-grid{grid-template-columns:repeat(auto-fit,minmax(150px,1fr));}}");
		html.append("</style>");
		html.append("</head>");
		html.append("<body>");
	}

	private static void appendPageEnd(StringBuilder html) {
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
}
