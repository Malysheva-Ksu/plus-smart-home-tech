package service;

import model.warehouse.NewProductRequest;
import model.warehouse.StockItemResponse;
import model.warehouse.WarehouseAddressDto;

import java.util.UUID;

public interface WarehouseService {

    public StockItemResponse getStock(UUID productId);

    public WarehouseAddressDto getAddress();

    void saveProduct(NewProductRequest request);

    void addStock(UUID productId, Integer quantity);
}