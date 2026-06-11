package test;

import bean.Product;

public class ProductTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		Product p1 = new Product();

		p1.setId(1);
		p1.setName("ゴラコーラ");
		p1.setPrice(160);
		p1.setQuantity(6);
		p1.setNumberOfPieces(20);
		p1.setNicotine(6);

		System.out.println("商品ID：" + p1.getId());
		System.out.println("商品名：" + p1.getName());
		System.out.println("単価：" + p1.getPrice());
		System.out.println("数量：" + p1.getQuantity());
		System.out.println("本数：" + p1.getNumberOfPieces());
		System.out.println("ニコチン：" + p1.getNicotine());

		Product p2 = new Product(2, "アグエリアス", 150, 10, 20, 8);

		System.out.println("商品ID：" + p2.getId());
		System.out.println("商品名：" + p2.getName());
		System.out.println("単価：" + p2.getPrice());
		System.out.println("数量：" + p2.getQuantity());
		System.out.println("本数：" + p2.getNumberOfPieces());
		System.out.println("ニコチン：" + p2.getNicotine());
	}

}
