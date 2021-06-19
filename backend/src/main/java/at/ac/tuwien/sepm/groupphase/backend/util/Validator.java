package at.ac.tuwien.sepm.groupphase.backend.util;

import at.ac.tuwien.sepm.groupphase.backend.entity.Operator;
import at.ac.tuwien.sepm.groupphase.backend.entity.Product;
import at.ac.tuwien.sepm.groupphase.backend.entity.Promotion;
import at.ac.tuwien.sepm.groupphase.backend.entity.InvoiceItem;
import at.ac.tuwien.sepm.groupphase.backend.entity.Customer;
import at.ac.tuwien.sepm.groupphase.backend.entity.Invoice;

import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.CustomerRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.OperatorRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void validateNewOperator(Operator operator, OperatorRepository operatorRepository) {
        LOGGER.trace("validateNewOperator({})", operator);

        Operator op = operatorRepository.findByLoginName(operator.getLoginName());
        if (op != null) {
            throw new ValidationException("Account already exists!");
        }

        op = operatorRepository.findByEmail(operator.getEmail());
        if (op != null) {
            throw new ValidationException("Account already exists!");
        }

    }

    public void validateUpdatedOperator(Operator operator, OperatorRepository operatorRepository) {
        LOGGER.trace("validateUpdatedOperator({})", operator);

        Operator op = operatorRepository.findByLoginName(operator.getLoginName());
        if (op != null && !op.getId().equals(operator.getId())) {
            throw new ValidationException("Account already exists!");
        }

        op = operatorRepository.findByEmail(operator.getEmail());
        if (op != null && !op.getId().equals(operator.getId())) {
            throw new ValidationException("Account already exists!");
        }

    }

    public void validateNewCustomer(Customer customer, CustomerRepository customerRepository) {
        LOGGER.trace("validateNewCustomer({})", customer);

        Customer c = customerRepository.findByLoginName(customer.getLoginName());
        if (c != null) {
            throw new ValidationException("Account already exists!");
        }

        c = customerRepository.findByEmail(customer.getEmail());
        if (c != null) {
            throw new ValidationException("Account already exists!");
        }

    }

    public void validateUpdatedCustomer(Customer customer, CustomerRepository customerRepository) {
        LOGGER.trace("validateUpdatedCustomer({})", customer);

        Customer c = customerRepository.findByLoginName(customer.getLoginName());
        if (c != null && !c.getId().equals(customer.getId())) {
            throw new ValidationException("Account already exists!");
        }

        c = customerRepository.findByEmail(customer.getEmail());
        if (c != null && !c.getId().equals(customer.getId())) {
            throw new ValidationException("Account already exists!");
        }

    }

    public void validateNewInvoice(Invoice invoice) {
        LOGGER.trace("validateNewInvoice({})", invoice);
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minus(1, ChronoUnit.DAYS);


        if (invoice.getAmount() <= 0) {
            throw new ValidationException("Something went wrong with the total calculation");
        }
        if (invoice.getDate() == null) {
            throw new ValidationException("The invoice date is not valid");
        }
        if (invoice.getDate().isBefore(yesterday)) {
            throw new ValidationException("The invoice date is before it is not valid");
        }
        if (invoice.getDate().isAfter(today)) {
            throw new ValidationException("The invoice date is after it is not valid");
        }
    }

    public void validateNewPromotion(Promotion promotion, PromotionService promotionService) {
        if (!promotion.getExpirationDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Expirationdate has to be after creationdate");
        }
        List<Promotion> promotions = promotionService.findAll();

        for (Promotion p : promotions) {
            if (p.getCode().equals(promotion.getCode())) {
                throw new ValidationException("Code has to be unique");
            }
        }
    }


    public void validateNewInvoiceItem(Set<InvoiceItem> items) {
        LOGGER.trace("validateNewInvoiceItem({})", items);
        if (items == null) {
            throw new ValidationException("There are no items in the invoice");
        } else {
            if (items.size() == 0) {
                throw new ValidationException("There are no items");
            }
            List<Product> productList = new ArrayList<>();
            for (InvoiceItem item : items) {

                if (item.getInvoice() == null) {
                    throw new ValidationException("Creating item entries without invoice");
                }
                if (item.getProduct() == null) {
                    throw new ValidationException("Creating item entries without products");
                }
                if (item.getNumberOfItems() <= 0) {
                    throw new ValidationException("Creating item entries without a quantity");
                }
                if (!productList.contains(item.getProduct())) {
                    productList.add(item.getProduct());
                } else {
                    throw new ValidationException("There are same items in the invoice");
                }
            }

        }
    }

    public void validateProduct(Product product) {
        LOGGER.trace("validateProduct({})", product);
        if (product.getExpiresAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (product.getExpiresAt().isBefore(now)) {
                throw new ValidationException("Expiration date cannot be in the past");
            }
        }
    }
}
