package test;

import key.Keyboard;

public class KeyBoardTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		String s = Keyboard.getInputString("文字列を入力してください。");
		System.out.println("入力した文字列：" + s);

		int n = Keyboard.getInputNumber("数値を入力してください。");
		System.out.println("入力した数値：" + n);
	}

}
