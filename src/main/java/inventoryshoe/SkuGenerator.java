package inventoryshoe;

public class SkuGenerator {
    public static String generate(String brand, String model, double size) {
        String b = brand.length() >= 3 ? brand.substring(0, 3) : brand;
        String m = model.length() >= 2 ? model.substring(0, 2) : model;
        return (b + "-" + m + "-" + size).toUpperCase();
    }
}