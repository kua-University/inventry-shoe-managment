package inventoryshoe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service Layer: Handles the business logic and database transactions.
 * Uses DatabaseConfig for settings and SkuGenerator for ID logic.
 */
public class InventoryService {

    /**
     * Adds a new shoe type to the main inventory table.
     * Uses SkuGenerator to ensure consistent ID formatting.
     */
    public void addShoe(Shoe shoe) throws InventoryException {
        String sku = SkuGenerator.generate(shoe.getBrand(), shoe.getModel(), shoe.getSize());
        String sql = "INSERT INTO inventory_shoes (sku, brand, model, shoe_size, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sku);
            pstmt.setString(2, shoe.getBrand());
            pstmt.setString(3, shoe.getModel());
            pstmt.setDouble(4, shoe.getSize());
            pstmt.setInt(5, shoe.getStock());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            // Wraps low-level SQL errors into a user-friendly Custom Exception
            throw new InventoryException("Database error while adding shoe: " + e.getMessage());
        }
    }

    /**
     * Records a sale using FIFO (First-In, First-Out) logic.
     * It depletes the oldest purchase batches first before updating the main stock.
     */
    public void sellShoe(String brand, String model, double size, int quantityToSell) throws InventoryException {
        String sku = SkuGenerator.generate(brand, model, size);
        
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASS)) {
            // Architectural Tool: Transaction Management
            conn.setAutoCommit(false); 

            try {
                // 1. Identify the oldest batches with remaining stock
                String selectBatches = "SELECT batch_id, quantity_remaining FROM purchase_batches " +
                                       "WHERE sku = ? AND quantity_remaining > 0 ORDER BY purchased_at ASC";
                
                int needed = quantityToSell;

                try (PreparedStatement ps = conn.prepareStatement(selectBatches)) {
                    ps.setString(1, sku);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next() && needed > 0) {
                        int batchId = rs.getInt("batch_id");
                        int inBatch = rs.getInt("quantity_remaining");
                        int take = Math.min(needed, inBatch);

                        // Update the specific batch
                        String updateBatchSql = "UPDATE purchase_batches SET quantity_remaining = quantity_remaining - ? WHERE batch_id = ?";
                        try (PreparedStatement upBatch = conn.prepareStatement(updateBatchSql)) {
                            upBatch.setInt(1, take);
                            upBatch.setInt(2, batchId);
                            upBatch.executeUpdate();
                        }
                        needed -= take;
                    }
                }

                // If we ran out of batches but still need more shoes, the sale cannot be completed
                if (needed > 0) {
                    throw new InventoryException("Insufficient stock in purchase batches to fulfill this sale!");
                }

                // 2. Update the main inventory table to reflect the new total stock
                String updateMainInv = "UPDATE inventory_shoes SET stock_quantity = stock_quantity - ? WHERE sku = ?";
                try (PreparedStatement upMain = conn.prepareStatement(updateMainInv)) {
                    upMain.setInt(1, quantityToSell);
                    upMain.setString(2, sku);
                    upMain.executeUpdate();
                }

                // All steps succeeded, commit the transaction
                conn.commit();

            } catch (Exception e) {
                // If anything went wrong, undo all changes to keep data accurate
                conn.rollback();
                throw new InventoryException("Sale failed and transaction rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new InventoryException("Connection error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all shoes from the database for the UI TableView.
     */
    public List<Shoe> getAllShoes() {
        List<Shoe> list = new ArrayList<>();
        String sql = "SELECT brand, model, shoe_size, stock_quantity FROM inventory_shoes ORDER BY brand ASC";
        
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASS);
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
        } catch (SQLException e) {
            System.err.println("Error fetching inventory: " + e.getMessage());
        }
        return list;
    }

    /**
     * Calculates total inventory value based on the original purchase price of remaining stock.
     */
    public double calculateTotalAssetValue() {
        double totalValue = 0.0;
        String sql = "SELECT SUM(quantity_remaining * purchase_price) as total FROM purchase_batches";
        
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                totalValue = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating asset value: " + e.getMessage());
        }
        return totalValue;
    }
}
