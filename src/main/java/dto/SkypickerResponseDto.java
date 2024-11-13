package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SkypickerResponseDto {
    private String currency;
    private List<OptionDto> data;

    public static String epochToUtc(long epochTime) {
        LocalDateTime localDateTime = LocalDateTime
                .ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return localDateTime.format(formatter);
    }

    @Override
    public String toString() {
        if (data.isEmpty()) {
            return "Ошибка: Проверь введенные даты!";
        } else {
            StringBuilder tickets = new StringBuilder("Цена: " + data.get(0).price + " " + currency + ", маршрут: " + "\n");
            for (int i = 0; i < data.get(0).getRoute().size(); i++) {
                tickets.append(data.get(0).route.get(i).flyFrom)
                        .append(" -> ")
                        .append(data.get(0).route.get(i).flyTo)
                        .append("\n").append("Из: ")
                        .append(data.get(0).route.get(i).cityFrom)
                        .append("\n").append("Время вылета: ")
                        .append(epochToUtc(data.get(0).route.get(i).dTime))
                        .append("\n").append("в : ")
                        .append(data.get(0).route.get(i).cityTo)
                        .append("\n").append("Время прилета: ")
                        .append(epochToUtc(data.get(0).route.get(i).aTime))
                        .append("\n").append("Код авиакомпании: ")
                        .append(data.get(0).route.get(i).airline)
                        .append("\n").append("\n");
            }
            return tickets.toString();
        }
    }

    //data
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class OptionDto {
        private int price;
        private List<RouteDto> route;
    }

    //route
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class RouteDto {
        @JsonProperty("dTime")
        private long dTime;
        @JsonProperty("aTime")
        private long aTime;
        private String cityFrom;
        private String cityTo;
        private String flyFrom;
        private String flyTo;
        private String airline;
    }
}



