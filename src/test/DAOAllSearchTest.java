package test;

import java.util.ArrayList;

import bean.Product;
import dao.ProductDAO;

public class DAOAllSearchTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		ProductDAO dao = new ProductDAO();

		System.out.println("■商品一覧表示テスト");
		ArrayList<Product> list = dao.searchAllProducts();

		for (Product p : list) {
			System.out.println("商品ID:" + p.getId());
			System.out.println("商品名:" + p.getName());
			System.out.println("単価:" + p.getPrice());
			System.out.println("数量:" + p.getQuantity());
			System.out.println();
		}
	}

}
