package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
//어노테이션이 있으면 Getter,Setter 자동 생성
@Getter
@Setter
@Table(name = "Ingredient")
public class Ingredient {
    //@Id:Primary Key 설정
    @Id
    //@GeneratedValue: 자동으로 생성되는 것을 나타냄,IDENTITY는 자동 증가를 나타냄
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredient_id;
    //@Column:  엔티티의 필드명과 데이터베이스 테이블의 컬럼명이 다를 때 또는 컬럼의 길이,
    //length 는 명시하지않으면 255를 기본값으로 갖음
    //NULL 허용 여부 등과 같은 속성을 지정해야 할 때 @Column 어노테이션을 사용
    @Column(name = "category_id")
    private Long category_id;

    @Column(name = "name",length = 255,nullable = false)
    private String name;

    @Column(name = "detail",length = 255)
    private String detail;

    //precision: 정수의 전체 자릿수, scale: 소수점 이하의 자릿수
    @Column(name = "state",precision = 1)
    private String state;

    @Column(name = "created_at")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date created_at;

}