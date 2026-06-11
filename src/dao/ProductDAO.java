package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.PrintWriter;
import java.util.ArrayList;

import bean.Product;

public class ProductDAO {

	private static final String FILE_NAME = "product_list.txt";
	private static final Path FILE_PATH = Path.of(FILE_NAME);

	public ArrayList<Product> searchAllProducts() {
		ArrayList<Product> list = new ArrayList<Product>();

		try (BufferedReader br = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
			String line;

			while ((line = br.readLine()) != null) {
				line = removeBom(line);
				String[] data = line.split(",");

				int id = Integer.parseInt(data[0]);
				String name = data[1];
				int price = Integer.parseInt(data[2]);
				int quantity = Integer.parseInt(data[3]);

				Product product = new Product(id, name, price, quantity);
				list.add(product);
			}

		} catch (IOException e) {
			System.out.println("ファイル処理時にエラーが発生しました。");
			System.exit(0);
		}

		return list;
	}

	public Product searchProduct(int id) {
		ArrayList<Product> list = searchAllProducts();

		for (Product product : list) {
			if (product.getId() == id) {
				return product;
			}
		}

		return null;
	}

	private String removeBom(String line) {
		if (line != null && line.startsWith("\uFEFF")) {
			return line.substring(1);
		}

		return line;
	}

	public void editProduct(Product product) {
		ArrayList<Product> list = searchAllProducts();

		for (int i = 0; i < list.size(); i++) {
			Product p = list.get(i);

			if (p.getId() == product.getId()) {
				list.set(i, product);
				break;
			}
		}

		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8))) {
			for (Product p : list) {
				pw.println(
					p.getId() + "," +
					p.getName() + "," +
					p.getPrice() + "," +
					p.getQuantity()
				);
			}

		} catch (IOException e) {
			System.out.println("ファイル処理時にエラーが発生しました。");
			System.exit(0);
		}
	}
	
	public void registProduct(Product product) {

		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
				FILE_PATH,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.APPEND))) {
			pw.println(
				product.getId() + "," +
				product.getName() + "," +
				product.getPrice() + "," +
				product.getQuantity()
			);

		} catch (IOException e) {
			System.out.println("ファイル処理時にエラーが発生しました。");
			System.exit(0);
		}
	}

	public void deleteProduct(Product product) {

		ArrayList<Product> list = searchAllProducts();

		for (int i = 0; i < list.size(); i++) {
			Product p = list.get(i);

			if (p.getId() == product.getId()) {
				list.remove(i);
				break;
			}
		}

		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8))) {
			for (Product p : list) {
				pw.println(
					p.getId() + "," +
					p.getName() + "," +
					p.getPrice() + "," +
					p.getQuantity()
				);
			}

		} catch (IOException e) {
			System.out.println("ファイル処理時にエラーが発生しました。");
			System.exit(0);
		}
	}
}
