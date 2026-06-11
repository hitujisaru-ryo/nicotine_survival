package client_bean;

import java.util.ArrayList;

import bean.Product;
import dao.ProductDAO;
import key.Keyboard;
import util.ProductTableFormat;

public class ProductPurchaseBean {

	public void execute() {

		ProductDAO dao = new ProductDAO();

		int money = Keyboard.getInputNumber("商品を購入するための金額を入力してください。\n->");

		while (money >= 0) {

			System.out.println("残金：" + money + "円");
			System.out.println(ProductTableFormat.tableHeader());

			ArrayList<Product> list = dao.searchAllProducts();

			for (Product product : list) {
				System.out.print(ProductTableFormat.tableFormat(product));

				if (product.getQuantity() == 0) {
					System.out.print("【売り切れ】");
				}

				System.out.println();
			}

			String answer = Keyboard.getInputString("商品購入手続きを行いますか？（y/n）\n->");

			if (!answer.equals("y")) {
				System.out.println("ありがとうございました。");
				System.out.println("返金金額：" + money + "円");
				return;
			}

			int id = Keyboard.getInputNumber("購入する商品IDを選択してください。\n->");

			Product product = dao.searchProduct(id);

			if (product == null) {
				System.out.println("該当する商品がありません。");
				continue;
			}

			if (product.getQuantity() == 0) {
				System.out.println("売り切れです。購入できません。");
				continue;
			}

			if (money - product.getPrice() < 0) {
				System.out.println("お金が足りません。購入できません。");
				continue;
			}

			System.out.println("ご購入ありがとうございました。");
			System.out.println("■購入した商品");
			System.out.println("■購入した商品");
			System.out.println(ProductTableFormat.buyHeader());
			System.out.println(ProductTableFormat.buyTableFormat(product));

			product.setQuantity(product.getQuantity() - 1);
			dao.editProduct(product);

			money = money - product.getPrice();
		}
	}
}