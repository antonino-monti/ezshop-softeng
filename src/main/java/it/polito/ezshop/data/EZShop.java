package it.polito.ezshop.data;

import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.data.classes.*;

import java.sql.SQLException;
import java.time.LocalDate;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EZShop implements EZShopInterface {
    List<User> userList;
    Map<Integer, Customer> customerMap;
    Map<Integer, BalanceOperation> transactionMap;
    Map<Integer, SaleTransaction> saleTransactionMap;
    Map<Integer, ReturnTransaction> returnTransactionMap;
    Map<Integer, Order> orderTransactionMap;
    Map<String, ProductType> productTypeMap; //Key= barcode, value= ProductType
    User userSession;
    int idUsers;
    int idCustomer;
    Integer idCustomerCard;
    int counter_returnTransactionID;
    int counter_transactionID;
    private int productIds;
    EZDatabase dbase;

    public EZShop() {
        try {
            this.dbase = new EZDatabase();


            // --- Users
            try {
                this.userList = this.dbase.getUsers();
                this.idUsers = dbase.getNextUserId();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.userList = new ArrayList<>();
                this.idUsers = 0;
            }
            // --- Balance Operations and respective ID counter
            try {
                this.transactionMap = this.dbase.getBalanceOperations();
                this.counter_transactionID = this.dbase.getLastTransactionID();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.transactionMap = new HashMap<>();
                this.counter_transactionID = 0;
            }
            // --- Sale Transactions
            try {
                this.saleTransactionMap = this.dbase.getSaleTransactions();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.saleTransactionMap = new HashMap<>();
            }
            // --- Return Transactions and respective ID counter
            try {
                this.returnTransactionMap = this.dbase.getReturnTransactions();
                this.counter_returnTransactionID = this.dbase.getLastReturnID();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.returnTransactionMap = new HashMap<>();
                this.counter_returnTransactionID = 0;
            }
            // --- Orders
            try {
                this.orderTransactionMap = this.dbase.getOrders();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.orderTransactionMap = new HashMap<>();
            }
            //Customers
            try {
                this.customerMap = this.dbase.getCustomerMap();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.customerMap = new HashMap<>();
            }
            //CustomerID
            try {
                this.idCustomer = this.dbase.getLastCustomer();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.idCustomer=1;
            }
            //CustomerId Card
            try {
                this.idCustomerCard = this.dbase.getCustomerCard();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.idCustomerCard=1;
            }
            //ProductIds Inizializzato da db
            try{
                this.productIds= this.dbase.getLastProductId();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.productIds= 0;
            }

            //ProductTypeMap inizializzato da db
            try{
                this.productTypeMap= this.dbase.getProductTypeMap();
            }
            catch (SQLException e) {
                System.out.println("There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                e.printStackTrace();
                this.productTypeMap = new HashMap<>();
            }
        }
        catch (SQLException e) {
            // Se la connessione al database fallisce, si inizializza tutto con i valori di default
            System.out.println("There was a problem in connecting with the SQLite database:");
            System.out.println(e.getSQLState());
            this.userList = new ArrayList<>();
            this.customerMap = new HashMap<>();
            this.transactionMap = new HashMap<>();
            this.saleTransactionMap = new HashMap<>();
            this.returnTransactionMap = new HashMap<>();
            this.orderTransactionMap = new HashMap<>();
            this.productTypeMap = new HashMap<>();

            this.idUsers = 0;
            this.idCustomer = 1;
            this.idCustomerCard = 1;
            this.counter_transactionID = 0;
            this.counter_returnTransactionID = 0;
            this.productIds = 0;
        }

        // Questo rimane inizializzato a null in ogni caso
        this.userSession = null;
    }

    /**
        checkUserRole(String expectedRole)
        @param expectedRole il ruolo da controllare; può avere come valore "ADMINISTRATOR", "SHOPMANAGER" o "CASHIER"

        @return
            true,  se l'utente è loggato e ha il permesso che ci si aspetta
            false, se l'utente non è loggato o non ha i permessi adatti
     */
    public boolean checkUserRole(String expectedRole) {
        return (this.userSession != null && this.userSession.getRole().equalsIgnoreCase(expectedRole));
    }

    /**
        checkBarCodeValidity(String barCode)
        Controlla che il barcode sia valido. Per essere valido il codice deve essere composto da soli interi,
        deve essere lungo 12/13/14 numeri ed avere il check digit (calcolato tramite algoritmo) corretto

        @param barCode: il codice a barre da controllare

        @return:
            true,  se il codice è valido
            false, se il codice non è valido o ci sono problemi
     */
    public boolean checkBarCodeValidity(String barCode) {
        if (barCode == null || !barCode.matches("[0-9]{12,14}")) {
            // se in input non abbiamo un codice con solo interi e con lunghezza compresa
            // tra 12 e 14 inclusi, ritorna false
            return false;
        }

        int codeLength = barCode.length();
        int parity = codeLength % 2;

        int digits[] = new int[codeLength - 1];

        char barCode_arr[] = barCode.toCharArray();

        // converti la stringa in un vettore di interi da 0 a 9
        for (int i = 0; i < codeLength - 1; i++) {
            digits[i] = Character.getNumericValue(barCode_arr[i]);
        }

        int sum = 0;
        for (int i = 0; i < codeLength - 1; i++) {
            sum += ((i % 2 == parity)? (digits[i] * 3) : digits[i]);
        }

        int i = 0;
        // prendi il multiplo di 10 uguale o subito più grande di sum
        while(i < sum) {
            i += 10;
        }
        // sottrai sum a questo numero
        int checkVal = i - sum;

        // confronta il risultato con l'ultima cifra del codice a barre
        return (checkVal == Character.getNumericValue(barCode_arr[codeLength - 1]));
    }

    /**
        checkCreditCardValidity(String cardCode)
        Controlla la validità del codice di una carta di credito tramite l'algoritmo di Luhn.

        @param cardCode: codice da controllare

        @return:
            true,  se il codice è valido
            false, se il codice non è valido
     */
    public boolean checkCreditCardValidity(String cardCode) {
        // se la carta non è formata da soli numeri, ritorna false
        if (cardCode == null || !cardCode.matches("[0-9]+")) {
            return false;
        }

        int nDigits = cardCode.length();
        char cardCode_arr[] = cardCode.toCharArray();
        int sum = Character.getNumericValue(cardCode_arr[nDigits - 1]);
        int parity = nDigits % 2;

        for (int i = 0; i < nDigits - 1; i++) {
            int digit = Character.getNumericValue(cardCode_arr[i]);
            if (i % 2 == parity) {
                digit *= 2;
            }
            if (digit > 9) {
                digit = digit - 9;
            }
            sum += digit;
        }

        return (sum % 10 == 0);
    }
    public boolean checkCustomerCardValidity(String CCard){
        return CCard.matches("[0-9]{10}");
    }

    @Override
    public void reset() {
        /*Delete all users from the database and from the local structure*/

            try {
                dbase.clearUsers();
            }
            catch (SQLException e) {
                System.out.println("USERS: There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                System.out.println(e.getMessage());
            }

        this.userList.clear();

        /*Delete all the orders from the database*/

            try {
                dbase.clearOrders();
            }
            catch (SQLException e) {
                System.out.println("ORDERS: There was a problem in connecting with the SQLite database:");
                System.out.println(e.getSQLState());
                System.out.println(e.getMessage());
            }

        this.orderTransactionMap.clear();

        try {
            this.dbase.deleteCustomerTable();
        } catch (SQLException e) {
            System.out.println("CUSTOMER: There was a problem in connecting with the SQLite database:");
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        }
        this.customerMap = new HashMap<>();

        // --- Clear Balance Operations
        try {
            this.dbase.clearBalanceOperations();
        }
        catch (SQLException e) {
            System.out.println("BALANCE OPERATIONS: There was a problem in connecting with the SQLite database:");
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        }
        this.transactionMap = new HashMap<>();
        // --- Clear Sale Transactions
        try {
            this.dbase.clearSaleTransactions();
        }
        catch (SQLException e) {
            System.out.println("SALE TRANSACTIONS: There was a problem in connecting with the SQLite database:");
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        }
        this.saleTransactionMap = new HashMap<>();
        // --- Clear Return Transactions
        try {
            this.dbase.clearReturnTransactions();
        }
        catch (SQLException e) {
            System.out.println("RETURN TRANSACTIONS: There was a problem in connecting with the SQLite database:");
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        }
        this.returnTransactionMap = new HashMap<>();

        try {
            this.dbase.deleteProductTable();
        } catch (SQLException e) {
            System.out.println("PRODUCT : There was a problem in connecting with the SQLite database:");
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
        }
        this.productTypeMap = new HashMap<>();

        this.userSession = null;
        this.idUsers = 1;
        this.idCustomer = 1;
        this.idCustomerCard = 1;
        this.counter_transactionID = 0;
        this.counter_returnTransactionID = 0;
        this.productIds = 0;
    }




    /**
     * This method creates a new user with given username, password and role. The returned value is a unique identifier
     * for the new user.
     *
     * @param username the username of the new user. This value should be unique and not empty.
     * @param password the password of the new user. This value should not be empty.
     * @param role the role of the new user. This value should not be empty and it should assume
     *             one of the following values : "Administrator", "Cashier", "ShopManager"
     *
     * @return The id of the new user ( > 0 ).
     *          -1 if there is an error while saving the user or if another user with the same username exists
     *
     * @throws InvalidUsernameException If the username has an invalid value (empty or null)
     * @throws InvalidPasswordException If the password has an invalid value (empty or null)
     * @throws InvalidRoleException     If the role has an invalid value (empty, null or not among the set of admissible values)
     */

    @Override
    public Integer createUser(String username, String password, String role) throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
        //username unique and not empty

        if ( username==null|| username.trim().equals("") )
            throw new InvalidUsernameException();

        //password not empty
        if (password==null || password.trim().equals("")  )
            throw new InvalidPasswordException();
        //Role not valid
        if (role==null|| role.trim().equals("" )  || (!role.equalsIgnoreCase("SHOPMANAGER") && !role.equalsIgnoreCase("ADMINISTRATOR") && !role.equalsIgnoreCase("CASHIER") ))
            throw new InvalidRoleException();

        for (User u : this.userList) {
            if (u.getUsername().equals(username))
            {
                return (-1);
            }
        }

        int newuserId = this.idUsers;
        User newUsr =new EZUser(newuserId, username,password,role);

        /*Try to add the user to the database*/
        try {
            this.dbase.insertUser(newUsr);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;  /*Error while saving*/
        }
        userList.add(newUsr);

        this.idUsers++;

        return newuserId;
    }

    // ------------------- ADMIN ------------------ //
    /**
     * This method deletes the user with given id. It can be invoked only after a user with role "Administrator" is
     * logged in.
     *
     * @param id the user id, this value should not be less than or equal to 0 or null.
     *
     * @return  true if the user was deleted
     *          false if the user cannot be deleted
     *
     * @throws InvalidUserIdException if id is less than or equal to 0 or if it is null.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean deleteUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        if (id==null ||id<=0 )
            throw new InvalidUserIdException();
        if (!checkUserRole("Administrator"))
            throw new UnauthorizedException();

        /*Look for the user with the same id*/
        for (User u : this.userList)
            if (u.getId().equals(id))
            {
                try {
                    this.dbase.deleteUser(u.getId()); // Updated database

                } catch (SQLException e) {
                    System.out.println(e.getMessage()); //error deleting the user
                    return false;
                }
                this.userList.remove(u);
                return true;
            }
        // if no user has been found return false
        return false;
    }

    /**
     * This method returns the list of all registered users. It can be invoked only after a user with role "Administrator" is
     * logged in.
     *
     * @return  a list of all registered users. If there are no users the list should be empty
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public List<User> getAllUsers() throws UnauthorizedException {
        if (!checkUserRole("Administrator"))
            throw new UnauthorizedException();
        else
            return this.userList;
    }

    /**
     * This method returns a User object with given id. It can be invoked only after a user with role "Administrator" is
     * logged in.
     *
     * @param id the id of the user
     *
     * @return  the requested user if it exists, null otherwise
     *
     * @throws InvalidUserIdException if id is less than or equal to zero or if it is null
     * @throws UnauthorizedException  if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public User getUser(Integer id) throws InvalidUserIdException, UnauthorizedException {

        //Check id validity
        if (id==null ||id<=0 )
            throw new InvalidUserIdException();
        //check userSession
                if (!checkUserRole("Administrator"))
                    throw new UnauthorizedException();

        /*Look for the user with the same id*/
        for (User u : this.userList)
            if (u.getId().equals(id))
            {
                return u;
            }
        // if no user has been found return null
        return null;
    }


    /**
     * This method updates the role of a user with given id. It can be invoked only after a user with role "Administrator" is
     * logged in.
     *
     * @param id the id of the user
     * @param role the new role the user should be assigned to
     *
     * @return true if the update was successful, false if the user does not exist
     *
     * @throws InvalidUserIdException   if the user Id is less than or equal to 0 or if it is null
     * @throws InvalidRoleException     if the new role is empty, null or not among one of the following : {"Administrator", "Cashier", "ShopManager"}
     * @throws UnauthorizedException    if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean updateUserRights(Integer id, String role) throws InvalidUserIdException, InvalidRoleException, UnauthorizedException {

        //Check id validity
        if (id==null ||id<=0 )
            throw new InvalidUserIdException();
        //Check role validity
        if (role==null|| role.trim().equals("" )  || (!role.equalsIgnoreCase("SHOPMANAGER") && !role.equalsIgnoreCase("ADMINISTRATOR") && !role.equalsIgnoreCase("CASHIER") ))
            throw new InvalidRoleException();
        //check userSession
        if (!checkUserRole("Administrator"))
            throw new UnauthorizedException();

        /*Look for the user with the same id*/
        for (User u : this.userList)
            if (u.getId().equals(id))
            {
                try {
                    this.dbase.updateUserRole(u.getId(), role);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    return false;
                }

                u.setRole(role);
                if (u.getId().equals(this.userSession.getId())) //I'm asking to modify the role of the user logged in in this moment
                    this.userSession.setRole(role);

                return true;
            }
        // if no user has been found return false
        return false;
    }

    // -------------------- LOGIN ----------------- //

    /**
     * This method lets a user with given username and password login into the system
     *
     * @param username the username of the user
     * @param password the password of the user
     *
     * @return an object of class User filled with the logged user's data if login is successful, null otherwise ( wrong credentials or db problems)
     *
     * @throws InvalidUsernameException if the username is empty or null
     * @throws InvalidPasswordException if the password is empty or null
     */

    @Override
    public User login(String username, String password) throws InvalidUsernameException, InvalidPasswordException {
        // username validity
        if (username==null || username.trim().equals(""))
            throw new InvalidUsernameException();
        // password validity
        if (password==null || password.trim().equals(""))
            throw new InvalidPasswordException();

        //look for user

        /*Look for the user with the same id*/
        for (User u : this.userList)
            if (u.getUsername().equals(username))
            {
                if (!u.getPassword().equals(password)) return null; //wrong credentials
                else  //Found user
                {
                    this.userSession= u; //Add the user as user session
                    return u;
                }

            }
        // if no user has been found return null
        return null;
    }

    /**
     * This method makes a user to logout from the system
     *
     * @return true if the logout is successful, false otherwise (there is no logged user)
     */

    @Override
    public boolean logout() {
        if (this.userSession==null) //no logged user
            return false;
        else
        {   this.userSession=null; //user logs out
            return true;
        }
    }

    /**
     * This method creates a product type and returns its unique identifier. It can be invoked only after a user with role "Administrator"
     * or "ShopManager" is logged in.
     *
     * @param description the description of product to be created
     * @param productCode  the unique barcode of the product
     * @param pricePerUnit the price per single unit of product
     * @param note the notes on the product (if null an empty string should be saved as description)
     *
     * @return The unique identifier of the new product type ( > 0 ).
     *         -1 if there is an error while saving the product type or if it exists a product with the same barcode
     *
     * @throws InvalidProductDescriptionException if the product description is null or empty
     * @throws InvalidProductCodeException if the product code is
     * or empty, if it is not a number or if it is not a valid barcode
     * @throws InvalidPricePerUnitException if the price per unit si less than or equal to 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public Integer createProductType(String description, String productCode, double pricePerUnit, String note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();
        // check description validity
        if (description==null || description.trim().equals(""))
            throw new InvalidProductDescriptionException();
        // check product code validity
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (productCode==null ||
                productCode.trim().equals("") ||
                !pattern.matcher(productCode).matches() ||
                !this.checkBarCodeValidity(productCode)) //check if the product code is not a number
            throw new InvalidProductCodeException();
        //check price validity
        if (pricePerUnit<=0 )
            throw new InvalidPricePerUnitException();

        //check if exists a productType with the same barcode
        if (this.productTypeMap.containsKey(productCode))
                return -1;
        //create new ProductType
        int newProductId=++this.productIds;
        //note null
        if (note==null) note="";
        ProductType pt=new EZProductType(description, productCode, pricePerUnit, note, newProductId);
        //this.productIds++;
        this.productTypeMap.put(productCode, pt);
        try {
            this.dbase.insertProductType((EZProductType) pt);
        } catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
        }

        return newProductId;
    }

    /**
     * This method updates the product id with given barcode and id. It can be invoked only after a user with role "Administrator"
     * or "ShopManager" is logged in.
     *
     * @param id the type of product to be updated
     * @param newDescription the new product type
     * @param newCode the new product code
     * @param newPrice the new product price
     * @param newNote the new product notes
     *
     * @return  true if the update is successful
     *          false if the update is not successful (no products with given product id or another product already has
     *              the same barcode)
     *
     * @throws InvalidProductIdException if the product id is less than or equal to 0 or if it is null
     * @throws InvalidProductDescriptionException if the product description is null or empty
     * @throws InvalidProductCodeException if the product code is null or empty, if it is not a number or if it is not a valid barcode
     * @throws InvalidPricePerUnitException if the price per unit si less than or equal to 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();

        //check product validity
        if (id==null || id<=0)
            throw new InvalidProductIdException();
        //check description validity
        if (newDescription==null || newDescription.trim().equals(""))
            throw new InvalidProductDescriptionException();
        //check product validity
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (newCode==null ||
                newCode.trim().equals("") ||
                !pattern.matcher(newCode).matches() ||
                !this.checkBarCodeValidity(newCode)) //check if the product code is not a number
            throw new InvalidProductCodeException();
        //check price per unit validity
        if (newPrice<=0 || newDescription.trim().equals(""))
            throw new InvalidPricePerUnitException();


        //check if there is a product with the same id
        for (ProductType p: this.productTypeMap.values())
            if (p.getId().equals(id))
            { //Found

                //check if it already exists a product with that given barcode (that is a new barcode for the prodoct selected)
                if (!p.getBarCode().equals(newCode) && this.productTypeMap.containsKey(newCode))
                    return false;

                //update map product type, deleting the record with the barcode to update
                this.productTypeMap.remove(p.getBarCode());
                p.setBarCode(newCode);
                p.setProductDescription(newDescription);
                p.setPricePerUnit(newPrice);
                p.setNote(newNote);
                //update map product type with the item associated with the new barcode
                this.productTypeMap.put(p.getBarCode(), p);
                try {
                    this.dbase.updateProduct((EZProductType) p);
                } catch (SQLException e) {
                    System.out.println("There was a problem with the database:");
                    System.out.println(e.getSQLState());
                }
                return true;
            }

        return false; //No product with that id found

    }
    /**
     * This method deletes a product with given product id. It can be invoked only after a user with role "Administrator"
     * or "ShopManager" is logged in.
     *
     * @param id the id of the product to be deleted
     *
     * @return true if the product was deleted, false otherwise
     *
     * @throws InvalidProductIdException if the product id is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean deleteProductType(Integer id) throws InvalidProductIdException, UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();
        //check product validity
        if (id==null || id<=0)
            throw new InvalidProductIdException();

        //check if there is already a product with the same id
        for (ProductType p: this.productTypeMap.values())
            if (p.getId().equals(id))
            { //Found
                try {
                    this.dbase.deleteProduct((EZProductType) p);
                } catch (SQLException e) {
                    System.out.println("There was a problem with the database:");
                    System.out.println(e.getSQLState());
                }
                this.productTypeMap.remove(p.getBarCode());
                return true;
            }

        return false; //No product with that id found

    }

    /**
     * This method returns the list of all registered product types. It can be invoked only after a user with role "Administrator",
     * "ShopManager" or "Cashier" is logged in.
     *
     * @return a list containing all saved product types
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public List<ProductType> getAllProductTypes() throws UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager") )
            throw new UnauthorizedException();

        return new ArrayList<>(this.productTypeMap.values());
    }
    /**
     * This method returns a product type with given barcode. It can be invoked only after a user with role "Administrator"
     * or "ShopManager" is logged in.
     *
     * @param barCode the unique barCode of a product
     *
     * @return the product type with given barCode if present, null otherwise
     *
     * @throws InvalidProductCodeException if barCode is not a valid bar code, if is it empty or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public ProductType getProductTypeByBarCode(String barCode) throws InvalidProductCodeException, UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();
        //check product code validity
        if (barCode== null
                || barCode.trim().equals("")
                || !this.checkBarCodeValidity(barCode.trim())
        )
            throw new InvalidProductCodeException();

        if (this.productTypeMap.containsKey(barCode))
            return this.productTypeMap.get(barCode);

        else
           return null;
    }

    /**
     * This method returns a list of all products with a description containing the string received as parameter. It can be invoked only after a user with role "Administrator"
     * or "ShopManager" is logged in.
     *
     * @param description the description (or part of it) of the products we are searching for.
     *                    Null should be considered as the empty string.
     *
     * @return a list of products containing the requested string in their description
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public List<ProductType> getProductTypesByDescription(String description) throws UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();

        // Null should be considered as the empty string.

        if (description==null) description="";

        String stringToFind = description;
        return this.productTypeMap.values()
                .stream()
                .filter((ProductType p) -> p.getProductDescription().contains(stringToFind) )
                .collect(Collectors.toList());


    }


    // -------------------- FR4 ------------------- //
    // ------------------- ADMIN ------------------ //
    // --------------- SHOP SHOPMANAGER --------------- //

    /**
     * This method updates the quantity of product available in store. <toBeAdded> can be negative but the final updated
     * quantity cannot be negative. The product should have a location assigned to it.
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @param productId the id of the product to be updated
     * @param toBeAdded the quantity to be added. If negative it decrease the available quantity of <toBeAdded> elements.
     *
     * @return  true if the update was successful
     *          false if the product does not exists, if <toBeAdded> is negative and the resulting amount would be
     *          negative too or if the product type has not an assigned location.
     *
     * @throws InvalidProductIdException if the product id is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean updateQuantity(Integer productId, int toBeAdded) throws InvalidProductIdException, UnauthorizedException {
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();
        // check product validity
        if (productId==null || productId<=0)
            throw new InvalidProductIdException();

        //check if the product exists in the map
        for (ProductType p: this.productTypeMap.values())
        {
            if (p.getId().equals(productId)) //Found
            {
                int newQuantity =p.getQuantity() + toBeAdded;

                if (toBeAdded<0 && newQuantity<0)
                    return false;
                if (p.getLocation()==null)  //not assigned to a location
                    return false;

                try {
                    EZProductType e = new EZProductType(p.getProductDescription(),
                                p.getBarCode(),
                                p.getPricePerUnit(),
                                p.getNote(),
                                p.getId(),
                                p.getLocation());
                    e.setQuantity(newQuantity);
                    this.dbase.updateProduct(e);
                }
                catch (SQLException e) {
                    System.out.println("There was a problem with the DB:");
                    e.printStackTrace();
                    return false;
                }

                //all good, update quantity
                p.setQuantity(newQuantity);
                return true;
            }
        }
        return false;
    }

    /**
     * This method assign a new position to the product with given product id. The position has the following format :
     * <aisleNumber>-<rackAlphabeticIdentifier>-<levelNumber>
     * The position should be unique (unless it is an empty string, in this case this means that the product type
     * has not an assigned location). If <newPos> is null or empty it should reset the position of given product type.
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @param productId the id of the product to be updated
     * @param newPos the new position the product should be placed to.
     *
     * @return true if the update was successful
     *          false if the product does not exists or if <newPos> is already assigned to another product
     *
     * @throws InvalidProductIdException if the product id is less than or equal to 0 or if it is null
     * @throws InvalidLocationException if the product location is in an invalid format (not <aisleNumber>-<rackAlphabeticIdentifier>-<levelNumber>)
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean updatePosition(Integer productId, String newPos) throws InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        // check product validity
        if (productId==null || productId<=0)
            throw new InvalidProductIdException();
        // check role administrator/shop manager
        if (!checkUserRole("Administrator") && !checkUserRole("ShopManager"))
            throw new UnauthorizedException();

        // Nota: assicurarsi che quello nell'espressione regolare sia effettivamente
        // il pattern corretto. In questo momento è generico
        if (newPos!=null && !newPos.equals("") && !newPos.trim().matches("[0-9]+-[A-Za-z]+-[0-9]+")) {
            throw new InvalidLocationException();
        }

        //check if the product does not exist
        String barcodeProduct= null;

        for (ProductType p : this.productTypeMap.values()) {
            if (p.getId().equals(productId)) { //check if the producttype exists
                barcodeProduct=p.getBarCode();
            }
            if ((newPos!=null && !newPos.equals(""))  && p.getLocation()!=null && p.getLocation().equals(newPos)) //check whether the position has already been assigned
                return false;
        }

        if (barcodeProduct==null) //product not found
            return false;

        if (newPos != null && newPos.equals("")) newPos=null;

        //Everything good
        EZProductType pr = (EZProductType) this.productTypeMap.get(barcodeProduct);
        pr.setLocation(newPos);
        try {
            this.dbase.updateProduct((EZProductType) pr);
        } catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getMessage());
        }

        return true;
    }

    /**
     * This method issues an order of <quantity> units of product with given <productCode>, each unit will be payed
     * <pricePerUnit> to the supplier. <pricePerUnit> can differ from the re-selling price of the same product. The
     * product might have no location assigned in this step.
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @param productCode the code of the product that we should order as soon as possible
     * @param quantity the quantity of product that we should order
     * @param pricePerUnit the price to correspond to the supplier (!= than the resale price of the shop) per unit of
     *                     product
     *
     * @return  the id of the order (> 0)
     *          -1 if the product does not exists, if there are problems with the db
     *
     * @throws InvalidProductCodeException if the productCode is not a valid bar code, if it is null or if it is empty
     * @throws InvalidQuantityException if the quantity is less than or equal to 0
     * @throws InvalidPricePerUnitException if the price per unit of product is less than or equal to 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        // check barcode validity
        if (productCode==null || productCode.trim().equals("")
           || !this.checkBarCodeValidity(productCode.trim())
            )
            throw new InvalidProductCodeException();
        // check quantity validity
        if ( quantity <=0)
            throw new InvalidQuantityException();
        //check price per unit validity
        if ( pricePerUnit <=0)
            throw new InvalidPricePerUnitException();
        //user session validity
        if (!this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("SHOPMANAGER"))
            throw new UnauthorizedException();


        //check if the product is in the map
        if (!this.productTypeMap.containsKey(productCode))
            return -1;

        //Everything good. Create new order
        int newID = ++this.counter_transactionID;

        Order newOrder =new EZOrder(newID, productCode, quantity, pricePerUnit);
        // set status ISSUED
        newOrder.setStatus("ISSUED");

        //Try insert it in the db
        try {
            this.dbase.insertOrder(newOrder);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }

        // insert order in the map (not also in the balance operation map because this order has still to be paid)
        this.orderTransactionMap.put(newID, newOrder);


        return newID;

    }

    /**
     * This method directly orders and pays <quantity> units of product with given <productCode>, each unit will be payed
     * <pricePerUnit> to the supplier. <pricePerUnit> can differ from the re-selling price of the same product. The
     * product might have no location assigned in this step.
     * This method affects the balance of the system.
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @param productCode the code of the product to be ordered
     * @param quantity the quantity of product to be ordered
     * @param pricePerUnit the price to correspond to the supplier (!= than the resale price of the shop) per unit of
     *                     product
     *
     * @return  the id of the order (> 0)
     *          -1 if the product does not exists, if the balance is not enough to satisfy the order, if there are some
     *          problems with the db
     *
     * @throws InvalidProductCodeException if the productCode is not a valid bar code, if it is null or if it is empty
     * @throws InvalidQuantityException if the quantity is less than or equal to 0
     * @throws InvalidPricePerUnitException if the price per unit of product is less than or equal to 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
         // check barcode validity
        if (productCode==null || productCode.trim().equals("")
            || !this.checkBarCodeValidity(productCode.trim())
        )
            throw new InvalidProductCodeException();
        // check quantity validity
        if ( quantity <=0)
            throw new InvalidQuantityException();
        //check price per unit validity
        if ( pricePerUnit <=0)
            throw new InvalidPricePerUnitException();
        //user session validity
        if (!this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("SHOPMANAGER"))
            throw new UnauthorizedException();

        //check if the product is in the map
        if (!this.productTypeMap.containsKey(productCode))
            return -1;
        //check if the balance is enough for the order
        double priceToPay=quantity*pricePerUnit;

        if (this.computeBalance()< priceToPay)
            return -1;


        //Everything good. Create new order
        int newID = ++this.counter_transactionID;

        Order newOrder =new EZOrder(newID, productCode, quantity, pricePerUnit);
        // set status PAYED
        newOrder.setStatus("PAYED");

        EZBalanceOperation blOp= new EZBalanceOperation(newID, LocalDate.now(),  -priceToPay);

        //insertbalance id in order
        newOrder.setBalanceId(newID);

        //Try insert it in the db
        try {
            this.dbase.insertOrder(newOrder);

            this.dbase.addBalanceOperation(blOp);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }


        //insert it in the map of orders
        this.orderTransactionMap.put(newID, newOrder);

        //insert order in the balance operation
        this.transactionMap.put(newID, blOp);

        return newID;
    }

    /**
     * This method change the status the order with given <orderId> into the "PAYED" state. The order should be either
     * issued (in this case the status changes) or payed (in this case the method has no effect).
     * This method affects the balance of the system.
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @param orderId the id of the order to be ORDERED
     *
     * @return  true if the order has been successfully ordered
     *          false if the order does not exist or if it was not in an ISSUED/ORDERED state
     *
     * @throws InvalidOrderIdException if the order id is less than or equal to 0 or if it is null.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        // check order code validity
        if (orderId==null || orderId<=0)
            throw new InvalidOrderIdException();

        //user session validity
        if (!this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("SHOPMANAGER"))
            throw new UnauthorizedException();

        //look for the order
        if (!this.orderTransactionMap.containsKey(orderId))
            return false;
        //check status
        String orderStatus = this.orderTransactionMap.get(orderId).getStatus();
        if (orderStatus.equalsIgnoreCase("payed") )
            return true; //senza fare nulla
        if (!orderStatus.equalsIgnoreCase("issued") )
            return false;

        // update balance
        double toPay =this.orderTransactionMap.get(orderId).getQuantity()*this.orderTransactionMap.get(orderId).getPricePerUnit();
        if (toPay> this.computeBalance())
            return false; //If the balance is not enough return false!
        //new balance operation
        EZBalanceOperation balOp =new EZBalanceOperation(orderId, LocalDate.now(),  -toPay);


        //Update the db

        try {
            // set status PAYED
            this.dbase.updateOrderStatus(orderId, "PAYED");

            //insert order in the balance operation
            this.dbase.addBalanceOperation(balOp);
            //set balanceId in order
            this.dbase.updateOrderBalanceId(orderId, orderId);


        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        // set status PAYED
        this.orderTransactionMap.get(orderId).setStatus("PAYED");

        //insert order in the balance operation
        this.transactionMap.put(orderId, balOp);

        //set balanceId in order
        this.orderTransactionMap.get(orderId).setBalanceId((orderId));

        return true;

    }

    /**
     * This method records the arrival of an order with given <orderId>. This method changes the quantity of available product.
     * The product type affected must have a location registered. The order should be either in the PAYED state (in this
     * case the state will change to the COMPLETED one and the quantity of product type will be updated) or in the
     * COMPLETED one (in this case this method will have no effect at all).
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @param orderId the id of the order that has arrived
     *
     * @return  true if the operation was successful
     *          false if the order does not exist or if it was not in an PAYED/COMPLETED state (payed no ordered!!!)
     *
     * @throws InvalidOrderIdException if the order id is less than or equal to 0 or if it is null.
     * @throws InvalidLocationException if the ordered product type has not an assigned location.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {

        // check order code validity
        if (orderId==null || orderId<=0)
            throw new InvalidOrderIdException();

        //user session validity
        if (!this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("SHOPMANAGER"))
            throw new UnauthorizedException();


        //check if the order exists
        if (!this.orderTransactionMap.containsKey(orderId))
            return false;

        //check location productType
        String productCode= this.orderTransactionMap.get(orderId).getProductCode();
        if (this.productTypeMap.get(productCode).getLocation()==null)
            throw new InvalidLocationException();

        //check order status
        String status =this.orderTransactionMap.get(orderId).getStatus();
        if (!status.equalsIgnoreCase("completed") && !status.equalsIgnoreCase("payed"))
            return false;
        // if already completed do nothing
        if (status.equalsIgnoreCase("completed"))
            return true;

        try {
            // set status PAYED
            this.dbase.updateOrderStatus(orderId, "COMPLETED");

        } catch (SQLException e) {
            System.out.println(e.getMessage()+"ORDER STATUS");
            return false;
        }
        //state changes to completed
        this.orderTransactionMap.get(orderId).setStatus("completed");
        //update product Quantity
        int quantity=this.orderTransactionMap.get(orderId).getQuantity();

        int oldQuantity= this.productTypeMap.get(productCode).getQuantity();

        this.productTypeMap.get(productCode).setQuantity(oldQuantity+quantity);

        try {
            this.dbase.updateProduct((EZProductType) this.productTypeMap.get(productCode));
        } catch (SQLException e) {
            System.out.println(e.getMessage()+"UPDATE PRODUCT");
        }


        return true;
    }

    /**
     * This method return the list of all orders ISSUED, ORDERED and COMLPETED.
     * It can be invoked only after a user with role "Administrator" or "ShopManager" is logged in.
     *
     * @return a list containing all orders
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public List<Order> getAllOrders() throws UnauthorizedException {
        //user session validity
        if (!this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("SHOPMANAGER"))
            throw new UnauthorizedException();

        return new ArrayList<>(this.orderTransactionMap.values());
    }

    /**
     * This method saves a new customer into the system. The customer's name should be unique.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param customerName the name of the customer to be registered
     *
     * @return the id (>0) of the new customer if successful, -1 otherwise
     *
     * @throws InvalidCustomerNameException if the customer name is empty or null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public Integer defineCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {
        if (customerName==null || customerName.trim().equals("")) //nome null or empty string
            throw new InvalidCustomerNameException();
        if ( userSession==null ) // if the user is not logged
            throw new UnauthorizedException();
        Integer newCustomerId = this.idCustomer;
        EZCustomer c = new EZCustomer(customerName, newCustomerId);
        customerMap.put(newCustomerId,c); // the new customer is inserted on the customer map and in the db
        try {
            if(!this.dbase.insertCustomer(c))
                return -1;
        } catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
        }
        this.idCustomer++; //idcustomer is a counter that is used to define the id customer
        return newCustomerId;
    }
    /**
     * This method updates the data of a customer with given <id>. This method can be used to assign/delete a card to a
     * customer. If <newCustomerCard> has a numeric value than this value will be assigned as new card code, if it is an
     * empty string then any existing card code connected to the customer will be removed and, finally, it assumes the
     * null value then the card code related to the customer should not be affected from the update. The card code should
     * be unique and should be a string of 10 digits.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param id the id of the customer to be updated
     * @param newCustomerName the new name to be assigned
     * @param newCustomerCard the new card code to be assigned. If it is empty it means that the card must be deleted,
     *                        if it is null then we don't want to update the cardNumber
     *
     * @return true if the update is successful
     *          false if the update fails ( cardCode assigned to another user, db unreacheable)
     *
     * @throws InvalidCustomerNameException if the customer name is empty or null
     * @throws InvalidCustomerCardException if the customer card is empty, null or if it is not in a valid format (string with 10 digits)
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard) throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException, UnauthorizedException {
        boolean flag = false;
        if (newCustomerName==null || newCustomerName.trim().equals(""))  // Check if the newCustomerName is valid.
            throw new InvalidCustomerNameException();

        if(!customerMap.containsKey(id))                                 //If the Customer id doesn't exist or the customerMap doesn't contain the id
            throw new InvalidCustomerIdException();

        if(userSession == null)                                         //if the user is not logged
            throw new UnauthorizedException();


        if(newCustomerCard != null){
            if (!newCustomerCard.equals("")){
                if(!checkCustomerCardValidity(newCustomerCard))             //newCustomerCard is not in a valid format
                    throw new InvalidCustomerCardException();

                for (Customer c : this.customerMap.values()) {              //if the new customerCard already exists, return false
                    if (c.getCustomerCard().equals(newCustomerCard)) {
                        return false;
                    }
                }
                if(checkCustomerCardValidity(newCustomerCard)) {
                    EZCustomer c = (EZCustomer) customerMap.get(id);
                    c.setCustomerCard(newCustomerCard);
                    try {
                        if (!this.dbase.updateCustomerCard(c.getId(), newCustomerCard))
                            return false;
                    } catch (SQLException e) {
                        System.out.println("There was a problem with the database:");
                        System.out.println(e.getSQLState());
                        return false;
                    }
                }
            }else{
                EZCustomer s = (EZCustomer) customerMap.get(id);        // if the customerCard is empty, delete the Card
                s.removeCustomerCard();
                try {
                    this.dbase.deleteCustomerCard(s.getId());
                } catch (SQLException e) {
                    System.out.println("There was a problem with the database:");
                    System.out.println(e.getSQLState());
                    return false;
                }
            }
        }

        EZCustomer c = (EZCustomer) customerMap.get(id);
        c.setCustomerName(newCustomerName);
        try {
            this.dbase.updateCustomer(c);
        } catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return false;
        }

        return true;
    }
    /**
     * This method deletes a customer with given id from the system.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param id the id of the customer to be deleted
     * @return true if the customer was successfully deleted
     *          false if the user does not exists or if we have problems to reach the db
     *
     * @throws InvalidCustomerIdException if the id is null, less than or equal to 0.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean deleteCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        if(id==null || id<=0)
            throw new InvalidCustomerIdException();
        if(userSession==null)
            throw new UnauthorizedException();
        if(!customerMap.containsKey(id))
            return false;
        EZCustomer c = (EZCustomer) customerMap.get(id);
        customerMap.remove(id);
        c.removeCustomerCard();
        try {
            this.dbase.deleteCustomer(c.getId());
        } catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return false;
        }
        c=null;

        return true;


    }
    /**
     * This method returns a customer with given id.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param id the id of the customer
     *
     * @return the customer with given id
     *          null if that user does not exists
     *
     * @throws InvalidCustomerIdException if the id is null, less than or equal to 0.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public Customer getCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        if(id==null || id<=0)
            throw new InvalidCustomerIdException();
        if(userSession==null )
            throw new UnauthorizedException();
        if(!customerMap.containsKey(id))
            return null;
        return customerMap.get(id);
    }

    /**
     * This method returns a list containing all registered users.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @return the list of all the customers registered
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public List<Customer> getAllCustomers() throws UnauthorizedException {
        if(userSession==null)
            throw new UnauthorizedException();
        return (new ArrayList<Customer>(customerMap.values()));
    }
    /**
     * This method returns a string containing the code of a new assignable card.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @return the code of a new available card. An empty string if the db is unreachable
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public String createCard() throws UnauthorizedException {
        if(userSession==null)
            throw new UnauthorizedException();
        Integer newCustomerCard = idCustomerCard;
        String customerCardString = newCustomerCard.toString();
        if (customerCardString.length() != 10){                                  //CustomerCardString must be 10 characters long.
            String tmp = new String("5") ;                               // I convert the number to a string and add the amount of zeros needed to get to 10 characters
            String str1 = "0";                                                 // ex. idCustomerCard = 3 -> String CustomerCardString="0000000003"
            for(int i=0; i<(9-customerCardString.length()); i++){              // Integer max value is 2ˆ32 -1 . Integer cannot be used for 10-digit numbers. LONG?
                tmp = tmp + str1;
            }
            customerCardString = tmp + customerCardString;
        }
        this.idCustomerCard++;
        return customerCardString;
    }

    /**
     * This method assigns a card with given card code to a customer with given identifier. A card with given card code
     * can be assigned to one customer only.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param customerCard the number of the card to be attached to a customer
     * @param customerId the id of the customer the card should be assigned to
     *
     * @return true if the operation was successful
     *          false if the card is already assigned to another user, if there is no customer with given id, if the db is unreachable
     *
     * @throws InvalidCustomerIdException if the id is null, less than or equal to 0.
     * @throws InvalidCustomerCardException if the card is null, empty or in an invalid format
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean attachCardToCustomer(String customerCard, Integer customerId) throws InvalidCustomerIdException, InvalidCustomerCardException, UnauthorizedException {
        if(userSession==null)
            throw new UnauthorizedException();
        if(customerId==null || customerId<=0)
            throw new InvalidCustomerIdException();
        if( customerCard==null || !checkCustomerCardValidity(customerCard) )         //newCustomerCard is not in a valid format, the regex expression should check also if the string is empty.
            throw new InvalidCustomerCardException();

        for (Customer c : this.customerMap.values()) {              //if the new customerCard already exists, return false
            if (c.getCustomerCard().equals(customerCard))
            {
                return false;
            }
        }
        if(!customerMap.containsKey(customerId))
            return false;

        EZCustomer c = (EZCustomer) customerMap.get(customerId);
        c.setCustomerCard(customerCard);
        try {
            if(!this.dbase.updateCustomerCard(c.getId(), customerCard))
                return false;
        } catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return false;
        }

        return true;
    }
    /**
     * This method updates the points on a card adding to the number of points available on the card the value assumed by
     * <pointsToBeAdded>. The points on a card should always be greater than or equal to 0.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param customerCard the card the points should be added to
     * @param pointsToBeAdded the points to be added or subtracted ( this could assume a negative value)
     *
     * @return true if the operation is successful
     *          false   if there is no card with given code,
     *                  if pointsToBeAdded is negative and there were not enough points on that card before this operation,
     *                  if we cannot reach the db.
     *
     * @throws InvalidCustomerCardException if the card is null, empty or in an invalid format
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */

    @Override
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException, UnauthorizedException {
        boolean found = false;
        if(userSession==null)
            throw new UnauthorizedException();
        if( customerCard==null || !checkCustomerCardValidity(customerCard))         //newCustomerCard is not in a valid format, the regex expression should check also if the string is empty.
            throw new InvalidCustomerCardException();
        for (Customer c : customerMap.values()){
            if (c.getCustomerCard().equals(customerCard))
            {
                found = true;
                if(c.getPoints()<Math.abs(pointsToBeAdded) && pointsToBeAdded < 0)   // if pointsToBeAdded is negative and there were not enough points on that card before this operation
                    return false;
                c.setPoints(c.getPoints() + pointsToBeAdded);
                try {
                    if(!this.dbase.updatePoints(c.getId(),c.getPoints() + pointsToBeAdded))
                        return false;
                    break;
                } catch (SQLException e) {
                    System.out.println("There was a problem with the database:");
                    System.out.println(e.getSQLState());
                    return false;
                }
            }
        }
        return found;
    }

    // --- Manage Sale Transactions --- //

    /**
        computeSaleTransactionPrice(Integer transactionId)
        Calcola il prezzo totale di una singola transazione, iterando tutti i prodotti
        presenti al suo interno e applicando i relativi sconti. Non viene applicato
        lo sconto totale della transazione.
        @param st: la transazione da controllare

        @return il prezzo calcolato
     */
    public double computeSaleTransactionPrice(SaleTransaction st) {
        List<TicketEntry> list = st.getEntries();
        double total = 0;

        for (TicketEntry e : list) {
            total += e.getPricePerUnit() * e.getAmount() * (1 - e.getDiscountRate());
        }

        return total;
    }

    /**
     * This method starts a new sale transaction and returns its unique identifier.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @return the id of the transaction (greater than or equal to 0)
     */
    @Override
    public Integer startSaleTransaction() throws UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        int newID = ++this.counter_transactionID;
        // aggiungi SaleTransaction alla mappa specifica
        this.saleTransactionMap.put(newID, new EZSaleTransaction(newID));
        // l'oggetto BalanceOperation verrà aggiunto solo quando verrà chiusa la transazione

        return newID;
    }

    /**
     * This method adds a product to a sale transaction decreasing the temporary amount of product available on the
     * shelves for other customers.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     * @param productCode the barcode of the product to be added
     * @param amount the quantity of product to be added
     * @return  true if the operation is successful
     *          false   if the product code does not exist,
     *                  if the quantity of product cannot satisfy the request,
     *                  if the transaction id does not identify a started and open transaction.
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidQuantityException if the quantity is less than 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean addProductToSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId == null || transactionId <= 0  ) {
            throw new InvalidTransactionIdException();
        }

        if (amount < 0) {
            throw new InvalidQuantityException();
        }

        productCode = productCode.trim();
        if (productCode == null || productCode.equals("") || !this.checkBarCodeValidity(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return false;
        }

        if (!productTypeMap.containsKey(productCode)) {
            return false;
        }
        else {
            ProductType p = productTypeMap.get(productCode);
            if (p.getQuantity() < amount) {
                return false;
            }

            EZSaleTransaction s = (EZSaleTransaction) saleTransactionMap.get(transactionId);

            // aggiorna la lista di prodotti della transazione e ricalcola il prezzo totale
            s.addEntry(p, amount, s.getDiscountRate());
            s.setPrice(this.computeSaleTransactionPrice(s) * (1 - s.getDiscountRate()));

            // aggiorna (temporaneamente) la quantità del prodotto e la mappa dei prodotti
            p.setQuantity(p.getQuantity() - amount);

            // aggiorna la mappa dei prodotti
            productTypeMap.put(productCode, p);

            // aggiorna la mappa
            saleTransactionMap.put(transactionId, s);

            return true;
        }
    }

    /**
     * This method deletes a product from a sale transaction increasing the temporary amount of product available on the
     * shelves for other customers.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     * @param productCode the barcode of the product to be deleted
     * @param amount the quantity of product to be deleted
     *
     * @return  true if the operation is successful
     *          false   if the product code does not exist,
     *                  if the quantity of product cannot satisfy the request,
     *                  if the transaction id does not identify a started and open transaction.
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidQuantityException if the quantity is less than 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean deleteProductFromSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
         && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId == null ||transactionId <= 0  ) {
            throw new InvalidTransactionIdException();
        }

        if (amount < 0) {
            throw new InvalidQuantityException();
        }

        productCode = productCode.trim();
        if (productCode == null || productCode.equals("") || !this.checkBarCodeValidity(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return false;
        }

        if (!productTypeMap.containsKey(productCode)) {
            return false;
        }
        else {
            ProductType p = productTypeMap.get(productCode);

            EZSaleTransaction s = (EZSaleTransaction) saleTransactionMap.get(transactionId);

            List<TicketEntry> l = s.getEntries();

            boolean found = false;

            // cerca l'elemento corrispondente al prodotto nella lista dei prodotti
            // della transazione
            for (TicketEntry e : l) {
                if (e.getBarCode().equals(productCode)) {
                    // se non vi sono abbastanza unità del prodotto in magazzino, ritorna false
                    if (e.getAmount() < amount) {
                        return false;
                    }
                    found = true;
                    break;
                }
            }
            // se non viene trovato, ritorna false
            if (!found) {
                return false;
            }

            // elimina il prodotto dalla lista
            s.updateProductInEntry(productCode, -amount);

            // aggiorna il prezzo della transazione e la mappa
            s.setPrice(this.computeSaleTransactionPrice(s) * (1 - s.getDiscountRate()));
            saleTransactionMap.put(transactionId, s);


            // aggiorna (temporaneamente) la quantità del prodotto e la mappa dei prodotti
            p.setQuantity(p.getQuantity() + amount);

            // aggiorna la mappa dei prodotti
            productTypeMap.put(productCode, p);


            return true;
        }
    }

    /**
     * This method applies a discount rate to all units of a product type with given type in a sale transaction. The
     * discount rate should be greater than or equal to 0 and less than 1.
     * The sale transaction should be started and open.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     * @param productCode the barcode of the product to be discounted
     * @param discountRate the discount rate of the product
     *
     * @return  true if the operation is successful
     *          false   if the product code does not exist,
     *                  if the transaction id does not identify a started and open transaction.
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidDiscountRateException if the discount rate is less than 0 or if it greater than or equal to 1.00
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId == null||transactionId <= 0  ) {
            throw new InvalidTransactionIdException();
        }

        if (discountRate < 0 || discountRate > 1) {
            throw new InvalidDiscountRateException();
        }

        productCode = productCode.trim();
        if (productCode == null || productCode.equals("") || !this.checkBarCodeValidity(productCode)) {
            throw new InvalidProductCodeException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return false;
        }

        if (!productTypeMap.containsKey(productCode)) {
            return false;
        }
        else {
            EZSaleTransaction s = (EZSaleTransaction) saleTransactionMap.get(transactionId);

            List<TicketEntry> l = s.getEntries();

            boolean found = false;

            // trova l'elemento corrispondente nella lista di prodotti della transazione
            // e ne cambia il tasso di sconto
            for (TicketEntry e : l) {
                if (e.getBarCode().equals(productCode)) {
                    found = true;
                    e.setDiscountRate(discountRate);
                    break;
                }
            }
            if (!found) {
                return false;
            }

            // Aggiorna il prezzo della transazione e la mappa
            s.setPrice(this.computeSaleTransactionPrice(s) * (1 - s.getDiscountRate()));
            saleTransactionMap.put(transactionId, s);

            return true;
        }
    }

    /**
     * This method applies a discount rate to the whole sale transaction.
     * The discount rate should be greater than or equal to 0 and less than 1.
     * The sale transaction can be either started or closed but not already payed.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     * @param discountRate the discount rate of the sale
     *
     * @return  true if the operation is successful
     *          false if the transaction does not exists
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws InvalidDiscountRateException if the discount rate is less than 0 or if it greater than or equal to 1.00
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean applyDiscountRateToSale(Integer transactionId, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId <= 0 || transactionId == null) {
            throw new InvalidTransactionIdException();
        }

        if (discountRate < 0 || discountRate > 1) {
            throw new InvalidDiscountRateException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return false;
        }

        EZSaleTransaction s = (EZSaleTransaction) saleTransactionMap.get(transactionId);

        // aggiorna il tasso di sconto per la transazione e il prezzo
        s.setDiscountRate(discountRate);
        s.setPrice(this.computeSaleTransactionPrice(s) * (1 - s.getDiscountRate()));
        // aggiorna la mappa delle transazioni
        saleTransactionMap.put(transactionId, s);

        return true;
    }

    /**
     * This method returns the number of points granted by a specific sale transaction.
     * Every 10€ the number of points is increased by 1 (i.e. 19.99€ returns 1 point, 20.00€ returns 2 points).
     * If the transaction with given id does not exist then the number of points returned should be -1.
     * The transaction may be in any state (open, closed, payed).
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     *
     * @return the points of the sale (1 point for each 10€) or -1 if the transaction does not exists
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public int computePointsForSale(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId <= 0 || transactionId == null) {
            throw new InvalidTransactionIdException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return -1;
        }

        EZSaleTransaction s = (EZSaleTransaction) saleTransactionMap.get(transactionId);

        // calcola e ritorna il numero di punti
        return (int) (this.computeSaleTransactionPrice(s) / 10);
    }

    /**
     * This method closes an opened transaction. After this operation the
     * transaction is persisted in the system's memory.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the Sale transaction
     *
     * @return  true    if the transaction was successfully closed
     *          false   if the transaction does not exist,
     *                  if it has already been closed,
     *                  if there was a problem in registering the data
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean endSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId <= 0 || transactionId == null) {
            throw new InvalidTransactionIdException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return false;
        }

        EZSaleTransaction st = (EZSaleTransaction) this.saleTransactionMap.get(transactionId);
        st.setPrice(this.computeSaleTransactionPrice(st) * (1 - st.getDiscountRate()));
        st.setStatus("CLOSED");

        // Aggiungo una BalanceOperation corrispondente alla SaleTransaction
        EZBalanceOperation bo = new EZBalanceOperation(transactionId, LocalDate.now(), this.computeSaleTransactionPrice(st) * (1 - st.getDiscountRate()));

        // Nota: le mappe vengono salvate in memoria in modo persistente solo qui
        try {
            this.dbase.addBalanceOperation(bo);
            this.dbase.addSaleTransaction(st);
            this.dbase.updateSaleInventoryQuantity(st);
        }
        catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return false;
        }

        // aggiorna dati in locale
        /*List<TicketEntry> entryList = st.getEntries();

        for (TicketEntry e : entryList) {
            ProductType p = this.productTypeMap.get(e.getBarCode());

            // aggiorna la quantità del prodotto
            p.setQuantity(p.getQuantity() - e.getAmount());
            productTypeMap.put(p.getBarCode(), p);
        }*/

        this.transactionMap.put(transactionId, bo);
        this.saleTransactionMap.put(transactionId, st);

        return true;
    }

    /**
     * This method deletes a sale transaction with given unique identifier from the system's data store.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param saleNumber the number of the transaction to be deleted
     *
     * @return  true if the transaction has been successfully deleted,
     *          false   if the transaction doesn't exist,
     *                  if it has been payed,
     *                  if there are some problems with the db
     *
     * @throws InvalidTransactionIdException if the transaction id number is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean deleteSaleTransaction(Integer saleNumber) throws InvalidTransactionIdException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (saleNumber <= 0 || saleNumber == null) {
            throw new InvalidTransactionIdException();
        }

        if (!saleTransactionMap.containsKey(saleNumber)) {
            return false;
        }

        EZSaleTransaction st = (EZSaleTransaction) this.saleTransactionMap.get(saleNumber);
        if (st.getStatus().equalsIgnoreCase("CLOSED")
            || st.getStatus().equalsIgnoreCase("PAID")) {
            return false;
        }

        /*
            NOTE: since a sale transaction is permanently recorded only when it's closed
            and a closed sale transaction cannot be deleted, this method does not communicate with
            the DB; instead, it merely removes the sale transaction from the corresponding map.

            The Balance Operations map isn't updated either since a BO is inserted in the DB/map
            only when the corresponding sale transaction is closed.
         */

        // riaggiunge le quantità dei prodotti temporaneamente eliminate
        for (TicketEntry e : st.getEntries()) {
            ProductType p = this.productTypeMap.get(e.getBarCode());

            p.setQuantity(p.getQuantity() + e.getAmount());

            this.productTypeMap.put(p.getBarCode(), p);
        }

        this.saleTransactionMap.remove(saleNumber);

        return true;
    }

    /**
     * This method returns  a closed sale transaction.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param transactionId the id of the CLOSED Sale transaction
     *
     * @return the transaction if it is available (transaction closed), null otherwise
     *
     * @throws InvalidTransactionIdException if the transaction id less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public SaleTransaction getSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (transactionId <= 0 || transactionId == null) {
            throw new InvalidTransactionIdException();
        }

        if (!saleTransactionMap.containsKey(transactionId)) {
            return null;
        }

        return this.saleTransactionMap.get(transactionId);
    }

    // --- Manage Return Transactions --- //

    /**
     * This method starts a new return transaction for units of products that have already been sold and payed.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param saleNumber the number of the transaction
     *
     * @return the id of the return transaction (>= 0), -1 if the transaction is not available.
     *
     * @throws InvalidTransactionIdException if the transactionId  is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public Integer startReturnTransaction(Integer saleNumber) throws /*InvalidTicketNumberException,*/InvalidTransactionIdException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER")
                && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }
        if (saleNumber < 0) {
            throw new InvalidTransactionIdException();
        }

        if (!this.saleTransactionMap.containsKey(saleNumber)) {
            return -1;
        }

        EZSaleTransaction e = (EZSaleTransaction) this.saleTransactionMap.get(saleNumber);

        if (!e.getStatus().equalsIgnoreCase("PAID")) {
            return -1;
        }

        int newID = ++this.counter_returnTransactionID;

        e.addReturn(new EZReturnTransaction(saleNumber, newID));

        this.saleTransactionMap.put(saleNumber, e);
        this.returnTransactionMap.put(newID, new EZReturnTransaction(saleNumber, newID));

        return newID;
    }

    /**
     * This method adds a product to the return transaction
     * The amount of units of product to be returned should not exceed the amount originally sold.
     * This method DOES NOT update the product quantity
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param returnId the id of the return transaction
     * @param productCode the bar code of the product to be returned
     * @param amount the amount of product to be returned
     *
     * @return  true if the operation is successful
     *          false   if the the product to be returned does not exists,
     *                  if it was not in the transaction,
     *                  if the amount is higher than the one in the sale transaction,
     *                  if the transaction does not exist
     *
     * @throws InvalidTransactionIdException if the return id is less ther or equal to 0 or if it is null
     * @throws InvalidProductCodeException if the product code is empty, null or invalid
     * @throws InvalidQuantityException if the quantity is less than or equal to 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        if (returnId <= 0 || returnId == null) {
            throw new InvalidTransactionIdException();
        }
        if (productCode == null || productCode.equals("") || !this.checkBarCodeValidity(productCode)) {
            throw new InvalidProductCodeException();
        }
        if (amount <= 0) {
            throw new InvalidQuantityException();
        }
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
         && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }
        // check if product exists
        if (!this.productTypeMap.containsKey(productCode)) {
            return false;
        }
        // check if the return transaction exists
        if (!this.returnTransactionMap.containsKey(returnId)) {
            return false;
        }

        EZReturnTransaction ret = (EZReturnTransaction) this.returnTransactionMap.get(returnId);
        // check if sale transaction exists
        if (!this.saleTransactionMap.containsKey(ret.getSaleTransactionID())) {
            return false;
        }

        EZSaleTransaction sale = (EZSaleTransaction) this.saleTransactionMap.get(ret.getSaleTransactionID());
        // check if the product is in the sale transaction
        int prodAmt = -1;
        for (TicketEntry e : sale.getEntries()) {
            if (e.getBarCode().equals(productCode)) {
                prodAmt = e.getAmount();
                break;
            }
        }
        // this happens if the product is not in the transaction
        if (prodAmt < 0) {
            return false;
        }
        // otherwise, check if the amount inserted is larger than the amount bought
        else if (amount > prodAmt) {
            return false;
        }

        // ok, everything should be fine at this point
        HashMap<String, Integer> returnProdMap = (HashMap<String, Integer>) ret.getMapOfProducts();

        returnProdMap.put(productCode, amount);

        ret.setMapOfProducts(returnProdMap);

        // update both the sale transaction's list and the return map
        sale.updateReturn(ret);
        this.returnTransactionMap.put(ret.getReturnID(), ret);

        return true;
    }

    /**
     * This method closes a return transaction. A closed return transaction can be committed (i.e. <commit> = true) thus
     * it increases the product quantity available on the shelves or not (i.e. <commit> = false) thus the whole trasaction
     * is undone.
     * This method updates the transaction status (decreasing the number of units sold by the number of returned one and
     * decreasing the final price).
     * If committed, the return transaction must be persisted in the system's memory.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param returnId the id of the transaction
     * @param commit whether we want to commit (True) or rollback(false) the transaction
     *
     * @return  true if the operation is successful
     *          false   if the returnId does not correspond to an active return transaction,
     *                  if there is some problem with the db
     *
     * @throws InvalidTransactionIdException if returnId is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean endReturnTransaction(Integer returnId, boolean commit) throws InvalidTransactionIdException, UnauthorizedException {
        if (returnId < 0 || returnId == null) {
            throw new InvalidTransactionIdException();
        }
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }
        // check if return exists
        if (!this.returnTransactionMap.containsKey(returnId)) {
            return false;
        }

        EZReturnTransaction ret = (EZReturnTransaction) this.returnTransactionMap.get(returnId);

        if (!this.transactionMap.containsKey(ret.getSaleTransactionID())
            || !this.saleTransactionMap.containsKey(ret.getSaleTransactionID())) {
            return false;
        }

        EZBalanceOperation bo = (EZBalanceOperation) this.transactionMap.get(ret.getSaleTransactionID());
        EZSaleTransaction sale = (EZSaleTransaction) this.saleTransactionMap.get(ret.getSaleTransactionID());

        if (!commit) {
            // rollback the transaction
            // remove the return transaction from the sale and update the map
            sale.deleteReturn(ret.getReturnID());
            this.saleTransactionMap.put(sale.getTicketNumber(), sale);
            // note that, at this point, the return transaction only exists temporarily in
            // this class' map, thus there's no need to delete it from the DB (because it's not there)
            this.returnTransactionMap.remove(returnId);
        }
        else {
            /*
                Must update:
                * the corresponding sale transaction, i.e. entries and price
                * the quantity for each product type returned
             */
            HashMap<String, Integer> rMap = (HashMap<String, Integer>) ret.getMapOfProducts();

            for (String prodCode : ret.getMapOfProducts().keySet()) {
                // update the quantity for each product in the return list
                EZProductType p = (EZProductType) this.productTypeMap.get(prodCode);
                int quantity = rMap.get(prodCode);

                // update corresponding sale entry
                sale.updateProductInEntry(prodCode, -quantity);
            }
            double prevMoney = sale.getPrice();
            double newMoney = this.computeSaleTransactionPrice(sale);

            // (extra check) check if there's enough money to refund
            if (this.computeBalance() - (prevMoney - newMoney) < 0) {
                return false;
            }

            // set the amount of money returned in the return transaction
            ret.setMoneyReturned(prevMoney - newMoney);
            // change the return transaction's status
            ret.setStatus("CLOSED");

            // recompute the sale's price
            sale.setPrice(newMoney);
            bo.setMoney(newMoney);

            try {
                this.dbase.addReturnTransaction(ret);
                this.dbase.updateBalanceOperation(bo);
                this.dbase.updateSaleTransaction(sale);
                this.dbase.updateReturnInventoryQuantity(ret);
            }
            catch (SQLException e) {
                System.out.println("There was a problem with the database:");
                System.out.println(e.getSQLState());
                return false;
            }

            // update the corresponding BO
            this.transactionMap.put(bo.getBalanceId(), bo);

            // update both the ST's list and the return map
            sale.updateReturn(ret);
            this.returnTransactionMap.put(ret.getReturnID(), ret);

            // update the map
            this.saleTransactionMap.put(sale.getTicketNumber(), sale);

            // update quantities in inventory
            for (String prodCode : ret.getMapOfProducts().keySet()) {
                EZProductType p = (EZProductType) this.productTypeMap.get(prodCode);
                int quantity = rMap.get(prodCode);
                p.setQuantity(p.getQuantity() + quantity);
                this.productTypeMap.put(prodCode, p);
            }
        }

        return true;
    }

    /**
     * This method deletes a closed return transaction. It affects the quantity of product sold in the connected sale transaction
     * (and consequently its price) and the quantity of product available on the shelves.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param returnId the identifier of the return transaction to be deleted
     *
     * @return  true if the transaction has been successfully deleted,
     *          false   if it doesn't exist,
     *                  if it has been payed,
     *                  if there are some problems with the db
     *
     * @throws InvalidTransactionIdException if the transaction id is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean deleteReturnTransaction(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        if (returnId < 0 || returnId == null) {
            throw new InvalidTransactionIdException();
        }
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }
        // check if return exists
        if (!this.returnTransactionMap.containsKey(returnId)) {
            return false;
        }

        EZReturnTransaction ret = (EZReturnTransaction) this.returnTransactionMap.get(returnId);
        EZSaleTransaction sale = (EZSaleTransaction) this.saleTransactionMap.get(ret.getSaleTransactionID());

        if (ret.getStatus().equalsIgnoreCase("CLOSED")
            || ret.getStatus().equalsIgnoreCase("PAID")) {
            return false;
        }

        /*
            Must update:
            * the corresponding sale transaction, i.e. entries and price
            * the quantity for each product type returned
         */
        HashMap<String, Integer> rMap = (HashMap<String, Integer>) ret.getMapOfProducts();

        for (String prodCode : ret.getMapOfProducts().keySet()) {
            // update the quantity for each product in the return list
            int quantity = rMap.get(prodCode);

            // update corresponding sale entry
            sale.updateProductInEntry(prodCode, +quantity);
        }
        // recompute the sale's price
        sale.setPrice(this.computeSaleTransactionPrice(sale));
        // remove the return transaction from the ST's list
        sale.deleteReturn(ret.getReturnID());

        /*
            NOTE: this method does not actually connect to the DB; see the comment
            in deleteSaleTransaction for more info (the reason behind this decision
            is practically the same).
         */

        // update the ST map
        this.saleTransactionMap.put(sale.getTicketNumber(), sale);
        // update the RT map
        this.returnTransactionMap.remove(returnId);

        return true;
    }

    // --- Manage Payments --- //

    /**
     * This method record the payment of a sale transaction with cash and returns the change (if present).
     * This method affects the balance of the system.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param ticketNumber the number of the transaction that the customer wants to pay
     * @param cash the cash received by the cashier
     *
     * @return the change (cash - sale price)
     *         -1   if the sale does not exists,
     *              if the cash is not enough,
     *              if there is some problemi with the db
     *
     * @throws InvalidTransactionIdException if the  number is less than or equal to 0 or if it is null
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     * @throws InvalidPaymentException if the cash is less than or equal to 0
     */
    @Override
    public double receiveCashPayment(Integer ticketNumber, double cash) throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException {
        if (cash <= 0) {
            throw new InvalidPaymentException();
        }

        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (ticketNumber <= 0 || ticketNumber == null) {
            throw new InvalidTransactionIdException();
        }

        if (!saleTransactionMap.containsKey(ticketNumber)) {
            return -1;
        }

        EZSaleTransaction result = (EZSaleTransaction) this.saleTransactionMap.get(ticketNumber);

        if (!result.getStatus().equalsIgnoreCase("CLOSED")) {
            return -1;
        }

        if (cash < result.getPrice()) {
            return -1;
        }

        // registra la modifica del conto, aggiungendo una nuova BalanceOperation
        // richiamando un metodo dell'API
        /*boolean success = this.recordBalanceUpdate(result.getPrice() * result.getDiscountRate());
        if (!success) {
            return -1;
        }*/

        result.setStatus("PAID");

        try {
            this.dbase.updateSaleTransaction(result);
        }
        catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            e.printStackTrace();
            return -1;
        }

        this.saleTransactionMap.put(result.getTicketNumber(), result);

        return (cash - result.getPrice());
    }

    /**
     * This method record the payment of a sale with credit card. If the card has not enough money the payment should
     * be refused.
     * The credit card number validity should be checked. It should follow the luhn algorithm.
     * The credit card should be registered in the system.
     * This method affects the balance of the system.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param ticketNumber the number of the sale that the customer wants to pay
     * @param creditCard the credit card number of the customer
     *
     * @return  true if the operation is successful
     *          false   if the sale does not exists,
     *                  if the card has not enough money,
     *                  if the card is not registered,
     *                  if there is some problem with the db connection
     *
     * @throws InvalidTransactionIdException if the sale number is less than or equal to 0 or if it is null
     * @throws InvalidCreditCardException if the credit card number is empty, null or if luhn algorithm does not
     *                                      validate the credit card
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean receiveCreditCardPayment(Integer ticketNumber, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        if (!this.checkCreditCardValidity(creditCard)) {
            throw new InvalidCreditCardException();
        }

        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (ticketNumber <= 0 || ticketNumber == null) {
            throw new InvalidTransactionIdException();
        }

        // Controllo se la carta esiste tra quelle registrate
        EZFileReader reader = new EZFileReader();
        Map<String, Double> ccMap = reader.readCreditCards("testFiles/creditCardFile.csv");

        if (!ccMap.containsKey(creditCard)) {
            return false;
        }
        // prendo la ST
        EZSaleTransaction result = (EZSaleTransaction) this.saleTransactionMap.get(ticketNumber);

        if (!result.getStatus().equalsIgnoreCase("CLOSED")) {
            return false;
        }

        // Controllo se la carta ha abbastanza soldi
        double ccAmount = ccMap.get(creditCard);

        if (ccAmount < result.getPrice()) {
            return false;
        };

        // registra il pagamento aggiungendo una nuova BalanceOperation
        // tramite metodo dell'API
        /*boolean success = this.recordBalanceUpdate(result.getPrice() * result.getDiscountRate());
        if (!success) {
            return false;
        }*/

        result.setStatus("PAID");

        try {
            this.dbase.updateSaleTransaction(result);
        }
        catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return false;
        }

        this.saleTransactionMap.put(result.getTicketNumber(), result);

        return true;
    }

    /**
     * This method record the payment of a closed return transaction with given id. The return value of this method is the
     * amount of money to be returned.
     * This method affects the balance of the application.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param returnId the id of the return transaction
     *
     * @return  the money returned to the customer
     *          -1  if the return transaction is not ended,
     *              if it does not exist,
     *              if there is a problem with the db
     *
     * @throws InvalidTransactionIdException if the return id is less than or equal to 0
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public double returnCashPayment(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (returnId <= 0) {
            throw new InvalidTransactionIdException();
        }

        if (!this.returnTransactionMap.containsKey(returnId)) {
            return -1;
        }

        EZReturnTransaction result = (EZReturnTransaction) this.returnTransactionMap.get(returnId);

        if (!result.getStatus().equalsIgnoreCase("CLOSED")
                && !result.getStatus().equalsIgnoreCase("PAID")) {
            return -1;
        }

        if (!this.saleTransactionMap.containsKey(result.getSaleTransactionID())) {
            return -1;
        }

        EZSaleTransaction sale = (EZSaleTransaction) this.saleTransactionMap.get(result.getSaleTransactionID());

        // registra la modifica del conto, aggiungendo una nuova BalanceOperation
        // richiamando un metodo dell'API
        /*boolean success = this.recordBalanceUpdate(result.getMoneyReturned());
        if (!success) {
            return -1;
        }*/

        result.setStatus("PAID");
        sale.updateReturn(result);

        try {
            this.dbase.updateReturnTransaction(result);
        }
        catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return -1;
        }

        this.saleTransactionMap.put(sale.getTicketNumber(), sale);
        this.returnTransactionMap.put(result.getReturnID(), result);

        return result.getMoneyReturned();
    }

    /**
     * This method record the payment of a return transaction to a credit card.
     * The credit card number validity should be checked. It should follow the luhn algorithm.
     * The credit card should be registered and its balance will be affected.
     * This method affects the balance of the system.
     * It can be invoked only after a user with role "Administrator", "ShopManager" or "Cashier" is logged in.
     *
     * @param returnId the id of the return transaction
     * @param creditCard the credit card number of the customer
     *
     * @return  the money returned to the customer
     *          -1  if the return transaction is not ended,
     *              if it does not exist,
     *              if the card is not registered,
     *              if there is a problem with the db
     *
     * @throws InvalidTransactionIdException if the return id is less than or equal to 0
     * @throws InvalidCreditCardException if the credit card number is empty, null or if luhn algorithm does not
     *                                      validate the credit card
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public double returnCreditCardPayment(Integer returnId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        if (!this.checkCreditCardValidity(creditCard)) {
            throw new InvalidCreditCardException();
        }
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")
                && !this.checkUserRole("CASHIER")) {
            throw new UnauthorizedException();
        }

        if (returnId <= 0) {
            throw new InvalidTransactionIdException();
        }

        // Controllo se la carta esiste tra quelle registrate
        EZFileReader reader = new EZFileReader();
        Map<String, Double> ccMap = reader.readCreditCards("testFiles/creditCardFile.csv");

        if (!ccMap.containsKey(creditCard)) {
            return -1;
        }

        // controllo se la RT esiste
        if (!this.returnTransactionMap.containsKey(returnId)) {
            return -1;
        }

        EZReturnTransaction result = (EZReturnTransaction) this.returnTransactionMap.get(returnId);

        if (!result.getStatus().equalsIgnoreCase("CLOSED")
                && !result.getStatus().equalsIgnoreCase("PAID")) {
            return -1;
        }

        if (!this.saleTransactionMap.containsKey(result.getSaleTransactionID())) {
            return -1;
        }

        EZSaleTransaction sale = (EZSaleTransaction) this.saleTransactionMap.get(result.getSaleTransactionID());

        // registra il pagamento aggiungendo una nuova BalanceOperation
        // tramite metodo dell'API
        /*boolean success = this.recordBalanceUpdate(-result.getMoneyReturned());
        if (!success) {
            return -1;
        }*/

        result.setStatus("PAID");
        sale.updateReturn(result);

        try {
            this.dbase.updateReturnTransaction(result);
        }
        catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return -1;
        }

        this.saleTransactionMap.put(sale.getTicketNumber(), sale);
        this.returnTransactionMap.put(result.getReturnID(), result);

        return result.getMoneyReturned();
    }

    // --- Manage Accounting --- //
    /**
     * This method record a balance update. <toBeAdded> can be both positive and nevative. If positive the balance entry
     * should be recorded as CREDIT, if negative as DEBIT. The final balance after this operation should always be
     * positive.
     * It can be invoked only after a user with role "Administrator", "ShopManager" is logged in.
     *
     * @param toBeAdded the amount of money (positive or negative) to be added to the current balance. If this value
     *                  is >= 0 than it should be considered as a CREDIT, if it is < 0 as a DEBIT
     *
     * @return  true if the balance has been successfully updated
     *          false if toBeAdded + currentBalance < 0.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")) {
            throw new UnauthorizedException();
        }

        EZBalanceOperation bo = new EZBalanceOperation(++this.counter_transactionID, LocalDate.now(), toBeAdded);

        if (this.computeBalance() + toBeAdded < 0) {
            return false;
        }

        try {
            this.dbase.addBalanceOperation(bo);
        }
        catch (SQLException e) {
            System.out.println("There was a problem with the database:");
            System.out.println(e.getSQLState());
            return false;
        }

        this.transactionMap.put(bo.getBalanceId(), bo);

        return true;
    }

    /**
     * This method returns a list of all the balance operations (CREDIT,DEBIT,ORDER,SALE,RETURN) performed between two
     * given dates.
     * This method should understand if a user exchanges the order of the dates and act consequently to correct
     * them.
     * Both <from> and <to> are included in the range of dates and might be null. This means the absence of one (or
     * both) temporal constraints.
     *
     *
     * @param from the start date : if null it means that there should be no constraint on the start date
     * @param to the end date : if null it means that there should be no constraint on the end date
     *
     * @return All the operations on the balance whose date is <= to and >= from
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")) {
            throw new UnauthorizedException();
        }

        List<BalanceOperation> returnList = new ArrayList<>(this.transactionMap.values());

        // Filtra la lista rimuovendo tutte le transazioni al di fuori dell'intervallo di tempo
        // Se sia from che to sono == null allora non si filtra nulla
        if (from == null) {
            if (to != null) {
                for (int i = returnList.size() - 1; i >= 0; i--) {
                    if (returnList.get(i).getDate().isAfter(to)) {
                        returnList.remove(i);
                    }
                }
            }
        }
        else {
            if (to == null) {
                for (int i = returnList.size() - 1; i >= 0; i--) {
                    if (returnList.get(i).getDate().isBefore(from)) {
                        returnList.remove(i);
                    }
                }
            }
            else {
                for (int i = returnList.size() - 1; i >= 0; i--) {
                    if (returnList.get(i).getDate().isBefore(from) || returnList.get(i).getDate().isAfter(to)) {
                        returnList.remove(i);
                    }
                }
            }
        }

        return returnList;
    }

    /**
     * This method returns the actual balance of the system.
     *
     * @return the value of the current balance
     *
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    @Override
    public double computeBalance() throws UnauthorizedException {
        if (!this.checkUserRole("SHOPMANAGER") && !this.checkUserRole("ADMINISTRATOR")) {
            throw new UnauthorizedException();
        }

        double total = 0;

        List<BalanceOperation> list = new ArrayList<BalanceOperation>(this.transactionMap.values());

        for (BalanceOperation bo : list) {
            total += bo.getMoney();
        }

        return total;
    }
}
