package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.entity.Category;
import at.ac.tuwien.sepm.groupphase.backend.entity.Invoice;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceItem;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceItemKey;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceType;
import at.ac.tuwien.sepm.groupphase.backend.entity.Product;
import at.ac.tuwien.sepm.groupphase.backend.entity.TaxRate;
import at.ac.tuwien.sepm.groupphase.backend.repository.CategoryRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.InvoiceItemRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.InvoiceRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.ProductRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.TaxRateRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class InvoiceServiceTest implements TestData {
    private final InvoiceItemKey invoiceItemKey = new InvoiceItemKey();
    private final InvoiceItem invoiceItem = new InvoiceItem();
    private final Invoice invoice = new Invoice();
    private final Product product = new Product();
    private final Category category = new Category();
    private final TaxRate taxRate = new TaxRate();

    @Autowired
    InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaxRateRepository taxRateRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @BeforeEach
    public void beforeEach() {
        invoiceRepository.deleteAll();
        product.setId(0L);
        product.setName(TEST_PRODUCT_NAME);
        product.setDescription(TEST_PRODUCT_DESCRIPTION);
        product.setPrice(TEST_PRODUCT_PRICE);

        category.setId(1L);
        category.setName(TEST_CATEGORY_NAME);

        taxRate.setId(1L);
        taxRate.setPercentage(TEST_TAX_RATE_PERCENTAGE);

        // product
        product.setId(0L);
        product.setName(TEST_PRODUCT_NAME);
        product.setDescription(TEST_PRODUCT_DESCRIPTION);
        product.setPrice(TEST_PRODUCT_PRICE);
        product.setTaxRate(taxRateRepository.save(taxRate));
        product.setCategory(categoryRepository.save(category));

        // invoiceItem
        invoiceItemKey.setInvoiceId(null);
        invoiceItemKey.setProductId(product.getId());

        invoiceItem.setId(invoiceItemKey);
        invoiceItem.setProduct(productRepository.save(product));
        invoiceItem.setNumberOfItems(10);

        // invoiceItem to invoice
        Set<InvoiceItem> invoiceItemSet = new HashSet<>();
        invoiceItemSet.add(invoiceItem);
        invoice.setItems(invoiceItemSet);
        invoice.setInvoiceNumber(TEST_INVOICE_NUMBER_1);
        invoice.setDate(LocalDateTime.now());
        invoice.setAmount(TEST_INVOICE_AMOUNT);
        invoice.setInvoiceType(InvoiceType.operator);

    }

    @Test
    public void createInvoice() {
       Invoice created = invoiceService.createInvoice(invoice);
       assertNotNull(created);
    }

    @Test
    public void createInvoiceAndGetTotalInvoiceCount() {
        Invoice created = invoiceService.createInvoice(invoice);
        Long count = invoiceService.getInvoiceCount();
        assertNotNull(created);
        assertEquals(1L, count);
    }

    @Test
    public void createInvoiceAndGetCustomerInvoiceCount() {
        invoice.setInvoiceType(InvoiceType.customer);
        Invoice created = invoiceService.createInvoice(invoice);
        Long count = invoiceService.getCustomerInvoiceCount();
        assertNotNull(created);
        assertEquals(1L, count);
    }

    @Test
    public void createInvoiceAndFindOneById() {
        invoice.setInvoiceType(InvoiceType.customer);
        Invoice created = invoiceService.createInvoice(invoice);
        Invoice foundInvoice = invoiceService.findOneById(created.getId());
        assertNotNull(created);
        assertNotNull(foundInvoice);
        assertEquals(created.getId(), foundInvoice.getId());
        assertEquals(created.getAmount(), foundInvoice.getAmount());
        assertEquals(created.getItems().size(), foundInvoice.getItems().size());
        assertEquals(created.getInvoiceNumber(), foundInvoice.getInvoiceNumber());
    }

    @Test
    public void createInvoiceAndGetInvoicePage() {
        invoice.setInvoiceType(InvoiceType.customer);
        Invoice created = invoiceService.createInvoice(invoice);
        Page<Invoice> invoicePage = invoiceService.findAll(0, 15, InvoiceType.customer);

        assertNotNull(created);
        assertNotNull(invoicePage);
        assertEquals(created.getId(), invoicePage.getContent().get(0).getId());
        assertEquals(created.getAmount(), invoicePage.getContent().get(0).getAmount());
        assertEquals(created.getItems().size(), invoicePage.getContent().get(0).getItems().size());
        assertEquals(created.getInvoiceNumber(), invoicePage.getContent().get(0).getInvoiceNumber());
    }
}