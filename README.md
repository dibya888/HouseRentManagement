# House Rent Management System

A modern desktop-based rental property management solution built with JavaFX and SQLite. The application helps landlords and property managers efficiently manage flats, tenants, rent collection, expenses, reports, and user access from a single interface.

## Features

### Dashboard

* Real-time property and financial overview
* Monthly Paid vs Due visualization
* Financial summary cards
* Quick access to important statistics

### Property Management

* Manage buildings and flats
* Track flat information and rental details
* View occupancy status

### Tenant Management

* Add, edit, and remove tenants
* Maintain tenant records
* Associate tenants with specific flats

### Rent Management

* Record rent payments
* Track due amounts
* View payment history
* Read-only Flat Rent display during payment processing

### Expense Management

* Record property-related expenses
* Categorize expenses
* Track monthly spending

### Reports

* Redesigned reporting interface
* Generate financial reports
* Generate tenant reports
* Generate rent collection reports
* Improved usability and organization

### Export Features

* Professional PDF report generation
* Excel report export
* Accurate summaries and calculations
* Improved formatting and presentation

### User Management

* Multiple user accounts
* Role-based access control
* Secure authentication
* Remember Login functionality

### Data Storage

* SQLite database
* User data stored in AppData
* Data preserved across application updates

---

## Technology Stack

* Java 21
* JavaFX 21
* SQLite
* Maven
* Apache PDFBox
* Apache POI
* WiX Toolset
* jpackage

---

## System Requirements

### End Users

* Windows 10 (64-bit) or later
* Windows 11 (64-bit)

No separate Java installation is required when using the installer package.

### Developers

* JDK 21+
* Maven 3.9+
* IntelliJ IDEA (recommended)

---

## Installation

### Using the Installer

1. Download the latest release.
2. Run the installer.
3. Accept the license agreement.
4. Choose an installation directory.
5. Complete the installation wizard.
6. Launch the application from the Desktop or Start Menu.

---

## Building from Source

### Clone Repository

```bash
git clone https://github.com/dibya888/HouseRentManagement.git
cd HouseRentManagement
```

### Build Project

```bash
mvn clean package
```

Generated JAR:

```text
target/HouseRentManagement-3.0.jar
```

---

## Creating Windows Installer

### Requirements

* JDK 21
* WiX Toolset 3.11+

### Package Installer

```powershell
jpackage ^
--type exe ^
--name "House Rent Management System" ^
--app-version 3.0 ^
--vendor "Dibya Software" ^
--description "Offline rental property management system" ^
--input target ^
--dest installer ^
--main-jar HouseRentManagement-3.0.jar ^
--main-class com.rent.main.Launcher ^
--win-menu ^
--win-menu-group "Dibya Software" ^
--win-shortcut ^
--win-dir-chooser ^
--win-upgrade-uuid 6f2a37f8-6e9f-4c1e-a2f7-9f18f7c6b431
```

---

## Version 3.0 Changes

### Dashboard Improvements

* Fixed dashboard pie chart color inconsistency
* Replaced all-time totals with Monthly Paid vs Due visualization

### Rent Payment Improvements

* Added read-only Flat Rent display

### Reports Redesign

* Improved layout and organization
* Enhanced usability and readability

### Export Improvements

* Professional PDF formatting
* Improved Excel report structure
* Accurate summaries and calculations

### Performance Improvements

* Reduced slow operations
* Faster data loading
* Improved responsiveness

### Installer Improvements

* Better installation experience
* Upgrade support
* Start Menu integration
* Desktop shortcuts

---

## Upgrade Support

The application uses a fixed Windows Upgrade UUID:

```text
6f2a37f8-6e9f-4c1e-a2f7-9f18f7c6b431
```

Benefits:

* Seamless upgrades
* No duplicate installations
* Existing user data preserved
* Existing settings retained

---

## Data Safety

User data is stored separately from the application installation directory.

Benefits:

* Safe upgrades
* Data persistence
* No data loss during application updates
* No administrator privileges required for normal use

---

## License

This project is distributed under the terms specified in the included LICENSE.txt file.

Copyright © 2026 Dibya Jyoti Dhar. All rights reserved.

---

## Author

**Dibya Jyoti Dhar**

Software Quality Assurance Engineer and Independent Software Developer.

GitHub:
https://github.com/dibya888

---

## Support

If you encounter any issues or would like to suggest improvements, please create an issue in the GitHub repository.

Thank you for using House Rent Management System.
