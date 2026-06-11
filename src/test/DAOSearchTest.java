package test;

import bean.Product;
import dao.ProductDAO;

public class DAOSearchTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		ProductDAO dao = new ProductDAO();

		System.out.println("■商品削除テスト");
		int id = 1;

		Product product = dao.searchProduct(id);

		if (product != null) {
			System.out.println("存在します。");
		} else {
			System.out.println("存在しません。");
		}

	}

}
