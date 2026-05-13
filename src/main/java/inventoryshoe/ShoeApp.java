package inventoryshoe;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShoeApp extends Application {
    private final InventoryService service = new InventoryService();
    private final TableView<Shoe> table = new TableView<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Professional Inventory System v2.0");

        // UI Components
        TextField bIn = new TextField(); bIn.setPromptText("Brand");
        TextField mIn = new TextField(); mIn.setPromptText("Model");
        TextField sIn = new TextField(); sIn.setPromptText("Size");
        TextField qIn = new TextField(); qIn.setPromptText("Stock");
        Button addBtn = new Button("Add Entry");

        TextField sellIn = new TextField(); sellIn.setPromptText("Qty to Sell");
        Button sellBtn = new Button("Record FIFO Sale");
        Button valBtn = new Button("Total Asset Valuation");
        valBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        // Table setup
        TableColumn<Shoe, String> col1 = new TableColumn<>("Brand");
        col1.setCellValueFactory(new PropertyValueFactory<>("brand"));
        TableColumn<Shoe, Integer> col2 = new TableColumn<>("Stock");
        col2.setCellValueFactory(new PropertyValueFactory<>("stock"));
        table.getColumns().addAll(col1, col2);

        // Actions
        addBtn.setOnAction(e -> {
            try {
                service.addShoe(new Shoe(bIn.getText(), mIn.getText(), Double.parseDouble(sIn.getText()), Integer.parseInt(qIn.getText())));
                table.getItems().setAll(service.getAllShoes());
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        sellBtn.setOnAction(e -> {
            Shoe selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    service.sellShoe(selected.getBrand(), selected.getModel(), selected.getSize(), Integer.parseInt(sellIn.getText()));
                    table.getItems().setAll(service.getAllShoes());
                } catch (Exception ex) { showError(ex.getMessage()); }
            }
        });

        valBtn.setOnAction(e -> {
            double total = service.calculateTotalAssetValue();
            new Alert(Alert.AlertType.INFORMATION, "Current Inventory Value: $" + String.format("%.2f", total)).show();
        });

        VBox root = new VBox(10, new Label("Inventory Management"), bIn, mIn, sIn, qIn, addBtn, 
                                 new Separator(), sellIn, sellBtn, valBtn, table);
        root.setPadding(new Insets(20));
        stage.setScene(new Scene(root, 450, 700));
        stage.show();
        table.getItems().setAll(service.getAllShoes());
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    public static void main(String[] args) { launch(args); }
}