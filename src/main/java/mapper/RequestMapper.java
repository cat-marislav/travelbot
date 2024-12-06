package mapper;

import dto.RequestDto;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RequestMapper {

    public String convertDtoToUrl(RequestDto requestDto) {
        String flyFrom = requestDto.getDepartureAirport();
        String flyTo = requestDto.getArrivalAirport();
        String departDate = requestDto.getDepartureTime();
        String returnDate = requestDto.getReturnTime();

        if (departDate==null) {
            LocalDate dateNow = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            departDate = dateNow.format(formatter);
            returnDate = LocalDate.parse(departDate, formatter).plusDays(15).format(formatter);
            requestDto.setDepartureTime(departDate);
            requestDto.setReturnTime(returnDate);
        }

        String returnFrom = requestDto.getReturnTime();

        String api = null;
        try {
            api = Files.readString(Paths.get("src/main/resources/api-token.txt"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        String url = MessageFormat.format("https://api.travelpayouts.com/v1/prices/cheap?" +
                        "origin={0}&" +
                        "depart_date={1}&" +
                        "return_date={2}&" +
                        "token=" + api + "&" +
                        "page=1&currency=RUB&limit=1"
                ,flyFrom
                ,departDate
                ,returnDate
        );

        if (flyTo != null) {
            url += "&destination="+flyTo;
        }

        return url;
    }
}
