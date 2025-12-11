package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItemNotInCartException extends RuntimeException {

    private final String username;
    private final UUID productId;

    public ItemNotInCartException(String username, UUID productId) {
        super(String.format("Product with ID %s was not found in the cart of user %s.",
                productId.toString(), username));

        this.username = username;
        this.productId = productId;
    }

    public ItemNotInCartException(UUID productId) {
        super(String.format("Product with ID %s was not found in the cart.",
                productId.toString()));
        this.username = null;
        this.productId = productId;
    }

    public String getUsername() {
        return username;
    }

    public UUID getProductId() {
        return productId;
    }
}