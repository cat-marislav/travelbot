package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDto {

    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String returnTime;

}

