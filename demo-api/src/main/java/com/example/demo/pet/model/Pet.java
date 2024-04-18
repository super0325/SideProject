package com.example.demo.pet.model;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "pet")
@Component
public class Pet {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	@Schema(
		    description = "The unique identifier of the pet",
		    example = "1",
		    name = "petId",
		    type = "integer",
		    format = "int64",
		    deprecated = false,
		    minimum = "1",
		    maximum = "9223372036854775807",
		    pattern = "^\\d+$",                           // 屬性的值必須是一個或多個數字
		    allowableValues = {"1", "2", "3", "4", "5"},  // 允許的值的列表
		    accessMode = Schema.AccessMode.READ_ONLY
		)
	@NotNull
	private Long id;

	@Column(name = "TYPE")
	private String type;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "AGE")
	private Integer age;

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pet [id=");
		builder.append(id);
		builder.append(", type=");
		builder.append(type);
		builder.append(", name=");
		builder.append(name);
		builder.append(", age=");
		builder.append(age);
		builder.append("]");
		return builder.toString();
	}

}
