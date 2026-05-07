package inventoryshoe;

public class Shoe {
    private String brand;
    private String model;
    private double size; // Must be double to handle 9.5, 10.5 etc.
    private int stock;   // Must be int

    public Shoe(String brand, String model, double size, int stock) {
        this.brand = brand;
        this.model = model;
        this.size = size;
        this.stock = stock;
    }

    // Getters must match the PropertyValueFactory in ShoeApp
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getSize() { return size; }
    public int getStock() { return stock; }
}