package inventoryshoe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres"; 
    private final String password = "0274"; 

    // --- EXISTING METHODS ---

    public void addShoe(Shoe shoe) throws Exception {
        String brandPart = shoe.getBrand().length() >= 3 ? shoe.getBrand().substring(0, 3) : shoe.getBrand();
        String modelPart = shoe.getModel().length() >= 2 ? shoe.getModel().substring(0, 2) : shoe.getModel();
        String generatedSku = (brandPart + "-" + modelPart + "-" + shoe.getSize()).toUpperCase();

        String sql = "INSERT INTO inventory_shoes (sku, brand, model, shoe_size, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, generatedSku);
            pstmt.setString(2, shoe.getBrand());
            pstmt.setString(3, shoe.getModel());
            pstmt.setDouble(4, shoe.getSize());
            pstmt.setInt(5, shoe.getStock());
            pstmt.executeUpdate();
        }
    }

    public List<Shoe> getAllShoes() {
        List<Shoe> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_shoes ORDER BY created_at DESC";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Shoe(
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getDouble("shoe_size"),
                    rs.getInt("stock_quantity")
                ));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return list;
    }

    public double calculateTotalAssetValue() {
        double totalValue = 0.0;
        String sql = "SELECT SUM(quantity_remaining * purchase_price) as total_value FROM purchase_batches";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) { totalValue = rs.getDouble("total_value"); }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return totalValue;
    }

    // --- NEW: FIFO SELL METHOD ---

    public void sellShoe(String brand, String model, double size, int quantityToSell) throws Exception {
        // 1. Reconstruct the SKU to find the batches
        String brandPart = brand.length() >= 3 ? brand.substring(0, 3) : brand;
        String modelPart = model.length() >= 2 ? model.substring(0, 2) : model;
        String sku = (brandPart + "-" + modelPart + "-" + size).toUpperCase();

        // SQL Queries
        String selectBatches = "SELECT batch_id, quantity_remaining FROM purchase_batches " +
                               "WHERE sku = ? AND quantity_remaining > 0 ORDER BY purchased_at ASC";
        String updateBatch = "UPDATE purchase_batches SET quantity_remaining = quantity_remaining - ? WHERE batch_id = ?";
        String updateMainInventory = "UPDATE inventory_shoes SET stock_quantity = stock_quantity - ? WHERE sku = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false); // Start Transaction

            try {
                int needed = quantityToSell;

                // A. Update the Batches (FIFO)
                try (PreparedStatement pstSelect = conn.prepareStatement(selectBatches)) {
                    pstSelect.setString(1, sku);
                    ResultSet rs = pstSelect.executeQuery();

                    while (rs.next() && needed > 0) {
                        int batchId = rs.getInt("batch_id");
                        int inBatch = rs.getInt("quantity_remaining");
                        int take = Math.min(needed, inBatch);

                        try (PreparedStatement pstUpdateBatch = conn.prepareStatement(updateBatch)) {
                            pstUpdateBatch.setInt(1, take);
                            pstUpdateBatch.setInt(2, batchId);
                            pstUpdateBatch.executeUpdate();
                        }
                        needed -= take;
                    }
                }

                if (needed > 0) {
                    throw new Exception("Insufficient stock in purchase batches to fulfill this sale!");
                }

                // B. Update Main Table
                try (PreparedStatement pstUpdateInv = conn.prepareStatement(updateMainInventory)) {
                    pstUpdateInv.setInt(1, quantityToSell);
                    pstUpdateInv.setString(2, sku);
                    pstUpdateInv.executeUpdate();
                }

                conn.commit(); // Success!
            } catch (Exception e) {
                conn.rollback(); // Failure! Undo everything.
                throw e;
            }
        }
    }
}