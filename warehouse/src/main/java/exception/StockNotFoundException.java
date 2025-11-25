package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StockNotFoundException extends RuntimeException {

    public StockNotFoundException(String message) {
        super(message);
    }

    public StockNotFoundException(Long productId) {
        super(String.format("Stock not found for product: %d", productId));
    }

    public StockNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}