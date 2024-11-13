package client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mapper.RequestMapper;
import dto.RequestDto;
import dto.SkypickerResponseDto;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.nio.charset.StandardCharsets;

public class SkypickerClient {

    private final RequestMapper mapper = new RequestMapper();

    public String getResponse(RequestDto requestDto) {
        String requestURL = mapper.convertDtoToUrl(requestDto);
        String temp = null;

        try (
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(new HttpGet(requestURL))
        ) {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200) {
                if (entity != null) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    SkypickerResponseDto responseDto = objectMapper.readValue(entity.getContent(), SkypickerResponseDto.class);
                    temp = responseDto.toString();
                } else {
                    temp = "Что-то пошло не так: " + response.getStatusLine();
                }
            } else {
                System.out.println(response.getStatusLine());
                if (entity != null) {
                    temp = "Ошибка: Проверь введенные даты!";
                    if(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8).contains("Not recognized location")){
                        temp = "Ошибка: Проверь корректность IATA кода аэропорта!";
                    }
                } else {
                    temp = "Что-то пошло не так: " + response.getStatusLine();
                }
            }
        } catch (Exception cause) {
            cause.printStackTrace();
        }
        return temp;
    }
}
