package it.polito.ezshop.data.classes;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.data.ReturnTransaction;
import it.polito.ezshop.data.SaleTransaction;
import it.polito.ezshop.data.TicketEntry;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class EZDatabase {

    String jdbcUrl;
    Connection connection;

    public EZDatabase() throws SQLException {
        this.jdbcUrl = "jdbc:sqlite:EZshop.db";
        this.connection = DriverManager.getConnection(jdbcUrl);
    }

    /********************* METODI PER LA TABELLA USER **************************/

    public void insertUser(EZUser user) throws SQLException {
        String values = user.getId()+", '"+user.getUsername()+"', '"+user.getPassword()+"', '"+user.getRole()+"'";
        String sql ="INSERT INTO USERS VALUES ("+ values +")";
        Statement statement =this.connection.createStatement();
        statement.executeUpdate(sql);
    }

    public List<EZUser> getUsers() throws SQLException {
        String query = "SELECT * FROM USERS";
        Statement statement =this.connection.createStatement();
        ResultSet rs= statement.executeQuery(query);
        List<EZUser> users = new ArrayList<>();

        while (rs.next())
        {
            EZUser usr = new EZUser(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("role"));
            users.add(usr);
        }

        return users;

    }

    public void deleteUser (Integer id) throws SQLException {


        String sql ="DELETE FROM USERS WHERE id =?";
        PreparedStatement pstm =this.connection.prepareStatement(sql);
        pstm.setInt(1, id);
        pstm.executeUpdate();

    }

    public void updateUser (EZUser updatedUser) throws SQLException {
        String sql = "UPDATE USERS SET username = ?, password = ?, role = ? WHERE id = ?";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setString(1, updatedUser.getUsername());
        pstm.setString(2, updatedUser.getPassword());
        pstm.setString(3, updatedUser.getRole());
        pstm.setInt(4, updatedUser.getId());

        pstm.executeUpdate();

    }

    /********************* METODI PER LA TABELLA ORDER **************************/
    public void insertOrder(EZOrder order) throws SQLException {

        String values = order.getOrderId()+", '"+order.getBalanceId()+"', '"+order.getProductCode()+"', '"+order.getPricePerUnit()+"', '"+order.getQuantity()+"', '"+order.getStatus()+"'";
        String sql ="INSERT INTO ORDERS VALUES ("+ values +")";
        Statement statement =this.connection.createStatement();
        statement.executeUpdate(sql);
    }

    public List<EZOrder> getOrders() throws SQLException {
        String query = "SELECT * FROM ORDERS";
        Statement statement =this.connection.createStatement();
        ResultSet rs= statement.executeQuery(query);
        List<EZOrder> orders = new ArrayList<>();

        while (rs.next())
        {
            EZOrder ordr = new EZOrder(rs.getInt("id"), rs.getString("productCode"), rs.getInt("quantity"), rs.getDouble("pricePerUnit"));
            ordr.setStatus(rs.getString("status"));
            ordr.setBalanceId(rs.getInt("balanceId"));
            orders.add(ordr);
        }

        return orders;

    }

    public void deleteOrder (Integer id) throws SQLException {


        String sql ="DELETE FROM ORDERS WHERE id =?";
        PreparedStatement pstm =this.connection.prepareStatement(sql);
        pstm.setInt(1, id);
        pstm.executeUpdate();

    }

    public void updateOrder (EZOrder updatedOrder) throws SQLException {
        String sql = "UPDATE ORDERS SET balanceId = ?, productCode = ?, pricePerUnit = ?, quantity=?, status=? WHERE id = ?";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setInt(1, updatedOrder.getBalanceId());
        pstm.setString(2, updatedOrder.getProductCode());
        pstm.setDouble(3, updatedOrder.getPricePerUnit());
        pstm.setInt(4, updatedOrder.getQuantity());
        pstm.setString(5, updatedOrder.getStatus());
        pstm.setInt(6, updatedOrder.getOrderId());

        pstm.executeUpdate();

    }

    // ---------------- METODI PER LA TABELLA BALANCEOPERATIONS ------------------- //
    public void addBalanceOperation(EZBalanceOperation bo) throws SQLException {
        String sql = "INSERT INTO BalanceOperations(id, money, date, type) VALUES (?, ?, ?, ?);";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setInt(1, bo.getBalanceId());
        pstm.setDouble(2, bo.getMoney());
        pstm.setString(3, bo.getDate().toString());
        pstm.setString(4, bo.getType());

        pstm.executeUpdate();
    }

    public void updateBalanceOperation(EZBalanceOperation bo) throws SQLException {
        String sql = "UPDATE BalanceOperations" +
                "SET money = ?, date = ?, type = ?" +
                "WHERE id = ?;";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setDouble(1, bo.getMoney());
        pstm.setString(2, bo.getDate().toString());
        pstm.setString(3, bo.getType());
        pstm.setInt(4, bo.getBalanceId());

        pstm.executeUpdate();
    }

    public void deleteBalanceOperation(int balanceId) throws SQLException {
        String sql = "DELETE FROM BalanceOperations" +
                "WHERE id = ?;";
        PreparedStatement pstm = this.connection.prepareStatement(sql);

        pstm.setInt(1, balanceId);

        pstm.executeUpdate();
    }

    public Map<Integer, BalanceOperation> getBalanceOperations() throws SQLException {
        String query = "SELECT * FROM BalanceOperations;";
        Statement statement =this.connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        Map<Integer, BalanceOperation> boMap = new HashMap<>();

        while(rs.next()) {
            EZBalanceOperation bo = new EZBalanceOperation(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getDouble("money")
            );
            boMap.put(bo.getBalanceId(), bo);
        }

        return boMap;
    }

    public int getLastTransactionID() throws SQLException {
        String sql = "SELECT MAX(id) AS maxTransID FROM BalanceOperations;";
        Statement stat = this.connection.createStatement();
        ResultSet rs = stat.executeQuery(sql);

        return rs.getInt("maxTransID");
    }

    // ---------------------- METODI PER LA TABELLA SALETRANSACTIONS --------------- //
    public void addSaleTransaction(EZSaleTransaction st) throws SQLException {
        // NOTA: in questo metodo viene aggiunta anche la lista di ProductEntry
        String sql = "INSERT INTO SaleTransactions(id, discountRate, price, status) VALUES (?, ?, ?, ?);";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setInt(1, st.getTicketNumber());
        pstm.setDouble(2, st.getDiscountRate());
        pstm.setDouble(3, st.getPrice());
        pstm.setString(4, st.getStatus());

        pstm.executeUpdate();

        // add all entries
        List<TicketEntry> entryList = st.getEntries();

        for (TicketEntry e : entryList) {
            String query_e = "INSERT INTO ProductEntry(barCode, saleId, prodDesc, amount, discountRate, pricePerUnit) " +
                    "VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement stat_e = this.connection.prepareStatement(query_e);

            stat_e.setString(1, e.getBarCode());
            stat_e.setInt(2, st.getTicketNumber());
            stat_e.setString(3, e.getProductDescription());
            stat_e.setInt(4, e.getAmount());
            stat_e.setDouble(5, e.getDiscountRate());
            stat_e.setDouble(6, e.getPricePerUnit());

            stat_e.executeUpdate();
        }
    }

    public void updateSaleTransaction(EZSaleTransaction st) throws SQLException {
        String sql = "UPDATE SaleTransactions" +
                "SET discountRate = ?, price = ?, status = ?" +
                "WHERE id = ?;";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setDouble(1, st.getDiscountRate());
        pstm.setDouble(2, st.getPrice());
        pstm.setString(3, st.getStatus());
        pstm.setInt(4, st.getTicketNumber());

        // update every entry
        List<TicketEntry> entryList = st.getEntries();

        for (TicketEntry e : entryList) {
            String query_e = "UPDATE ProductEntry" +
                    "SET prodDesc = ?, amount = ?,  discountRate = ?, pricePerUnit = ?" +
                    "WHERE barCode = ?, saleId = ?;";
            PreparedStatement stat_e = this.connection.prepareStatement(query_e);

            stat_e.setString(1, e.getProductDescription());
            stat_e.setInt(2, e.getAmount());
            stat_e.setDouble(3, e.getDiscountRate());
            stat_e.setDouble(4, e.getPricePerUnit());

            stat_e.setString(5, e.getBarCode());
            stat_e.setInt(6, st.getTicketNumber());

            stat_e.executeUpdate();
        }

        pstm.executeUpdate();
    }

    public void deleteSaleTransaction(EZSaleTransaction st) throws SQLException {
        String sql = "DELETE FROM SaleTransactions" +
                "WHERE id = ?;";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setInt(1, st.getTicketNumber());

        // delete every entry associated with the st
        List<TicketEntry> entryList = st.getEntries();

        for (TicketEntry e : entryList) {
            String query_e = "DELETE FROM ProductEntry WHERE barCode = ?, saleId = ?;";
            PreparedStatement stat_e = this.connection.prepareStatement(query_e);

            stat_e.setString(1, e.getBarCode());
            stat_e.setInt(2, st.getTicketNumber());

            stat_e.executeUpdate();
        }
        // delete the st
        pstm.executeUpdate();
    }

    public Map<Integer, SaleTransaction> getSaleTransactions() throws SQLException {
        String query = "SELECT * FROM SaleTransactions;";
        Statement statement =this.connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        Map<Integer, SaleTransaction> stMap = new HashMap<>();

        while(rs.next()) {
            // create a new st object
            EZSaleTransaction st = new EZSaleTransaction(
                    rs.getInt("id"),
                    rs.getDouble("discountRate"),
                    rs.getDouble("price"),
                    rs.getString("status")
            );

            // get all product entries associated with the st
            String productQuery = "SELECT * FROM productEntry WHERE saleId = ?;";
            PreparedStatement pstat = this.connection.prepareStatement(productQuery);

            pstat.setInt(1, st.getTicketNumber());

            ResultSet rs_prod = pstat.executeQuery(productQuery);
            List<TicketEntry> entryList = new ArrayList<>();

            // for each product entry...
            while (rs_prod.next()) {
                // create a new object
                EZTicketEntry e = new EZTicketEntry(
                        rs_prod.getString("barCode"),
                        rs_prod.getString("prodDesc"),
                        rs_prod.getInt("amount"),
                        rs_prod.getDouble("pricePerUnit"),
                        rs_prod.getDouble("discountRate")
                );
                // add it to the temporary list
                entryList.add(e);
            }
            // set the list for the sale transaction
            st.setEntries(entryList);
            // add the st to the st list
            stMap.put(st.getTicketNumber(), st);
        }

        return stMap;
    }

    public EZSaleTransaction getSaleTransaction(int id) throws SQLException {
        String query = "SELECT * FROM SaleTransactions WHERE id = ?;";
        PreparedStatement statement = this.connection.prepareStatement(query);

        statement.setInt(1, id);

        ResultSet rs = statement.executeQuery(query);
        List<EZSaleTransaction> stList = new ArrayList<>();

        // for each sale transaction returned...
        while(rs.next()) {
            // create a new st object
            EZSaleTransaction st = new EZSaleTransaction(
                    rs.getInt("id"),
                    rs.getDouble("discountRate"),
                    rs.getDouble("price"),
                    rs.getString("status")
            );

            // get all product entries associated with the st
            String productQuery = "SELECT * FROM productEntry WHERE saleId = ?;";
            PreparedStatement pstat = this.connection.prepareStatement(productQuery);

            pstat.setInt(1, st.getTicketNumber());

            ResultSet rs_prod = pstat.executeQuery(productQuery);
            List<TicketEntry> entryList = new ArrayList<>();

            // for each product entry...
            while (rs_prod.next()) {
                // create a new object
                EZTicketEntry e = new EZTicketEntry(
                        rs_prod.getString("barCode"),
                        rs_prod.getString("prodDesc"),
                        rs_prod.getInt("amount"),
                        rs_prod.getDouble("pricePerUnit"),
                        rs_prod.getDouble("discountRate")
                );
                // add it to the temporary list
                entryList.add(e);
            }
            // set the list for the sale transaction
            st.setEntries(entryList);
            // add the st to the st list
            stList.add(st);
        }

        return stList.get(0);
    }

    public void updateSaleInventoryQuantity(EZSaleTransaction st) throws SQLException {
        // get list of entries
        List<TicketEntry> entryList = st.getEntries();

        // update all of the product quantities in the DB
        for (TicketEntry e : entryList) {
            String query_e = "UPDATE Products" +
                    "SET Quantity = Quantity - ?" +
                    "WHERE barcode = ?;";
            PreparedStatement stat_e = this.connection.prepareStatement(query_e);

            stat_e.setInt(1, e.getAmount());
            stat_e.setString(2, e.getBarCode());

            stat_e.executeUpdate();
        }
    }

    // ---------------------- METODI PER LA TABELLA RETURNTRANSACTION ------------------ //
    public void addReturnTransaction(EZReturnTransaction rt) throws SQLException {
        String sql = "INSERT INTO ReturnTransactions(returnId, saleId, status, money) VALUES (?, ?, ?, ?);";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setInt(1, rt.getReturnID());
        pstm.setDouble(2, rt.getSaleTransactionID());
        pstm.setString(3, rt.getStatus());
        pstm.setDouble(4, rt.getMoneyReturned());

        pstm.executeUpdate();

        Map<String, Integer> prodMap = rt.getMapOfProducts();

        for(Map.Entry<String, Integer> e : prodMap.entrySet()) {
            String query_e = "INSERT INTO ReturnProductEntry(returnId, barCode, amount)" +
                    "VALUES(?, ?, ?);";
            PreparedStatement stat_e = this.connection.prepareStatement(query_e);

            stat_e.setInt(1, rt.getReturnID());
            stat_e.setString(2, e.getKey());
            stat_e.setInt(3, e.getValue());

            stat_e.executeUpdate();
        }
    }

    public void updateReturnTransaction(EZReturnTransaction rt) throws SQLException {
        String sql = "UPDATE ReturnTransactions " +
                "SET status = ?, money = ?" +
                "WHERE returnId = ?;";
        PreparedStatement pstm =this.connection.prepareStatement(sql);

        pstm.setString(1, rt.getStatus());
        pstm.setDouble(2, rt.getMoneyReturned());
        pstm.setInt(3, rt.getReturnID());

        pstm.executeUpdate();

        Map<String, Integer> prodMap = rt.getMapOfProducts();

        for(Map.Entry<String, Integer> e : prodMap.entrySet()) {
            String query_e = "UPDATE ReturnProductEntry" +
                    "SET amount = ?" +
                    "WHERE returnId = ? AND barCode = ?;";
            PreparedStatement stat_e = this.connection.prepareStatement(query_e);

            stat_e.setInt(1, e.getValue());
            stat_e.setInt(2, rt.getReturnID());
            stat_e.setString(3, e.getKey());

            stat_e.executeUpdate();
        }
    }

    public Map<Integer, ReturnTransaction> getReturnTransactions() throws SQLException {
        String query = "SELECT * FROM ReturnTransactions;";
        Statement statement =this.connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        Map<Integer, ReturnTransaction> rtMap = new HashMap<>();

        while(rs.next()) {
            // create a new rt object
            EZReturnTransaction rt = new EZReturnTransaction(
                    rs.getInt("saleId"),
                    rs.getInt("returnId"),
                    rs.getString("status")
            );

            // get all return product entries associated with the rt
            String productQuery = "SELECT * FROM ReturnProductEntry WHERE returnId = ?;";
            PreparedStatement pstat = this.connection.prepareStatement(productQuery);

            pstat.setInt(1, rt.getReturnID());

            ResultSet rs_prod = pstat.executeQuery(productQuery);
            Map<String, Integer> prodMap = rt.getMapOfProducts();

            // for each product entry...
            while (rs_prod.next()) {
                // add an element to the rt's product map
                prodMap.put(rs_prod.getString("barCode"), rs_prod.getInt("amount"));
            }
            // set the list for the sale transaction
            rt.setMapOfProducts(prodMap);
            // add the rt to the rt map
            rtMap.put(rt.getReturnID(), rt);
        }

        return rtMap;
    }

    public int getLastReturnID() throws SQLException {
        String sql = "SELECT MAX(returnId) AS maxRetID FROM ReturnTransactions;";
        Statement stat = this.connection.createStatement();
        ResultSet rs = stat.executeQuery(sql);

        return rs.getInt("maxRetID");
    }
    /*******************************************************************************************/
    public static void main (String[] args) throws SQLException
    {
        EZDatabase db = new EZDatabase();

        //EZUser user =new EZUser(2, "antonino", "ciao2", "Manager");
        //db.insertUser(user);
        //db.updateUser(user);
        //db.deleteUser(2);

        //EZOrder order =new EZOrder(1, "12345", 5, 3.40);
        //order.setBalanceId(1);
        //order.setStatus("PAYED");

        //db.insertOrder(order2);

        //List<EZOrder> ordini= db.getOrders();

        //System.out.println(ordini.stream().map(o -> o.getOrderId()).count());
        //db.deleteOrder(3);
        //db.updateOrder(order);

    }
}
