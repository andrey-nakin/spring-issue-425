package com.example.demo;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.example.demo.MyObject;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class DemoController {

    public DemoController(BatchLoaderRegistry reg) {
        reg.forTypePair(String.class, Integer.class)
                .registerMappedBatchLoader((values, env) -> Mono.just(loadInt(values, env)));
        reg.forTypePair(Integer.class, Boolean.class)
                .registerMappedBatchLoader((values, env) -> Mono.just(loadBool(values, env)));
    }

    @QueryMapping
    public MyObject myObject(@Argument String input) {
        return new MyObject(input);
    }

    @SchemaMapping
    public CompletionStage<Boolean> myField(MyObject myObject, DataLoader<String, Integer> dl1,
            DataLoader<Integer, Boolean> dl2) {

        return dl1.load(myObject.getInput()).thenCompose(i -> dl2.load(i));
    }

    public Map<String, Integer> loadInt(Set<String> keys, final BatchLoaderEnvironment env) {
        return keys.stream().collect(Collectors.toMap(Function.identity(), (String value) -> Integer.valueOf(value)));
    }

    public Map<Integer, Boolean> loadBool(Set<Integer> keys, final BatchLoaderEnvironment env) {
        return keys.stream().collect(Collectors.toMap(Function.identity(), (Integer value) -> value % 2 == 0));
    }
}
