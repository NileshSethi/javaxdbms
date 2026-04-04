package gamersync;

import gamersync.ui.DashboardUI;

/*
 * ╔══════════════════════════════════════════════════════╗
 * ║        USE CASE DIAGRAM — GamerSync System           ║
 * ╠══════════════════════════════════════════════════════╣
 * ║                                                      ║
 * ║   Actor: Staff (Café Employee)                       ║
 * ║                                                      ║
 * ║   Staff ──→ [Add Customer]                           ║
 * ║   Staff ──→ [View All Customers]                     ║
 * ║   Staff ──→ [Update Customer]                        ║
 * ║   Staff ──→ [Delete Customer]                        ║
 * ║   Staff ──→ [Add Gaming Session]                     ║
 * ║   Staff ──→ [View All Sessions]                      ║
 * ║   Staff ──→ [Delete Gaming Session]                  ║
 * ║                                                      ║
 * ║   [Delete Gaming Session] ..includes..               ║
 * ║        [Delete Linked Payments]                      ║
 * ║        [Delete Linked Food Orders]                   ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * HOW TO RUN:
 * 1. Download MySQL Connector/J JAR from https://dev.mysql.com/downloads/connector/j/
 * 2. In VSCode: Ctrl+Shift+P → "Java: Configure Classpath" → add JAR to Referenced Libraries
 * 3. Open DBConnection.java → set your MySQL password
 * 4. Make sure GamerSync DB exists and is populated (run your .sql file in MySQL Workbench)
 * 5. Run this Main.java
 *
 * PROJECT STRUCTURE:
 *   gamersync.db        → DBConnection, InvalidDataException
 *   gamersync.model     → Customer, GamingSession
 *   gamersync.dao       → BaseDAO, ICustomerDAO, ISessionDAO, CustomerDAO, SessionDAO
 *   gamersync.ui        → DashboardUI, CustomerUI, SessionUI
 *   gamersync           → Main
 */
public class Main {
    public static void main(String[] args) {
        // Launch dashboard on the Swing Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            DashboardUI dashboard = new DashboardUI();
            dashboard.setVisible(true);
        });
    }
}
