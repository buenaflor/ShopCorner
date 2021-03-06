package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.entity.Invoice;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceType;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ServiceException;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A service class handling Invoices.
 */
public interface InvoiceService {

    /**
     * Find a single invoice entry by id.
     *
     * @param id the id of the invoice entry
     * @return the invoice entry
     * @throws NotFoundException when no invoice with the id is found
     * @throws RuntimeException  upon encountering errors with the database
     */
    Invoice findOneById(Long id);

    /**
     * Find a single invoice entry by order number.
     *
     * @param orderNumber the order number of the invoice entry
     * @return the invoice entry
     * @throws NotFoundException when no invoice with the id is found
     * @throws RuntimeException  upon encountering errors with the database
     */
    Invoice findOneByOrderNumber(String orderNumber);

    /**
     * Find a single invoice entry with the given id and customerId.
     *
     * @param id the id of the invoice entry
     * @param customerId the id of the customer
     * @return the invoice entry
     * @throws NotFoundException when no invoice with the id is found
     */
    Invoice getByIdAndCustomerId(Long id, Long customerId);


    /**
     * Returns amount of invoices in the database.
     *
     * @return The amount of invoices in the database
     * @throws RuntimeException upon encountering errors with the database
     */
    Long getInvoiceCount();

    /**
     * Updates the given invoice in the database to canceled.
     *
     * @param invoice is the invoice to be canceled
     * @return The canceled invoice from the database
     * @throws RuntimeException upon encountering errors with the database
     */
    Invoice setInvoiceCanceled(Invoice invoice);

    /**
     * Returns amount of customerInvoices in the database.
     *
     * @return The amount of customer invoices in the database
     * @throws RuntimeException upon encountering errors with the database
     */
    Long getCustomerInvoiceCount();

    /**
     * Returns amount of canceledInvoices in the database.
     *
     * @return The amount of canceled invoices in the database
     * @throws RuntimeException upon encountering errors with the database
     */
    Long getCanceledInvoiceCount();

    /**
     * Create new invoice.
     *
     * @param invoice is the new invoice to create
     * @return new invoice
     * @throws ServiceException upon encountering errors with the database
     */
    Invoice createInvoice(Invoice invoice);

    /**
     * Returns page with all needed Invoices.
     *
     * @param page        which should be returned
     * @param invoiceType of invoices which should be returned
     * @param pageCount   amount of invoices per page
     * @return Page with all Invoices with right permission
     */
    Page<Invoice> findAll(int page, int pageCount, InvoiceType invoiceType);

    /**
     * Returns all Invoices in specified time period.
     *
     * @param start of time period
     * @param end   of time period
     * @return List of Invoices in time period
     * @throws RuntimeException upon encountering errors with the database
     */
    List<Invoice> findByDate(LocalDateTime start, LocalDateTime end);

    /**
     * Returns the count of invoices in the given year.
     *
     * @param firstDateOfYear the start of the year to return the count of
     * @return The count of invoices for the given year
     */
    long getInvoiceCountByYear(LocalDateTime firstDateOfYear);

}
