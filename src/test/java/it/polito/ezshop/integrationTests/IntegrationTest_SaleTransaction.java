package it.polito.ezshop.integrationTests;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.classes.EZProductType;
import it.polito.ezshop.data.classes.EZSaleTransaction;
import it.polito.ezshop.data.classes.EZUser;
import it.polito.ezshop.exceptions.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntegrationTest_SaleTransaction {
    EZShop ez = new EZShop();

    @Before
    public void test_reset() {
        ez.reset();
    }

    @Test
    public void test_SaleTransactions() {
        // first create a user for testing
        try {
            ez.createUser("testSt", "equjoin", "SHOPMANAGER");
        }
        catch (Exception e) {
            System.out.println("There was a problem in creating the user:");
            e.printStackTrace();
            return;
        }

        assertThrows(UnauthorizedException.class, () -> {
            ez.startSaleTransaction();
        });

        try {
            ez.login("testSt", "equjoin");
        }
        catch (Exception e) {
            System.out.println("There was a problem with logging in:");
            e.printStackTrace();
            return;
        }

        int tID = -1;
        try {
            tID = ez.startSaleTransaction();
        }
        catch (UnauthorizedException e) {
            System.out.println("User not authorized/logged in.");
            e.printStackTrace();
            return;
        }

        assertTrue(tID >= 0);

        String validBC = "6291041500213";

        try {
            int a = ez.createProductType("potato", validBC, 1, "tasty");
            ez.updatePosition(a,"2-C-4");
            ez.updateQuantity(a, 999);
        }
        catch (Exception e) {
            System.out.println("problem in adding the product");
            e.printStackTrace();
            return;
        }

        try {
            int finalTID = tID;
            assertThrows(InvalidProductCodeException.class, () -> {
                ez.addProductToSale(finalTID, "42", 1);
            });
            assertThrows(InvalidQuantityException.class, () -> {
                ez.addProductToSale(finalTID, validBC, -1);
            });
            assertThrows(InvalidTransactionIdException.class, () -> {
                ez.addProductToSale(-1, validBC, -1);
            });

            assertTrue(ez.addProductToSale(tID, validBC, 4));
            assertTrue(ez.deleteProductFromSale(tID, validBC, 2));

            assertTrue(ez.applyDiscountRateToProduct(tID, validBC, 0.5));
            assertTrue(ez.applyDiscountRateToSale(tID, 0.5));

            assertNotNull(ez.getSaleTransaction(tID));

            double price = ez.computeSaleTransactionPrice(ez.getSaleTransaction(tID));

            assertTrue(price > 0);

            assertEquals(price / 10, ez.computePointsForSale(tID), 0.1);

            assertTrue(ez.endSaleTransaction(tID));

            assertEquals(price * (1 - ez.getSaleTransaction(tID).getDiscountRate()), ez.getCreditsAndDebits(null, null).get(0).getMoney(), 0.0);

            System.out.println(ez.computeBalance());

            assertEquals(price * (1 - ez.getSaleTransaction(tID).getDiscountRate()), ez.computeBalance(), 0.0);

            assertFalse(ez.deleteSaleTransaction(tID));
        }
        catch (Exception e) {
            System.out.println("Encountered exception while testing:");
            e.printStackTrace();
            return;
        }
    }
}
