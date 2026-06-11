package util;

import bean.Product;

public class ProductTableFormat {

	public static String textFormat(String text, int width) {
		int count = 0;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (String.valueOf(c).matches("[ -~]")) {
				count += 1;
			} else {
				count += 2;
			}
		}

		String result = text;

		while (count < width) {
			result += " ";
			count++;
		}

		return result;
	}

	public static String tableHeader() {
		return textFormat("商品ID", 8)
			 + textFormat("商品名", 20)
			 + textFormat("単価", 8)
			 + textFormat("数量", 8);
	}

	public static String tableFormat(Product product) {
		return textFormat(String.valueOf(product.getId()), 8)
			 + textFormat(product.getName(), 20)
			 + textFormat(String.valueOf(product.getPrice()) + "円", 8)
			 + textFormat(String.valueOf(product.getQuantity()), 8);
	}

	public static String buyHeader() {
		return textFormat("商品ID", 8)
			 + textFormat("商品名", 20)
			 + textFormat("単価", 8);
	}

	public static String buyTableFormat(Product product) {
		return textFormat(String.valueOf(product.getId()), 8)
			 + textFormat(product.getName(), 20)
			 + textFormat(String.valueOf(product.getPrice()) + "円", 8);
	}
}