package key;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Keyboard {

	public static String getInputString(String msg) {
		System.out.println(msg);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input = br.readLine();
			return input;
		} catch (Exception e) {
			System.exit(0);
		}

		return null;
	}

	public static int getInputNumber(String msg) {
		System.out.println(msg);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input = br.readLine();
			int number = Integer.parseInt(input);
			return number;
		} catch (Exception e) {
			System.exit(0);
		}

		return 0;
	}
}