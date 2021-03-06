package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.ConfirmedPaymentDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.ConfirmedPaymentSearchDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.OrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.OrderMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.PaymentMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.service.PayPalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;


import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;

@RestController
@RequestMapping(PayPalEndpoint.BASE_URL)
public class PayPalEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static final String BASE_URL = "api/v1/paypal";
    private final PayPalService payPalService;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;

    @Autowired
    public PayPalEndpoint(PayPalService payPalService, OrderMapper orderMapper, PaymentMapper paymentMapper) {
        this.payPalService = payPalService;
        this.orderMapper = orderMapper;
        this.paymentMapper = paymentMapper;
    }

    /**
     * Initiates a Payment transaction with PayPal's API.
     *
     * @param orderDto the order containing all information for the payment process
     * @return a redirect URL to confirm the payment
     * @throws PayPalRESTException if something goes wrong with PayPal
     */
    @Secured({"ROLE_CUSTOMER"})
    @PostMapping()
    @Operation(summary = "Posts a new request to PayPal's API to initiate a payment", security = @SecurityRequirement(name = "apiKey"))
    public ResponseEntity<String> createPayment(@Valid @RequestBody OrderDto orderDto) throws PayPalRESTException {
        LOGGER.info("POST" + BASE_URL + "({})", orderDto);
        return new ResponseEntity<>(this.payPalService.createPayment(this.orderMapper.orderDtoToOrder(orderDto)), HttpStatus.CREATED);
    }

    /**
     * Confirms a payment with the specified PayerID and PaymentID in the request.
     *
     * @param confirmedPaymentDto the confirmedPaymentDto contains the necessary parameters to confirm the payment
     * @return string indicating the success or failure of a payment
     * @throws PayPalRESTException if something goes wrong with PayPal
     */
    @Secured({"ROLE_CUSTOMER"})
    @PostMapping("/confirmation")
    @Operation(summary = "Confirms a payment", security = @SecurityRequirement(name = "apiKey"))
    public ResponseEntity<String> confirmPayment(@RequestBody ConfirmedPaymentDto confirmedPaymentDto) throws PayPalRESTException {
        LOGGER.info("POST" + BASE_URL + "({})", confirmedPaymentDto);
        Payment confirmedPayment = this.payPalService.confirmPayment(this.paymentMapper.confirmedPaymentDtoToConfirmedPayment(confirmedPaymentDto));
        if (confirmedPayment != null) {
            return new ResponseEntity<>("Payment successful", HttpStatus.OK);
        }
        return new ResponseEntity<>("Payment not successful", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gets a ConfirmedPayment entry with the given payerId and paymentId.
     *
     * @param confirmedPaymentSearchDto the confirmedPaymentSearchDto contains the necessary parameters retrieval
     * @return the ConfirmedPayment with the given PayerId and paymentId
     */
    @Secured({"ROLE_CUSTOMER"})
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets a ConfirmedPayment specified by payerId and paymentId", security = @SecurityRequirement(name = "apiKey"))
    public ConfirmedPaymentDto getConfirmedPaymentByPaymentIdAndPayerId(ConfirmedPaymentSearchDto confirmedPaymentSearchDto) {
        LOGGER.info("GET" + BASE_URL + "({})", confirmedPaymentSearchDto);
        String payerId = confirmedPaymentSearchDto.getPayerId();
        String paymentId = confirmedPaymentSearchDto.getPaymentId();
        return this.paymentMapper.confirmedPaymentToConfirmedPaymentDto(this.payPalService.getConfirmedPaymentByPaymentIdAndPayerId(payerId, paymentId));
    }

}
