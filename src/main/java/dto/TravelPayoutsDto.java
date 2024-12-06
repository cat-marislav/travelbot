package dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class TravelPayoutsDto {
    boolean success;
    String currency;
    public Map<String, Map<String, FlightInfo>> data;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FlightInfo {
        public String airline;
        String departure_at;
        String return_at;
        String expires_at;
        Integer price;
        Integer flight_number;
    }

}
