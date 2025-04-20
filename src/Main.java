import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    static ArrayList<Category> categories = new ArrayList<>();
    private static final String options = "Enter 0 to change name; Enter 1 to add; Enter 2 to update; Enter 3 to delete; Enter 4 to exit";

    public static boolean contains(String[] arr, String target) {
        for (String str : arr) {
            if (str.equals(target)) return true;
        }
        return false;
    }

    public static void loadAllData(){
        ArrayList<String> categoryData = readFromFile("src/Categories.txt");
        for (String str : categoryData) {
            String[] arr = str.split(" \\|");
            categories.add(new Category(arr[0], arr[1]));
        }

        for (Category c : categories) {
            ArrayList<String> categoryProducts = readFromFile("ProductCategories/" + c.getName() + ".txt");
            ArrayList<Product> products = new ArrayList<>();
            for (String str : categoryProducts) {
                String[] arr = str.split(" \\| ");
                Product productToAdd = new Product(arr[0], arr[1], arr[2], Integer.parseInt(arr[3]), Double.parseDouble(arr[4]));
                products.add(productToAdd);
            }
            c.setProducts(products);
        }
    }

    public static void addCategory(Category category) {
        ArrayList<String> existingCategories = readFromFile("src/Categories.txt");
        if (!existingCategories.contains(category.toString())) {
            categories.add(category);
            appendToFile("src/Categories.txt", category.toString());

            File folder = new File("ProductCategories/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String[] fileNames = folder.list();
            if (!contains(fileNames, category.getName()+".txt")) {
                File categoryFile = new File("ProductCategories/" + category.getName() + ".txt");
                try {
                    categoryFile.createNewFile();
                }
                catch (IOException e) {
                    System.out.println("Error creating file: " + e.getMessage());
                }
            }
        }
        else {
            System.out.println("Category already exists, cannot add new category");
        }
    }

    public static void deleteCategory(Category category) {
        if (categories.contains(category)) {
            categories.remove(category);
            System.out.println("Deleted Category: " + category.getName());
            System.out.println(categories);
            ArrayList<String> existingCategories = readFromFile("src/Categories.txt");
            existingCategories.remove(category.toString());
            System.out.println(existingCategories);
            writeAllToFile("src/Categories.txt", existingCategories);

            File folder = new File("ProductCategories/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            for (File file : files) {
                System.out.println(file.getName());
                if (file.getName().equals(category.getName()+".txt")) {
                    file.delete();
                    break;
                }
            }
        }
        else {
            System.out.println("Category does not exist, cannot delete category");
        }
    }

    public static void updateCategoryName(Category category, String newName) {
        if (categories.contains(category)) {
            String oldName = category.getName();
            categories.remove(category);
            category.setName(newName);
            categories.add(category);
            ArrayList<String> existingCategories = readFromFile("src/Categories.txt");
            Collections.replaceAll(existingCategories, oldName, newName);
            writeAllToFile("src/Categories.txt", existingCategories);

            File folder = new File("ProductCategories/");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().equals(oldName + ".txt")) {
                    file.renameTo(new File(folder, newName + ".txt"));
                    break;
                }
            }
            System.out.println("Updated Category: " + category.getName() + "current state of ProductCategories" + categories);
        }
        else {
            System.out.println("Category does not exist, cannot update category");
        }
    }

    public static boolean containsName(ArrayList<Product> products, String targetName) {
        for (Product product : products) {
            if (product.getName().equals(targetName)) return true;
        }
        return false;
    }

    public static void addProduct(Product product, Category category) {
        boolean exists = false;
        System.out.println("Current categoies: " + categories);
        for (Category c : categories) {
            System.out.println("Category products - " + c.getProducts());
            if (containsName(c.getProducts(), product.getName())) {
                exists = true;
                System.out.println("Product " + product.getName() + " already exists in Category " + category.getName());
                break;
            }
        }
        if (!exists) {
            System.out.println("Product " + product.getName() + " does not exist in Category " + category.getName());
            category.getProducts().add(product);
            appendToFile("ProductCategories/" + category.getName() + ".txt", product.toString());
        }
        else {
            System.out.println("Product already exists, cannot add new product");
        }
    }

    public static Product returnProductByName(ArrayList<Product> products, String target) {
        for (Product product : products) {
            if (product.getName().equals(target)) {
                return product;
            }
        }
        return null;
    }

    public static void deleteProduct(Product product, Category category) {
        Product toRemove = returnProductByName(category.getProducts(), product.getName());
        if (toRemove != null) {
            category.getProducts().remove(toRemove);
            ArrayList<String> categoryProducts = readFromFile("ProductCategories/" + category.getName() + ".txt");
            categoryProducts.remove(toRemove.toString());
            writeAllToFile("ProductCategories/" + category.getName() + ".txt", categoryProducts);
        }
        else {
            System.out.println("Product does not exist, cannot delete product");
        }
    }

    public static void updateProductData(Product product, Category category, String newName, String newDescription, String newProducer, int newAmountInStock, double newPrice) {
        Product toUpdate = returnProductByName(category.getProducts(), product.getName());
        if (toUpdate != null) {
            category.getProducts().remove(toUpdate);
            ArrayList<String> categoryProducts = readFromFile("ProductCategories/" + category.getName() + ".txt");
            categoryProducts.remove(toUpdate.toString());
            writeAllToFile("ProductCategories/" + category.getName() + ".txt", categoryProducts);
            Product newProduct = new Product(newName, newDescription, newProducer, newAmountInStock, newPrice);
            category.getProducts().add(newProduct);
            appendToFile("ProductCategories/" + category.getName() + ".txt", newProduct.toString());
        }
        else {
            System.out.println("Product does not exist, cannot update product");
        }
    }

    public static void writeToFile(String fileName, String text) {
        try (FileWriter writer = new FileWriter(fileName)){
            writer.write(text + "\n");
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void appendToFile(String fileName, String text) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(text + "\n");
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void writeAllToFile(String fileName, ArrayList<String> lines) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<String> readFromFile(String fileName) {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            String line;
            while((line = reader.readLine()) != null){
                list.add(line.trim());
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return list;
    }
    public static ArrayList<Product> searchForProducts(String searchText) {
        ArrayList<Product> foundProducts = new ArrayList<>();
        String searchPattern = searchText.toLowerCase();

        for (Category category : categories) {
            for (Product product : category.getProducts()) {
                if (product.getName().toLowerCase().contains(searchPattern)
                        || product.getDescription().toLowerCase().contains(searchPattern)
                        || product.getProducer().toLowerCase().contains(searchPattern)) {
                    foundProducts.add(product);
                }
            }
        }
        return foundProducts;
    }


    public static void main(String[] args) {
        loadAllData();

        Category electronics = new Category("Electronics", "Electronics");
        addCategory(electronics);
        Category books = new Category("Books", "jsadklfjas;dlkfj");
        addCategory(books);
        Product laptop = new Product("Laptop", "Gaming Laptop", "MSI", 10, 1499.99);
        addProduct(laptop, electronics);
        updateProductData(laptop, electronics, "Laptop Pro", "High-end Gaming Laptop", "MSI", 8, 1899.99);
        updateProductData(laptop, electronics, "Legion", "Gaming Laptop", "MSI", 10, 2499.99);
    }
}