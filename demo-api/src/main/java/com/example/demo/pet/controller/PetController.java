package com.example.demo.pet.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pet.model.Pet;
import com.example.demo.pet.service.PetService;
import com.example.demo.utils.controller.BaseController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pets")
@RestController
@RequestMapping("/pets")
public class PetController extends BaseController {
    
    @Autowired
    private PetService petService;

    
    @GetMapping
    public ResponseEntity<List<Pet>> getAllPets() {
        List<Pet> pets = petService.getAllPets();
        return ResponseEntity.ok().body(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        Pet pet = petService.getPetById(id);
        return ResponseEntity.ok().body(pet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pet> updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        Pet updatedPet = petService.savePet(pet);
        return ResponseEntity.ok().body(updatedPet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        Pet createdPet = petService.savePet(pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);
    }
    
    @Operation(summary = "Get pets by age and name",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Age of the pets to be fetched",
                    content = @Content(schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {
                                        "age": 5,
                                        "name": "jack"
                                    }
                                    """))))
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Successfully retrieved pets",
	                     content = @Content(mediaType = "application/json",
	                                        examples = @ExampleObject(value = """
	                                                [
	                                                    {
	                                                        "id": 1,
	                                                        "type": "dog",
	                                                        "name": "Rex",
	                                                        "age": 3
	                                                    },
	                                                    {
	                                                        "id": 2,
	                                                        "type": "cat",
	                                                        "name": "Whiskers",
	                                                        "age": 2
	                                                    }
	                                                ]
	                                                """))),
	        @ApiResponse(responseCode = "400", description = "Invalid input provided",
	                     content = @Content(mediaType = "application/json",
	                                        examples = @ExampleObject(value = ERROR_RESPONSE_400))),
	        @ApiResponse(responseCode = "500", description = "Internal server error",
	                     content = @Content(mediaType = "application/json",
	                                        examples = @ExampleObject(value = ERROR_RESPONSE_500)))
	})
    @PostMapping(value = "/age", produces = { "application/json" })
    public ResponseEntity<?> getPetByage(@RequestBody(required = true) HashMap<String, Object> input) {
        // 檢查請求主體是否存在
        if (input == null) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input", "Request body is missing");
        }

        // 檢查是否包含 'age' 和 'name' 欄位
        if (!input.containsKey("age") || !input.containsKey("name")) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input", "Only 'name' and 'age' fields are allowed");
        }

        // 檢查是否還有其他欄位
        input.remove("age");
        input.remove("name");
        if (!input.isEmpty()) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input", "Only 'name' and 'age' fields are allowed");
        }

        // 檢查 'age' 欄位是否是預期的類型（Integer）
        Object ageObject = input.get("age");
        if (!(ageObject instanceof Integer)) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "'age' field must be an integer");
        }

        Integer age = (Integer) input.get("age");
        String name = (String) input.get("name");
        System.out.println("Age: " + age);
        System.out.println("Name: " + name);
        return ResponseEntity.ok().body(null); // Placeholder return statement
    }
    
}
