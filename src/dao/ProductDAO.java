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
				
				if (data.length < 6) {
					continue;
				}

				try {
					int id = Integer.parseInt(data[0].trim());
					String name = data[1].trim();
					int price = Integer.parseInt(data[2].trim());
					int quantity = Integer.parseInt(data[3].trim());
					int numberOfPieces = Integer.parseInt(data[4].trim());
					int nicotine = Integer.parseInt(data[5].trim());

					Product product = new Product(id, name, price, quantity, numberOfPieces, nicotine);
					list.add(product);
				} catch (NumberFormatException e) {
					continue;
				}
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
					p.getQuantity() + "," +
					p.getNumberOfPieces() + "," +
					p.getNicotine()
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
				product.getQuantity() + "," +
				product.getNumberOfPieces() + "," +
				product.getNicotine()
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
					p.getQuantity() + "," +
					p.getNumberOfPieces() + "," +
					p.getNicotine()
				);
			}

		} catch (IOException e) {
			System.out.println("ファイル処理時にエラーが発生しました。");
			System.exit(0);
		}
	}
}
