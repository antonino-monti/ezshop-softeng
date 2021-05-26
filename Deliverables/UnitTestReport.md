# Unit Testing Documentation

Authors: Giulia D'Ascenzi, Francesco Di Franco, Antonino Monti

Date: 19/05/2021

Version: 1.0

# Contents

- [Black Box Unit Tests](#black-box-unit-tests)


- [White Box Unit Tests](#white-box-unit-tests)


# Black Box Unit Tests

    <Define here criteria, predicates and the combination of predicates for each function of each class.
    Define test cases to cover all equivalence classes and boundary conditions.
    In the table, report the description of the black box test case and (traceability) the correspondence with the JUnit test case writing the 
    class and method name that contains the test case>
    <JUnit test classes must be in src/test/java/it/polito/ezshop   You find here, and you can use,  class TestEzShops.java that is executed  
    to start tests
    >

 ### **Class *EZShop* - method *checkBarCodeValidity***

**Criteria for method *checkBarCodeValidity*:**

 - Length of barCode
 - Data type of barCode digits
 - Validity of barCode following a validation protocol

**Predicates for method *checkBarCodeValidity*:**

| Criteria | Predicate |
| -------- | --------- |
| Length of barCode | >= 0 && < 12 |
|  | >= 12 && <= 14 |
| | > 14 |
| Data type of barCode digits | All characters are numeric |
|  | Not all characters are numeric |
| Validity of barCode following a validation protocol | Valid |
|  | Invalid |

**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
| Length of barCode | 0, 12, 14, +maxint |
| Data type of barCode digits | All numeric, not all numeric |
| Validity of barCode following a validation protocol | Valid, Invalid |

**Combination of predicates**:


| Length of barCode | Data type of barCode digits | Validity of barCode | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|-------|-------|
|[0, 12)|*|*|Invalid|checkBarCodeValidity("42")<br/> -> false<br/>checkBarCodeValidity(null)<br/> -> false|BB_UnitTesting.test_InvalidBarCode|
|(14, maxint)| *                           |*|Invalid|checkBarCodeValidity("62910415002187326548")<br/> -> false<br/>|BB_UnitTesting.test_InvalidBarCode|
|*| Not all numeric             |*|Invalid|checkBarCodeValidity("1234a234b")<br/> -> false<br/>|BB_UnitTesting.test_InvalidBarCode|
|*| *                           |Invalid|Invalid|checkBarCodeValidity("6291041500218")<br/> -> false<br/>|BB_UnitTesting.test_InvalidBarCode|
|[12,14]|All numeric|Valid|Valid|checkBarCodeValidity("6291041500213")<br/> -> true|BB_UnitTesting.test_ValidBarCode|

 ### **Class *EZShop* - method *checkCreditCardValidity***

**Criteria for method *checkCreditCardValidity*:**

 - Validity of creditCard following Luhn Algorithm
 - Data type of the digits

**Predicates for method *checkCreditCardValidity*:**

| Criteria               | Predicate |
| ---------------------- | --------- |
| Validity of creditCard | valid     |
| ''                     | invalid/NULL   |
| Data type of the digits | All numeric |
|  | Not all numeric |

**Boundaries**:

| Criteria               | Boundary values |
| ---------------------- | --------------- |
| Validity of creditCard | Valid, Invalid, NULL |
| Data type of the digits | All numeric, Not all numeric |

**Combination of predicates**:


| Data type of the digits | Validity of creditCard | Valid / Invalid | Description of the test case                         | JUnit test case                       |
| ---------------------- | --------------- | ---------------------------------------------------- | ------------------------------------- | ---------------------- |
| Not all numeric | *                | Invalid         | checkCreditCardValidity("cane")<br/> -> false<br/>checkCreditCardValidity(null)<br/> -> false | BB_UnitTesting.test_InvalidCreditCard |
| * | Invalid/NULL | Invalid | checkCreditCardValidity("79927398718")<br/> -> false<br/> | BB_UnitTesting.test_InvalidCreditCard |
| All numeric       | Valid                  | Valid           | checkBarCodeValidity("79927398713")<br/> -> true     | BB_UnitTesting.test_ValidCreditCard   |

### **Class *EZShop* - method *checkCustomerCardValidity***
**Criteria for method *checkCustomerCardValidity*:**

- Length of CustomerCard 
- Data type of CustomerCard digits

**Predicates for method *checkBarCodeValidity*:**

  | Criteria | Predicate |
  | -------- | --------- |
  | Length of CustomerCard | =0 && <10 |
  |  | = 10 |
  | | 10 <  |
  | Data type of barCode digits | All characters are numeric |
  |  | Not all characters are numeric |

**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
| Length of barCode | 0, 10, +maxint |
| Data type of barCode digits | All numeric, not all numeric |

| Length of CustomerCard | Data type of CustomerCard digits | Valid / Invalid | Description of the test case | JUnit test case |
|-------|--------------|-------|-------|-------|
|[0, 10)|*|Invalid|checkCustomerCardValidity("42")<br/> -> false<br/>checkCustomerCardValidity(null)<br/> -> false|BB_UnitTesting.test_CustomerCardVal|
|(10, maxint)| *|Invalid|checkCustomerCardValidity("62910415002187326548")<br/> -> false<br/>|BB_UnitTesting.test_CustomerCardVal|
|*| Not all numeric  |Invalid|checkCustomerCardValidity("1234a234b")<br/> -> false<br/>|BB_UnitTesting.test_CustomerCardVal|
|[10]|All numeric|Valid|checkCustomerCardValidity("5000000001")<br/> -> true|BB_UnitTesting.test_CustomerCardVal|

  

# White Box Unit Tests

### Test cases definition

    <JUnit test classes must be in src/test/java/it/polito/ezshop>
    <Report here all the created JUnit test cases, and the units/classes under test >
    <For traceability write the class and method name that contains the test case>


| Unit name | JUnit test case |
| -- | -- |
| Class - EZSaleTransaction | WB_UnitTesting.test_SaleTransaction |
| Class - EZBalanceOperation | WB_UnitTesting.test_BalanceOperation |
| Class - EZReturnTransaction | WB_UnitTesting.test_ReturnTransaction |
| Class - EZTicketEntry | WB_UnitTesting.test_TicketEntry |
| Class - EZOrder | WB_UnitTesting.test_OrderClassMethods |
| Class - EZUser | WB_UnitTesting.test_UserClassMethods |
| Class - EZCustomer | WB_UnitTesting.test_CustomerClassMethods |
| Class - EZProductType | WB_UnitTesting.test_ProductTypeMethods |

### Code coverage report

    <Add here the screenshot report of the statement and branch coverage obtained using
    the Eclemma tool. >

![unit test coverage report](TestingPNGs/wb_unit_coverage.png)

### Loop coverage analysis

    <Identify significant loops in the units and reports the test cases
    developed to cover zero, one or multiple iterations >

|Unit name | Loop rows | Number of iterations | JUnit test case |
|---|---|---|---|
| EZSaleTransaction | 136-149, 160-164, 185-190, 201-206 | Multiple iterations (depends on the class' internal list length) | WB_UnitTesting.test_SaleTransaction |

