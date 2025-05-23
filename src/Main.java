/*
Лабораторна 2
Головний клас, в якому ініціалізовані методи праці з об'єктами та графічний інтерфейс.
File: Main.java
Authors: Artem Gulidov, Dmytro Kvitka, Romanyuk Nazariy
*/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

public class Main extends JFrame {
    static ArrayList<Category> categories = new ArrayList<>();

    private double totalCategoryCostRounded;
    private JList goodsGroupList;
    private JTable goodsArea;
    private JTextArea categoryStatisticsText;
    private JTextArea generalStatisticsText;
    private JFrame addProductFrame;
    private JFrame addCategoryFrame;
    private JFrame removeCategoryFrame;
    private JFrame removeProductFrame;
    private JFrame redactCategoryFrame;
    private JFrame redactProductFrame;
    private JFrame searchFrame;
    ArrayList<String> categoryNames;
    Category[] selectedCateg = {null};

    public static boolean contains(String[] arr, String target) {
        for (String str : arr) {
            if (str.equals(target)) return true;
        }
        return false;
    }

    public static void loadAllData() {
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
        if (!existingCategories.contains(category.toString()) && !category.getName().equalsIgnoreCase("All Categories")) {
            categories.add(category);
            appendToFile("src/Categories.txt", category.toString());

            File folder = new File("ProductCategories/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String[] fileNames = folder.list();
            if (!contains(fileNames, category.getName() + ".txt")) {
                File categoryFile = new File("ProductCategories/" + category.getName() + ".txt");
                try {
                    categoryFile.createNewFile();
                } catch (IOException e) {
                    System.out.println("Error creating file: " + e.getMessage());
                }
            }
        }
    }

    public static void deleteCategory(Category category) {
        if (categories.contains(category)) {
            categories.remove(category);
            System.out.println("Deleted Category: " + category.getName());
            System.out.println(categories);
            ArrayList<String> existingCategories = readFromFile("src/Categories.txt");
            existingCategories.removeIf(line -> line.startsWith(category.getName() + " |"));
            writeAllToFile("src/Categories.txt", existingCategories);

            File folder = new File("ProductCategories/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            for (File file : files) {
                System.out.println(file.getName());
                if (file.getName().equals(category.getName() + ".txt")) {
                    file.delete();
                    break;
                }
            }
        }
    }

    public static void updateCategoryName(Category category, String newName) {
        if (categories.contains(category)) {
            String oldName = category.getName();

            categories.remove(category);
            category.setName(newName);
            categories.add(category);

            ArrayList<String> existingCategories = readFromFile("src/Categories.txt");
            //System.out.println("Existing categories before update: " + existingCategories);

            for (int i = 0; i < existingCategories.size(); i++) {
                if (existingCategories.get(i).trim().contains(oldName.trim())) {
                    existingCategories.set(i, newName + " | " + category.getDescription());
                    break;
                }
                //System.out.println(existingCategories.get(i).trim());
            }

            System.out.println("Existing categories after update: " + existingCategories);
            writeAllToFile("src/Categories.txt", existingCategories);

            File folder = new File("ProductCategories/");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();

            for (File file : files) {
                if (file.getName().equals(oldName + ".txt")) {
                    if (file.renameTo(new File(folder, newName + ".txt"))) {
                        //System.out.println("Renamed file: " + file.getName() + " to " + newName + ".txt");
                    } else {
                        //System.out.println("Failed to rename file: " + oldName + ".txt");
                    }
                    break;
                }
            }
        }
    }

    public static void updateCategoryDescription(Category category, String newDescription) {
        if (categories.contains(category)) {
            String oldDescription = category.getDescription();
            categories.remove(category);
            category.setDescription(newDescription);
            categories.add(category);

            ArrayList<String> existingCategories = readFromFile("src/Categories.txt");

            for (int i = 0; i < existingCategories.size(); i++) {
                if (existingCategories.get(i).trim().startsWith(category.getName().trim() + " | ")) {
                    existingCategories.set(i, category.getName() + " | " + newDescription);
                    break;
                }
            }

            writeAllToFile("src/Categories.txt", existingCategories);
        }
    }

    public static Category returnCategoryByName(String target) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(target)) {
                return c;
            }
        }
        return null;
    }

    public static ArrayList<String> returnAllCategoryProductNames() {
        ArrayList<String> allProductNames = new ArrayList<>();
        for (Category c : categories) {
            for (Product p : c.getProducts()) {
                allProductNames.add(p.getName().toLowerCase());
            }
        }
        return allProductNames;
    }

    public static void addProduct(Product product, Category category) {
        Category existingCategory = returnCategoryByName(category.getName());
        if (existingCategory == null) {
            //System.out.println("Category not found in list, cannot add product.");
            return;
        }

        if (!returnAllCategoryProductNames().contains(product.getName().toLowerCase())) {
            //System.out.println("Product " + product.getName() + " can be added to Category " + existingCategory.getName());
            existingCategory.getProducts().add(product);
            appendToFile("ProductCategories/" + existingCategory.getName() + ".txt", product.toString());
        }
        else {
            //System.out.println("Product " + product.getName() + " already exists somewhere, cannot add product.");
        }
    }

    public static Product returnProductByName(String target) {
        for (Category c : categories) {
            for (Product p : c.getProducts()) {
                if (p.getName().equalsIgnoreCase(target)) return p;
            }
        }
        return null;
    }

    public static Category returnCategoryByProduct(Product product) {
        for (Category c : categories) {
            if (c.getProducts().contains(product)) return c;
        }
        return null;
    }

    public static void deleteProduct(Product product) {
        Product toRemove = returnProductByName(product.getName());
        if (toRemove != null) {
            Category category = returnCategoryByProduct(toRemove);
            if (category != null) {
                category.getProducts().remove(toRemove);
                ArrayList<String> categoryProducts = readFromFile("ProductCategories/" + category.getName() + ".txt");
                categoryProducts.remove(toRemove.toString());
                writeAllToFile("ProductCategories/" + category.getName() + ".txt", categoryProducts);
            }
        }
    }

    public static void updateProductData(Product product, Category category, String newName, String newDescription, String newProducer, int newAmountInStock, double newPrice) {

        Product toUpdate = returnProductByName(product.getName());
        if (toUpdate != null) {
            toUpdate.setName(newName);
            toUpdate.setDescription(newDescription);
            toUpdate.setProducer(newProducer);
            toUpdate.setPrice(newPrice);
            toUpdate.setAmountInStock(newAmountInStock);

            ArrayList<String> updatedProductStrings = new ArrayList<>();
            for (Product p : Objects.requireNonNull(returnCategoryByProduct(toUpdate)).getProducts()) {
                updatedProductStrings.add(p.toString());
            }
            writeAllToFile("ProductCategories/" + Objects.requireNonNull(returnCategoryByProduct(toUpdate)).getName() + ".txt", updatedProductStrings);
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
        //System.out.println("Attempting to write to file: " + fileName);  // Debugging line
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
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

    public static ArrayList<Product> searchForProducts(Category category, String searchText) {
        ArrayList<Product> foundProducts = new ArrayList<>();
        String searchPattern = searchText.toLowerCase();

        for (Product product : category.getProducts()) {
            if (product.getName().toLowerCase().contains(searchPattern)
                    || product.getDescription().toLowerCase().contains(searchPattern)
                    || product.getProducer().toLowerCase().contains(searchPattern)) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }

    private boolean productExists(Category category, String productName) {
        for (Product product : category.getProducts()) {
            if(product.getName().equals(productName)) {
                return true;
            }
        }
        return false;
    }

    private void updateCategoryList() {
        String[] categoryList = new String[categoryNames.size() + 1];
        categoryList[0] = "All Categories";
        for (int i = 0; i < categoryNames.size(); i++) {
            categoryList[i + 1] = categoryNames.get(i);
        }
        goodsGroupList.setListData(categoryList);
    }

    private void updateGeneralStatistics() {
        int totalCategories = 0;
        for(Category category : categories) {
            totalCategories++;
        }

        int totalStorageAmount = 0;
        for(Category c : categories) {
            for(Product p : c.getProducts()) {
                totalStorageAmount += p.getAmountInStock();
            }
        }
        double totalStorageCost = 0;
        for(Category c : categories) {
            for(Product p : c.getProducts()) {
                totalStorageCost += p.getPrice() * p.getAmountInStock();
            }
        }
        totalStorageCost = Math.round(totalStorageCost * 100.0) / 100.0;

        generalStatisticsText.setText("Total amount of categories: " + totalCategories
                + "\n\nTotal amount of goods is: " + totalStorageAmount
                + "\n\nTotal cost of goods is: " + totalStorageCost);
    }

    private Main() {
        super();
        this.setSize(1000, 700);
        this.setTitle("Store House");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        JLabel goodsCatagoryLabel = new JLabel("Goods Categories");
        goodsCatagoryLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        goodsCatagoryLabel.setBounds(15, 10, 300, 50);

        JLabel goodsLabel = new JLabel("Goods List");
        goodsLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        goodsLabel.setBounds(375, 10, 300, 50);

        // Updating Category List
        addCategoryName();


        String[] categoryList = new String[categoryNames.size() + 1];
        categoryList[0] = "All Categories";
        for (int i = 0; i < categoryNames.size(); i++) {
            categoryList[i + 1] = categoryNames.get(i);
        }
        goodsGroupList = new JList<>(categoryList);
        goodsGroupList.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JScrollPane goodsGroupScrollPane = new JScrollPane(goodsGroupList);
        goodsGroupScrollPane.setBounds(15, 50, 300, 400);
        goodsGroupList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    refreshGoodsTable();
                }
            }
        });

        goodsArea = new JTable();
        //goodsArea
        JScrollPane goodsScrollPane = new JScrollPane(goodsArea);
        goodsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        goodsScrollPane.setBounds(375, 50, 590, 200);
        goodsArea.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = goodsArea.getSelectedRow();
                    if (selectedRow != -1) {
                        Object amountInRow = goodsArea.getValueAt(selectedRow, 3);
                        Object priceInRow = goodsArea.getValueAt(selectedRow, 4);


                        int amount = Integer.parseInt(amountInRow.toString());
                        double price = Double.parseDouble(priceInRow.toString());

                        double totalCost = amount * price;
                        double totalCostRounded = Math.round(totalCost * 100.0) / 100.0;

                        if (selectedCateg[0] != null) {
                            categoryStatisticsText.setText("The Category description:" + selectedCateg[0].getDescription()
                                    + "\n\nTotal cost of the goods in the category: " + totalCategoryCostRounded
                                    + "\n\nTotal cost of the selected goods: " + totalCostRounded);
                        }
                        else {
                            categoryStatisticsText.setText("All categories"
                            + "\n\nTotal cost of the selected goods: " + totalCostRounded);
                        }


                    }
                }
            }
        });

        // Category Statistics
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

        // General statistics information
        JLabel generalStatsLabel = new JLabel("General Statistics");
        generalStatsLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        generalStatsLabel.setBounds(15, 450, 300, 50);



        generalStatisticsText = new JTextArea();
        generalStatisticsText.setEditable(false);
        generalStatisticsText.setMargin(new Insets(15, 10, 15,10));
        generalStatisticsText.setLineWrap(true);
        generalStatisticsText.setWrapStyleWord(true);
        generalStatisticsText.setFont(new Font("Arial", Font.PLAIN, 19));
        updateGeneralStatistics();
        JScrollPane generalStatisticsScrollPane = new JScrollPane(generalStatisticsText);
        generalStatisticsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        generalStatisticsScrollPane.setBounds(15, 490, 460, 150);

        // Button for adding categories and products
        JButton addButton = new JButton();
        addButton.setText("Add");
        addButton.setFont(new Font("Arial", Font.PLAIN, 24));
        addButton.setBounds(520, 490, 200, 50);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(addCategoryFrame != null) {
                    addCategoryFrame.dispose();
                }
                if(addProductFrame != null) {
                    addProductFrame.dispose();
                }
                showAddChoiceWindow();
            }
        });

        // Button for adding categories and products
        JButton removeButton = new JButton("Delete");
        removeButton.setFont(new Font("Arial", Font.PLAIN, 24));
        removeButton.setBounds(750, 490, 200, 50);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(removeCategoryFrame != null) {
                    removeCategoryFrame.dispose();
                }
                if(removeProductFrame != null) {
                    removeProductFrame.dispose();
                }
                showRemoveChoiceWindow();
            }
        });

        // Redacting categories and products
        JButton redactButton = new JButton("Redact");
        redactButton.setFont(new Font("Arial", Font.PLAIN, 24));
        redactButton.setBounds(520, 570, 200, 50);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRedactChoiceWindow();
            }
        });

        // Searching for categories and products
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 24));
        searchButton.setBounds(750, 570, 200, 50);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(searchFrame != null) {
                    searchFrame.dispose();
                }
                showSearchWindow();
            }
        });

        this.add(addButton);
        this.add(removeButton);
        this.add(redactButton);
        this.add(searchButton);
        this.add(generalStatsLabel);
        this.add(generalStatisticsScrollPane);
        this.add(categoryStatisticsLabel);
        this.add(categoryStatisticsScrollPane);
        this.add(goodsCatagoryLabel);
        this.add(goodsLabel);
        this.add(goodsGroupScrollPane);
        this.add(goodsScrollPane);
    }

    private void showRedactChoiceWindow() {
        JFrame redactChoiceFrame = new JFrame("Redacting");
        redactChoiceFrame.setSize(350, 200);
        redactChoiceFrame.setResizable(false);
        redactChoiceFrame.setLayout(null);
        redactChoiceFrame.setLocationRelativeTo(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to redact a category<br> or a product?</html>");
        choiceLabel.setBounds(15, 10, 250, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton redactCategoryButton = new JButton("Redact Category");
        redactCategoryButton.setBounds(25, 100, 130, 30);
        redactCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactChoiceFrame.dispose();
                if(redactCategoryFrame != null) {
                    redactCategoryFrame.dispose();
                }
                showRedactCategoryWindow();
            }
        });

        JButton redactProductButton = new JButton("Redact Product");
        redactProductButton.setBounds(180, 100, 130, 30);
        redactProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactChoiceFrame.dispose();
                if(redactProductFrame != null) {
                    redactProductFrame.dispose();
                }
                showRedactProductWindow();
            }
        });

        redactChoiceFrame.add(redactCategoryButton);
        redactChoiceFrame.add(redactProductButton);
        redactChoiceFrame.add(choiceLabel);
        redactChoiceFrame.setVisible(true);
    }

    private void showRedactCategoryWindow() {
        redactCategoryFrame = new JFrame("Redacting Category");
        redactCategoryFrame.setSize(350, 200);
        redactCategoryFrame.setResizable(false);
        redactCategoryFrame.setLayout(null);
        redactCategoryFrame.setLocationRelativeTo(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to redact a category name<br> or a description?</html>");
        choiceLabel.setBounds(15, 10, 300, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton addCategoryButton = new JButton("<html>Redact Category<br> Name</html>");
        addCategoryButton.setBounds(25, 100, 130, 50);
        addCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactCategoryFrame.dispose();

                showRedactCategoryNameWindow();
            }
        });

        JButton addProductButton = new JButton("<html>Redact Category<br> Description</html>");
        addProductButton.setBounds(180, 100, 130, 50);
        addProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactCategoryFrame.dispose();

                showRedactCategoryDescriptionWindow();
            }
        });

        redactCategoryFrame.add(addCategoryButton);
        redactCategoryFrame.add(addProductButton);
        redactCategoryFrame.add(choiceLabel);
        redactCategoryFrame.setVisible(true);
    }

    private void showRedactCategoryNameWindow() {
        JFrame redactCategoryNameFrame = new JFrame("Redact Category Name");
        redactCategoryNameFrame.setSize(400, 270);
        redactCategoryNameFrame.setResizable(false);
        redactCategoryNameFrame.setLayout(null);
        redactCategoryNameFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Redact Category");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactCategoryNameFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        JTextField newNameField = new JTextField();
        newNameField.setBounds(150, 125, 200, 35);

        JLabel nameLabel = new JLabel("Category Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        redactCategoryNameFrame.add(nameLabel);
        redactCategoryNameFrame.add(nameField);

        JLabel newNameLabel = new JLabel("New Category Name:");
        newNameLabel.setBounds(25, 110, 185, 65);
        redactCategoryNameFrame.add(newNameLabel);
        redactCategoryNameFrame.add(newNameField);

        JButton addButton = new JButton("Redact");
        addButton.setBounds(30, 180, 150, 30);
        redactCategoryNameFrame.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String categoryName = nameField.getText();
                    String categoryNewName = newNameField.getText();
                    Category categoryToUpdate = returnCategoryByName(categoryName);
                    boolean duplicateCategory = false;
                    for(Category c : categories) {
                        if (categoryNewName.equalsIgnoreCase(c.getName())) {
                            duplicateCategory = true;
                            break;
                        }
                    }
                    if (categoryNewName.equalsIgnoreCase("All Categories")) {
                        JOptionPane.showMessageDialog(redactCategoryNameFrame, "Name \"All Categories\" reserved!");
                        return;
                    }
                    if(categoryToUpdate != null && !duplicateCategory && !categoryNewName.isEmpty()) {
                        updateCategoryName(categoryToUpdate, categoryNewName);
                        JOptionPane.showMessageDialog(redactCategoryNameFrame, "Category name updated successfully");
                        addCategoryName();
                        updateCategoryList();
                        updateCategoryList();
                        nameField.setText("");
                        newNameField.setText("");
                    }
                    else if (duplicateCategory) {
                        JOptionPane.showMessageDialog(redactCategoryNameFrame, "Category name already exists!");
                    }
                    else if (!categoryNewName.isEmpty()) {
                        JOptionPane.showMessageDialog(redactCategoryNameFrame, "Category name cannot be empty!");
                    }
                    else {
                        JOptionPane.showMessageDialog(redactCategoryNameFrame, "Category not found");
                        nameField.setText("");
                        newNameField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(redactCategoryNameFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactCategoryNameFrame.dispose();
            }
        });
        redactCategoryNameFrame.add(cancelButton);

        redactCategoryNameFrame.setVisible(true);
    }

    private void showRedactCategoryDescriptionWindow() {
        JFrame redactCategoryDescrFrame = new JFrame("Redact Category Description");
        redactCategoryDescrFrame.setSize(400, 270);
        redactCategoryDescrFrame.setResizable(false);
        redactCategoryDescrFrame.setLayout(null);
        redactCategoryDescrFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Redact Category");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactCategoryDescrFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        JTextField newDescriptionField = new JTextField();
        newDescriptionField.setBounds(180, 125, 170, 35);

        JLabel nameLabel = new JLabel("Category Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        redactCategoryDescrFrame.add(nameLabel);
        redactCategoryDescrFrame.add(nameField);

        JLabel newDescrLabel = new JLabel("New Category Description:");
        newDescrLabel.setBounds(25, 110, 185, 65);
        redactCategoryDescrFrame.add(newDescrLabel);
        redactCategoryDescrFrame.add(newDescriptionField);

        JButton addButton = new JButton("Redact");
        addButton.setBounds(30, 180, 150, 30);
        redactCategoryDescrFrame.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String categoryName = nameField.getText();
                    String categoryNewDescription = newDescriptionField.getText();
                    Category categoryToUpdate = returnCategoryByName(categoryName);
                    if(categoryToUpdate != null && !categoryNewDescription.isEmpty()) {
                       updateCategoryDescription(categoryToUpdate, categoryNewDescription);
                        JOptionPane.showMessageDialog(redactCategoryDescrFrame, "Category description updated successfully");
                        categoryStatisticsText.setText("The Category description: " + categoryNewDescription
                                + "\n\nTotal cost of the goods in the category: " + totalCategoryCostRounded);

                    }
                    else if (categoryNewDescription.isEmpty()) {
                        JOptionPane.showMessageDialog(redactCategoryDescrFrame, "Category description cannot be empty!");
                        nameField.setText("");
                        newDescriptionField.setText("");
                    }
                    else {
                        JOptionPane.showMessageDialog(redactCategoryDescrFrame, "Category not found");
                        nameField.setText("");
                        newDescriptionField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(redactCategoryDescrFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactCategoryDescrFrame.dispose();
            }
        });
        redactCategoryDescrFrame.add(cancelButton);

        redactCategoryDescrFrame.setVisible(true);
    }

    private void showRedactProductWindow() {
        redactProductFrame = new JFrame("Redact Product");
        redactProductFrame.setSize(400, 300);
        redactProductFrame.setResizable(false);
        redactProductFrame.setLayout(null);
        redactProductFrame.setLocationRelativeTo(null);
        JLabel titleField = new JLabel("What to redact?");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactProductFrame.add(titleField);

        JButton redactName = new JButton("Redact Name");
        redactName.setFont(new Font("Arial", Font.PLAIN, 18));
        redactName.setBounds(25, 70, 160, 30);
        redactName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProductFrame.dispose();
                showRedactNameWindow();
            }
        });

        JButton redactDescr = new JButton("Redact Description");
        redactDescr.setFont(new Font("Arial", Font.PLAIN, 14));
        redactDescr.setBounds(200, 70, 160, 30);
        redactDescr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProductFrame.dispose();
                showRedactDescriptionWindow();
            }
        });

        JButton redactAmount = new JButton("Redact Amount");
        redactAmount.setFont(new Font("Arial", Font.PLAIN, 18));
        redactAmount.setBounds(25, 110, 160, 30);
        redactAmount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProductFrame.dispose();
                showRedactAmountWindow();
            }
        });

        JButton redactPrice = new JButton("Redact Price");
        redactPrice.setFont(new Font("Arial", Font.PLAIN, 18));
        redactPrice.setBounds(200, 110, 160, 30);
        redactPrice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProductFrame.dispose();
                showRedactPriceWindow();
            }
        });

        JButton redactProducer = new JButton("Redact Producer");
        redactProducer.setBounds(105, 150, 180, 30);
        redactProducer.setFont(new Font("Arial", Font.PLAIN, 17));
        redactProducer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProductFrame.dispose();
                showRedactProducerWindow();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(125, 200, 150, 30);
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 18));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProductFrame.dispose();
            }
        });

        redactProductFrame.setVisible(true);
        redactProductFrame.add(redactName);
        redactProductFrame.add(redactDescr);
        redactProductFrame.add(redactProducer);
        redactProductFrame.add(redactAmount);
        redactProductFrame.add(redactPrice);
        redactProductFrame.add(cancelButton);
    }

    private void showRedactNameWindow() {
        JFrame redactNameFrame = new JFrame("Redact Name");
        redactNameFrame.setSize(400, 270);
        redactNameFrame.setResizable(false);
        redactNameFrame.setLayout(null);
        redactNameFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Redact Product");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactNameFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        redactNameFrame.add(nameField);
        JTextField newNameField = new JTextField();
        newNameField.setBounds(150, 125, 200, 35);
        redactNameFrame.add(newNameField);


        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        redactNameFrame.add(nameLabel);

        JLabel newNameLabel = new JLabel("New Product Name:");
        newNameLabel.setBounds(25, 110, 185, 65);
        redactNameFrame.add(newNameLabel);

        JButton redactButton = new JButton("Redact");
        redactButton.setBounds(30, 180, 150, 30);
        redactNameFrame.add(redactButton);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    String newName = newNameField.getText();
                        Product productToUpdate = returnProductByName(name);
                        boolean duplicate = false;
                        for(Category c : categories) {
                            for(Product p : c.getProducts()) {
                                if (newName.equalsIgnoreCase(p.getName())) {
                                    duplicate = true;
                                    break;
                                }
                            }
                        }
                        if (productToUpdate != null && !duplicate && !newName.isEmpty()) {
                            updateProductData(productToUpdate, returnCategoryByProduct(productToUpdate), newName, productToUpdate.getDescription(), productToUpdate.getProducer(), productToUpdate.getAmountInStock(), productToUpdate.getPrice());
                            refreshGoodsTable();
                            JOptionPane.showMessageDialog(redactProductFrame, "Product name updated successfully");
                        }
                        else if (duplicate) {
                            JOptionPane.showMessageDialog(redactProductFrame, "Product already exists!");
                        }
                        else if (newName.isEmpty()) {
                            JOptionPane.showMessageDialog(redactProductFrame, "New Product Name is empty!");
                        }
                        else {
                            JOptionPane.showMessageDialog(redactProductFrame, "Product not found");
                        }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(redactNameFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactNameFrame.dispose();
            }
        });
        redactNameFrame.add(cancelButton);

        redactNameFrame.setVisible(true);
    }

    private void showRedactDescriptionWindow() {
        JFrame redactDescriptionFrame = new JFrame("Redact Description");
        redactDescriptionFrame.setSize(400, 270);
        redactDescriptionFrame.setResizable(false);
        redactDescriptionFrame.setLayout(null);
        redactDescriptionFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Redact Product");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactDescriptionFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        redactDescriptionFrame.add(nameField);
        JTextField newDescrField = new JTextField();
        newDescrField.setBounds(180, 125, 170, 35);
        redactDescriptionFrame.add(newDescrField);


        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        redactDescriptionFrame.add(nameLabel);

        JLabel newNameLabel = new JLabel("New Product Description:");
        newNameLabel.setBounds(25, 110, 185, 65);
        redactDescriptionFrame.add(newNameLabel);

        JButton redactButton = new JButton("Redact");
        redactButton.setBounds(30, 180, 150, 30);
        redactDescriptionFrame.add(redactButton);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    String newDescription = newDescrField.getText();

                        Product productToUpdate = returnProductByName(name);
                        if (productToUpdate != null && !newDescription.isEmpty()) {
                            updateProductData(productToUpdate, returnCategoryByProduct(productToUpdate), productToUpdate.getName(), newDescription, productToUpdate.getProducer(), productToUpdate.getAmountInStock(), productToUpdate.getPrice());
                            refreshGoodsTable();
                            JOptionPane.showMessageDialog(redactProductFrame, "Product description updated successfully");
                        }
                        else if (newDescription.isEmpty()) {
                            JOptionPane.showMessageDialog(redactProductFrame, "New Product Description is empty!");
                        }
                        else {
                            JOptionPane.showMessageDialog(redactProductFrame, "Product not found");
                        }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(redactDescriptionFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactDescriptionFrame.dispose();
            }
        });
        redactDescriptionFrame.add(cancelButton);

        redactDescriptionFrame.setVisible(true);
    }

    private void showRedactAmountWindow() {
        JFrame redactAmountFrame = new JFrame("Redact Amount");
        redactAmountFrame.setSize(350, 200);
        redactAmountFrame.setResizable(false);
        redactAmountFrame.setLayout(null);
        redactAmountFrame.setLocationRelativeTo(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to add or write-off<br> an amount of products?</html>");
        choiceLabel.setBounds(35, 10, 250, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 17));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        redactAmountFrame.add(choiceLabel);
        redactAmountFrame.setVisible(true);

        JButton addProductsButton = new JButton("Add Product");
        addProductsButton.setBounds(15, 100, 150, 30);
        addProductsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactAmountFrame.dispose();
                showAddAmountWindow();
            }
        });

        JButton removeProductButton = new JButton("Write-off Product");
        removeProductButton.setBounds(170, 100, 150, 30);
        removeProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactAmountFrame.dispose();
                showWriteOffAmountWindow();
            }
        });
        redactAmountFrame.add(addProductsButton);
        redactAmountFrame.add(removeProductButton);
    }

    private void showAddAmountWindow() {
        JFrame addAmountFrame = new JFrame("Redact Amount");
        addAmountFrame.setSize(400, 270);
        addAmountFrame.setResizable(false);
        addAmountFrame.setLayout(null);
        addAmountFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Add Amount");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        addAmountFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        addAmountFrame.add(nameField);
        JTextField newAmountField = new JTextField();
        newAmountField.setBounds(150, 125, 200, 35);
        addAmountFrame.add(newAmountField);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        addAmountFrame.add(nameLabel);

        JLabel newNameLabel = new JLabel("Amount to add:");
        newNameLabel.setBounds(25, 110, 185, 65);
        addAmountFrame.add(newNameLabel);

        JButton redactButton = new JButton("Redact");
        redactButton.setBounds(30, 180, 150, 30);
        addAmountFrame.add(redactButton);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    int AmountToAdd = Integer.parseInt(newAmountField.getText());

                    Product productToUpdate = returnProductByName(name);
                    if (productToUpdate != null && AmountToAdd > 0) {
                        int newAmount = AmountToAdd + productToUpdate.getAmountInStock();
                        updateProductData(productToUpdate, returnCategoryByProduct(productToUpdate), productToUpdate.getName(), productToUpdate.getDescription(), productToUpdate.getProducer(), newAmount, productToUpdate.getPrice());
                        updateGeneralStatistics();
                        refreshGoodsTable();
                        JOptionPane.showMessageDialog(addAmountFrame, "Product amount increased by " + AmountToAdd);
                    } else if (AmountToAdd == 0) {
                        JOptionPane.showMessageDialog(addAmountFrame, "Amount not updated");
                    } else if (AmountToAdd < 0) {
                        JOptionPane.showMessageDialog(addAmountFrame, "The number cannot be negative!");
                    }
                    else {
                        JOptionPane.showMessageDialog(addAmountFrame, "Product not found");
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(addAmountFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAmountFrame.dispose();
            }
        });
        addAmountFrame.add(cancelButton);

        addAmountFrame.setVisible(true);
    }

    private void showWriteOffAmountWindow() {
        JFrame writeOffAmountFrame = new JFrame("Redact Amount");
        writeOffAmountFrame.setSize(400, 270);
        writeOffAmountFrame.setResizable(false);
        writeOffAmountFrame.setLayout(null);
        writeOffAmountFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Write-off Amount");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        writeOffAmountFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        writeOffAmountFrame.add(nameField);
        JTextField newAmountField = new JTextField();
        newAmountField.setBounds(150, 125, 200, 35);
        writeOffAmountFrame.add(newAmountField);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        writeOffAmountFrame.add(nameLabel);

        JLabel newNameLabel = new JLabel("Amount to write-off:");
        newNameLabel.setBounds(25, 110, 185, 65);
        writeOffAmountFrame.add(newNameLabel);

        JButton redactButton = new JButton("Redact");
        redactButton.setBounds(30, 180, 150, 30);
        writeOffAmountFrame.add(redactButton);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    int AmountToWriteOff = Integer.parseInt(newAmountField.getText());

                    Product productToUpdate = returnProductByName(name);
                    if (productToUpdate != null && AmountToWriteOff > 0) {
                        int newAmount = productToUpdate.getAmountInStock() - AmountToWriteOff;
                        if (newAmount < 0) {
                            JOptionPane.showMessageDialog(writeOffAmountFrame, "Amount cannot be negative!");
                            return;
                        }
                        updateProductData(productToUpdate, returnCategoryByProduct(productToUpdate), productToUpdate.getName(), productToUpdate.getDescription(), productToUpdate.getProducer(), newAmount, productToUpdate.getPrice());
                        updateGeneralStatistics();
                        refreshGoodsTable();
                        JOptionPane.showMessageDialog(writeOffAmountFrame,  AmountToWriteOff + " units of the Product have been written off.");
                    } else if (AmountToWriteOff == 0) {
                        JOptionPane.showMessageDialog(writeOffAmountFrame, "Amount not updated");
                    }
                    else if (AmountToWriteOff < 0){
                        JOptionPane.showMessageDialog(writeOffAmountFrame, "Cannot write-off negative amount!");
                    }else {
                        JOptionPane.showMessageDialog(writeOffAmountFrame, "Product not found");
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(writeOffAmountFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeOffAmountFrame.dispose();
            }
        });
        writeOffAmountFrame.add(cancelButton);

        writeOffAmountFrame.setVisible(true);
    }

    private void showRedactPriceWindow() {
        JFrame redactPriceFrame = new JFrame("Redact Price");
        redactPriceFrame.setSize(400, 270);
        redactPriceFrame.setResizable(false);
        redactPriceFrame.setLayout(null);
        redactPriceFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Redact Product");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactPriceFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        redactPriceFrame.add(nameField);
        JTextField newPriceField = new JTextField();
        newPriceField.setBounds(150, 125, 200, 35);
        redactPriceFrame.add(newPriceField);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        redactPriceFrame.add(nameLabel);

        JLabel newNameLabel = new JLabel("New Product Price:");
        newNameLabel.setBounds(25, 110, 185, 65);
        redactPriceFrame.add(newNameLabel);

        JButton redactButton = new JButton("Redact");
        redactButton.setBounds(30, 180, 150, 30);
        redactPriceFrame.add(redactButton);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    double newPrice = Double.parseDouble(newPriceField.getText());

                        Product productToUpdate = returnProductByName(name);
                        if (productToUpdate != null && newPrice > 0) {
                            updateProductData(productToUpdate, returnCategoryByProduct(productToUpdate), productToUpdate.getName(), productToUpdate.getDescription(), productToUpdate.getProducer(), productToUpdate.getAmountInStock(), newPrice);
                            updateGeneralStatistics();
                            refreshGoodsTable();
                            JOptionPane.showMessageDialog(redactProductFrame, "Product price updated successfully");
                        } else if (newPrice == 0) {
                            JOptionPane.showMessageDialog(redactProductFrame, "Price cannot be 0!");
                        } else if (newPrice < 0) {
                            JOptionPane.showMessageDialog(removeProductFrame, "Price cannot be negative!");
                        }
                        else {
                            JOptionPane.showMessageDialog(redactProductFrame, "Product not found");
                        }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(redactPriceFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactPriceFrame.dispose();
            }
        });
        redactPriceFrame.add(cancelButton);

        redactPriceFrame.setVisible(true);
    }

    private void showRedactProducerWindow() {
        JFrame redactProducerFrame = new JFrame("Redact Producer");
        redactProducerFrame.setSize(400, 270);
        redactProducerFrame.setResizable(false);
        redactProducerFrame.setLayout(null);
        redactProducerFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Redact Product");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(90, 10, 250, 50);
        redactProducerFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 65, 200, 35);
        redactProducerFrame.add(nameField);
        JTextField newProducerField = new JTextField();
        newProducerField.setBounds(180, 125, 170, 35);
        redactProducerFrame.add(newProducerField);


        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        redactProducerFrame.add(nameLabel);

        JLabel newNameLabel = new JLabel("New Product Producer:");
        newNameLabel.setBounds(25, 110, 185, 65);
        redactProducerFrame.add(newNameLabel);

        JButton redactButton = new JButton("Redact");
        redactButton.setBounds(30, 180, 150, 30);
        redactProducerFrame.add(redactButton);
        redactButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    String newProducer = newProducerField.getText();

                        Product productToUpdate = returnProductByName(name);
                        if (productToUpdate != null && !newProducer.isEmpty() && !name.isEmpty()) {
                            updateProductData(productToUpdate, returnCategoryByProduct(productToUpdate), productToUpdate.getName(), productToUpdate.getDescription(), newProducer, productToUpdate.getAmountInStock(), productToUpdate.getPrice());
                            refreshGoodsTable();
                            JOptionPane.showMessageDialog(redactProductFrame, "Product producer updated successfully");
                        }
                        else if (name.isEmpty()) {
                            JOptionPane.showMessageDialog(redactProductFrame, "Product name cannot be empty!");
                        }
                        else if (newProducer.isEmpty()) {
                                JOptionPane.showMessageDialog(redactProductFrame, "New producer cannot be empty!");
                            }
                        else {
                            JOptionPane.showMessageDialog(redactProductFrame, "Product not found");
                        }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(redactProducerFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 180, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redactProducerFrame.dispose();
            }
        });
        redactProducerFrame.add(cancelButton);

        redactProducerFrame.setVisible(true);
    }

    private void addCategoryName() {
        categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
    }

    private void showAddChoiceWindow() {
        JFrame addChoiceFrame = new JFrame("Add");
        addChoiceFrame.setSize(350, 200);
        addChoiceFrame.setResizable(false);
        addChoiceFrame.setLayout(null);
        addChoiceFrame.setLocationRelativeTo(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to add a category<br> or a product?</html>");
        choiceLabel.setBounds(15, 10, 250, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton addCategoryButton = new JButton("Add Category");
        addCategoryButton.setBounds(25, 100, 130, 30);
        addCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addChoiceFrame.dispose();
                showAddCategoryWindow();
            }
        });

        JButton addProductButton = new JButton("Add Product");
        addProductButton.setBounds(180, 100, 130, 30);
        addProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addChoiceFrame.dispose();
                showAddProductWindow();
            }
        });

        addChoiceFrame.add(addCategoryButton);
        addChoiceFrame.add(addProductButton);
        addChoiceFrame.add(choiceLabel);
        addChoiceFrame.setVisible(true);
    }

    private void showAddCategoryWindow() {
        addCategoryFrame = new JFrame("Add Category");
        addCategoryFrame.setSize(400, 270);
        addCategoryFrame.setResizable(false);
        addCategoryFrame.setLayout(null);
        addCategoryFrame.setLocationRelativeTo(null);

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
                    if(checkCategory == null && !name.equalsIgnoreCase("All Categories")) {
                        Category categoryToAdd = new Category(name, desc);
                        if(categoryToAdd.getName().isEmpty()) {
                            JOptionPane.showMessageDialog(addCategoryFrame, "Enter the name of the Category");
                            return;
                        }
                        if (categoryToAdd.getDescription().isEmpty()) {
                            JOptionPane.showMessageDialog(addCategoryFrame, "Enter the description of the Category");
                            return;
                        }
                        addCategory(categoryToAdd);
                        updateGeneralStatistics();
                        JOptionPane.showMessageDialog(addCategoryFrame, "Category added successfully!");
                        nameField.setText("");
                        descField.setText("");
                        addCategoryName();
                        updateCategoryList();
        //              addCategoryFrame.dispose();
                    } else if (name.equalsIgnoreCase("All Categories")) {
                        JOptionPane.showMessageDialog(addCategoryFrame, "Name \"All Categories\" reserved!");
                        nameField.setText("");
                        descField.setText("");
                    }
                    else {
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
        addProductFrame.setResizable(false);
        addProductFrame.setLayout(null);
        addProductFrame.setLocationRelativeTo(null);

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

                    Category checkCategory = returnCategoryByName(categoryName);
                    if (checkCategory != null) {
                        if(!productExists(checkCategory, name) && !returnAllCategoryProductNames().contains(name.toLowerCase()) && amount >= 0 && price > 0 && !name.isEmpty() && !desc.isEmpty() && !producer.isEmpty()) {
                            Product p = new Product(name, desc, producer, amount, price);
                            addProduct(p, checkCategory);
                            updateGeneralStatistics();
                            refreshGoodsTable();
                            JOptionPane.showMessageDialog(addProductFrame, "Product added successfully!");
                        }
                        else if (amount < 0) {
                            JOptionPane.showMessageDialog(addProductFrame, "Amount cannot be negative!");
                        }
                        else if (price <= 0) {
                            JOptionPane.showMessageDialog(addProductFrame, "Price cannot be less than 0!");
                        }
                        else if (name.isEmpty() || desc.isEmpty() || producer.isEmpty()) {
                            JOptionPane.showMessageDialog(addProductFrame, "Fields cannot be empty!");
                        }
                        else {
                            JOptionPane.showMessageDialog(addProductFrame, "Product already exists!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(addProductFrame, "Category not found");
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
        JFrame removeChoiceFrame = new JFrame("Write-off");
        removeChoiceFrame.setSize(350, 200);
        removeChoiceFrame.setResizable(false);
        removeChoiceFrame.setLayout(null);
        removeChoiceFrame.setLocationRelativeTo(null);
        JLabel choiceLabel = new JLabel("<html>Do you want to write-off a product<br> or remove a category?</html>");
        choiceLabel.setBounds(15, 10, 250, 70);
        choiceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        choiceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        removeChoiceFrame.add(choiceLabel);
        removeChoiceFrame.setVisible(true);

        JButton removeCategoryButton = new JButton("Remove Category");
        removeCategoryButton.setBounds(25, 100, 130, 30);
        removeCategoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeChoiceFrame.dispose();
                showRemoveCategoryWindow();
            }
        });

        JButton removeProductButton = new JButton("Remove Product");
        removeProductButton.setBounds(180, 100, 130, 30);
        removeProductButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeChoiceFrame.dispose();
                showRemoveProductWindow();
            }
        });
        removeChoiceFrame.add(removeCategoryButton);
        removeChoiceFrame.add(removeProductButton);
    }

    private void showRemoveCategoryWindow() {
        removeCategoryFrame = new JFrame("Remove Category");
        removeCategoryFrame.setSize(400, 200);
        removeCategoryFrame.setResizable(false);
        removeCategoryFrame.setLayout(null);
        removeCategoryFrame.setLocationRelativeTo(null);

        JLabel titleField = new JLabel("Remove Category");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(70, 10, 250, 50);
        removeCategoryFrame.add(titleField);

        JTextField nameField = new JTextField();
        nameField.setBounds(130, 65, 220, 35);
        JTextField descField = new JTextField();
        descField.setBounds(130, 125, 220, 35);

        JLabel nameLabel = new JLabel("Category Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        removeCategoryFrame.add(nameLabel);
        removeCategoryFrame.add(nameField);

        JButton addButton = new JButton("Remove");
        addButton.setBounds(30, 120, 150, 30);
        removeCategoryFrame.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();

                    Category checkCategory = returnCategoryByName(name);
                    if(checkCategory != null) {
                        deleteCategory(checkCategory);
                        updateGeneralStatistics();
                        JOptionPane.showMessageDialog(removeCategoryFrame, "Category removed successfully!");
                        goodsArea.clearSelection();
                        nameField.setText("");
                        addCategoryName();
                        updateCategoryList();
                        goodsArea.setVisible(false);
                        //              addCategoryFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(removeCategoryFrame, "Category not found");
                        nameField.setText("");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(addProductFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 120, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeCategoryFrame.dispose();
            }
        });
        removeCategoryFrame.add(cancelButton);

        removeCategoryFrame.setVisible(true);
    }

    private void showRemoveProductWindow() {
        removeProductFrame = new JFrame("Write-off Product");
        removeProductFrame.setSize(400, 220);
        removeProductFrame.setLayout(null);
        removeProductFrame.setLocationRelativeTo(null);
        removeProductFrame.setResizable(false);

        JTextField nameField = new JTextField();
        nameField.setBounds(130, 65, 220, 35);

        JLabel titleField = new JLabel("Write-off Product");
        titleField.setFont(new Font("Arial", Font.PLAIN, 28));
        titleField.setBounds(110, 10, 200, 30);
        removeProductFrame.add(titleField);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(25, 50, 185, 65);
        removeProductFrame.add(nameLabel);
        removeProductFrame.add(nameField);


        JButton addButton = new JButton("Write-off Product");
        addButton.setBounds(30, 120, 150, 30);
        removeProductFrame.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String productName = nameField.getText();

                        Product checkProduct = returnProductByName(productName);
                        if(checkProduct != null) {
                            deleteProduct(checkProduct);
                            updateGeneralStatistics();
                            refreshGoodsTable();
                            JOptionPane.showMessageDialog(removeProductFrame, "Product removed successfully!");
                        } else {
                            JOptionPane.showMessageDialog(removeProductFrame, "Product not found");
                        }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(removeProductFrame, "Error: " + ex.getMessage());
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 120, 150, 30);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeProductFrame.dispose();
            }
        });
        removeProductFrame.add(cancelButton);

        removeProductFrame.setVisible(true);
    }

    private void showSearchWindow() {
        searchFrame = new JFrame("Search");
        searchFrame.setSize(600, 550);
        searchFrame.setResizable(false);
        searchFrame.setLayout(null);
        searchFrame.setLocationRelativeTo(null);

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 28));
        searchLabel.setBounds(245, 7, 185, 40);

        ArrayList<String> categoriesNames = new ArrayList<>();
        categoriesNames.add("All Categories");
        for (Category categName : categories) {
            categoriesNames.add(categName.getName());
        }
        String[] categoriesArray = categoriesNames.toArray(new String[0]);

        JLabel categoryLabel = new JLabel("Choose the Category:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        categoryLabel.setBounds(15, 45, 285, 55);

        JComboBox<String> categoriesComboBox = new JComboBox<>(categoriesArray);
        categoriesComboBox.setBounds(15, 85, 555, 35);

        JLabel searchLabel2 = new JLabel("Enter your search query");
        searchLabel2.setFont(new Font("Arial", Font.PLAIN, 19));
        searchLabel2.setBounds(15, 115, 285, 55);

        JTextField searchField = new JTextField();
        searchField.setBounds(15, 155, 555, 35);

        JLabel foundLabel = new JLabel("Found Goods");
        foundLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        foundLabel.setBounds(15, 185, 285, 55);

        JTable foundGoods = new JTable();
        JScrollPane foundGoodsScrollPane = new JScrollPane(foundGoods);
        foundGoodsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        foundGoodsScrollPane.setBounds(15, 225, 555, 200);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 24));
        searchButton.setBounds(210, 450, 170, 50);
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedCategory = (String) categoriesComboBox.getSelectedItem();
                String searchQuery = searchField.getText();

                if ("All Categories".equals(selectedCategory)) {
                    ArrayList<Product> allProducts = new ArrayList<>();
                    for (Category category : categories) {
                        allProducts.addAll(category.getProducts());
                    }

                    ArrayList<Product> foundProducts = new ArrayList<>();
                    for (Product product : allProducts) {
                        if (product.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                product.getDescription().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                product.getProducer().toLowerCase().contains(searchQuery.toLowerCase())) {
                            foundProducts.add(product);
                        }
                    }

                    String[][] data = new String[foundProducts.size()][5];
                    for (int i = 0; i < foundProducts.size(); i++) {
                        Product p = foundProducts.get(i);
                        data[i][0] = p.getName();
                        data[i][1] = p.getDescription();
                        data[i][2] = p.getProducer();
                        data[i][3] = String.valueOf(p.getAmountInStock());
                        data[i][4] = String.valueOf(p.getPrice());
                    }

                    String[] property = {"Name", "Description", "Producer", "Remained", "Price per piece"};
                    DefaultTableModel model = new DefaultTableModel(data, property) {
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    foundGoods.setModel(model);
                }
                else {
                    Category checkCategory = returnCategoryByName(selectedCategory);
                    if (checkCategory != null) {
                        ArrayList<Product> products = searchForProducts(checkCategory, searchQuery);

                        String[][] data = new String[products.size()][5];
                        for (int i = 0; i < products.size(); i++) {
                            Product p = products.get(i);
                            data[i][0] = p.getName();
                            data[i][1] = p.getDescription();
                            data[i][2] = p.getProducer();
                            data[i][3] = String.valueOf(p.getAmountInStock());
                            data[i][4] = String.valueOf(p.getPrice());
                        }

                        String[] property = {"Name", "Description", "Producer", "Remained", "Price per piece"};
                        DefaultTableModel model = new DefaultTableModel(data, property) {
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };
                        foundGoods.setModel(model);
                    }
                }
            }
        });

        searchFrame.add(searchLabel);
        searchFrame.add(categoryLabel);
        searchFrame.add(categoriesComboBox);
        searchFrame.add(searchLabel2);
        searchFrame.add(searchField);
        searchFrame.add(foundLabel);
        searchFrame.add(foundGoodsScrollPane);
        searchFrame.add(searchButton);
        searchFrame.setVisible(true);
    }

    private void refreshGoodsTable() {
        goodsArea.setVisible(true);
        String selected = (String) goodsGroupList.getSelectedValue();
        if (selected == null) return;

        if (selected.equals("All Categories")) {
            selectedCateg[0] = null;
            ArrayList<Product> allProducts = new ArrayList<>();
            for (Category c : categories) {
                allProducts.addAll(c.getProducts());
            }
            String[][] data = new String[allProducts.size()][5];
            for (int i = 0; i < allProducts.size(); i++) {
                Product p = allProducts.get(i);
                data[i][0] = p.getName();
                data[i][1] = p.getDescription();
                data[i][2] = p.getProducer();
                data[i][3] = String.valueOf(p.getAmountInStock());
                data[i][4] = String.valueOf(p.getPrice());
            }
            String[] property = {"Name", "Description", "Producer", "Remained", "Price per piece"};
            DefaultTableModel model = new DefaultTableModel(data, property) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            goodsArea.setModel(model);

            categoryStatisticsText.setText("All categories");
        }

        // Searching for a category
        Category selectedCategory = null;
        for (Category c : categories) {
            if (c.getName().equals(selected)) {
                selectedCategory = c;
                selectedCateg[0] = c;
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
            DefaultTableModel model = new DefaultTableModel(data, property) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            goodsArea.setModel(model);

            double totalCategoryCost = 0;
            for(Product p : selectedCategory.getProducts()) {
                totalCategoryCost += p.getAmountInStock() * p.getPrice();
            }
            totalCategoryCostRounded = Math.round(totalCategoryCost * 100.0) / 100.0;

            categoryStatisticsText.setText("The Category description:" + selectedCategory.getDescription()
                    + "\n\nTotal cost of the goods in the category: " + totalCategoryCostRounded);
        }
    }

    public static void main(String[] args) {
        loadAllData();

        Main window = new Main();
        window.setVisible(true);
    }
}