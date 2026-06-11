package master_bean;

import bean.Product;
import dao.ProductDAO;
import key.Keyboard;

public class ProductEditBean implements BeanInf {

	@Override
	public void execute() {
		ProductDAO dao = new ProductDAO();

		int id = Keyboard.getInputNumber("商品IDを入力してください。\n->");

		Product searchProduct = dao.searchProduct(id);

		if (searchProduct == null) {
			System.out.println("入力された商品IDの商品が存在しません。");
			return;
		}

		String name = Keyboard.getInputString("商品名を入力してください。\n->");
		int price = Keyboard.getInputNumber("単価を入力してください。\n->");
		int quantity = Keyboard.getInputNumber("数量を入力してください。\n->");
		int numberOfPieces = Keyboard.getInputNumber("1箱あたりの本数を入力してください。\n->");
		int nicotine = Keyboard.getInputNumber("ニコチン量を入力してください。\n->");

		Product product = new Product(id, name, price, quantity, numberOfPieces, nicotine);
		dao.editProduct(product);

		System.out.println("商品を更新しました。");
	}
}
