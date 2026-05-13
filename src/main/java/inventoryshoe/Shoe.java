package inventoryshoe;

public class Shoe {
    private String brand, model;
    private double size;
    private int stock;

    public Shoe(String brand, String model, double size, int stock) {
        this.brand = brand; this.model = model; this.size = size; this.stock = stock;
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getSize() { return size; }
    public int getStock() { return stock; }
}