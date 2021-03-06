package at.ac.tuwien.sepm.groupphase.backend.service;

import at.ac.tuwien.sepm.groupphase.backend.entity.Product;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service class handling business logic for products.
 */

public interface ProductService {
    /**
     * Creates a new product.
     *
     * @param product the newly added product
     * @return the newly created product
     * @throws RuntimeException occurs during database operations
     */
    Product createProduct(Product product);

    /**
     * Gets all products per page that were previously added in the database in a paginated manner.
     *
     * @param page describes the number of the page
     * @param pageCount the number of entries each page holds
     * @param categoryId the category id, which a product contains
     * @param name the name of the product
     * @return all products that are currently saved in the database
     * @throws RuntimeException upon encountering errors with the database
     */
    Page<Product> getAllProductsPerPage(int page, int pageCount, Long categoryId, String sortBy, String name);

    /**
     * Gets all products  that were previously added in the database.
     *
     * @return all products that are currently saved in the database
     * @throws RuntimeException upon encountering errors with the database
     */
    List<Product> getAllProducts();

    /**
     * Updates an already existing product in the database.
     *
     * @param productId the Id of the product to update
     * @param product the newly updated product entity with the updated fields
     * @throws RuntimeException occurs during database operations
     */
    void updateProduct(Long productId, Product product);

    /**
     * Gets a specific product with the given productId.
     *
     * @param productId the is to search in the database
     * @return the product entity with the given product Id
     * @throws NotFoundException if the entity is not available in the database
     * @throws RuntimeException occurs during database operations
     */
    Product findById(Long productId);

    /**
     * Deletes a specific product with the given productId.
     *
     * @param productId the is to search in the database
     * @throws NotFoundException if the entity is not available in the database
     */
    void deleteProductById(Long productId);

    /**
     * Gets all products that are associated to a specific category.
     *
     * @param categoryId the id to search in the database
     * @throws NotFoundException if the entity is not available in the database
     * @throws RuntimeException occurs during database operations
     */
    List<Product> getAllProductsByCategory(Long categoryId);
}
