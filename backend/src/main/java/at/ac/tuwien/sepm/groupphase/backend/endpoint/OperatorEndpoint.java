package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.OperatorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.OverviewOperatorDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.OperatorMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Operator;
import at.ac.tuwien.sepm.groupphase.backend.entity.Permissions;
import at.ac.tuwien.sepm.groupphase.backend.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(OperatorEndpoint.BASE_URL)
public class OperatorEndpoint {

    static final String BASE_URL = "api/v1/operators";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final OperatorMapper operatorMapper;
    private final OperatorService operatorService;

    @Autowired
    public OperatorEndpoint(OperatorMapper operatorMapper, OperatorService operatorService) {
        this.operatorMapper = operatorMapper;
        this.operatorService = operatorService;
    }

    @PermitAll
    @GetMapping(params = {"page"})
    @Operation(summary = "Get list of operators", security = @SecurityRequirement(name = "apiKey"))
    public List<OverviewOperatorDto> getPage(@RequestParam("page") int page, @RequestParam("permissions") Permissions permissions) {
        LOGGER.info("GET " + BASE_URL + "?{}&{}", page, permissions);
        return operatorMapper.operatorToOverviewOperatorDto(operatorService.findAll(page, permissions).getContent());
    }

    @PermitAll
    @GetMapping()
    @Operation(summary = "Get count of operators", security = @SecurityRequirement(name = "apiKey"))
    public int[] getCount() {
        LOGGER.info("GET " + BASE_URL);
        return operatorService.getCollectionSize();
    }

    @PermitAll //TODO change to @Secured("ROLE_ADMIN")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new operator account", security = @SecurityRequirement(name = "apiKey"))
    public OperatorDto registerOperator(@Valid @RequestBody OperatorDto newOperator) {
        LOGGER.info("POST " + BASE_URL + "/register body: {}", newOperator);
        Operator operator = operatorMapper.dtoToEntity(newOperator);
        operatorService.save(operator);
        OperatorDto result = operatorMapper.entityToDto(operator);
        result.setPassword(null);
        return result;
    }
}
