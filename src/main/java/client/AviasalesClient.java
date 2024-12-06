package client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.AirlineData;
import dto.TravelPayoutsDto;
import lombok.val;
import mapper.RequestMapper;
import dto.RequestDto;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AviasalesClient {

    private final RequestMapper mapper = new RequestMapper();

    public String getResponse(RequestDto requestDto) {
        String requestURL = mapper.convertDtoToUrl(requestDto);
        String responseMSG = null;

        try (
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(new HttpGet(requestURL))
        ) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200) {
                if (entity != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    TravelPayoutsDto responseDto = objectMapper.readValue(entity.getContent(), TravelPayoutsDto.class);
                    responseMSG = "";
                    int flightPos = 1;
                    for (val entry: responseDto.data.entrySet()) {
                        String destinationIATA = entry.getKey();
                        if (requestDto.getArrivalAirport()==null || requestDto.getArrivalAirport().equals(destinationIATA)) {
                            Map<String, TravelPayoutsDto.FlightInfo> tickets = entry.getValue();
                            responseMSG += flightPos + ". Из аэропорта " + requestDto.getDepartureAirport() + " в аэропорт " + destinationIATA;
                            for (val flightList : tickets.entrySet()) {
                                TravelPayoutsDto.FlightInfo flightInfo = flightList.getValue();
                                responseMSG += " рейсом авиалинии " + AirlineData.findByCode(flightInfo.airline) +
                                        " (Вылет - " + flightInfo.getDeparture_at() + ", обратная путёвка - " +
                                        flightInfo.getReturn_at() +
                                        ", цена - " + flightInfo.getPrice() + " рублей" +
                                        ").\n\n";
                            }
                            if (flightPos == 15) {
                                break;
                            }
                            flightPos += 1;
                        }
                    }
                    if (flightPos==1) {
                        responseMSG = "Билетов найдено не было.";
                    } else if (flightPos==2) {
                        responseMSG = "Нашел билет!\n\n" + responseMSG;
                    } else {
                        responseMSG = "Нашел несколько билетов!\n\n" + responseMSG;
                    }
                } else {
                    responseMSG = "Что-то пошло не так: " + response.getStatusLine();
                }
            } else {
                if (entity != null) {
                    if(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8).contains("Not recognized location")){
                        responseMSG = "Ошибка: Проверь корректность IATA кода аэропорта!";
                    } else {
                        responseMSG = "Ошибка: Проверь введенные даты!";
                    }
                } else {
                    responseMSG = "Что-то пошло не так: " + response.getStatusLine();
                }
            }
        } catch (Exception cause) {
            cause.printStackTrace();
        }
        return responseMSG;
    }
}
