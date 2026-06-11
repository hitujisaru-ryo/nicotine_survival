package master_bean;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import bean.Product;
import dao.ProductDAO;
import key.Keyboard;
import util.ProductTableFormat;

public class ProductSearchBean implements BeanInf {

	@Override
	public void execute() {
		ProductDAO dao = new ProductDAO();

		System.out.println("【商品検索】");
		System.out.println("1・商品IDで検索");
		System.out.println("2・商品名で検索");

		int searchType = Keyboard.getInputNumber("検索方法を選択してください。");

		switch (searchType) {
		case 1:
			searchById(dao);
			break;

		case 2:
			searchByName(dao);
			break;

		default:
			System.out.println("該当する商品はありません");
			break;
		}
	}

	private void searchById(ProductDAO dao) {
		int id = Keyboard.getInputNumber("商品IDを入力してください。");
		Product product = dao.searchProduct(id);

		if (product == null) {
			System.out.println("該当する商品はありません");
			return;
		}

		System.out.println(ProductTableFormat.tableHeader());
		System.out.println(ProductTableFormat.tableFormat(product));
	}

	private void searchByName(ProductDAO dao) {
		String keyword = Keyboard.getInputString("商品名を入力してください。");
		ArrayList<Product> list = dao.searchAllProducts();
		ArrayList<Product> resultList = new ArrayList<Product>();

		for (Product product : list) {
			if (matchesProductName(product.getName(), keyword)) {
				resultList.add(product);
			}
		}

		if (resultList.isEmpty()) {
			System.out.println("該当する商品はありません");
			return;
		}

		System.out.println(ProductTableFormat.tableHeader());

		for (Product product : resultList) {
			System.out.println(ProductTableFormat.tableFormat(product));
		}
	}

	private boolean matchesProductName(String productName, String keyword) {
		Set<String> productNameList = createSearchTextList(productName);
		Set<String> keywordList = createSearchTextList(keyword);

		for (String searchKeyword : keywordList) {
			for (String searchProductName : productNameList) {
				if (searchProductName.contains(searchKeyword)) {
					return true;
				}
			}
		}

		return false;
	}

	private Set<String> createSearchTextList(String text) {
		Set<String> textList = new LinkedHashSet<String>();

		addSearchText(textList, text);
		addSearchText(textList, restoreMojibake(text));

		return textList;
	}

	private void addSearchText(Set<String> textList, String text) {
		String searchText = normalizeSearchText(text);

		if (!searchText.isEmpty()) {
			textList.add(searchText);
		}
	}

	private String normalizeSearchText(String text) {
		if (text == null) {
			return "";
		}

		String trimmedText = Normalizer.normalize(text.trim(), Normalizer.Form.NFKC);
		StringBuilder normalizedText = new StringBuilder();

		for (int i = 0; i < trimmedText.length(); i++) {
			char c = trimmedText.charAt(i);

			if (!Character.isWhitespace(c) && c != '\u3000') {
				normalizedText.append(c);
			}
		}

		return normalizedText.toString().toLowerCase(Locale.ROOT);
	}

	private String restoreMojibake(String text) {
		if (text == null) {
			return "";
		}

		try {
			return new String(text.getBytes(Charset.forName("Windows-31J")), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}
}
