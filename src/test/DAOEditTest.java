package test;

import java.util.ArrayList;

import bean.Product;
import dao.ProductDAO;

public class DAOEditTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		ProductDAO dao = new ProductDAO();

		System.out.println("■商品変更テスト");
		Product product = new Product(4, "ボズ2", 130, 10, 20, 8);
		dao.editProduct(product);

		System.out.println("■商品一覧表示テスト");
		ArrayList<Product> list = dao.searchAllProducts();

		for (Product p : list) {
			System.out.println("商品ID:" + p.getId());
			System.out.println("商品名:" + p.getName());
			System.out.println("単価:" + p.getPrice());
			System.out.println("数量:" + p.getQuantity());
			System.out.println("本数:" + p.getNumberOfPieces());
			System.out.println("ニコチン:" + p.getNicotine());
			System.out.println();
		}

	}

}
