NAME  DANIEL TEKLE
ID  UGR/187996/16
SECTION  1







General Explanation of the App
my Shoe Inventory Management System follows a modern Three-Tier Architecture:

The Presentation Tier (JavaFX): A GUI that allows users to interact with the inventory, add shoes, and generate SKUs.

The Logic Tier (Java/Maven): The "brain" of the app that handles data validation, SKU generation logic, and database communication.

The Data Tier (PostgreSQL): A persistent database running in its own container to ensure shoe data is never lost, even if the app stops.





## 📂 Project Structure

This project follows the **Maven Standard Directory Layout** and is containerized using **Docker**.

```text
inventry-shoe-managment/
├── .github/
│   └── workflows/
│       └── main.yml           # CI/CD Pipeline (GitHub Actions)
├── sql/
│   └── schema.sql             # Database initialization script
├── src/
│   └── main/
│       ├── java/
│       │   └── inventoryshoe/ # Application Package
│       │       ├── ShoeApp.java          # Main GUI Entry Point
│       │       ├── Shoe.java             # Data Model
│       │       ├── InventoryService.java # Business Logic
│       │       ├── DatabaseConfig.java   # DB Connection Logic
│       │       ├── SkuGenerator.java     # Utility Class
│       │       └── InventoryException.java # Custom Error Handling
│       └── resources/         # FXML and CSS files
├── Dockerfile                 # Instructions for the App Container
├── docker-compose.yml         # Orchestration for App + Database
├── pom.xml                    # Maven dependencies and build plugins
└── README.md                  # Project documentation
