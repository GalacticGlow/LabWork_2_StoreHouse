import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class Main {
    static ArrayList<Category> categories = new ArrayList<>();
    private static final String options = "Enter 0 to change name; Enter 1 to add; Enter 2 to update; Enter 3 to delete; Enter 4 to exit";
    private static final char[] bilingualAlphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'А', 'Б', 'В', 'Г', 'Ґ', 'Д', 'Е', 'Є', 'Ж', 'З', 'И', 'І', 'Ї', 'Й',
            'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч',
            'Ш', 'Щ', 'Ь', 'Ю', 'Я'};

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

    public static Category returnCategoryByName(String target) {
        for (Category c : categories) {
            if (c.getName().equals(target)) {
                return c;
            }
        }
        return null;
    }

    public static void addProduct(Product product, Category category) {
        Category existingCategory = returnCategoryByName(category.getName());
        if (existingCategory == null) {
            System.out.println("Category not found in list, cannot add product.");
            return;
        }

        Product existingProduct = returnProductByName(existingCategory.getProducts(), product.getName());
        if (existingProduct == null) {
            System.out.println("Product " + product.getName() + " can be added to Category " + existingCategory.getName());
            existingCategory.getProducts().add(product);
            appendToFile("ProductCategories/" + existingCategory.getName() + ".txt", product.toString());
        }
        else {
            System.out.println("Product " + product.getName() + " already exists in Category " + existingCategory.getName());
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
        Category existingCategory = returnCategoryByName(category.getName());
        if (existingCategory == null) {
            System.out.println("Category not found in list, cannot delete product.");
            return;
        }

        System.out.println(existingCategory.getProducts());
        Product toRemove = returnProductByName(existingCategory.getProducts(), product.getName());
        if (toRemove != null) {
            existingCategory.getProducts().remove(toRemove);
            ArrayList<String> categoryProducts = readFromFile("ProductCategories/" + category.getName() + ".txt");
            categoryProducts.remove(toRemove.toString());
            writeAllToFile("ProductCategories/" + existingCategory.getName() + ".txt", categoryProducts);
        }
        else {
            System.out.println("Product does not exist, cannot delete product");
        }
    }

    public static void updateProductData(Product product, Category category, String newName, String newDescription, String newProducer, int newAmountInStock, double newPrice) {
        Category existingCategory = returnCategoryByName(category.getName());
        if (existingCategory == null) {
            System.out.println("Category not found in list, cannot delete product.");
            return;
        }

        Product toUpdate = returnProductByName(existingCategory.getProducts(), product.getName());
        if (toUpdate != null) {
            toUpdate.setName(newName);
            toUpdate.setDescription(newDescription);
            toUpdate.setProducer(newProducer);
            toUpdate.setPrice(newPrice);
            toUpdate.setAmountInStock(newAmountInStock);

            ArrayList<String> updatedProductStrings = new ArrayList<>();
            for (Product p : existingCategory.getProducts()) {
                updatedProductStrings.add(p.toString());
            }

            writeAllToFile("ProductCategories/" + existingCategory.getName() + ".txt", updatedProductStrings);
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
    public static String printAllProducts(){
        StringBuilder sb = new StringBuilder();
        ArrayList<Product> products = new ArrayList<>();

        for (Category category : categories) {
            products.addAll(category.getProducts());
        }
        products.sort((p1, p2) -> {
            String o1 = p1.getName();
            String o2 = p2.getName();

            int len = Math.min(o1.length(), o2.length());

            for (int i = 0; i < len; i++) {
                if (getCharIndex(o1.charAt(i)) != getCharIndex(o2.charAt(i))) {
                    return getCharIndex(o1.charAt(i)) - getCharIndex(o2.charAt(i));
                }
            }
            return o1.length() - o2.length();
        });
        sb.append("All ").append(products.size()).append(" products: \n");
        for (Product product : products) {
            sb.append(product.toString()).append(" | Full price in stock: ").append(product.getFullPrice()).append("\n");
        }
        return sb.toString();

    }

    public static String printAllProductsByCategory() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Category> categoriesSorted = new ArrayList<>(categories);
        if (!categoriesSorted.isEmpty()) {
            categoriesSorted.sort((c1, c2) -> {
                String o1 = c1.getName();
                String o2 = c2.getName();

                int len = Math.min(o1.length(), o2.length());

                for (int i = 0; i < len; i++) {
                    if (getCharIndex(o1.charAt(i)) != getCharIndex(o2.charAt(i))) {
                        return getCharIndex(o1.charAt(i)) - getCharIndex(o2.charAt(i));
                    }
                }
                return o1.length() - o2.length();
            });
            for (Category category : categoriesSorted) {
                double totalCategoryPrice = 0;
                ArrayList<Product> products = category.getProducts();
                if (!products.isEmpty()) {
                    for (Product product : products) {
                        totalCategoryPrice += product.getFullPrice();
                    }
                    sb.append("Category: ").append(category).append(" | Full price in stock: ").append(totalCategoryPrice).append("\n").append("\n");
                    products.sort((p1, p2) -> {
                        String o1 = p1.getName();
                        String o2 = p2.getName();

                        int len = Math.min(o1.length(), o2.length());

                        for (int i = 0; i < len; i++) {
                            if (getCharIndex(o1.charAt(i)) != getCharIndex(o2.charAt(i))) {
                                return getCharIndex(o1.charAt(i)) - getCharIndex(o2.charAt(i));
                            }
                        }
                        return o1.length() - o2.length();
                    });
                    for (Product product : products) {
                        sb.append(product.toString()).append(" | Full price in stock: ").append(product.getFullPrice()).append("\n");
                    }
                    sb.append("\n");
                }
                else sb.append("No products found in Category ").append(category.getName()).append("\n").append("\n");
            }
        }
        else sb.append("No categories found.");

        return sb.toString();
    }

    private static int getCharIndex(char c){
        for (int i = 0; i < bilingualAlphabet.length; i++) {
            if(bilingualAlphabet[i] == c){
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static void main(String[] args) {
    loadAllData();

<<<<<<< HEAD
    JFrame frame = new JFrame("Add Product");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 300);
    frame.setLayout(new GridLayout(7, 2));

    JTextField nameField = new JTextField();
    JTextField descField = new JTextField();
    JTextField producerField = new JTextField();
    JTextField amountField = new JTextField();
    JTextField priceField = new JTextField();
    JTextField categoryField = new JTextField();

    frame.add(new JLabel("Product Name:"));
    frame.add(nameField);
    frame.add(new JLabel("Description:"));
    frame.add(descField);
    frame.add(new JLabel("Producer:"));
    frame.add(producerField);
    frame.add(new JLabel("Amount in Stock:"));
    frame.add(amountField);
    frame.add(new JLabel("Price:"));
    frame.add(priceField);
    frame.add(new JLabel("Category Name:"));
    frame.add(categoryField);

    JButton addButton = new JButton("Add Product");
    frame.add(addButton);

    addButton.addActionListener(e -> {
        try {
            String name = nameField.getText();
            String desc = descField.getText();
            String producer = producerField.getText();
            int amount = Integer.parseInt(amountField.getText());
            double price = Double.parseDouble(priceField.getText());
            String categoryName = categoryField.getText();

            Category existing = returnCategoryByName(categoryName);
            if (existing != null) {
                Product p = new Product(name, desc, producer, amount, price);
                addProduct(p, existing);
                JOptionPane.showMessageDialog(frame, "Product added successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Category does not exist.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    });

    frame.setVisible(true);
=======
        /*Category electronics = new Category("Electronics", "Electronics");
        addCategory(electronics);
        Category books = new Category("Books", "jsadklfjas;dlkfj");
        addCategory(books);
        Product laptop = new Product("Laptop", "Gaming Laptop", "MSI", 10, 1499.99);
        addProduct(laptop, electronics);
        updateProductData(laptop, electronics, "Laptop Pro", "High-end Gaming Laptop", "MSI", 8, 1899.99);
        updateProductData(laptop, electronics, "Legion", "Gaming Laptop", "MSI", 10, 2499.99);
         */

        Category food = new Category("Food", "Food");
        addCategory(food);
        Product burger = new Product("Burger", "Gaming burger", "McDonalds", 0, 3.99);
        addProduct(burger, food);
        Product laptop = new Product("Laptop", "Gaming Laptop", "MSI", 10, 1499.99);
        Category electronics = new Category("Electronics", "Electronics");
        addProduct(laptop, electronics);
        Product TV = new Product("TV", "TV", "TV", 10, 1999.99);
        addProduct(TV, electronics);



        System.out.println(printAllProducts());
        System.out.println(printAllProductsByCategory());
>>>>>>> ca6797e320f6c20809261dcc79ed281fb59b0add
    }
}