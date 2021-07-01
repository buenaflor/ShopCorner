package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.entity.Invoice;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceType;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.exception.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.InvoiceArchiveService;
import at.ac.tuwien.sepm.groupphase.backend.service.OrderService;
import at.ac.tuwien.sepm.groupphase.backend.service.PdfGeneratorService;
import at.ac.tuwien.sepm.groupphase.backend.util.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PdfGenerator pdfGenerator;
    private final OrderService orderService;
    private final InvoiceArchiveService invoiceArchiveService;

    @Autowired
    public PdfGeneratorServiceImpl(PdfGenerator pdfGenerator,
                                   OrderService orderService,
                                   InvoiceArchiveService invoiceArchiveService) {
        this.pdfGenerator = pdfGenerator;
        this.orderService = orderService;
        this.invoiceArchiveService = invoiceArchiveService;
    }

    @Override
    public void setPdfInvoiceCanceled(Invoice invoice) {
        LOGGER.trace("createPdfInvoiceOperator({})", invoice);
        if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber()) && invoice.getCustomerId() == null) {
            this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, null));
        } else if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber()) && invoice.getCustomerId() != null) {
            this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, this.orderService.getOrderByInvoice(invoice)));
        } else if (invoice.getCustomerId() == null) {
            this.invoiceArchiveService.updateInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, null));
        } else {
            this.invoiceArchiveService.updateInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, this.orderService.getOrderByInvoice(invoice)));
        }
    }

    @Override
    public void setPdfOrderCanceled(Order order) {
        LOGGER.trace("setPdfOrderCanceled({})", order);
        this.invoiceArchiveService.updateInvoiceArchive(order.getInvoice().getInvoiceNumber(), pdfGenerator.generatePdf(order.getInvoice(), order));
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
        if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(order.getInvoice(), order));
        }
        return this.invoiceArchiveService.findByInvoiceNumber(invoice.getInvoiceNumber());
    }

    @Override
    public byte[] createPdfInvoiceCustomer(Order order) {
        LOGGER.trace("createPdfInvoiceCustomer({})", order);
        if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(order.getInvoice().getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(order.getInvoice().getInvoiceNumber(), pdfGenerator.generatePdf(order.getInvoice(), order));
        }
        return this.invoiceArchiveService.findByInvoiceNumber(order.getInvoice().getInvoiceNumber());
    }

    @Override
    public byte[] createPdfCanceledInvoiceOperator(Invoice invoice) {
        LOGGER.trace("createPdfCanceledInvoiceOperator({})", invoice);
        if (invoice.getInvoiceType() != InvoiceType.canceled) {
            throw new ServiceException("Diese Rechnung kann nicht storniert werden");
        }
        if (invoice.getCustomerId() == null) {
            if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
                return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, null));
            }
            return this.invoiceArchiveService.findByInvoiceNumber(invoice.getInvoiceNumber());
        }
        if (!this.invoiceArchiveService.invoiceExistsByInvoiceNumber(invoice.getInvoiceNumber())) {
            return this.invoiceArchiveService.createInvoiceArchive(invoice.getInvoiceNumber(), pdfGenerator.generatePdf(invoice, this.orderService.getOrderByInvoice(invoice)));
        }
        return this.invoiceArchiveService.findByInvoiceNumber(invoice.getInvoiceNumber());
    }

}