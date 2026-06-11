package master_bean;

import bean.Product;
import dao.ProductDAO;
import key.Keyboard;

public class ProductDeleteBean implements BeanInf {

	@Override
	public void execute() {
		ProductDAO dao = new ProductDAO();

		int id = Keyboard.getInputNumber("商品IDを入力してください。\n->");

		Product searchProduct = dao.searchProduct(id);

		if (searchProduct == null) {
			System.out.println("入力された商品IDの商品が存在しません。");
			return;
		}

		dao.deleteProduct(searchProduct);

		System.out.println("商品を削除しました。");
	}
}