package com.example.demo.service;

import com.example.demo.model.Ingredient;
import com.example.demo.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {
    @Autowired
    private IngredientRepository ingredientRepository;
    public List<Ingredient> getAllIngredients(){
        return ingredientRepository.findAll();
    }
    //save(S entity) : 신규 데이터 인서트 혹은 기존 데이터 업데이트(Upsert 와 비슷)
    //findById(데이터형 id) : id로 조회
    //findAll() : Select * 전체 데이터 조회
    //deleteById() : 해당 id의 데이터를 Delete 함
    //deleteAll() : 테이블의 모든 데이터 삭제
}