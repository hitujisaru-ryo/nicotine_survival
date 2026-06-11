package client;

import client_bean.ProductPurchaseBean;
import key.Keyboard;

public class Menu {

	public static void showMenu() {

		while (true) {
			System.out.println("【クライアント・メニュー】");
			System.out.println("1．商品購入");
			System.out.println("0．終了");

			int menu = Keyboard.getInputNumber("メニューを選択してください。");

			switch (menu) {
			case 1:
				ProductPurchaseBean bean = new ProductPurchaseBean();
				bean.execute();
				break;

			default:
				System.out.println("終了します。");
				return;
			}
		}
	}
}