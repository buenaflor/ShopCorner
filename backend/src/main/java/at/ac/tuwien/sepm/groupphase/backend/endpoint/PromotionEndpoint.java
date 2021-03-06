package at.ac.tuwien.sepm.groupphase.backend.endpoint;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.PaginationDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.PaginationRequestDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.PromotionDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.PromotionMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Promotion;
import at.ac.tuwien.sepm.groupphase.backend.service.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(PromotionEndpoint.BASE_URL)
public class PromotionEndpoint {
    static final String BASE_URL = "/api/v1/promotions";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PromotionMapper promotionMapper;
    private final PromotionService promotionService;

    @Autowired
    public PromotionEndpoint(PromotionMapper promotionMapper, PromotionService promotionService) {
        this.promotionMapper = promotionMapper;
        this.promotionService = promotionService;
    }

    /**
     * Adds a new promotion to the database.
     *
     * @param dto The promotion dto containing the promotion information
     * @return The response dto containing the added promotion
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Secured("ROLE_ADMIN")
    public PromotionDto addPromotion(@Valid @RequestBody PromotionDto dto) {
        LOGGER.info("POST " + BASE_URL);
        return promotionMapper.promotionToPromotionDto(promotionService.addNewPromotion(promotionMapper.promotionDtoToPromotion(dto)));
    }

    /**
     * Retrieves a page of promotions from the database.
     *
     * @param paginationRequestDto information about page
     * @return A list of all the retrieved promotions
     */
    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_EMPLOYEE"})
    public PaginationDto<PromotionDto> getAllPages(@Valid PaginationRequestDto paginationRequestDto) {
        int page = paginationRequestDto.getPage();
        int pageCount = paginationRequestDto.getPageCount();
        LOGGER.info("GET api/v1/promotions?page={}&page_count={}", page, pageCount);
        Page<Promotion> promotionPage = promotionService.getAllPromotions(page, pageCount);
        return new PaginationDto<>(promotionMapper.promotionListToPromotionDtoList(promotionPage.getContent()), page, pageCount, promotionPage.getTotalPages(), promotionService.getPromotionCount());
    }

    /**
     * Retrieves a promotion based on its code.
     *
     * @param code that should be searched for
     * @return The found promotion
     */
    @GetMapping(value = "/{code}")
    @PermitAll
    public PromotionDto getPromotionByCode(@PathVariable String code) {
        LOGGER.info("GET ap1/v1/promotions/{}", code);
        return promotionMapper.promotionToPromotionDto(promotionService.findPromotionByCode(code));
    }
}
