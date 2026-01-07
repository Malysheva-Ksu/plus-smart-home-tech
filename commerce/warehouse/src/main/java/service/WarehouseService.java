package service;

import model.warehouse.NewProductRequest;
import model.warehouse.StockItemResponse;
import model.warehouse.AddressDto;

import java.util.UUID;

public interface WarehouseService {

    public StockItemResponse getStock(UUID productId);

    public AddressDto getAddress();

    void saveProduct(NewProductRequest request);

    void addStock(UUID productId, Integer quantity);
}