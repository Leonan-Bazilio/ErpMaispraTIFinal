package com.erp.maisPraTi.service;

import com.erp.maisPraTi.dto.partyDto.clients.ClientDto;
import com.erp.maisPraTi.dto.partyDto.clients.ClientUpdateDto;
import com.erp.maisPraTi.enums.TypePfOrPj;
import com.erp.maisPraTi.model.Client;
import com.erp.maisPraTi.repository.ClientRepository;
import com.erp.maisPraTi.service.exceptions.DatabaseException;
import com.erp.maisPraTi.service.exceptions.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import static com.erp.maisPraTi.util.EntityMapper.convertToDto;
import static com.erp.maisPraTi.util.EntityMapper.convertToEntity;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Transactional
    public ClientDto insert(ClientDto dto) {
        verifyExistsDocuments(dto.getCpfCnpj(), dto.getRgIe(), dto.getTypePfOrPj());
        Client client = new Client();
        client = convertToDto(dto, Client.class);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        client = clientRepository.save(client);
        return convertToDto(client, ClientDto.class);
    }

    @Transactional(readOnly = true)
    public Optional<ClientDto> findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Id não localizado: " + id));
        return Optional.of(convertToDto(client, ClientDto.class));
    }

    public Page<ClientDto> findAll(Pageable pageable) {
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(c -> convertToDto(c, ClientDto.class));
    }

    @Transactional
    public ClientDto update(Long id, ClientUpdateDto clientUpdateDto) {
        verifyExistsId(id);
        try {
            Client client = clientRepository.getReferenceById(id);
            if(!client.getCpfCnpj().equals(clientUpdateDto.getCpfCnpj()))
                verifyExistsDocuments(clientUpdateDto.getCpfCnpj(), clientUpdateDto.getRgIe(), clientUpdateDto.getTypePfOrPj());
            convertToEntity(clientUpdateDto, client);
            client.setUpdatedAt(LocalDateTime.now());
            client = clientRepository.save(client);
            return convertToDto(client, ClientDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não foi possível fazer a alteração neste cliente.");
        }
    }

    @Transactional
    public void delete(Long id) {
        verifyExistsId(id);
        try {
            clientRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não foi possível excluir este cliente. Ele pode estar vinculado a outros registros.");
        } catch (Exception e) {
            throw new DatabaseException("Erro inesperado ao tentar excluir o cliente.");
        }
    }

    private void verifyExistsId(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Id não localizado: " + id);
        }
    }

    private void verifyExistsDocuments(String cpfCnpj, String rgIe, TypePfOrPj typePfOrPj) {
        if(clientRepository.existsByCpfCnpj(cpfCnpj))
            throw new DatabaseException(typePfOrPj.equals(TypePfOrPj.PJ) ? "CNPJ já cadastrado no sistema." : "CPF já cadastrado no sistema.");
        if(clientRepository.existsByRgIe(rgIe))
            throw new DatabaseException(typePfOrPj.equals(TypePfOrPj.PJ) ? "Inscrição estadual já cadastrada no sistema." : "RG já cadastrado no sistema.");
    }

}


