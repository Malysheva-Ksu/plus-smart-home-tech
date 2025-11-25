package model;

public enum MovementType {
    IN,          // Поступление товара на склад
    OUT,         // Отгрузка товара со склада
    RESERVE,     // Резервирование
    RELEASE,     // Освобождение резерва
    ADJUSTMENT,  // Корректировка количества
}