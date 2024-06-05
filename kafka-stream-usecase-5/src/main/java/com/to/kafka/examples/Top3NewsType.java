package com.to.kafka.examples;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.to.kafka.examples.types.ClicksByNewsType;

import java.io.IOException;
import java.util.TreeSet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "top3sorted"
})
public class Top3NewsType {
    private ObjectMapper mapper = new ObjectMapper();
    private TreeSet<ClicksByNewsType> top3sorted = new TreeSet<>((o1, o2) -> {
        final int result = o2.getClicks().compareTo(o1.getClicks());
        return result != 0 ? result : o1.getNewsType().compareTo(o2.getNewsType());
    });

    public void add(ClicksByNewsType newValue){
        top3sorted.add(newValue);
        if(top3sorted.size() > 3){
            top3sorted.remove(top3sorted.last());
        }
    }

    public void remove(ClicksByNewsType newValue){
        top3sorted.remove(newValue);
    }

    @JsonProperty("top3sorted")
    public String getTop3sorted() throws JsonProcessingException {
        return mapper.writeValueAsString(top3sorted);
    }

    @JsonProperty("top3sorted")
    public void setTop3sorted(String top3String) throws IOException {
        ClicksByNewsType[] top3 = mapper.readValue(top3String,ClicksByNewsType[].class);
        for(ClicksByNewsType i : top3){
            add(i);
        }
    }
}
