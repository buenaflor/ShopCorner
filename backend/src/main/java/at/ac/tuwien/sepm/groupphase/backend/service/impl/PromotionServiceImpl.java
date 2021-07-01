package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.entity.Promotion;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.repository.PromotionRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.PromotionService;
import at.ac.tuwien.sepm.groupphase.backend.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PromotionRepository promotionRepository;
    private final Validator validator;

    @Autowired
    public PromotionServiceImpl(PromotionRepository promotionRepository, Validator validator) {
        this.validator = validator;
        this.promotionRepository = promotionRepository;
    }

    @Caching(evict = {
        @CacheEvict(value = "counts", key = "'promotions'"),
        @CacheEvict(value = "promotionPages", allEntries = true)
    })
    @Override
    public Promotion addNewPromotion(Promotion promotion) {
        LOGGER.trace("addNewPromotion({})", promotion);
        validator.validateNewPromotion(promotion, this);
        return this.promotionRepository.save(promotion);
    }

    @Cacheable(value = "promotionPages")
    @Override
    public Page<Promotion> getAllPromotions(int page, int pageCount) {
        LOGGER.trace("getAllPromotions()");
        if (pageCount == 0) {
            pageCount = 15;
        } else if (pageCount > 50) {
            pageCount = 50;
        }
        Pageable returnPage = PageRequest.of(page, pageCount, Sort.by("expirationDate").descending());
        return promotionRepository.findAll(returnPage);
    }

    @Cacheable(value = "counts", key = "'promotions'")
    @Override
    public Long getPromotionCount() {
        return promotionRepository.count();
    }

    @Override
    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    @Override
    public Promotion findPromotionByCode(String code) {
        Promotion promotion = this.promotionRepository.findByCode(code);
        if (promotion == null) {
            throw new NotFoundException("Keine Aktion mit gegebenem Code gefunden");
        } else {
            return promotion;

        }
    }


}
