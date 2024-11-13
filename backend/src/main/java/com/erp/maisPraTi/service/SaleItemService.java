package com.erp.maisPraTi.service;

import com.erp.maisPraTi.dto.products.ProductDto;
import com.erp.maisPraTi.dto.sales.SaleDto;
import com.erp.maisPraTi.dto.saleItems.SaleInsertItemDto;
import com.erp.maisPraTi.dto.saleItems.SaleItemResponseDto;
import com.erp.maisPraTi.dto.saleItems.SaleItemUpdateDto;
import com.erp.maisPraTi.model.Product;
import com.erp.maisPraTi.model.Sale;
import com.erp.maisPraTi.model.SaleItem;
import com.erp.maisPraTi.repository.SaleItemRepository;
import com.erp.maisPraTi.service.exceptions.ProductException;
import com.erp.maisPraTi.service.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static com.erp.maisPraTi.util.EntityMapper.convertToDto;
import static com.erp.maisPraTi.util.EntityMapper.convertToEntity;

@Service
public class SaleItemService {

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    @Transactional
    public SaleItemResponseDto insert(Long saleId, SaleInsertItemDto saleInsertItemDto) {
        SaleDto saleDto = saleService.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com ID:" + saleId));
        Sale sale = convertToEntity(saleDto, Sale.class);

        // Faz a verificação se já existe um item com o mesmo id e valor nessa venda
        Optional<SaleItem> existingSaleItem = saleItemRepository.findByProductIdAndSaleIdAndSalePrice(saleInsertItemDto.getProductId(), saleId, saleInsertItemDto.getSalePrice());

        // Se o item existir atualizad o mesmo
        if(existingSaleItem.isPresent()){
            SaleItem saleItem = existingSaleItem.get();
            getProductAndUpdateStock(saleInsertItemDto.getProductId(), saleInsertItemDto.getQuantitySold());
            saleItem.addToQuantityPending(saleInsertItemDto.getQuantitySold());
            saleItem = saleItemRepository.save(saleItem);
            return convertToDto(saleItem, SaleItemResponseDto.class);
        }

        //Se o item não existe, cria um novo item de venda
        ProductDto productDto = productService.findById(saleInsertItemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + saleInsertItemDto.getProductId()));
        verifyProductStock(productDto, saleInsertItemDto.getQuantitySold());
        productService.updateStockBySale(saleInsertItemDto);
        Product product = convertToEntity(productDto, Product.class);
        product.setProductPrice(saleInsertItemDto.getSalePrice());

        SaleItem newSaleItem = convertToEntity(saleInsertItemDto, SaleItem.class);
        newSaleItem.setProduct(product);
        newSaleItem.setSale(sale);
        newSaleItem.setSalePrice(saleInsertItemDto.getSalePrice());
        newSaleItem.setUnitOfMeasure(product.getUnitOfMeasure());
        newSaleItem.setQuantityDelivered(new BigDecimal(0));
        newSaleItem = saleItemRepository.save(newSaleItem);

        return convertToDto(newSaleItem, SaleItemResponseDto.class);
    }

    @Transactional(readOnly = true)
    public Optional<SaleItemResponseDto> findBySaleIdAndSaleItemId(Long saleId, Long itemId) {
        SaleItem saleItem = saleItemRepository.findByIdAndSaleId(itemId, saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não localizado"));
        return Optional.of(convertToDto(saleItem, SaleItemResponseDto.class));
    }

    @Transactional(readOnly = true)
    public Page<SaleItemResponseDto> findAllBySaleId(Long saleId, Pageable pageable) {
        Page<SaleItem> sales = saleItemRepository.findBySaleId(saleId, pageable);
        return sales.map(c -> convertToDto(c, SaleItemResponseDto.class));
    }

    @Transactional(readOnly = true)
    public Page<SaleItemResponseDto> findAllByProductId(Long saleId, Long productId, Pageable pageable) {
        Page<SaleItem> sales = saleItemRepository.findBySaleIdAndProductId(saleId, productId, pageable);
        return sales.map(c -> convertToDto(c, SaleItemResponseDto.class));
    }

    @Transactional(readOnly = true)
    public Page<SaleItemResponseDto> findAll(Pageable pageable) {
        Page<SaleItem> sales = saleItemRepository.findAll(pageable);
        return sales.map(c -> convertToDto(c, SaleItemResponseDto.class));
    }

    @Transactional
    public SaleItemResponseDto update(Long id, SaleItemUpdateDto saleItemUpdateDto) {
            SaleItem existsItem = saleItemRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Item não localizado pelo id informado."));

        existsItem.setQuantitySold(saleItemUpdateDto.getQuantitySold());
        existsItem.setSalePrice(saleItemUpdateDto.getSalePrice());

        SaleItem updatedItem = saleItemRepository.save(existsItem);
        return convertToDto(updatedItem, SaleItemResponseDto.class);
    }

//    @Transactional
//    public void delete(Long id) {
//        verifyExistsId(id);
//        try {
//            saleItemRepository.deleteById(id);
//        } catch (DataIntegrityViolationException e) {
//            throw new DatabaseException("Não foi possível excluir esta item. Ele pode estar vinculado a outros registros.");
//        } catch (Exception e) {
//            throw new DatabaseException("Erro inesperado ao tentar excluir este item.");
//        }
//    }

    private void getProductAndUpdateStock(Long productId, BigDecimal quantitySale){
        ProductDto productDto = productService.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com ID: " + productId));
        verifyProductStock(productDto, quantitySale);
        productService.updateStockBySale(productId, quantitySale);
    }

    private void verifyProductStock(ProductDto productDto, BigDecimal quantitySold) {
        if(productDto.getAvailableForSale().compareTo(quantitySold) < 0)
            throw new ProductException("Estoque insuficiente, falta(m): "
                    + (new BigDecimal(String.valueOf(quantitySold.subtract(productDto.getAvailableForSale()))))
                    + " " + productDto.getUnitOfMeasure().getDescription() + "(s)");
    }

}
