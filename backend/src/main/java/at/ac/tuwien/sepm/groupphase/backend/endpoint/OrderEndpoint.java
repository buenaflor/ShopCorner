package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CancellationPeriodDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.OrderDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.PaginationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.CancellationPeriodMapper;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.OrderMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Order;
import at.ac.tuwien.sepm.groupphase.backend.exception.ServiceException;
import at.ac.tuwien.sepm.groupphase.backend.service.OrderService;
import at.ac.tuwien.sepm.groupphase.backend.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDateTime;
import java.security.Principal;

@RestController
@RequestMapping(OrderEndpoint.BASE_URL)
public class OrderEndpoint {
    static final String BASE_URL = "api/v1/orders";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final CancellationPeriodMapper cancellationPeriodMapper;
    private final PdfGeneratorService pdfGeneratorService;


    @Autowired
    public OrderEndpoint(OrderMapper orderMapper, OrderService orderService,
                         CancellationPeriodMapper cancellationPeriodMapper,
                         @Lazy PdfGeneratorService pdfGeneratorService) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.cancellationPeriodMapper = cancellationPeriodMapper;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    /**
     * Place a new Order.
     *
     * @param orderDto the order to be saved
     * @param sessionId of current session
     * @return newly placed order
     * @throws IOException if something with the shop settings goes wrong
     */
    @Secured({"ROLE_CUSTOMER"})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place a new order", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto placeNewOrder(@Valid @RequestBody OrderDto orderDto, @CookieValue(name = "sessionId", defaultValue = "default") String sessionId) throws IOException {
        LOGGER.info("POST " + BASE_URL);
        return orderMapper.orderToOrderDto(orderService.placeNewOrder(orderMapper.orderDtoToOrder(orderDto), sessionId));
    }

    /**
     * Retrieves a page of orders from the database.
     *
     * @param page that should be gotten
     * @param pageCount amount per page
     * @return A page of all the retrieved orders
     */
    @Secured({"ROLE_ADMIN", "ROLE_EMPLOYEE"})
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all orders", security = @SecurityRequirement(name = "apiKey"))
    public PaginationDto<OrderDto> getAllOrders(@RequestParam(name = "page", defaultValue = "0") Integer page,
                                                @RequestParam(name = "page_count", defaultValue = "15") Integer pageCount
    ) {
        LOGGER.info("GET api/v1/orders?page={}&page_count={}", page, pageCount);
        Page<Order> orderPage = orderService.getAllOrders(page, pageCount);
        return new PaginationDto<>(orderMapper.orderListToOrderDtoList(orderPage.getContent()), page, pageCount, orderPage.getTotalPages(), orderPage.getTotalElements());
    }

    /**
     * Get specified order by id.
     *
     * @param id of the order to be retrieved
     * @param principal currently logged in user
     * @return the specified order
     */
    @Secured({"ROLE_CUSTOMER"})
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve order", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto getOrderById(@PathVariable Long id, Principal principal) {
        LOGGER.info("GET " + BASE_URL + "/{}", id);
        Order order = this.orderService.findOrderById(id);
        if (order.getCustomer().getLoginName().equals(principal.getName())) {
            return this.orderMapper.orderToOrderDto(order);
        }
        throw new AccessDeniedException("Illegal access");
    }

    /**
     * Get specified order by orderNumber.
     *
     * @param orderNumber of the order to be retrieved
     * @return the specified order
     */
    @Secured({"ROLE_ADMIN", "ROLE_EMPLOYEE"})
    @GetMapping("/{orderNumber}/order")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve order", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto getOrderById(@PathVariable String orderNumber) {
        LOGGER.info("GET " + BASE_URL + "/{}", orderNumber);
        return this.orderMapper.orderToOrderDto(this.orderService.findOrderByOrderNumber(orderNumber));
    }

    /**
     * Sets the cancellation period for orders.
     *
     * @param dto the dto containing the info on the cancellation Period
     * @return dto containing info on the cancellation period
     * @throws IOException upon encountering problems with the configuration file
     */
    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/settings")
    @Operation(summary = "set cancellation period for orders", security = @SecurityRequirement(name = "apiKey"))
    public CancellationPeriodDto setCancellationPeriod(@RequestBody CancellationPeriodDto dto) throws IOException {
        LOGGER.info("PUT " + BASE_URL + "/settings");
        return cancellationPeriodMapper.cancellationPeriodToCancellationPeriodDto(orderService.setCancellationPeriod(cancellationPeriodMapper.cancellationPeriodDtoToCancellationPeriod(dto)));
    }

    /**
     * Returns a dto containing information on the cancellation period.
     *
     * @return The cancellation period Dto
     * @throws IOException upon encountering problems with the configuration file
     */
    @PermitAll
    @GetMapping(value = "/settings")
    @Operation(summary = "Retrieve cancellation period", security = @SecurityRequirement(name = "apiKey"))
    public CancellationPeriodDto getCancellationPeriod() throws IOException {
        LOGGER.info("GET " + BASE_URL + "/settings");
        return cancellationPeriodMapper.cancellationPeriodToCancellationPeriodDto(orderService.getCancellationPeriod());
    }

    /**
     * Set an order to canceled in the database.
     *
     * @param orderId id of the order which should be updated in the database
     * @param orderDto order which should be updated in the database
     * @return DetailedInvoiceDto with the updated invoice
     * @throws IOException upon encountering problems with the configuration file
     */
    @Secured({"ROLE_ADMIN", "ROLE_EMPLOYEE", "ROLE_CUSTOMER"})
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}")
    @Operation(summary = "create new invoice", security = @SecurityRequirement(name = "apiKey"))
    public OrderDto setOrderCanceled(@Valid @RequestBody OrderDto orderDto, @PathVariable("id") Long orderId) throws IOException {
        LOGGER.info("PATCH /api/v1/setOrderCanceled/{}: {}", orderId, orderDto);
        if (!orderDto.getId().equals(orderId)) {
            throw new ServiceException("Bad Request, orderId is not valid");
        }
        Order order = this.orderService.findOrderById(orderId);
        int days = this.orderService.getCancellationPeriod().getDays();
        Duration duration = Duration.between(order.getInvoice().getDate(), LocalDateTime.now());
        if (duration.toDays() > days) {
            throw new ServiceException("Stornofrist ist vorbei");
        }
        Order canceledOrder = this.orderService.setInvoiceCanceled(order);
        this.pdfGeneratorService.setPdfOrderCanceled(canceledOrder);
        return orderMapper.orderToOrderDto(canceledOrder);
    }
}
