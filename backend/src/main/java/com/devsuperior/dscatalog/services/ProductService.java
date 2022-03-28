package com.devsuperior.dscatalog.services;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    ProductRepository repository;

    @Transactional(readOnly = true) // Se der algum problema, td será desfeito. importante colocar o read only true para otimizar a consulta
    public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
        Page<Product> list = repository.findAll(pageRequest);
        return list.map(x -> new ProductDTO(x));
    }

    @Transactional(readOnly = true) // Se der algum problema, td será desfeito. importante colocar o read only true para otimizar a consulta
    public ProductDTO findById(Long id) {
        Optional<Product> obj = repository.findById(id);
        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity nor found")); // se tiver um erro na consulta, o objeto não existir, a msg será enviada
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        //entity.setName(dto.getName());
        entity = repository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product entity = repository.getOne(id); // getOne não toca no banco de dados, ele instancia um objeto provisorio, dessa forma bate so uma vez no banco de dados
            //entity.setName(dto.getName());
            entity = repository.save(entity);
            return new ProductDTO(entity);
        } catch (EntityNotFoundException e) {
           throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        } catch (DataIntegrityViolationException e) { // essa exceção ocorrerá quando alguém tentar deletar uma categoria que esteja vinculada a algum produto
           throw new DataBaseException("Integrity violation");
        }
    }
}
