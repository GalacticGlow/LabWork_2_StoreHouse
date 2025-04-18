import java.util.ArrayList;

public class Category {
    String name;
    String description;

    ArrayList<Product> products;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void removeProduct(Product product) {
        products.remove(product);
    }

    public void replaceProduct(Product product, Product newProduct) {
        int index = products.indexOf(product);
        products.set(index, newProduct);
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public String toString() {
        return "Name = " + name + ", description = " + description;
    }
}