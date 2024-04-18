package com.example.demo.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.pet.model.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {

}
