package master_bean;

import bean.Product;
import dao.ProductDAO;
import key.Keyboard;

public class ProductRegistBean implements BeanInf {

	@Override
	public void execute() {
		ProductDAO dao = new ProductDAO();

		int id = Keyboard.getInputNumber("商品IDを入力してください。\n->");
		String name = Keyboard.getInputString("商品名を入力してください。\n->");
		int price = Keyboard.getInputNumber("単価を入力してください。\n->");
		int quantity = Keyboard.getInputNumber("数量を入力してください。\n->");

		Product searchProduct = dao.searchProduct(id);

		if (searchProduct != null) {
			System.out.println("入力された商品IDの商品が既に存在します。");
			return;
		}

		Product product = new Product(id, name, price, quantity);
		dao.registProduct(product);

		System.out.println("商品を登録しました。");
	}
}