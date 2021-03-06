package at.ac.tuwien.sepm.groupphase.backend.basetest;

import at.ac.tuwien.sepm.groupphase.backend.entity.Permissions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public interface TestData {

    Long ID = 1L;
    String TEST_CUSTOMER_LOGINNAME = "test";
    String TEST_CUSTOMER_NAME = "Bonus Eventus";
    String TEST_CUSTOMER_PASSWORD = "strongpassword";
    String TEST_CUSTOMER_EMAIL = "test@gmail.com";
    String TEST_ADDRESS_STREET = "testStreet";
    int TEST_ADDRESS_POSTALCODE = 1000;
    String TEST_ADDRESS_HOUSENUMBER = "1a";
    LocalDateTime TEST_NEWS_PUBLISHED_AT =
        LocalDateTime.of(2019, 11, 13, 12, 15, 0, 0);

    String BASE_URI = "/api/v1";
    String ADDRESS_BASE_URI = BASE_URI + "/address";
    String CUSTOMER_BASE_URI = BASE_URI + "/customers";
    String INVOICE_BASE_URI = BASE_URI + "/invoices";
    String PROMOTION_BASE_URI = BASE_URI + "/promotions";
    String CART_BASE_URI = BASE_URI + "/carts";
    String ME_BASE_URI = BASE_URI + "/me";

    //TEST PROPERTIES FOR PRODUCTS, TAX-RATES AND CATEGORIES
    String PRODUCTS_BASE_URI = "/api/v1/products";
    Long TEST_PRODUCT_ID = 0L;
    String TEST_PRODUCT_NAME = "Product 1";
    Double TEST_PRODUCT_PRICE = 1.20;
    String TEST_PRODUCT_DESCRIPTION = "Description1";
    Double TEST_TAX_RATE_PERCENTAGE = 20.0;
    String TEST_CATEGORY_NAME = "Cat1";
    Long TEST_CATEGORY_ID = 0L;
    String CATEGORY_BASE_URI = "/api/v1/categories";

    //TEST PROPERTIES FOR PAYPAL
    String PAYPAL_BASE_URI = "/api/v1/paypal";



    String OPERATOR_BASE_URI = BASE_URI + "/operators";

    String ORDER_BASE_URI = BASE_URI + "/orders";
    String TEST_OPERATOR_NAME = "operator";
    String TEST_OPERATOR_LOGINNAME = "operatorLoginName";
    String TEST_OPERATOR_PASSWORD = "operatorPassword";
    String TEST_OPERATOR_EMAIL = "operator@email.com";
    Permissions TEST_OPERATOR_PERMISSION = Permissions.admin;


    //TEST PROPERTIES FOR Operator Overview
    String OPERATORS_BASE_URI = "/api/v1/operators";

    String TEST_ADMIN_NAME = "Admin";
    String TEST_ADMIN_LOGINNAME = "TestAdmin";
    String TEST_ADMIN_PASSWORD = "TestPassword";
    String TEST_ADMIN_EMAIL = "admin@gmail.com";
    Permissions TEST_ADMIN_PERMISSIONS = Permissions.admin;

    String TEST_EMPLOYEE_NAME = "Employee";
    String TEST_EMPLOYEE_LOGINNAME = "TestEmployee";
    String TEST_EMPLOYEE_PASSWORD = "TestPassword";
    String TEST_EMPLOYEE_EMAIL = "employee@gmail.com";
    Permissions TEST_EMPLOYEE_PERMISSIONS = Permissions.employee;

    String TEST_PROMOTION_NAME = "Promotion";
    double TEST_PROMOTION_DISCOUNT = 2;
    LocalDateTime TEST_PROMOTION_CREATIONDATE = LocalDateTime.now();
    LocalDateTime TEST_PROMOTION_EXPIRATIONDATE = LocalDateTime.of(Calendar.getInstance().get(Calendar.YEAR) + 1,10,9,13,20);
    String TEST_PROMOTION_CODE = "Testcode";
    double TEST_PROMOTION_MINIMUMORDERVALUE = 10;

    String ADMIN_USER = "adminLogin";
    List<String> ADMIN_ROLES = new ArrayList<>() {
        {
            add("ROLE_ADMIN");
        }
    };
    String EMPLOYEE_USER = "employeeLogin";
    List<String> EMPLOYEE_ROLES = new ArrayList<>() {
        {
            add("ROLE_EMPLOYEE");
        }
    };
    String CUSTOMER_USER = "customerLogin";
    List<String> CUSTOMER_ROLES = new ArrayList<>() {
        {
            add("ROLE_CUSTOMER");
        }
    };


    //TEST PROPERTIES FOR INVOICE
    Long TEST_INVOICE_ID = 0L;
    String TEST_INVOICE_NUMBER_1 = "42016";
    String TEST_INVOICE_NUMBER_2 = "42017";
    LocalDateTime TEST_INVOICE_DATE = null;
    Double TEST_INVOICE_AMOUNT = 20.0;

    //TEST PROPERTIES FOR CART
    Long CART_ID = 0L;
    Long CART_ITEM_ID = 0L;
    Long CART_ITEM_PRODUCT_ID = 0L;
    int CART_ITEM_QUANTITY = 5 ;

    Long CART_ID_2 = 1L;
    Long CART_ITEM_ID_2 = 1L;
    Long CART_ITEM_PRODUCT_ID_2 = 1L;
    int CART_ITEM_QUANTITY_2 = 10 ;
}
