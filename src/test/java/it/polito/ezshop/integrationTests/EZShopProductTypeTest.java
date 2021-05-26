package it.polito.ezshop.integrationTests;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.classes.EZProductType;
import it.polito.ezshop.exceptions.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EZShopProductTypeTest {

    EZShop sp = new EZShop();

    @Before
    public void resetShop() {
        sp.reset();

    }

    @Test
    public void Test_createProductType(){
        Integer id;
        //TestAuthorization
        try {
            //No User Logged
            assertThrows(UnauthorizedException.class, ()->{sp.createProductType("Granarolo","6291041500213",1.5,"milk");});
            sp.createUser("fridanco1","pass","Cashier"); //Created,but no logged
            assertThrows(UnauthorizedException.class, ()->{sp.createProductType("Granarolo","6291041500213",1.5,"milk");});
            sp.login("fridanco1","pass");//Wrong Authorization
            assertThrows(UnauthorizedException.class, ()->{sp.createProductType("Granarolo","6291041500213",1.5,"milk");});
            sp.logout();
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException e){
            fail(e.getMessage());
        }
        //Test-Description
        assertThrows(InvalidProductDescriptionException.class, ()->{sp.createProductType(null,"6291041500213",1.5,"milk");});
        assertThrows(InvalidProductDescriptionException.class, ()->{sp.createProductType("","6291041500213",1.5,"milk");});

        //Test-ProductCode
        assertThrows(InvalidProductCodeException.class, ()->{sp.createProductType("Granarolo",null,1.5,"milk");});
        assertThrows(InvalidProductCodeException.class, ()->{sp.createProductType("Granarolo","",1.5,"milk");});
        assertThrows(InvalidProductCodeException.class, ()->{sp.createProductType("Granarolo","I'm not a number",1.5,"milk");});
        assertThrows(InvalidProductCodeException.class, ()->{sp.createProductType("Granarolo","6291041500218",1.5,"milk");});
        //Test-PriceperUnit
        assertThrows(InvalidPricePerUnitException.class,()->{sp.createProductType("Granarolo","6291041500213",-0.3,"milk");});
        assertThrows(InvalidPricePerUnitException.class,()->{sp.createProductType("Granarolo","6291041500213",0,"milk");});
        try {
            id=sp.createProductType("Granarolo","6291041500213",1.5,"milk");
            assertTrue(id>0);
            id=sp.createProductType("Granarolo","6291041500213",1.5,"milk");

        } catch (InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            fail(e.getMessage());
        }

    }
    @Test
    public void Test_updateProduct(){
        Integer id;
        try {
            //No User Logged
            assertThrows(UnauthorizedException.class, ()->{sp.updateProduct(1,"Granarolo","6291041500213",1.5,"milk");});
            sp.createUser("fridanco1","pass","Cashier"); //Created,but no logged
            assertThrows(UnauthorizedException.class, ()->{sp.updateProduct(1,"Granarolo","6291041500213",1.5,"milk");});
            sp.login("fridanco1","pass");//Wrong Authorization
            assertThrows(UnauthorizedException.class, ()->{sp.updateProduct(1,"Granarolo","6291041500213",1.5,"milk");});
            sp.logout();
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException e){
            fail(e.getMessage());
        }
        //Test-ProductIdException
        assertThrows(InvalidProductIdException.class,()->{sp.updateProduct(null,"Granarolo","6291041500213",1.5,"milk");});
        assertThrows(InvalidProductIdException.class,()->{sp.updateProduct(-2,"Granarolo","6291041500213",1.5,"milk");});
        //Test-Description
        assertThrows(InvalidProductDescriptionException.class, ()->{sp.updateProduct(1,null,"6291041500213",1.5,"milk");});
        assertThrows(InvalidProductDescriptionException.class, ()->{sp.updateProduct(1,"","6291041500213",1.5,"milk");});

        //Test-ProductCode
        assertThrows(InvalidProductCodeException.class, ()->{sp.updateProduct(1,"Granarolo",null,1.5,"milk");});
        assertThrows(InvalidProductCodeException.class, ()->{sp.updateProduct(1,"Granarolo","",1.5,"milk");});
        assertThrows(InvalidProductCodeException.class, ()->{sp.updateProduct(1,"Granarolo","I'm not a number",1.5,"milk");});
        assertThrows(InvalidProductCodeException.class, ()->{sp.updateProduct(1,"Granarolo","6291041500218",1.5,"milk");});
        //Test-PriceperUnit
        assertThrows(InvalidPricePerUnitException.class,()->{sp.updateProduct(1,"Granarolo","6291041500213",-0.3,"milk");});
        assertThrows(InvalidPricePerUnitException.class,()->{sp.updateProduct(1,"Granarolo","6291041500213",0,"milk");});
        try {
            assertFalse(sp.updateProduct(20,"Granarolo","6291041500213",1.5,"milk")); //id doesn't exist
            id=sp.createProductType("Granarolo","6291041500213",1.5,"milk");
            //System.out.println(id);
            //System.out.println(sp.checkBarCodeValidity("3391041500213"));
            assertTrue(sp.updateProduct(id,"Grana","3391041500213",1.3,"mqk")); //It exists
            assertEquals("Grana",sp.getAllProductTypes().get(0).getProductDescription());
            assertEquals("3391041500213",sp.getAllProductTypes().get(0).getBarCode());
        } catch (InvalidProductIdException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e) {
            System.out.println(e);
            fail(e.getMessage());
        }
    }
    public Integer Before_updateQuantity(){
        Integer id;
        try {
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");
            return id= sp.createProductType("as","6291041500213",1.5,"milk");
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException e){
            fail(e.getMessage());
        }
        return -1;
    }
    @Test
    public void Test_updateQuantity(){
        assertThrows(UnauthorizedException.class, ()->{sp.updateQuantity(1,3);});
        Integer id = Before_updateQuantity();
        //Test-ProductIdException
        assertThrows(InvalidProductIdException.class,()->{sp.updateQuantity(null,3);});
        assertThrows(InvalidProductIdException.class,()->{sp.updateQuantity(-2,3);});
        //Test-toBeAddedreturn false
        try {
            assertFalse(sp.updateQuantity(20,3));//it doesn't exist
            assertFalse(sp.updateQuantity(id,-3)); // 0-3 -> False
            assertFalse(sp.updateQuantity(id,4));
            sp.updatePosition(id,"2-C-4");
            assertTrue(sp.updateQuantity(id,4));
            assertEquals(4,(int)sp.getProductTypeByBarCode("6291041500213").getQuantity());
        } catch (InvalidProductIdException | UnauthorizedException | InvalidLocationException | InvalidProductCodeException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
@Test
    public void Test_deleteProductType() {
        Integer id;
        assertThrows(UnauthorizedException.class, () -> {
         sp.deleteProductType(1);
      });
        try {
        assertThrows(UnauthorizedException.class, () -> {
            sp.deleteProductType(1);
        });
        sp.createUser("fridanco1", "pass", "Cashier"); //Created,but no logged
        assertThrows(UnauthorizedException.class, () -> {
            sp.deleteProductType(1);
        });
        sp.login("fridanco1", "pass");//Wrong Authorization
        assertThrows(UnauthorizedException.class, () -> {
            sp.deleteProductType(1);
        });
        sp.logout();
        sp.createUser("fridanco", "pass", "Administrator");
        sp.login("fridanco", "pass");
        assertFalse(sp.deleteProductType(3)); // The product type map is empty
        id = sp.createProductType("Granarolo", "6291041500213", 1.5, "milk");
        assertThrows(InvalidProductIdException.class, () -> {
            sp.deleteProductType(null);
        });
        assertThrows(InvalidProductIdException.class, () -> {
            sp.deleteProductType(-3);
        });
        assertTrue(sp.deleteProductType(id));
        assertNull(sp.getProductTypeByBarCode("6291041500213"));


    } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | UnauthorizedException | InvalidProductIdException e) {
        e.printStackTrace();
        System.out.println(e);
    }
    }
    @Test
    public void Test_getAllProductTypes(){
        Integer id;
        assertThrows(UnauthorizedException.class, ()->{sp.getAllProductTypes();});
        try {
            sp.createUser("fridanco1","pass","Cashier"); //Created,but no logged
            assertThrows(UnauthorizedException.class, ()->{sp.getAllProductTypes();});
            sp.login("fridanco1","pass");//Wrong Authorization
            assertThrows(UnauthorizedException.class, ()->{sp.getAllProductTypes();});
            sp.logout();
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");

            assertEquals(0,sp.getAllProductTypes().size());
            id = sp.createProductType("Granarolo", "6291041500213", 1.5, "milk");
            assertEquals(1,sp.getAllProductTypes().size());
            id = sp.createProductType("Granarolo", "889654050810", 1.5, "milk");
            assertEquals(2,sp.getAllProductTypes().size());

        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | UnauthorizedException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException e ) {
            System.out.println(e);
        }

    }
    @Test
    public void Test_getProductbyBarcode(){
        Integer id;
        assertThrows(UnauthorizedException.class, ()->{sp.getProductTypeByBarCode("6291041500213");});
        try {
            sp.createUser("fridanco1","pass","Cashier"); //Created,but no logged
            assertThrows(UnauthorizedException.class, ()->{sp.getProductTypeByBarCode("6291041500213");});
            sp.login("fridanco1","pass");//Wrong Authorization
            assertThrows(UnauthorizedException.class, ()->{sp.getProductTypeByBarCode("6291041500213");});
            sp.logout();
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");
            assertThrows(InvalidProductCodeException.class, ()->{sp.getProductTypeByBarCode(null);});
            assertThrows(InvalidProductCodeException.class, ()->{sp.getProductTypeByBarCode("");});
            assertThrows(InvalidProductCodeException.class, ()->{sp.getProductTypeByBarCode("I'm not number");});
            assertThrows(InvalidProductCodeException.class, ()->{sp.getProductTypeByBarCode("6291041500218");});

            assertNull(sp.getProductTypeByBarCode("6291041500213"));
            id = sp.createProductType("Granarolo", "6291041500213", 1.5, "milk");
            EZProductType pr = (EZProductType) sp.getProductTypeByBarCode("6291041500213");
            assertEquals("Granarolo",pr.getProductDescription());
            assertEquals("6291041500213",pr.getBarCode());
            assertEquals((Double)1.5,pr.getPricePerUnit());
            assertEquals("milk",pr.getNote());


        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | UnauthorizedException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException e ) {
            System.out.println(e);
        }

    }
    @Test
    public void Test_getProductbyDescription(){
        Integer id;
        assertThrows(UnauthorizedException.class, ()->{sp.getProductTypesByDescription("Granarolo");});
        try {
            sp.createUser("fridanco1","pass","Cashier"); //Created,but no logged
            assertThrows(UnauthorizedException.class, ()->{sp.getProductTypesByDescription("Granarolo");});
            sp.login("fridanco1","pass");//Wrong Authorization
            assertThrows(UnauthorizedException.class, ()->{sp.getProductTypesByDescription("Granarolo");});
            sp.logout();
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");
            sp.createProductType("Granarolo", "6291041500213", 1.5, "milk");
            sp.createProductType("", "889654050810", 1.5, "milk");
            sp.createProductType("Granarolo", "654291598588", 1.5, "milk");
            assertEquals(1,sp.getProductTypesByDescription(null).size());
            assertEquals("889654050810",sp.getProductTypesByDescription(null).get(1).getBarCode());
            assertEquals(2,sp.getProductTypesByDescription("Granarolo").size());
            assertEquals(0,sp.getProductTypesByDescription("asd").size());



        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | UnauthorizedException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException e ) {
            System.out.println(e);
        }

    }
    @Test
    public void Test_getUpdatePosition(){
        Integer id;
        assertThrows(UnauthorizedException.class, ()->{sp.updatePosition(1,"1-a-3");});
        try {
            sp.createUser("fridanco1","pass","Cashier"); //Created,but no logged
            assertThrows(UnauthorizedException.class, ()->{sp.updatePosition(1,"1-a-3");});
            sp.login("fridanco1","pass");//Wrong Authorization
            assertThrows(UnauthorizedException.class, ()->{sp.updatePosition(1,"1-a-3");});
            sp.logout();
            sp.createUser("fridanco","pass","Administrator");
            sp.login("fridanco","pass");

            assertThrows(InvalidProductIdException.class, ()->{sp.updatePosition(-1,"1-a-3");});
            assertThrows(InvalidProductIdException.class, ()->{sp.updatePosition(0,"1-a-3");});

            assertThrows(InvalidLocationException.class, ()->{sp.updatePosition(1,"123asd");});

            sp.createProductType("Granarolo", "6291041500213", 1.51, "milk");
            sp.createProductType("Parmalat", "889654050810", 1.2, "milk");
            sp.createProductType("Grana", "654291598588", 1.45, "milk");

            assertFalse(sp.updatePosition(5,"1-d-2"));
            assertTrue(sp.updatePosition(1,"1-a-3"));
            assertEquals("1-a-3",sp.getProductTypeByBarCode("6291041500213").getLocation());
            assertFalse(sp.updatePosition(2,"1-a-3"));
            assertTrue(sp.updatePosition(2,""));
            assertNull(sp.getProductTypeByBarCode("889654050810").getLocation());
            assertTrue(sp.updatePosition(3,null));
            assertNull(sp.getProductTypeByBarCode("654291598588").getLocation());


            //assertEquals("1-a-3",sp.getAllProductTypes().get(0).getLocation());

        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | UnauthorizedException | InvalidProductDescriptionException | InvalidProductCodeException | InvalidPricePerUnitException | InvalidProductIdException | InvalidLocationException e ) {
            System.out.println(e);
        }

    }


}

