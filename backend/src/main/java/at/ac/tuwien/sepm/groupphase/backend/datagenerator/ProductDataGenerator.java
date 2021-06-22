package at.ac.tuwien.sepm.groupphase.backend.datagenerator;


import at.ac.tuwien.sepm.groupphase.backend.entity.*;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.*;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Profile("generateData")
@Component
public class ProductDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<Double, Double> TAX_RATES = Map.of(10.0, 1.10, 13.00, 1.13, 20.0, 1.20);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TaxRateRepository taxRateRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CustomerRepository customerRepository;

    public ProductDataGenerator(ProductRepository productRepository, CategoryRepository categoryRepository,
                                TaxRateRepository taxRateRepository, InvoiceRepository invoiceRepository,
                                InvoiceItemRepository invoiceItemRepository, CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.taxRateRepository = taxRateRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    public void generateProducts() {
        if (productRepository.findAll().size() > 0) {
            LOGGER.debug("products already generated");
        } else {
            for (Map.Entry<Double, Double> entry : TAX_RATES.entrySet()) {
                TaxRate taxRate = TaxRate.TaxRateBuilder.getTaxRateBuilder()
                    .withPercentage(entry.getKey())
                    .withCalculationFactor(entry.getValue())
                    .build();
                taxRateRepository.save(taxRate);
            }
            Category category1 = Category.CategoryBuilder.getCategoryBuilder()
                .withName("IT & Elektronik")
                .build();
            categoryRepository.save(category1);
            Category category2 = Category.CategoryBuilder.getCategoryBuilder()
                .withName("Möbel")
                .build();
            categoryRepository.save(category2);
            Category category3 = Category.CategoryBuilder.getCategoryBuilder()
                .withName("Obst & Gemüse")
                .build();
            categoryRepository.save(category3);
            Category category4 = Category.CategoryBuilder.getCategoryBuilder()
                .withName("Kleidung")
                .build();
            categoryRepository.save(category4);
            TaxRate taxRate2 = this.taxRateRepository.findById(2L).orElseThrow(() -> new NotFoundException("Could not find tax-rate"));
            Product product1 = Product.ProductBuilder.getProductBuilder()
                .withName("Banane")
                .withDescription("leckere Bananen aus Ecuador")
                .withPrice(1.49)
                .withTaxRate(taxRate2)
                .withCategory(category3)
                .build();
            productRepository.save(product1);
            TaxRate taxRate3 = this.taxRateRepository.findById(3L).orElseThrow(() -> new NotFoundException("Could not find tax-rate"));
            Faker faker = new Faker(new Locale("de-AT"));
            TaxRate taxRate1 = this.taxRateRepository.findById(1L).orElseThrow(() -> new NotFoundException("Could not find tax-rate"));
            generateProductsWithTaxRate(taxRate1, category1, category2, category3, category4, faker);
            generateProductsWithTaxRate(taxRate2, category1, category2, category3, category4, faker);
            generateProductsWithTaxRate(taxRate3, category1, category2, category3, category4, faker);
        }

    }

    @Transactional
    public void generateProductsWithTaxRate(TaxRate taxRate, Category category1, Category category2, Category category3, Category category4, Faker faker) {
        for (int i = 0; i < 10; i++) {
            Date past = faker.date().past(10, 4, TimeUnit.DAYS);
            LocalDateTime pastLocalDateTime = LocalDateTime.ofInstant(past.toInstant(), ZoneId.of("UTC"));

            Date future = faker.date().future(10, 4, TimeUnit.DAYS);
            LocalDateTime futureLocalDateTime = LocalDateTime.ofInstant(future.toInstant(), ZoneId.of("UTC"));

            Product prod = Product.ProductBuilder.getProductBuilder()
                .withName(faker.space().nasaSpaceCraft())
                .withDescription(faker.lorem().sentence(2))
                .withPrice(faker.number().randomDouble(2, 1, 200))
                .withTaxRate(taxRate)
                .withCategory(category1)
                .withExpiresAt(futureLocalDateTime)
                .build();
            Long prodId = productRepository.save(prod).getId();

            Product prod1 = Product.ProductBuilder.getProductBuilder()
                .withName(faker.food().ingredient())
                .withDescription(faker.lorem().sentence(2))
                .withPrice(faker.number().randomDouble(2, 1, 200))
                .withTaxRate(taxRate)
                .withCategory(category2)
                .withExpiresAt(pastLocalDateTime)
                .build();
            Long prodId1 = productRepository.save(prod1).getId();

            Product prod2 = Product.ProductBuilder.getProductBuilder()
                .withName(faker.food().spice())
                .withDescription(faker.lorem().sentence(2))
                .withPrice(faker.number().randomDouble(2, 1, 200))
                .withTaxRate(taxRate)
                .withCategory(category3)
                .build();
            Long prodId2 = productRepository.save(prod2).getId();

            Product prod3 = Product.ProductBuilder.getProductBuilder()
                .withName(faker.food().vegetable())
                .withDescription(faker.lorem().sentence(2))
                .withPrice(faker.number().randomDouble(2, 1, 200))
                .withTaxRate(taxRate)
                .withCategory(category4)
                .build();
            Long prodId3 = productRepository.save(prod3).getId();

        }
        generateInvoices();

    }

    public void generateInvoices() {
        if (invoiceRepository.findAll().size() > 0) {
            LOGGER.debug("invoices already generated");
        } else {
            List<Product> products = productRepository.findAll();
            for (int i = 1; i <= 300; i++) {
                int rand = (int) (Math.random() * 10) + 1;
                List<InvoiceItem> items = new ArrayList<>();
                int[] used = new int[rand];
                Invoice invoice = new Invoice();
                double amount = 0;
                for (int j = 0; j < rand; j++) {
                    InvoiceItem item =  new InvoiceItem();
                    int prodId = (int) (Math.random() * products.size());
                    while (ArrayUtils.contains(used, prodId)) {
                        prodId = (int) (Math.random() * products.size());
                    }
                    used[j] = prodId;
                    Product p = products.get(prodId);
                    item.setProduct(p);
                    int quantity = (int) (Math.random() * 4) + 1;
                    item.setNumberOfItems(quantity);
                    items.add(item);
                    amount += (item.getNumberOfItems() * (p.getPrice() * p.getTaxRate().getCalculationFactor()));
                }
                int daysPast = (int) (Math.random() * 400) + 1;
                invoice.setDate(LocalDateTime.now().minus(daysPast, ChronoUnit.DAYS));
                invoice.setAmount(amount);
                invoice.setInvoiceNumber(i + "Operator" + invoice.getDate().getYear());
                invoice.setInvoiceType(InvoiceType.operator);
                Invoice newInvoice = invoiceRepository.save(invoice);
                for (InvoiceItem item : items) {
                    item.setInvoice(newInvoice);
                    InvoiceItemKey itemId = new InvoiceItemKey();
                    itemId.setInvoiceId(newInvoice.getId());
                    item.setId(itemId);
                    Set<InvoiceItem> itemSet = new HashSet<>();
                    itemSet.add(item);
                    invoice.setItems(itemSet);
                    invoiceItemRepository.save(item);
                }
            }
            List<Customer> customers = customerRepository.findAll();
            for (int i = 1; i <= 500; i++) {
                int rand = (int) (Math.random() * 10) + 1;
                List<InvoiceItem> items = new ArrayList<>();
                int[] used = new int[rand];
                Invoice invoice = new Invoice();
                double amount = 0;
                for (int j = 0; j < rand; j++) {
                    InvoiceItem item =  new InvoiceItem();
                    int prodId = (int) (Math.random() * products.size());
                    while (ArrayUtils.contains(used, prodId)) {
                        prodId = (int) (Math.random() * products.size());
                    }
                    used[j] = prodId;
                    Product p = products.get(prodId);
                    item.setProduct(p);
                    int quantity = (int) (Math.random() * 4) + 1;
                    item.setNumberOfItems(quantity);
                    items.add(item);
                    amount += (item.getNumberOfItems() * (p.getPrice() * p.getTaxRate().getCalculationFactor()));
                }
                int daysPast = (int) (Math.random() * 400) + 1;
                invoice.setDate(LocalDateTime.now().minus(daysPast, ChronoUnit.DAYS));
                invoice.setAmount(amount);
                invoice.setInvoiceNumber(i + "Customer" + invoice.getDate().getYear());
                int custId = (int) (Math.random() * customers.size());
                invoice.setCustomerId((long) custId);
                invoice.setOrderNumber("Test" + i);
                invoice.setInvoiceType(InvoiceType.customer);
                Invoice newInvoice = invoiceRepository.save(invoice);
                for (InvoiceItem item : items) {
                    item.setInvoice(newInvoice);
                    InvoiceItemKey itemId = new InvoiceItemKey();
                    itemId.setInvoiceId(newInvoice.getId());
                    item.setId(itemId);
                    Set<InvoiceItem> itemSet = new HashSet<>();
                    itemSet.add(item);
                    invoice.setItems(itemSet);
                    invoiceItemRepository.save(item);
                }
            }
        }
    }
}
