package inventoryshoe;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShoeApp extends Application {
    private final InventoryService service = new InventoryService();
    private final TableView<Shoe> table = new TableView<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Shoe Inventory & FIFO Sales System");

        // --- SECTION 1: ADD NEW STOCK ---
        TextField brandInput = new TextField(); brandInput.setPromptText("Brand");
        TextField modelInput = new TextField(); modelInput.setPromptText("Model");
        TextField sizeInput = new TextField();  sizeInput.setPromptText("Size");
        TextField stockInput = new TextField(); stockInput.setPromptText("Initial Stock");
        Button addButton = new Button("Add New Shoe Type");
        addButton.setMaxWidth(Double.MAX_VALUE);

        // --- SECTION 2: RECORD SALE (New Feature) ---
        TextField sellQtyInput = new TextField(); 
        sellQtyInput.setPromptText("Qty to Sell");
        sellQtyInput.setPrefWidth(100);
        Button sellButton = new Button("Record Sale");
        sellButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        // --- SECTION 3: REPORTS ---
        Button valuationButton = new Button("Calculate FIFO Asset Value");
        valuationButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        valuationButton.setMaxWidth(Double.MAX_VALUE);

        // --- TABLE CONFIGURATION ---
        TableColumn<Shoe, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        TableColumn<Shoe, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        TableColumn<Shoe, Double> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        TableColumn<Shoe, Integer> stockCol = new TableColumn<>("Stock Left");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        table.getColumns().addAll(brandCol, modelCol, sizeCol, stockCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshTable();

        // --- BUTTON LOGIC ---

        // Add Logic
        addButton.setOnAction(e -> {
            try {
                Shoe s = new Shoe(brandInput.getText(), modelInput.getText(), 
                                  Double.parseDouble(sizeInput.getText()), 
                                  Integer.parseInt(stockInput.getText()));
                service.addShoe(s);
                refreshTable();
                brandInput.clear(); modelInput.clear(); sizeInput.clear(); stockInput.clear();
            } catch (Exception ex) { showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR); }
        });

        // Sale Logic (FIFO)
        sellButton.setOnAction(e -> {
            Shoe selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    int qty = Integer.parseInt(sellQtyInput.getText());
                    service.sellShoe(selected.getBrand(), selected.getModel(), selected.getSize(), qty);
                    refreshTable();
                    sellQtyInput.clear();
                    showAlert("Sale Success", "Stock reduced from oldest batches.", Alert.AlertType.INFORMATION);
                } catch (Exception ex) { showAlert("Sale Failed", ex.getMessage(), Alert.AlertType.ERROR); }
            } else {
                showAlert("Selection Required", "Please select a shoe from the table first.", Alert.AlertType.WARNING);
            }
        });

        // Valuation Logic
        valuationButton.setOnAction(e -> {
            double total = service.calculateTotalAssetValue();
            showAlert("Inventory Value", "Total Asset Value: $" + String.format("%.2f", total), Alert.AlertType.INFORMATION);
        });

        // --- LAYOUT ---
        VBox addBox = new VBox(5, new Label("Add New Shoe"), brandInput, modelInput, sizeInput, stockInput, addButton);
        addBox.setPadding(new Insets(10));
        addBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        HBox sellBox = new HBox(10, new Label("Sell Selected:"), sellQtyInput, sellButton);
        sellBox.setPadding(new Insets(10));
        sellBox.setStyle("-fx-background-color: #f9f9f9; -fx-alignment: center-left;");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(
            addBox, 
            sellBox, 
            valuationButton, 
            new Label("Current Inventory:"), table
        );

        stage.setScene(new Scene(mainLayout, 550, 750));
        stage.show();
    }

    private void refreshTable() {
        table.getItems().setAll(service.getAllShoes());
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}