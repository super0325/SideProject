package com.example.demo.pet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.pet.model.Pet;
import com.example.demo.pet.repository.PetRepository;

import java.util.List;

@Service
public class PetService {
	
    @Autowired
    private PetRepository petRepository;

    
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    public Pet savePet(Pet pet) {
        return petRepository.save(pet);
    }

    public void deletePet(Long id) {
        petRepository.deleteById(id);
    }
    
}
