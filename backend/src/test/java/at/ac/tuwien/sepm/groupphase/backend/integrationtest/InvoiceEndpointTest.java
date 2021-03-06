package at.ac.tuwien.sepm.groupphase.backend.integrationtest;

import at.ac.tuwien.sepm.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepm.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedInvoiceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.PaginationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleInvoiceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.InvoiceMapper;
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
import at.ac.tuwien.sepm.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepm.groupphase.backend.service.InvoiceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InvoiceEndpointTest implements TestData {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaxRateRepository taxRateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    private final InvoiceItemKey invoiceItemKey = new InvoiceItemKey();
    private final InvoiceItem invoiceItem = new InvoiceItem();
    private final Invoice invoice1 = new Invoice();
    private final Invoice invoice2 = new Invoice();
    private final Product product = new Product();
    private final Category category = new Category();
    private final TaxRate taxRate = new TaxRate();

    @BeforeEach
    void beforeEach() {
        invoiceRepository.deleteAll();
        categoryRepository.deleteAll();
        taxRateRepository.deleteAll();
        productRepository.deleteAll();
        invoiceRepository.deleteAll();

        product.setId(0L);
        product.setName(TEST_PRODUCT_NAME);
        product.setDescription(TEST_PRODUCT_DESCRIPTION);
        product.setPrice(TEST_PRODUCT_PRICE);

        category.setId(1L);
        category.setName(TEST_CATEGORY_NAME);

        taxRate.setId(1L);
        taxRate.setPercentage(TEST_TAX_RATE_PERCENTAGE);
        taxRate.setCalculationFactor((TEST_TAX_RATE_PERCENTAGE/100)+1);

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
        Set<InvoiceItem> items = new HashSet<>();
        items.add(invoiceItem);
        invoice1.setInvoiceNumber(TEST_INVOICE_NUMBER_1);
        invoice1.setDate(LocalDateTime.now());
        invoice1.setAmount(TEST_INVOICE_AMOUNT);
        invoice1.setItems(items);
        invoice1.setInvoiceType(InvoiceType.operator);

        invoice2.setInvoiceNumber(TEST_INVOICE_NUMBER_2);
        invoice2.setDate(LocalDateTime.now());
        invoice2.setAmount(TEST_INVOICE_AMOUNT);
        invoice2.setItems(items);
        invoice2.setInvoiceType(InvoiceType.operator);

    }

    @Test
    void givenAllProperties_whenPost_thenInvoicePdf() throws Exception {
        DetailedInvoiceDto detailedInvoiceDto = invoiceMapper.invoiceToDetailedInvoiceDto(invoice1);
        String body = objectMapper.writeValueAsString(detailedInvoiceDto);

        MvcResult mvcResult = this.mockMvc.perform(post(INVOICE_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_PDF_VALUE, response.getContentType());
    }

    @Test
    void givenAllProperties_whenPut_thenCanceledInvoice() throws Exception {
        Set<InvoiceItem> set1 = invoice1.getItems();
        invoice1.setItems(null);

        Invoice newInvoice = invoiceRepository.save(invoice1);

        for (InvoiceItem item : set1) {
            item.setInvoice(newInvoice);
            invoiceItemRepository.save(item);
        }
        newInvoice.setItems(set1);
        DetailedInvoiceDto dto = invoiceMapper.invoiceToDetailedInvoiceDto(newInvoice);
        String body = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = this.mockMvc.perform(patch(INVOICE_BASE_URI + "/" + dto.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedInvoiceDto detailedInvoiceDto = objectMapper.readValue(response.getContentAsString(),
            DetailedInvoiceDto.class);

        assertAll(
            () -> assertNotNull(detailedInvoiceDto.getId()),
            () -> assertNotNull(detailedInvoiceDto.getDate()),
            () -> assertEquals(invoice1.getAmount(), detailedInvoiceDto.getAmount()),
            () -> assertNotEquals(invoice1.getInvoiceType(), detailedInvoiceDto.getInvoiceType()),
            () -> assertNotEquals(invoice1.getInvoiceType(), InvoiceType.canceled)
        );
    }

    @Test
    void givenItems_whenGetInvoice_thenInvoiceAsPdf() throws Exception {
        Set<InvoiceItem> set1 = invoice1.getItems();
        invoice1.setItems(null);

        Invoice newInvoice = invoiceRepository.save(invoice1);
        for (InvoiceItem item : set1) {
            item.setInvoice(newInvoice);
            invoiceItemRepository.save(item);
        }

        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "/" + newInvoice.getId() + "/pdf")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_PDF_VALUE, response.getContentType());
        assertNotNull(response);
    }

    @Test
    void givenItems_whenGetInvoice_thenInvoice() throws Exception {
        Set<InvoiceItem> set = invoice1.getItems();
        invoice1.setItems(null);
        invoice1.setDate(LocalDateTime.now());
        Invoice newInvoice = invoiceRepository.save(invoice1);
        for (InvoiceItem item : set) {
            item.setInvoice(newInvoice);
            invoiceItemRepository.save(item);
        }
        newInvoice.setItems(set);

        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "/" + newInvoice.getId())
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        DetailedInvoiceDto detailedInvoiceDto = objectMapper.readValue(response.getContentAsString(),
            DetailedInvoiceDto.class);

        assertAll(
            () -> assertNotNull(detailedInvoiceDto.getId()),
            () -> assertNotNull(detailedInvoiceDto.getDate()),
            () -> assertEquals(newInvoice.getAmount(), detailedInvoiceDto.getAmount()),
            () -> assertEquals(newInvoice.getItems().size(), detailedInvoiceDto.getItems().size()),
            () -> assertEquals(newInvoice.getInvoiceType(), detailedInvoiceDto.getInvoiceType())
        );
    }


    @Test
    void givenTwoInvoices_whenFindAllWithPageAndPermission_thenListWithSizeTwoAndOverviewOfAllInvoices()
        throws Exception {
        this.invoiceService.createInvoice(invoice1);
        Invoice newInvoice2 = this.invoiceService.createInvoice(invoice2);

        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "?page=0&page_count=0&invoiceType=operator")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        PaginationDto<SimpleInvoiceDto> paginationDto = objectMapper.readValue(response.getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(2, paginationDto.getItems().size());

        List<SimpleInvoiceDto> simpleInvoiceDtoList = paginationDto.getItems();
        SimpleInvoiceDto simpleInvoiceDto = simpleInvoiceDtoList.get(0);
        assertAll(
            () -> assertEquals(newInvoice2.getId(), simpleInvoiceDto.getId()),
            () -> assertNotNull(simpleInvoiceDto.getDate()),
            () -> assertEquals(newInvoice2.getAmount(), simpleInvoiceDto.getAmount())
        );
    }


    @Test
    void givenNothing_whenSetCanceled_then400() throws Exception {
        invoiceRepository.deleteAll();
        MvcResult mvcResult = this.mockMvc.perform(patch(INVOICE_BASE_URI + "/" + 0L)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

    }


    @Test
    void givenNothing_whenFindPage_thenEmptyList() throws Exception {
        invoiceRepository.deleteAll();
        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "?page=0&page_count=0&invoiceType=operator")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        PaginationDto<SimpleInvoiceDto> paginationDto = objectMapper.readValue(response.getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(0, paginationDto.getItems().size());
    }

    @Test
    void givenNothing_whenFindById_then404() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "/" + 0L)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus()),
            () -> {
                String content = response.getContentAsString();
                assertEquals(("Rechnung konnte nicht gefunden werden"), content);
            }
        );
    }


    @Test
    void givenNothing_whenPostInvalid_then400() throws Exception {
        invoice1.setAmount(0);
        invoice1.setDate(null);
        invoice1.setItems(null);

        DetailedInvoiceDto dto = invoiceMapper.invoiceToDetailedInvoiceDto(invoice1);
        String body = objectMapper.writeValueAsString(dto);

        MvcResult mvcResult = this.mockMvc.perform(post(INVOICE_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus()),
            () -> {
                String content = response.getContentAsString();
                content = content.substring(content.indexOf('[') + 1, content.indexOf(']'));
                String[] errors = content.split(",");
                assertEquals(3, errors.length);
            }
        );
    }

    @Test
    void givenOne_whenGetByDateInside_returnListWithInvoice() throws Exception {
        Set<InvoiceItem> set1 = invoice1.getItems();
        invoice1.setItems(null);

        Invoice newInvoice = invoiceRepository.save(invoice1);

        for (InvoiceItem item : set1) {
            item.setInvoice(newInvoice);
            invoiceItemRepository.save(item);
        }
        newInvoice.setItems(set1);

        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "/stats?start=1990-01-01&end=3000-01-01")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        List<Invoice> invoices = objectMapper.readValue(response.getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, invoices.size());
    }

    @Test
    void givenOne_whenGetByDateOutside_returnEmptyList() throws Exception {
        Set<InvoiceItem> set1 = invoice1.getItems();
        invoice1.setItems(null);

        Invoice newInvoice = invoiceRepository.save(invoice1);

        for (InvoiceItem item : set1) {
            item.setInvoice(newInvoice);
            invoiceItemRepository.save(item);
        }
        newInvoice.setItems(set1);

        MvcResult mvcResult = this.mockMvc.perform(get(INVOICE_BASE_URI + "/stats?start=3000-01-01&end=3000-02-01")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        List<Invoice> invoices = objectMapper.readValue(response.getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(0, invoices.size());
    }

}
