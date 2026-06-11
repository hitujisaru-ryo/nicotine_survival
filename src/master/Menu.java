package master;

import key.Keyboard;
import master_bean.ProductDeleteBean;
import master_bean.ProductEditBean;
import master_bean.ProductListBean;
import master_bean.ProductRegistBean;
import master_bean.ProductSearchBean;

public class Menu {

	public static void showMenu() {

		while (true) {
			System.out.println("【マスター・メニュー】");
			System.out.println("1・商品情報一覧");
			System.out.println("2・商品情報追加");
			System.out.println("3・商品情報変更");
			System.out.println("4・商品情報削除");
			System.out.println("5・商品検索");
			System.out.println("0・終了");

			int menu = Keyboard.getInputNumber("メニューを選択してください。");

			switch (menu) {
			case 1:
				ProductListBean bean1 = new ProductListBean();
				bean1.execute();
				break;

			case 2:
				ProductRegistBean bean2 = new ProductRegistBean();
				bean2.execute();
				break;

			case 3:
				ProductEditBean bean3 = new ProductEditBean();
				bean3.execute();
				break;

			case 4:
				ProductDeleteBean bean4 = new ProductDeleteBean();
				bean4.execute();
				break;

			case 5:
				ProductSearchBean bean5 = new ProductSearchBean();
				bean5.execute();
				break;

			default:
				System.out.println("終了します。");
				return;
			}
		}
	}
}
