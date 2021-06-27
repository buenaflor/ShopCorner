package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.entity.Invoice;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceArchive;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceType;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.exception.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.CustomerService;
import at.ac.tuwien.sepm.groupphase.backend.service.InvoiceArchiveService;
import at.ac.tuwien.sepm.groupphase.backend.service.OrderService;
import at.ac.tuwien.sepm.groupphase.backend.service.PdfGeneratorService;
import at.ac.tuwien.sepm.groupphase.backend.util.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.rowset.serial.SerialBlob;
import java.lang.invoke.MethodHandles;
import java.sql.Blob;
import java.sql.SQLException;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PdfGenerator pdfGenerator;
    private final OrderService orderService;
    private final InvoiceArchiveService invoiceArchiveService;

    @Autowired
    public PdfGeneratorServiceImpl(PdfGenerator pdfGenerator, OrderService orderService, InvoiceArchiveService invoiceArchiveService) {
        this.pdfGenerator = pdfGenerator;
        this.orderService = orderService;
        this.invoiceArchiveService = invoiceArchiveService;
    }


    @Override
    public byte[] createPdfInvoiceOperator(Invoice invoice) {
        LOGGER.trace("createPdfInvoiceOperator({})", invoice);
        if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, null));
        }
        return this.invoiceArchiveService.findByInvoiceNumber(invoice.getInvoiceNumber());
    }


    @Override
    public byte[] createPdfInvoiceCustomerFromInvoice(Invoice invoice) {
        LOGGER.trace("createPdfInvoiceCustomerFromInvoice({})", invoice);
        Order order = this.orderService.getOrderByInvoice(invoice);
        if (this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(order.getInvoice(), order));
        }
        return pdfGenerator.generatePdf(order.getInvoice(), order);
    }

    @Override
    public byte[] createPdfInvoiceCustomer(Order order) {
        LOGGER.trace("createPdfInvoiceCustomer({})", order);
        System.out.println(order.getInvoice().getOrderNumber());
        if (this.invoiceArchiveService.invoiceExistsByInvoiceNumber(order.getInvoice().getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(order.getInvoice().getInvoiceNumber(), pdfGenerator.generatePdf(order.getInvoice(), order));
        }
        return pdfGenerator.generatePdf(order.getInvoice(), order);
    }


    @Override
    public byte[] createPdfCanceledInvoiceOperator(Invoice invoice) {
        LOGGER.trace("createPdfCanceledInvoiceOperator({})", invoice);
        if (invoice.getInvoiceType() != InvoiceType.canceled) {
            throw new ServiceException("It is not possible to cancel this invoice");
        }
        if (invoice.getCustomerId() == null) {
            if (this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
                return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, null));
            }
            return pdfGenerator.generatePdf(invoice, null);
        }
        if (this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, this.orderService.getOrderByInvoice(invoice)));
        }
        return pdfGenerator.generatePdf(invoice, this.orderService.getOrderByInvoice(invoice));
    }

    @Override
    public byte[] createPdfCanceledInvoiceCustomer(Order order) {
        LOGGER.trace("createPdfCanceledInvoiceCustomer({})", order);
        if (order.getInvoice().getInvoiceType() != InvoiceType.canceled) {
            throw new ServiceException("It is not possible to cancel this invoice");
        }
        if (this.invoiceArchiveService.invoiceExistsByInvoiceNumber(order.getInvoice().getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(order.getInvoice().getInvoiceNumber(), pdfGenerator.generatePdf(order.getInvoice(), order));
        }
        return pdfGenerator.generatePdf(order.getInvoice(), order);
    }
}