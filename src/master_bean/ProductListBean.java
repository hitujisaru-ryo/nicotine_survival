package master_bean;

import java.util.ArrayList;

import bean.Product;
import dao.ProductDAO;
import util.ProductTableFormat;

public class ProductListBean implements BeanInf {

	@Override
	public void execute() {
		ProductDAO dao = new ProductDAO();
		ArrayList<Product> list = dao.searchAllProducts();

		System.out.println(ProductTableFormat.tableHeader());

		for (Product product : list) {
			System.out.println(ProductTableFormat.tableFormat(product));
		}
	}
}