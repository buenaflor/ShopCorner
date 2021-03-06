package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.config.properties.PayPalProperties;
import at.ac.tuwien.sepm.groupphase.backend.entity.ConfirmedPayment;
import at.ac.tuwien.sepm.groupphase.backend.entity.Invoice;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.repository.ConfirmedPaymentRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.PayPalService;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Transaction;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Links;

import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalServiceImpl implements PayPalService {

    private final PayPalProperties payPalProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ConfirmedPaymentRepository confirmedPaymentRepository;
    private static final String successUrl = "http://localhost:4200/#/order-success";
    private static final String cancelUrl = "http://localhost:4200/#/checkout";

    @Autowired
    public PayPalServiceImpl(PayPalProperties payPalProperties, ConfirmedPaymentRepository confirmedPaymentRepository) {
        this.payPalProperties = payPalProperties;
        this.confirmedPaymentRepository = confirmedPaymentRepository;
    }


    @Override
    public String createPayment(Order order) throws PayPalRESTException {
        LOGGER.trace("createPayment({})", order);
        Invoice invoice = order.getInvoice();
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        double total = invoice.getAmount();
        amount.setTotal(String.valueOf(total));
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setInvoiceNumber(order.getInvoice().getInvoiceNumber());

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        if (order.getPromotion() != null) {
            redirectUrls.setReturnUrl(successUrl + "?promotion=" + order.getPromotion().getCode());
        } else {
            redirectUrls.setReturnUrl(successUrl);
        }

        payment.setRedirectUrls(redirectUrls);

        Payment createdPayment;
        String redirectUrl = "";
        APIContext apiContext = payPalProperties.apiContext();
        createdPayment = payment.create(apiContext);
        List<Links> links = createdPayment.getLinks();
        for (Links link : links) {
            if (link.getRel().equals("approval_url")) {
                redirectUrl = link.getHref();
            }
        }
        return redirectUrl;
    }

    public Payment confirmPayment(ConfirmedPayment confirmedPayment) throws PayPalRESTException {
        LOGGER.trace("confirmPayment({})", confirmedPayment);
        Payment payment = new Payment();
        payment.setId(confirmedPayment.getPaymentId());
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(confirmedPayment.getPayerId());
        confirmedPaymentRepository.save(confirmedPayment);
        APIContext apiContext = payPalProperties.apiContext();
        return payment.execute(apiContext, paymentExecution);
    }

    public ConfirmedPayment getConfirmedPaymentByPaymentIdAndPayerId(String payerId, String paymentId) {
        LOGGER.trace("getConfirmedPaymentByPaymentIdAndPayerId({},{})", payerId, paymentId);
        return this.confirmedPaymentRepository.getConfirmedPaymentByPayerIdAndPaymentId(payerId, paymentId);
    }
}
