import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    private ArrayList<Category> categories = new ArrayList<>();
    private static final String options = "Enter 0 to change name; Enter 1 to add; Enter 2 to update; Enter 3 to delete; Enter 4 to exit";

    public static boolean contains(String[] arr, String target) {
        for (String str : arr) {
            if (str.equals(target)) return true;
        }
        return false;
    }

    public static void main(String[] args) {

    }

    public void addCategory(Category category) {
        ArrayList<String> existingCategories = readFromFile("Categories.txt");
        if (!existingCategories.contains(category.getName())) {
            categories.add(category);
            appendToFile("Categories.txt", category.getName());

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

    public void deleteCategory(Category category) {
        if (categories.contains(category)) {
            categories.remove(category);
            ArrayList<String> existingCategories = readFromFile("Categories.txt");
            existingCategories.remove(category.getName());
            writeAllToFile("Categories.txt", existingCategories);

            File folder = new File("ProductCategories/");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            for (File file : files) {
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

    public void updateCategoryName(Category category, String newName) {
        if (categories.contains(category)) {
            ArrayList<String> existingCategories = readFromFile("Categories.txt");
            Collections.replaceAll(existingCategories, category.getName(), newName);
            writeAllToFile("Categories.txt", existingCategories);

            File folder = new File("ProductCategories/");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().equals(category.getName()+".txt")) {
                    file.renameTo(new File(folder, newName + ".txt"));
                    break;
                }
            }
        }
        else {
            System.out.println("Category does not exist, cannot delete category");
        }
    }

    public void writeToFile(String fileName, String text) {
        try (FileWriter writer = new FileWriter(fileName)){
            writer.write(text + "\n");
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void appendToFile(String fileName, String text) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(text + "\n");
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void writeAllToFile(String fileName, ArrayList<String> lines) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<String> readFromFile(String fileName) {
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
}