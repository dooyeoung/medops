package com.medops.application.eventsourcing.handler;

import com.medops.domain.model.MedicalRecord;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

public interface EventHandler<E> {
    MedicalRecord handleEvent(MedicalRecord state, E event);

    default Class<?> getEventType() {
        return Arrays
            .stream(getClass().getGenericInterfaces())
            // ParameterizedType으로 캐스팅 (제네릭 타입 정보를 포함하는 인터페이스)
            .map(i -> (ParameterizedType) i)
            // 현재 인터페이스(EventHandler.class)와 일치하는 제네릭 인터페이스만 필터링
            .filter(p -> p.getRawType().equals(EventHandler.class))
            // 해당 제네릭 인터페이스의 실제 타입 인자들(Actual Type Arguments)을 가져옵니다.
            // EventHandler<E>에서 E는 첫 번째(0번 인덱스) 타입 인자입니다.
            .map(p -> p.getActualTypeArguments()[0]) // <-- 여기를 [0]으로 수정
            // Class 객체로 캐스팅
            .map(a -> (Class<?>) a)
            // 첫 번째 일치하는 결과를 찾습니다.
            .findFirst()
            // 결과가 없으면 예외 발생 (이 경우는 올바른 구현이라면 발생하지 않아야 함)
            .get();
    }
}
