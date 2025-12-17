package service;

import config.WarehouseAddressConfig;
import exception.ProductNotFoundException;
import model.warehouse.NewProductRequest;
import model.warehouse.ProductStock;
import model.warehouse.StockItemResponse;
import model.warehouse.WarehouseAddressDto;
import org.springframework.stereotype.Service;
import repository.WarehouseRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseAddressConfig addressConfig;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository, WarehouseAddressConfig addressConfig) {
        this.warehouseRepository = warehouseRepository;
        this.addressConfig = addressConfig;
    }

    @Override
    public WarehouseAddressDto getAddress() {
        return addressConfig.toDto();
    }

    @Override
    public StockItemResponse getStock(UUID productId) {
        ProductStock stock = warehouseRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        return new StockItemResponse(stock.getProductId(), stock.getQuantity(), stock.getPrice());
    }

    @Override
    public void saveProduct(NewProductRequest request) {
        ProductStock productStock = warehouseRepository.findByProductId(request.getProductId())
                .orElseGet(ProductStock::new);

        productStock.setProductId(request.getProductId());
        productStock.setFragile(request.getFragile());
        productStock.setQuantity(request.getQuantity());
        productStock.setWeight(request.getWeight());
        productStock.setDimensionWidth(request.getDimensionWidth());
        productStock.setDimensionHeight(request.getDimensionHeight());
        productStock.setDimensionDepth(request.getDimensionDepth());
        productStock.setPrice(request.getPrice());

        warehouseRepository.save(productStock);
        log.info("Product stock saved/updated: {}", request.getProductId());
    }

    @Override
    public void addStock(UUID productId, Integer quantity) {
        if (quantity <= 0) {
            log.warn("Attempted to add non-positive quantity: {}", quantity);
            return;
        }

        ProductStock productStock = warehouseRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not registered in warehouse: " + productId));

        int newQuantity = productStock.getQuantity() + quantity;
        productStock.setQuantity(newQuantity);

        warehouseRepository.save(productStock);
        log.info("Stock updated for product {}. New quantity: {}", productId, newQuantity);
    }
}