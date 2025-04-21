import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Main extends JFrame {
    static ArrayList<Category> categories = new ArrayList<>();
    private static final String options = "Enter 0 to change name; Enter 1 to add; Enter 2 to update; Enter 3 to delete; Enter 4 to exit";
    private JList goodsGroupList;
    private JTable goodsArea;
    private JTextArea categoryStatisticsText;

    private Main() {
        super();
        this.setSize(1000, 700);
        this.setTitle("Store House");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);

        JLabel goodsCatagoryLabel = new JLabel("Goods Categories");
        goodsCatagoryLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        goodsCatagoryLabel.setBounds(15, 10, 300, 50);

        JLabel goodsLabel = new JLabel("Goods List");
        goodsLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        goodsLabel.setBounds(375, 10, 300, 50);

        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
        String[] categoryList = categoryNames.toArray(new String[0]);
        goodsGroupList = new JList<>(categoryList);
        goodsGroupList.setBounds(15, 50, 300, 400);
        goodsGroupList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        goodsGroupList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String selected = (String) goodsGroupList.getSelectedValue();
                    if (selected == null) return;

                    // Searching for a category
                    Category selectedCategory = null;
                    for (Category c : categories) {
                        if (c.getName().equals(selected)) {
                            selectedCategory = c;
                            break;
                        }
                    }

                    if (selectedCategory != null) {
                        ArrayList<Product> products = selectedCategory.getProducts();

                        // Collecting goods for showing in the table
                        String[][] data = new String[products.size()][5];
                        for (int i = 0; i < products.size(); i++) {
                            Product p = products.get(i);
                            data[i][0] = p.getName();
                            data[i][1] = p.getDescription();
                            data[i][2] = p.getProducer();
                            data[i][3] = String.valueOf(p.getAmountInStock());
                            data[i][4] = String.valueOf(p.getPrice());
                        }

                        // Updating the table
                        String[] property = {"Name", "Description", "Producer", "Remained", "Price per piece"};
                        DefaultTableModel model = new DefaultTableModel(data, property);
                        goodsArea.setModel(model);

                        double totalCategoryCost = 0;
                        for(Product p : selectedCategory.getProducts()) {
                            totalCategoryCost += p.getAmountInStock() * p.getPrice();
                        }

                        categoryStatisticsText.setText("Total cost of goods in the category: " + totalCategoryCost);
                    }
                }
            }
        });

        goodsArea = new JTable();
        JScrollPane goodsScrollPane = new JScrollPane(goodsArea);
        goodsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        goodsScrollPane.setBounds(375, 50, 590, 200);

        JLabel categoryStatisticsLabel = new JLabel("Category Statistics");
        categoryStatisticsLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        categoryStatisticsLabel.setBounds(375, 260, 300, 50);

        categoryStatisticsText = new JTextArea();
        categoryStatisticsText.setEditable(false);
        categoryStatisticsText.setMargin(new Insets(15, 10, 15,10));
        categoryStatisticsText.setLineWrap(true);
        categoryStatisticsText.setWrapStyleWord(true);
        categoryStatisticsText.setFont(new Font("Arial", Font.PLAIN, 19));
        categoryStatisticsText.setText("Click on a category to see the statistics...");

        JScrollPane categoryStatisticsScrollPane = new JScrollPane(categoryStatisticsText);
        categoryStatisticsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        categoryStatisticsScrollPane.setBounds(375, 300, 590, 150);

        this.add(categoryStatisticsLabel);
        this.add(categoryStatisticsScrollPane);
        this.add(goodsCatagoryLabel);
        this.add(goodsLabel);
        this.add(goodsGroupList);
        this.add(goodsScrollPane);
    }

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

    public static void main(String[] args) {
        loadAllData();

        /*
        Category electronics = new Category("Electronics", "Electronics");
        addCategory(electronics);
        Category books = new Category("Books", "jsadklfjas;dlkfj");
        addCategory(books);
        Product laptop = new Product("Laptop", "Gaming Laptop", "MSI", 10, 1499.99);
        addProduct(laptop, electronics);
        updateProductData(laptop, electronics, "Laptop Pro", "High-end Gaming Laptop", "MSI", 8, 1899.99);
        updateProductData(laptop, electronics, "Legion", "Gaming Laptop", "MSI", 10, 2499.99);
        */

        Main window = new Main();
        window.setVisible(true);

       /* Main window2 = new Main(1);
        window2.setVisible(true);*/
    }
}