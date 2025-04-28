/*
Лабораторна 2
Клас товару. Має поля name, description, producer, amountInStock, та price.
File: Product.java
Author: Artem Gulidov
*/
public class Product {
    //all the product attributes in order
    String name;
    String description;
    String producer;
    int amountInStock;
    double price;

    public Product(String name, String description, String producer, int amountInStock, double price) {
        this.name = name;
        this.description = description;
        this.producer = producer;
        this.amountInStock = amountInStock;
        this.price = price;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProducer() {
        return producer;
    }

    public int getAmountInStock() {
        return amountInStock;
    }

    public double getPrice() {
        return price;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public void setAmountInStock(int amountInStock) {
        this.amountInStock = amountInStock;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    //method for task 9
    public double getFullPrice(){
        return price * amountInStock;
    }

    @Override
    public String toString() {
        return  name + " | " + description + " | " + producer + " | " + amountInStock + " | " + price;
    }
}
