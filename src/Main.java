import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

public class Main extends JFrame {
    static ArrayList<Category> categories = new ArrayList<>();
    private static final String options = "Enter 0 to change name; Enter 1 to add; Enter 2 to update; Enter 3 to delete; Enter 4 to exit";
    private static final char[] bilingualAlphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'А', 'Б', 'В', 'Г', 'Ґ', 'Д', 'Е', 'Є', 'Ж', 'З', 'И', 'І', 'Ї', 'Й',
            'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч',
            'Ш', 'Щ', 'Ь', 'Ю', 'Я'};
    private JList goodsGroupList;
    private JTable goodsArea;
    private JTextArea categoryStatisticsText;
    private JFrame addProductFrame;
    private JFrame addCategoryFrame;
    ArrayList<String> categoryNames;

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

    private void updateCategoryList() {
        String[] categoryList = categoryNames.toArray(new String[0]);
        goodsGroupList.setListData(categoryList);
    }

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

        // Updating Category List
        addCategoryName();

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
                        double totalCategoryCostRounded = Math.round(totalCategoryCost * 100.0) / 100.0;

                        categoryStatisticsText.setText("Total cost of goods in the category: " + totalCategoryCostRounded);
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

        // Button for adding categories and products
        JButton addButton = new JButton();
        addButton.setText("Add");
        addButton.setFont(new Font("Arial", Font.PLAIN, 24));
        addButton.setBounds(520, 490, 200, 50);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAddChoiceWindow();
            }
        });

        // Button for adding categories and products
        JButton removeButton = new JButton();
        removeButton.setText("Delete");
        removeButton.setFont(new Font("Arial", Font.PLAIN, 24));
        removeButton.setBounds(750, 490, 200, 50);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRemoveChoiceWindow();
            }
        });


        this.add(addButton);
        this.add(removeButton);
        this.add(categoryStatisticsLabel);
        this.add(categoryStatisticsScrollPane);
        this.add(goodsCatagoryLabel);
        this.add(goodsLabel);
        this.add(goodsGroupList);
        this.add(goodsScrollPane);
    }

    private void addCategoryName() {
        categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
    }

    private void showAddChoiceWindow() {
        JFrame choiceFrame = new JFrame("Add");
        choiceFrame.setSize(350, 200);
        choiceFrame.setLayout(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to add a category<br> or a product?</html>");
        choiceLabel.setBounds(15, 10, 250, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton addCategoryButton = new JButton("Add Category");
        addCategoryButton.setBounds(25, 100, 130, 30);
        addCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                choiceFrame.dispose();
                if(addCategoryFrame != null) {
                    addCategoryFrame.dispose();
                };
                showAddCategoryWindow();
            }
        });

        JButton addProductButton = new JButton("Add Product");
        addProductButton.setBounds(180, 100, 130, 30);
        addProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                choiceFrame.dispose();
                if(addProductFrame != null) {
                    addProductFrame.dispose();
                }
                showAddProductWindow();
            }
        });

        choiceFrame.add(addCategoryButton);
        choiceFrame.add(addProductButton);
        choiceFrame.add(choiceLabel);
        choiceFrame.setVisible(true);
    }

    private void showAddCategoryWindow() {
        addCategoryFrame = new JFrame("Add Category");
        addCategoryFrame.setSize(400, 270);
        addCategoryFrame.setLayout(null);

        JLabel titleField = new JLabel("Add Category");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(110, 10, 200, 30);
        addCategoryFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(130, 65, 220, 35);
        JTextField descField = new JTextField();
        descField.setBounds(130, 125, 220, 35);

        JLabel nameLabel = new JLabel("Category Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        addCategoryFrame.add(nameLabel);
        addCategoryFrame.add(nameField);

        JLabel descLabel = new JLabel("Description:");
        descLabel.setBounds(25, 110, 185, 65);
        addCategoryFrame.add(descLabel);
        addCategoryFrame.add(descField);

        JButton addButton = new JButton("Add");
        addButton.setBounds(30, 180, 150, 30);
        addCategoryFrame.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    String desc = descField.getText();

                    Category checkCategory = returnCategoryByName(name);
                    if(checkCategory == null) {
                        Category categoryToAdd = new Category(name, desc);
                        addCategory(categoryToAdd);
                        JOptionPane.showMessageDialog(addCategoryFrame, "Category added successfully!");
                        nameField.setText("");
                        descField.setText("");
                        addCategoryName();
                        updateCategoryList();
        //              addCategoryFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(addCategoryFrame, "Category already exists!");
                        nameField.setText("");
                        descField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(addProductFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addCategoryFrame.dispose();
            }
        });
        addCategoryFrame.add(cancelButton);

        addCategoryFrame.setVisible(true);
    }

    private void showAddProductWindow() {
        addProductFrame = new JFrame("Add Product");
        addProductFrame.setSize(400, 530);
        addProductFrame.setLayout(null);

        JTextField nameField = new JTextField();
        nameField.setBounds(130, 65, 220, 35);
        JTextField descField = new JTextField();
        descField.setBounds(130, 125, 220, 35);
        JTextField producerField = new JTextField();
        producerField.setBounds(130, 185, 220, 35);
        JTextField amountField = new JTextField();
        amountField.setBounds(130, 245, 220, 35);
        JTextField priceField = new JTextField();
        priceField.setBounds(130, 305, 220, 35);
        JTextField categoryField = new JTextField();
        categoryField.setBounds(130, 365, 220, 35);

        JLabel titleField = new JLabel("Add Product");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(110, 10, 200, 30);
        addProductFrame.add(titleField);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        addProductFrame.add(nameLabel);
        addProductFrame.add(nameField);

        JLabel descLabel = new JLabel("Description:");
        descLabel.setBounds(25, 110, 185, 65);
        addProductFrame.add(descLabel);
        addProductFrame.add(descField);

        JLabel producerLabel = new JLabel("Producer:");
        producerLabel.setBounds(25, 170, 185, 65);
        addProductFrame.add(producerLabel);
        addProductFrame.add(producerField);

        JLabel amountLabel = new JLabel("Amount in Stock:");
        amountLabel.setBounds(25, 230, 185, 65);
        addProductFrame.add(amountLabel);
        addProductFrame.add(amountField);

        JLabel priceLabel = new JLabel("Price per piece:");
        priceLabel.setBounds(25, 290, 185, 65);
        addProductFrame.add(priceLabel);
        addProductFrame.add(priceField);

        JLabel categoryLabel = new JLabel("Category Name:");
        categoryLabel.setBounds(25, 350, 185, 65);
        addProductFrame.add(categoryLabel);
        addProductFrame.add(categoryField);

        JButton addButton = new JButton("Add Product");
        addButton.setBounds(30, 430, 150, 30);
        addProductFrame.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
                        JOptionPane.showMessageDialog(addProductFrame, "Product added successfully!");
                    } else {
                        JOptionPane.showMessageDialog(addProductFrame, "Category does not exist.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(addProductFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 430, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProductFrame.dispose();
            }
        });
        addProductFrame.add(cancelButton);

        addProductFrame.setVisible(true);
    }

    private void showRemoveChoiceWindow() {
        JFrame choiceFrame = new JFrame("Write-off Product");
        choiceFrame.setSize(350, 200);
        choiceFrame.setLayout(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to write-off a product<br> or remove a category?</html>");
        choiceLabel.setBounds(15, 10, 250, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        choiceFrame.add(choiceLabel);
        choiceFrame.setVisible(true);

        JButton removeCategoryButton = new JButton("Remove Category");
        removeCategoryButton.setBounds(25, 100, 130, 30);
        removeCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });

        JButton removeProductButton = new JButton("Remove Product");
        removeProductButton.setBounds(180, 100, 130, 30);
        removeProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        choiceFrame.add(removeCategoryButton);
        choiceFrame.add(removeProductButton);
    }

    public static void main(String[] args) {
        loadAllData();

        Main window = new Main();
        window.setVisible(true);

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
        Product burger = new Product("Burger", "Gaming burger", "McDonald's", 0, 3.99);
        addProduct(burger, food);
        Product laptop = new Product("Laptop", "Gaming Laptop", "MSI", 10, 1499.99);
        Category electronics = new Category("Electronics", "Electronics");
        addProduct(laptop, electronics);
        Product TV = new Product("TV", "TV", "TV", 10, 1999.99);
        addProduct(TV, electronics);

        System.out.println(printAllProducts());
        System.out.println(printAllProductsByCategory());
    }
}